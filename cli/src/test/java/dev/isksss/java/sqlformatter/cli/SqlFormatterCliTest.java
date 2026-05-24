package dev.isksss.java.sqlformatter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.isksss.java.sqlformatter.core.SqlFormatterService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlFormatterCliTest {
    @TempDir
    Path tempDir;

    @Test
    void readsStdinAndAppliesJsonConfig() throws Exception {
        Path config = Files.writeString(
                tempDir.resolve("sql-formatter.json"),
                """
                {"dialect":"postgresql","keywordCase":"upper"}
                """);
        Result result = run(
                new String[] {"--config", config.toString()},
                "select id from users");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().startsWith("SELECT\n"));
    }

    @Test
    void formatsWithoutConfigOrDialect() {
        Result result = run(new String[] {}, "select id from users");

        assertEquals(0, result.exitCode());
        assertEquals(
                """
                select
                  id
                from
                  users
                """,
                result.stdout() + "\n");
    }

    @Test
    void commandLineOptionsOverrideJsonConfig() throws Exception {
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"keywordCase\":\"lower\"}");
        Result result = run(
                new String[] {"--config", config.toString(), "--keyword-case", "upper"},
                "select id from users");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().startsWith("SELECT\n"));
    }

    @Test
    void rejectsUnknownJsonConfigFields() throws Exception {
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"uppercase\":true}");
        Result result = run(new String[] {"--config", config.toString()}, "select 1");

        assertEquals(2, result.exitCode());
        assertTrue(result.stderr().contains("Unknown config field"));
    }

    @Test
    void rejectsInvalidJsonConfigValueTypes() throws Exception {
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"tabWidth\":\"2\"}");
        Result result = run(new String[] {"--config", config.toString()}, "select 1");

        assertEquals(2, result.exitCode());
        assertTrue(result.stderr().contains("tabWidth must be an integer"));
    }

    @Test
    void printsUsageForHelp() {
        Result result = run(new String[] {"--help"}, "select 1");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().startsWith("Usage: sql-formatter-java"));
        assertEquals("", result.stderr());
    }

    @Test
    void rejectsMultipleInputFiles() throws Exception {
        Path first = Files.writeString(tempDir.resolve("first.sql"), "select 1");
        Path second = Files.writeString(tempDir.resolve("second.sql"), "select 2");

        Result result = run(new String[] {first.toString(), second.toString()}, "");

        assertEquals(2, result.exitCode());
        assertTrue(result.stderr().contains("Only one SQL input file is supported"));
    }

    @Test
    void readsConfiguredInputCharset() throws Exception {
        Path sql = Files.writeString(
                tempDir.resolve("latin1.sql"),
                "select 'caf\u00e9' as label",
                StandardCharsets.ISO_8859_1);
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"charset\":\"ISO-8859-1\"}");

        Result result = run(new String[] {"--config", config.toString(), sql.toString()}, "");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("'caf\u00e9'"));
    }

    @Test
    void appliesIndentAndCaseOptions() {
        Result result = run(
                new String[] {
                    "--dialect", "postgresql", "--tab-width", "4", "--keyword-case", "upper", "--data-type-case", "upper"
                },
                "create table users(id uuid)");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("CREATE TABLE users(id UUID)"));
    }

    @Test
    void exposesThrowErrorPolicyAsFailure() {
        Result result = run(
                new String[] {"--dialect", "unknown", "--error-policy", "throw"},
                "select 1");

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Failed to format SQL"));
    }

    private Result run(String[] args, String stdin) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        int exitCode = new SqlFormatterCli(new JsonConfigLoader(), new SqlFormatterService())
                .run(
                        args,
                        new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                        new PrintStream(stdout, true, StandardCharsets.UTF_8),
                        new PrintStream(stderr, true, StandardCharsets.UTF_8));
        return new Result(exitCode, stdout.toString(StandardCharsets.UTF_8), stderr.toString(StandardCharsets.UTF_8));
    }

    private record Result(int exitCode, String stdout, String stderr) {}
}
