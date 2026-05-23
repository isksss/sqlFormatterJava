package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SqlFormatterServiceTest {
    private final SqlFormatterService service = new SqlFormatterService();

    @Test
    void formatsWithConfiguredKeywordCase() {
        String formatted = service.format(
                "select id,name from users where active=true",
                new FormatterConfig("postgresql", "    ", true, 1, 50, ErrorPolicy.THROW, null));

        assertEquals(
                """
                SELECT
                    id,
                    NAME
                FROM
                    users
                WHERE
                    active = TRUE
                """,
                formatted + "\n");
    }

    @Test
    void rejectsUnknownDialect() {
        assertThrows(
                SqlFormattingException.class,
                () -> service.format(
                        "select 1",
                        new FormatterConfig("unknown", null, null, null, null, ErrorPolicy.THROW, null)));
    }

    @Test
    void keepsInputWhenFormattingFailsByDefault() {
        String sql = "select 1";

        assertEquals(sql, service.format(sql, new FormatterConfig("unknown", null, null, null, null)));
    }
}
