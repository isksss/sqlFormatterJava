package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SqlFormatterOptionTest {
    private final SqlFormatterService service = new SqlFormatterService();

    @Test
    void formatsWithZeroConfigDefaults() {
        assertEquals(
                """
                select
                  id
                from
                  users
                """,
                service.format("select id from users", FormatterConfig.defaults()) + "\n");
    }

    @Test
    void usesConfiguredSpaceWidthForIndentation() {
        FormatterConfig config = config(null, 4, false, null, null, null, null, null, null, null, null);

        assertEquals(
                """
                select
                    id,
                    name
                from
                    users
                where
                    active = true
                """,
                service.format("select id,name from users where active=true", config) + "\n");
    }

    @Test
    void usesTabsForEngineAndJoinPostProcessingIndentation() {
        FormatterConfig config = config(null, 2, true, null, null, null, null, null, null, null, null);

        assertEquals(
                "select\n"
                        + "\ta.id\n"
                        + "from\n"
                        + "\ta\n"
                        + "\tjoin b\n"
                        + "\t\ton a.id = b.id\n"
                        + "\t\tand a.kind = b.kind\n",
                service.format("select a.id from a join b on a.id=b.id and a.kind=b.kind", config) + "\n");
    }

    @Test
    void appliesIndependentCaseOptions() {
        FormatterConfig config = config("upper", null, null, "upper", "upper", "upper", null, null, null, null, null);

        assertEquals(
                """
                SELECT
                  COALESCE(NAME, 'x') LABEL
                FROM
                  USERS
                """,
                service.format("select coalesce(name,'x') label from users", config) + "\n");
    }

    @Test
    void appliesDataTypeCaseWithoutChangingQuotedContent() {
        FormatterConfig config = config(null, null, null, null, "upper", null, null, null, null, null, null);

        String formatted = service.format(
                "create table users(id uuid,name varchar(255),note text default 'jsonb')",
                config);

        assertEquals(
                """
                create table users(id UUID, name VARCHAR(255), note TEXT default 'jsonb')
                """,
                formatted + "\n");
    }

    @Test
    void placesLogicalOperatorsAfterConditionsWhenConfigured() {
        FormatterConfig config = config(null, null, null, null, null, null, null, "after", null, null, null);

        assertEquals(
                """
                select
                  id
                from
                  users
                where
                  active = true and
                  locked = false or
                  role = 'admin'
                """,
                service.format("select id from users where active=true and locked=false or role='admin'", config) + "\n");
    }

    @Test
    void supportsDenseOperatorsAndSemicolonNewline() {
        FormatterConfig config = config(null, null, null, null, null, null, null, null, null, true, true);

        assertEquals(
                """
                select
                  id
                from
                  users
                where
                  a+b>=10
                ;
                select
                  2
                ;
                """,
                service.format("select id from users where a+b>=10; select 2;", config) + "\n");
    }

    @Test
    void appliesExpressionWidthAndLinesBetweenQueries() {
        FormatterConfig expressionConfig = config(null, null, null, null, null, null, 5, null, null, null, null);
        FormatterConfig querySpacingConfig = config(null, null, null, null, null, null, null, null, 2, null, null);

        assertEquals(
                """
                select
                  coalesce(
                    a,
                    b,
                    c
                  )
                from
                  metrics
                """,
                service.format("select coalesce(a,b,c) from metrics", expressionConfig) + "\n");
        assertEquals(
                """
                select
                  1;

                select
                  2;
                """,
                service.format("select 1; select 2;", querySpacingConfig) + "\n");
    }

    private FormatterConfig config(
            String keywordCase,
            Integer tabWidth,
            Boolean useTabs,
            String identifierCase,
            String dataTypeCase,
            String functionCase,
            Integer expressionWidth,
            String logicalOperatorNewline,
            Integer linesBetweenQueries,
            Boolean denseOperators,
            Boolean newlineBeforeSemicolon) {
        return new FormatterConfig(
                "postgresql",
                tabWidth,
                useTabs,
                keywordCase,
                dataTypeCase,
                functionCase,
                identifierCase,
                logicalOperatorNewline,
                expressionWidth,
                linesBetweenQueries,
                denseOperators,
                newlineBeforeSemicolon,
                ErrorPolicy.THROW,
                null);
    }
}
