package dev.isksss.java.sqlformatter.cli;

import dev.isksss.java.sqlformatter.core.FormatterConfig;
import dev.isksss.java.sqlformatter.core.ErrorPolicy;
import java.nio.file.Path;
import java.util.Locale;

record CliArguments(Path configFile, Path sqlFile, Path rustCorePath, FormatterConfig overrides) {
    static CliArguments parse(String[] args) {
        Path configFile = null;
        Path sqlFile = null;
        Path rustCorePath = null;
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

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--config" -> configFile = Path.of(nextValue(args, ++index, argument));
                case "--rust-core" -> rustCorePath = Path.of(nextValue(args, ++index, argument));
                case "--dialect" -> dialect = nextValue(args, ++index, argument);
                case "--tab-width" -> tabWidth = parseInteger(nextValue(args, ++index, argument), argument);
                case "--use-tabs" -> useTabs = Boolean.parseBoolean(nextValue(args, ++index, argument));
                case "--keyword-case" -> keywordCase = nextValue(args, ++index, argument);
                case "--data-type-case" -> dataTypeCase = nextValue(args, ++index, argument);
                case "--function-case" -> functionCase = nextValue(args, ++index, argument);
                case "--identifier-case" -> identifierCase = nextValue(args, ++index, argument);
                case "--logical-operator-newline" -> logicalOperatorNewline = nextValue(args, ++index, argument);
                case "--expression-width" -> expressionWidth = parseInteger(nextValue(args, ++index, argument), argument);
                case "--lines-between-queries" ->
                        linesBetweenQueries = parseInteger(nextValue(args, ++index, argument), argument);
                case "--dense-operators" -> denseOperators = Boolean.parseBoolean(nextValue(args, ++index, argument));
                case "--newline-before-semicolon" ->
                        newlineBeforeSemicolon = Boolean.parseBoolean(nextValue(args, ++index, argument));
                case "--error-policy" -> errorPolicy = parseErrorPolicy(nextValue(args, ++index, argument));
                case "--charset" -> charset = nextValue(args, ++index, argument);
                case "--help", "-h" -> throw new HelpRequestedException();
                default -> {
                    if (argument.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + argument);
                    }
                    if (sqlFile != null) {
                        throw new IllegalArgumentException("Only one SQL input file is supported.");
                    }
                    sqlFile = Path.of(argument);
                }
            }
        }

        return new CliArguments(
                configFile,
                sqlFile,
                rustCorePath,
                new FormatterConfig(
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
                        charset));
    }

    private static String nextValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option + ".");
        }
        return args[index];
    }

    private static int parseInteger(String value, String option) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer for " + option + ": " + value, exception);
        }
    }

    private static ErrorPolicy parseErrorPolicy(String value) {
        try {
            return ErrorPolicy.valueOf(value.replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid error policy: " + value, exception);
        }
    }
}
