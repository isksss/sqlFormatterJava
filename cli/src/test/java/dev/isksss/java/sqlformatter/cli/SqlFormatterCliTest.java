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
                {"dialect":"postgresql","uppercase":true}
                """);
        Result result = run(
                new String[] {"--config", config.toString()},
                "select id from users");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().startsWith("SELECT\n"));
    }

    @Test
    void commandLineOptionsOverrideJsonConfig() throws Exception {
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"uppercase\":false}");
        Result result = run(
                new String[] {"--config", config.toString(), "--uppercase", "true"},
                "select id from users");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().startsWith("SELECT\n"));
    }

    @Test
    void rejectsUnknownJsonConfigFields() throws Exception {
        Path config = Files.writeString(tempDir.resolve("config.json"), "{\"keywordCase\":\"upper\"}");
        Result result = run(new String[] {"--config", config.toString()}, "select 1");

        assertEquals(2, result.exitCode());
        assertTrue(result.stderr().contains("Unknown config field"));
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
