package dev.isksss.java.sqlformatter.core;

import dev.isksss.java.sqlformatter.core.engine.SqlFormatter;
import dev.isksss.java.sqlformatter.core.engine.core.FormatConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL文字列を整形するサービス。
 *
 * <p>基本の整形は内部エンジンに委譲し、CTEやJOIN条件など、このライブラリが責務として持つ一部の
 * 出力安定化を後処理として適用する。
 */
public final class SqlFormatterService {
    private static final String DEFAULT_INDENT = "  ";
    private static final Pattern CTE_OPENING_PATTERN =
            Pattern.compile("(?im)^(with(?:\\s+recursive)?)[ \\t]+([^\\n]+?)[ \\t]+as[ \\t]*\\($");
    private static final Pattern JOIN_ON_PATTERN = Pattern.compile(
            "(?i)^(\\s*)((?:(?:left|right|full)(?:\\s+outer)?|inner|cross)?\\s*join\\b.+?)\\s+on\\s+(.+)$");
    private static final Pattern AND_CONDITION_PATTERN = Pattern.compile("(?i)^\\s*and\\s+(.+)$");
    private final RustCoreFormatterClient rustCoreFormatter;

    /**
     * SQL整形サービスを作成する。
     */
    public SqlFormatterService() {
        this(new RustCoreFormatterClient());
    }

    SqlFormatterService(RustCoreFormatterClient rustCoreFormatter) {
        this.rustCoreFormatter = rustCoreFormatter;
    }

    /**
     * 指定された設定でSQLを整形する。
     *
     * <p>{@link FormatterConfig#errorPolicy()} が {@link ErrorPolicy#KEEP_INPUT} の場合、
     * 整形中に例外が発生しても入力SQLをそのまま返す。
     *
     * @param sql 整形対象のSQL
     * @param config 整形設定
     * @return 整形後のSQL。設定により失敗時は入力SQLを返す
     * @throws SqlFormattingException 整形に失敗し、エラー方針が {@link ErrorPolicy#THROW} の場合
     */
    public String format(String sql, FormatterConfig config) {
        try {
            java.util.Optional<String> rustFormatted = rustCoreFormatter.format(sql, config);
            if (rustFormatted.isPresent()) {
                return rustFormatted.get();
            }
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

    /**
     * 内部エンジンの設定へ変換する。
     *
     * <p>nullの設定値はエンジン側の既定値に委ねる。
     */
    private FormatConfig toEngineConfig(FormatterConfig config) {
        FormatConfig.FormatConfigBuilder builder = FormatConfig.builder();
        if (config.tabWidth() != null) {
            builder.tabWidth(config.tabWidth());
        }
        if (config.useTabs() != null) {
            builder.useTabs(config.useTabs());
        }
        if (config.keywordCase() != null) {
            builder.keywordCase(config.keywordCase());
        }
        if (config.dataTypeCase() != null) {
            builder.dataTypeCase(config.dataTypeCase());
        }
        if (config.functionCase() != null) {
            builder.functionCase(config.functionCase());
        }
        if (config.identifierCase() != null) {
            builder.identifierCase(config.identifierCase());
        }
        if (config.logicalOperatorNewline() != null) {
            builder.logicalOperatorNewline(config.logicalOperatorNewline());
        }
        if (config.expressionWidth() != null) {
            builder.maxColumnLength(config.expressionWidth());
        }
        if (config.linesBetweenQueries() != null) {
            builder.linesBetweenQueries(config.linesBetweenQueries());
        }
        if (config.denseOperators() != null) {
            builder.denseOperators(config.denseOperators());
        }
        if (config.newlineBeforeSemicolon() != null) {
            builder.newlineBeforeSemicolon(config.newlineBeforeSemicolon());
        }
        return builder.build();
    }

    /**
     * フォーマッタエンジンだけでは安定しない構文別責務の出力を補正する。
     */
    private String normalizeResponsibilityFormatting(String sql, FormatterConfig config) {
        String indent = resolveIndent(config);
        return normalizeClauseBodyIndent(normalizeJoinOn(normalizeCteOpening(sql, indent), indent), indent);
    }

    private String resolveIndent(FormatterConfig config) {
        if (Boolean.TRUE.equals(config.useTabs())) {
            return "\t";
        }
        int tabWidth = config.tabWidth() != null ? config.tabWidth() : DEFAULT_INDENT.length();
        if (tabWidth < 1) {
            throw new IllegalArgumentException("tabWidth must be greater than zero.");
        }
        return " ".repeat(tabWidth);
    }

    /**
     * {@code WITH cte AS (} を、CTE単位の改行形式へ揃える。
     */
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

    /**
     * JOIN句のON条件をJOIN行から分離し、トップレベルのAND条件を折り返す。
     */
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

    /**
     * JOINのON条件を、文字列・識別子・括弧内・BETWEEN句を壊さずトップレベルのANDで分割する。
     */
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

    /**
     * エンジンの再整形で揺れやすい、句直後の括弧行インデントを安定化する。
     */
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

    /**
     * 指定位置にSQLキーワードが独立した語として存在するか判定する。
     */
    private boolean isKeywordAt(String text, int index, String keyword) {
        if (index + keyword.length() > text.length() || !text.regionMatches(true, index, keyword, 0, keyword.length())) {
            return false;
        }
        boolean beforeBoundary = index == 0 || !isIdentifierPart(text.charAt(index - 1));
        boolean afterBoundary = index + keyword.length() == text.length()
                || !isIdentifierPart(text.charAt(index + keyword.length()));
        return beforeBoundary && afterBoundary;
    }

    /**
     * 条件文字列がトップレベルにBETWEEN句を含むか判定する。
     */
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
