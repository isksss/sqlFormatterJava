package dev.isksss.java.sqlformatter.core;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;

public final class SqlFormatterService {
    public String format(String sql, FormatterConfig config) {
        try {
            FormatConfig formatConfig = toEngineConfig(config);
            if (config.dialect() == null || config.dialect().isBlank()) {
                return SqlFormatter.format(sql, formatConfig);
            }
            return SqlFormatter.of(config.dialect()).format(sql, formatConfig);
        } catch (RuntimeException exception) {
            if (config.errorPolicy() != ErrorPolicy.THROW) {
                return sql;
            }
            throw new SqlFormattingException("Failed to format SQL.", exception);
        }
    }

    private FormatConfig toEngineConfig(FormatterConfig config) {
        FormatConfig.FormatConfigBuilder builder = FormatConfig.builder();
        if (config.indent() != null) {
            builder.indent(config.indent());
        }
        if (config.uppercase() != null) {
            builder.uppercase(config.uppercase());
        }
        if (config.linesBetweenQueries() != null) {
            builder.linesBetweenQueries(config.linesBetweenQueries());
        }
        if (config.maxColumnLength() != null) {
            builder.maxColumnLength(config.maxColumnLength());
        }
        return builder.build();
    }
}
