package dev.isksss.java.sqlformatter.core;

public record FormatterConfig(
        String dialect,
        String indent,
        Boolean uppercase,
        Integer linesBetweenQueries,
        Integer maxColumnLength,
        ErrorPolicy errorPolicy,
        String charset) {

    public FormatterConfig(
            String dialect, String indent, Boolean uppercase, Integer linesBetweenQueries, Integer maxColumnLength) {
        this(dialect, indent, uppercase, linesBetweenQueries, maxColumnLength, null, null);
    }

    public static FormatterConfig defaults() {
        return new FormatterConfig(null, null, null, null, null, ErrorPolicy.KEEP_INPUT, "UTF-8");
    }

    public FormatterConfig mergeOver(FormatterConfig fallback) {
        return new FormatterConfig(
                choose(dialect, fallback.dialect),
                choose(indent, fallback.indent),
                choose(uppercase, fallback.uppercase),
                choose(linesBetweenQueries, fallback.linesBetweenQueries),
                choose(maxColumnLength, fallback.maxColumnLength),
                choose(errorPolicy, fallback.errorPolicy),
                choose(charset, fallback.charset));
    }

    private static <T> T choose(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
