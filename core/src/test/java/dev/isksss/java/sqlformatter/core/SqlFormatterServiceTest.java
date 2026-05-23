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
                new FormatterConfig(
                        "postgresql",
                        4,
                        false,
                        "upper",
                        "preserve",
                        "preserve",
                        "preserve",
                        "before",
                        50,
                        1,
                        false,
                        false,
                        ErrorPolicy.THROW,
                        null));

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
                        config("unknown", ErrorPolicy.THROW)));
    }

    @Test
    void keepsInputWhenFormattingFailsByDefault() {
        String sql = "select 1";

        assertEquals(sql, service.format(sql, config("unknown", null)));
    }

    @Test
    void wrapsJoinOnConditions() {
        String formatted = service.format(
                "select a.id from table_a a left join table_b b on a.id=b.id and a.type=b.type",
                config("postgresql", ErrorPolicy.THROW));

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
                config("postgresql", ErrorPolicy.THROW));

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
                config("postgresql", ErrorPolicy.THROW));

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
        FormatterConfig config = config("postgresql", ErrorPolicy.THROW);
        String formatted = service.format(
                "with a as(select * from table_a a join table_b b on a.id=b.id and a.kind=b.kind) select * from a",
                config);

        assertEquals(formatted, service.format(formatted, config));
    }

    private FormatterConfig config(String dialect, ErrorPolicy errorPolicy) {
        return new FormatterConfig(
                dialect,
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
                errorPolicy,
                null);
    }
}
