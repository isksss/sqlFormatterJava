package dev.isksss.java.sqlformatter.gradle;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlFormatterPluginTest {
    @TempDir
    Path projectDir;

    @BeforeEach
    void setUpProject() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'consumer'\n");
        Files.writeString(
                projectDir.resolve("build.gradle"),
                """
                plugins {
                    id 'dev.isksss.java.sql-formatter'
                }

                sqlFormatter {
                    files {
                        from 'sql'
                        include '**/*.sql'
                        exclude '**/generated/**'
                    }
                    dialect = 'postgresql'
                    uppercase = true
                }
                """);
    }

    @Test
    void formatsIncludedFilesOnly() throws IOException {
        Path sql = write("sql/query.sql", "select id from users");
        Path excluded = write("sql/generated/query.sql", "select id from generated_users");

        BuildResult result = runner("sqlFormat").build();

        assertEquals(SUCCESS, result.task(":sqlFormat").getOutcome());
        assertTrue(Files.readString(sql).startsWith("SELECT\n"));
        assertEquals("select id from generated_users", Files.readString(excluded));
    }

    @Test
    void checkFailsForUnformattedSql() throws IOException {
        write("sql/query.sql", "select id from users");

        BuildResult result = runner("sqlCheck").buildAndFail();

        assertEquals(FAILED, result.task(":sqlCheck").getOutcome());
        assertTrue(result.getOutput().contains("query.sql"));
    }

    @Test
    void checkSucceedsAfterFormat() throws IOException {
        write("sql/query.sql", "select id from users");
        runner("sqlFormat").build();

        BuildResult result = runner("sqlCheck").build();

        assertEquals(SUCCESS, result.task(":sqlCheck").getOutcome());
    }

    @Test
    void formatsFilesWithConfiguredCharset() throws IOException {
        Files.writeString(
                projectDir.resolve("build.gradle"),
                Files.readString(projectDir.resolve("build.gradle"))
                        .replace("uppercase = true", "uppercase = true\n    charset = 'UTF-16'"));
        Path sql = projectDir.resolve("sql/latin1.sql");
        Files.createDirectories(sql.getParent());
        Files.writeString(sql, "select 'caf\u00e9' as label", StandardCharsets.UTF_16);

        BuildResult result = runner("sqlFormat").build();

        assertEquals(SUCCESS, result.task(":sqlFormat").getOutcome());
        assertTrue(Files.readString(sql, StandardCharsets.UTF_16).contains("'caf\u00e9'"));
    }

    private Path write(String relative, String contents) throws IOException {
        Path path = projectDir.resolve(relative);
        Files.createDirectories(path.getParent());
        return Files.writeString(path, contents);
    }

    private GradleRunner runner(String task) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(task, "--stacktrace", "--configuration-cache");
    }
}
