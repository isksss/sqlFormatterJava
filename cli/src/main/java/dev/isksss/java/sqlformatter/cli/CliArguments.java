package dev.isksss.java.sqlformatter.cli;

import dev.isksss.java.sqlformatter.core.FormatterConfig;
import dev.isksss.java.sqlformatter.core.ErrorPolicy;
import java.nio.file.Path;
import java.util.Locale;

record CliArguments(Path configFile, Path sqlFile, FormatterConfig overrides) {
    static CliArguments parse(String[] args) {
        Path configFile = null;
        Path sqlFile = null;
        String dialect = null;
        String indent = null;
        Boolean uppercase = null;
        Integer linesBetweenQueries = null;
        Integer maxColumnLength = null;
        ErrorPolicy errorPolicy = null;
        String charset = null;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--config" -> configFile = Path.of(nextValue(args, ++index, argument));
                case "--dialect" -> dialect = nextValue(args, ++index, argument);
                case "--indent" -> indent = nextValue(args, ++index, argument);
                case "--uppercase" -> uppercase = Boolean.parseBoolean(nextValue(args, ++index, argument));
                case "--lines-between-queries" ->
                        linesBetweenQueries = parseInteger(nextValue(args, ++index, argument), argument);
                case "--max-column-length" ->
                        maxColumnLength = parseInteger(nextValue(args, ++index, argument), argument);
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
                new FormatterConfig(dialect, indent, uppercase, linesBetweenQueries, maxColumnLength, errorPolicy, charset));
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
