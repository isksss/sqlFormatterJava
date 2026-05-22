package dev.isksss.java.sqlformatter.core;

public record FormatterConfig(
        String dialect,
        String indent,
        Boolean uppercase,
        Integer linesBetweenQueries,
        Integer maxColumnLength) {

    public static FormatterConfig defaults() {
        return new FormatterConfig(null, null, null, null, null);
    }

    public FormatterConfig mergeOver(FormatterConfig fallback) {
        return new FormatterConfig(
                choose(dialect, fallback.dialect),
                choose(indent, fallback.indent),
                choose(uppercase, fallback.uppercase),
                choose(linesBetweenQueries, fallback.linesBetweenQueries),
                choose(maxColumnLength, fallback.maxColumnLength));
    }

    private static <T> T choose(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
