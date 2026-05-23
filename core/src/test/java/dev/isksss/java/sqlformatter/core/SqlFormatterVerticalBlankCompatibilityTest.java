package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SqlFormatterVerticalBlankCompatibilityTest {
    private final SqlFormatterService service = new SqlFormatterService();

    @ParameterizedTest
    @CsvSource({
        "postgresql, select id,name from users where active=true order by id limit 10",
        "mysql, select `user`.id,`user`.name from `user` where `user`.active=1 order by `user`.id limit 10"
    })
    void matchesVerticalBlankForSupportedCompatibilityDialects(String dialect, String sql) {
        String expected = com.github.vertical_blank.sqlformatter.SqlFormatter.of(dialect)
                .format(sql, FormatConfig.builder().build());
        String actual = service.format(sql, config(dialect));

        assertEquals(expected, actual);
    }

    private FormatterConfig config(String dialect) {
        return new FormatterConfig(
                dialect, null, null, null, null, null, null, null, null, null, null, null, ErrorPolicy.THROW, null);
    }
}
