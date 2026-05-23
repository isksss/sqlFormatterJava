package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.isksss.java.sqlformatter.core.engine.SqlFormatter;
import dev.isksss.java.sqlformatter.core.engine.core.FormatConfig;
import org.junit.jupiter.api.Test;

class SqlFormatterEngineDialectTest {
    @Test
    void formatsPostgresqlDialectWithPostgresqlTokens() {
        String formatted = SqlFormatter.of("postgresql")
                .format("select $1::jsonb->>'name' name from users where id=$2", FormatConfig.builder().build());

        assertEquals(
                """
                select
                  $1 :: jsonb ->> 'name' name
                from
                  users
                where
                  id = $2
                """,
                formatted + "\n");
    }

    @Test
    void formatsMysqlDialectWithBacktickIdentifiersAndDuplicateKeyUpdate() {
        String formatted = SqlFormatter.of("mysql")
                .format(
                        "insert into `users`(id,name) values(1,'a') on duplicate key update name=values(name);",
                        FormatConfig.builder().build());

        assertEquals(
                """
                insert into
                  `users`(id, name)
                values
                (1, 'a') on duplicate key
                update
                  name =
                values
                (name);
                """,
                formatted + "\n");
    }

    @Test
    void formatsSqliteDialectWithVariablesAndConflictClause() {
        String formatted = SqlFormatter.of("sqlite")
                .format(
                        "insert or replace into users(id,name) values(1,:name) on conflict(id) do update set name=excluded.name;",
                        FormatConfig.builder().build());

        assertEquals(
                """
                insert or replace into
                  users(id, name)
                values
                (1, :name) on conflict(id) do
                update
                set
                  name = excluded.name;
                """,
                formatted + "\n");
    }

    @Test
    void appliesKeywordCaseAndIndentConfig() {
        String formatted = SqlFormatter.of("postgresql")
                .format(
                        "select id,name from users where active=true",
                        FormatConfig.builder().tabWidth(4).keywordCase("upper").identifierCase("upper").build());

        assertTrue(formatted.startsWith("SELECT\n    ID,\n    NAME\nFROM\n    USERS"));
    }
}
