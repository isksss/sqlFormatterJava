package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SqlFormatterDialectServiceTest {
    private final SqlFormatterService service = new SqlFormatterService();

    @ParameterizedTest
    @CsvSource({
        "postgresql, select $1::jsonb->>'name' name, $1 :: jsonb ->> 'name'",
        "mysql, select `user`.id from `user` where `user`.active=1, `user`.active = 1",
        "sqlite, select id from users where id=? and status=:status, status = :status"
    })
    void formatsSupportedDialects(String dialect, String sql, String expectedToken) {
        FormatterConfig config =
                new FormatterConfig(dialect, null, null, null, null, null, null, null, null, null, null, null, ErrorPolicy.THROW, null);

        String formatted = service.format(sql, config);

        assertTrue(formatted.contains(expectedToken));
        assertEquals(formatted, service.format(formatted, config));
    }
}
