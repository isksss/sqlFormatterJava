package dev.isksss.java.sqlformatter.cli;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import dev.isksss.java.sqlformatter.core.ErrorPolicy;
import dev.isksss.java.sqlformatter.core.FormatterConfig;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

final class JsonConfigLoader {
    private final JsonFactory jsonFactory = new JsonFactory();

    FormatterConfig load(Path path) throws IOException {
        String dialect = null;
        String indent = null;
        Boolean uppercase = null;
        Integer linesBetweenQueries = null;
        Integer maxColumnLength = null;
        ErrorPolicy errorPolicy = null;
        String charset = null;
        try (JsonParser parser = jsonFactory.createParser(path.toFile())) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("Config must be a JSON object.");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                if (field == null) {
                    throw new IllegalArgumentException("Config fields must have names.");
                }
                parser.nextToken();
                switch (field) {
                    case "dialect" -> dialect = string(parser, field);
                    case "indent" -> indent = string(parser, field);
                    case "uppercase" -> uppercase = bool(parser, field);
                    case "linesBetweenQueries" -> linesBetweenQueries = integer(parser, field);
                    case "maxColumnLength" -> maxColumnLength = integer(parser, field);
                    case "errorPolicy" -> errorPolicy = errorPolicy(parser, field);
                    case "charset" -> charset = string(parser, field);
                    default -> throw new IllegalArgumentException("Unknown config field: " + field);
                }
            }
            if (parser.nextToken() != null) {
                throw new IllegalArgumentException("Config must contain one JSON object.");
            }
        }
        return new FormatterConfig(
                dialect, indent, uppercase, linesBetweenQueries, maxColumnLength, errorPolicy, charset);
    }

    private String string(JsonParser parser, String field) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            throw new IllegalArgumentException(field + " must be a string.");
        }
        return parser.getValueAsString();
    }

    private Boolean bool(JsonParser parser, String field) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (parser.currentToken() != JsonToken.VALUE_TRUE && parser.currentToken() != JsonToken.VALUE_FALSE) {
            throw new IllegalArgumentException(field + " must be a boolean.");
        }
        return parser.getBooleanValue();
    }

    private Integer integer(JsonParser parser, String field) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw new IllegalArgumentException(field + " must be an integer.");
        }
        try {
            return parser.getIntValue();
        } catch (IOException exception) {
            throw new IllegalArgumentException(field + " must be an integer.", exception);
        }
    }

    private ErrorPolicy errorPolicy(JsonParser parser, String field) throws IOException {
        String value = string(parser, field);
        if (value == null) {
            return null;
        }
        try {
            return ErrorPolicy.valueOf(value.replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + field + ": " + value, exception);
        }
    }
}
