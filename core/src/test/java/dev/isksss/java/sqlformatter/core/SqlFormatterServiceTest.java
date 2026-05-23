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

    @Test
    void wrapsJoinOnConditions() {
        String formatted = service.format(
                "select a.id from table_a a left join table_b b on a.id=b.id and a.type=b.type",
                new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.THROW, null));

        assertEquals(
                """
                select
                  a.id
                from
                  table_a a
                  left join table_b b
                    on a.id = b.id
                    and a.type = b.type
                """,
                formatted + "\n");
    }

    @Test
    void doesNotSplitJoinOnAndInsideStringLiterals() {
        String formatted = service.format(
                "select a.id from table_a a join table_b b on a.label='x and y' and a.id=b.id",
                new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.THROW, null));

        assertEquals(
                """
                select
                  a.id
                from
                  table_a a
                  join table_b b
                    on a.label = 'x and y'
                    and a.id = b.id
                """,
                formatted + "\n");
    }

    @Test
    void doesNotSplitJoinOnBetweenCondition() {
        String formatted = service.format(
                "select a.id from table_a a join table_b b on a.created_at between b.started_at and b.ended_at and a.id=b.id",
                new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.THROW, null));

        assertEquals(
                """
                select
                  a.id
                from
                  table_a a
                  join table_b b
                    on a.created_at between b.started_at and b.ended_at
                    and a.id = b.id
                """,
                formatted + "\n");
    }

    @Test
    void keepsCteOpeningAndJoinWrappingIdempotent() {
        FormatterConfig config = new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.THROW, null);
        String formatted = service.format(
                "with a as(select * from table_a a join table_b b on a.id=b.id and a.kind=b.kind) select * from a",
                config);

        assertEquals(formatted, service.format(formatted, config));
    }
}
