package dev.isksss.java.sqlformatter.core;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlFormatterService {
    private static final String DEFAULT_INDENT = "  ";
    private static final Pattern CTE_OPENING_PATTERN =
            Pattern.compile("(?im)^(with(?:\\s+recursive)?)[ \\t]+([^\\n]+?)[ \\t]+as[ \\t]*\\($");
    private static final Pattern JOIN_ON_PATTERN = Pattern.compile(
            "(?i)^(\\s*)((?:(?:left|right|full)(?:\\s+outer)?|inner|cross)?\\s*join\\b.+?)\\s+on\\s+(.+)$");
    private static final Pattern AND_CONDITION_PATTERN = Pattern.compile("(?i)^\\s*and\\s+(.+)$");

    public String format(String sql, FormatterConfig config) {
        try {
            FormatConfig formatConfig = toEngineConfig(config);
            String formatted;
            if (config.dialect() == null || config.dialect().isBlank()) {
                formatted = SqlFormatter.format(sql, formatConfig);
            } else {
                formatted = SqlFormatter.of(config.dialect()).format(sql, formatConfig);
            }
            return normalizeResponsibilityFormatting(formatted, config);
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

    private String normalizeResponsibilityFormatting(String sql, FormatterConfig config) {
        String indent = config.indent() != null ? config.indent() : DEFAULT_INDENT;
        return normalizeClauseBodyIndent(normalizeJoinOn(normalizeCteOpening(sql, indent), indent), indent);
    }

    private String normalizeCteOpening(String sql, String indent) {
        Matcher matcher = CTE_OPENING_PATTERN.matcher(sql);
        StringBuffer normalized = new StringBuffer();
        while (matcher.find()) {
            String withKeyword = matcher.group(1);
            String cteName = matcher.group(2);
            String asKeyword = keyword(withKeyword, "as");
            matcher.appendReplacement(
                    normalized,
                    Matcher.quoteReplacement(withKeyword + "\n" + indent + cteName + " " + asKeyword + " ("));
        }
        matcher.appendTail(normalized);
        return normalized.toString();
    }

    private String normalizeJoinOn(String sql, String indent) {
        String[] lines = sql.split("\\R", -1);
        List<String> normalized = new ArrayList<>(lines.length);
        String joinConditionIndent = null;
        String joinAndKeyword = null;
        boolean joinBetweenNeedsAnd = false;
        for (String line : lines) {
            Matcher matcher = JOIN_ON_PATTERN.matcher(line);
            if (!matcher.matches()) {
                Matcher andMatcher = AND_CONDITION_PATTERN.matcher(line);
                if (joinConditionIndent != null && andMatcher.matches()) {
                    if (joinBetweenNeedsAnd) {
                        int last = normalized.size() - 1;
                        normalized.set(last, normalized.get(last) + " " + joinAndKeyword + " " + andMatcher.group(1));
                        joinBetweenNeedsAnd = false;
                        continue;
                    }
                    normalized.add(joinConditionIndent + joinAndKeyword + " " + andMatcher.group(1));
                    continue;
                }
                joinConditionIndent = null;
                joinAndKeyword = null;
                joinBetweenNeedsAnd = false;
                normalized.add(line);
                continue;
            }

            String leading = matcher.group(1);
            String joinClause = matcher.group(2);
            List<String> conditions = splitAndConditions(matcher.group(3));
            String onKeyword = keyword(joinClause, "on");
            String andKeyword = keyword(joinClause, "and");

            normalized.add(leading + joinClause);
            normalized.add(leading + indent + onKeyword + " " + conditions.get(0));
            for (int i = 1; i < conditions.size(); i++) {
                normalized.add(leading + indent + andKeyword + " " + conditions.get(i));
            }
            joinConditionIndent = leading + indent;
            joinAndKeyword = andKeyword;
            joinBetweenNeedsAnd = containsTopLevelBetween(conditions.get(conditions.size() - 1));
        }
        return String.join("\n", normalized);
    }

    private List<String> splitAndConditions(String condition) {
        List<String> conditions = new ArrayList<>();
        int start = 0;
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean betweenNeedsAnd = false;
        for (int i = 0; i < condition.length(); i++) {
            char current = condition.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                if (inSingleQuote && i + 1 < condition.length() && condition.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')' && depth > 0) {
                depth--;
                continue;
            }
            if (depth == 0 && isKeywordAt(condition, i, "between")) {
                betweenNeedsAnd = true;
                i += "between".length() - 1;
                continue;
            }
            if (depth == 0 && isAndSeparator(condition, i)) {
                if (betweenNeedsAnd) {
                    betweenNeedsAnd = false;
                    i += 2;
                    continue;
                }
                conditions.add(condition.substring(start, i).trim());
                i += 2;
                start = i + 1;
            }
        }
        conditions.add(condition.substring(start).trim());
        return conditions;
    }

    private String normalizeClauseBodyIndent(String sql, String indent) {
        String[] lines = sql.split("\\R", -1);
        List<String> normalized = new ArrayList<>(lines.length);
        String previousKeyword = "";
        for (String line : lines) {
            String trimmed = line.stripLeading();
            if (shouldIndentAfter(previousKeyword, trimmed) && !line.startsWith(indent)) {
                normalized.add(indent + line);
            } else {
                normalized.add(line);
            }
            previousKeyword = line.trim().toLowerCase(Locale.ROOT);
        }
        return String.join("\n", normalized);
    }

    private boolean shouldIndentAfter(String previousKeyword, String currentTrimmed) {
        return (previousKeyword.equals("insert") || previousKeyword.equals("values") || previousKeyword.equals("where"))
                && currentTrimmed.startsWith("(");
    }

    private boolean isAndSeparator(String text, int index) {
        return isKeywordAt(text, index, "and");
    }

    private boolean isKeywordAt(String text, int index, String keyword) {
        if (index + keyword.length() > text.length() || !text.regionMatches(true, index, keyword, 0, keyword.length())) {
            return false;
        }
        boolean beforeBoundary = index == 0 || !isIdentifierPart(text.charAt(index - 1));
        boolean afterBoundary = index + keyword.length() == text.length()
                || !isIdentifierPart(text.charAt(index + keyword.length()));
        return beforeBoundary && afterBoundary;
    }

    private boolean containsTopLevelBetween(String condition) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int depth = 0;
        for (int i = 0; i < condition.length(); i++) {
            char current = condition.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                if (inSingleQuote && i + 1 < condition.length() && condition.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')' && depth > 0) {
                depth--;
                continue;
            }
            if (depth == 0 && isKeywordAt(condition, i, "between")) {
                return true;
            }
        }
        return false;
    }

    private boolean isIdentifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    private String keyword(String context, String lowerCaseKeyword) {
        return context.equals(context.toUpperCase(Locale.ROOT)) ? lowerCaseKeyword.toUpperCase(Locale.ROOT) : lowerCaseKeyword;
    }
}
