package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlFormatterFixtureTest {
    private static final FormatterConfig POSTGRESQL =
            new FormatterConfig(
                    "postgresql",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ErrorPolicy.THROW,
                    null);

    private final SqlFormatterService service = new SqlFormatterService();

    @ParameterizedTest
    @ValueSource(strings = {"select", "cte", "insert", "update", "delete", "create", "alter", "lexical"})
    void formatsPostgresqlFixtures(String name) throws IOException {
        String formatted = service.format(resource(name + ".input.sql"), POSTGRESQL);

        assertEquals(resource(name + ".expected.sql").stripTrailing(), formatted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"select", "cte", "update", "delete", "create", "alter", "lexical"})
    void keepsFormattedFixturesStable(String name) throws IOException {
        String formatted = service.format(resource(name + ".input.sql"), POSTGRESQL);

        assertEquals(formatted, service.format(formatted, POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"lexical", "select", "insert", "update"})
    void preservesSensitivePostgresqlTokens(String name) throws IOException {
        String formatted = service.format(resource(name + ".input.sql"), POSTGRESQL);

        switch (name) {
            case "lexical" -> {
                assertTrue(formatted.contains("-- keep comment"));
                assertTrue(formatted.contains("/* keep block */"));
                assertTrue(formatted.contains("\"Sales Report\""));
                assertTrue(formatted.contains("'do not alter :: text'"));
                assertTrue(formatted.contains("id = ?"));
            }
            case "select" -> {
                assertTrue(formatted.contains("\"display Name\""));
                assertTrue(formatted.contains(":limit"));
                assertTrue(formatted.contains("'2026-01-01'"));
            }
            case "insert" -> {
                assertTrue(formatted.contains("$1"));
                assertTrue(formatted.contains("'{\"kind\":\"login\"}'"));
                assertTrue(formatted.contains("array [ 'auth', 'ok' ]"));
            }
            case "update" -> {
                assertTrue(formatted.contains(":count"));
                assertTrue(formatted.contains("$1"));
                assertTrue(formatted.contains("returning sku"));
            }
            default -> throw new IllegalArgumentException("Unexpected fixture: " + name);
        }
    }

    private String resource(String name) throws IOException {
        String path = "/fixtures/postgresql/" + name;
        try (InputStream input = getClass().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing test resource " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
