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
        Integer tabWidth = null;
        Boolean useTabs = null;
        String keywordCase = null;
        String dataTypeCase = null;
        String functionCase = null;
        String identifierCase = null;
        String logicalOperatorNewline = null;
        Integer expressionWidth = null;
        Integer linesBetweenQueries = null;
        Boolean denseOperators = null;
        Boolean newlineBeforeSemicolon = null;
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
                    case "tabWidth" -> tabWidth = integer(parser, field);
                    case "useTabs" -> useTabs = bool(parser, field);
                    case "keywordCase" -> keywordCase = string(parser, field);
                    case "dataTypeCase" -> dataTypeCase = string(parser, field);
                    case "functionCase" -> functionCase = string(parser, field);
                    case "identifierCase" -> identifierCase = string(parser, field);
                    case "logicalOperatorNewline" -> logicalOperatorNewline = string(parser, field);
                    case "expressionWidth" -> expressionWidth = integer(parser, field);
                    case "linesBetweenQueries" -> linesBetweenQueries = integer(parser, field);
                    case "denseOperators" -> denseOperators = bool(parser, field);
                    case "newlineBeforeSemicolon" -> newlineBeforeSemicolon = bool(parser, field);
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
                dialect,
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
                errorPolicy,
                charset);
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
