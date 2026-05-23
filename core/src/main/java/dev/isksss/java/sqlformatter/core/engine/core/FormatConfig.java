package dev.isksss.java.sqlformatter.core.engine.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Configurations for formatting. */
public class FormatConfig {

  public static final String DEFAULT_INDENT = "  ";
  public static final int DEFAULT_COLUMN_MAX_LENGTH = 50;

  public final String indent;
  public final int maxColumnLength;
  public final Params params;
  public final String keywordCase;
  public final String dataTypeCase;
  public final String functionCase;
  public final String identifierCase;
  public final String logicalOperatorNewline;
  public final Integer linesBetweenQueries;
  public final boolean denseOperators;
  public final boolean newlineBeforeSemicolon;
  public final boolean skipWhitespaceNearBlockParentheses;

  FormatConfig(
      String indent,
      int maxColumnLength,
      Params params,
      String keywordCase,
      String dataTypeCase,
      String functionCase,
      String identifierCase,
      String logicalOperatorNewline,
      Integer linesBetweenQueries,
      boolean denseOperators,
      boolean newlineBeforeSemicolon,
      boolean skipWhitespaceNearBlockParentheses) {
    this.indent = indent;
    this.maxColumnLength = maxColumnLength;
    this.params = params == null ? Params.EMPTY : params;
    this.keywordCase = keywordCase;
    this.dataTypeCase = dataTypeCase;
    this.functionCase = functionCase;
    this.identifierCase = identifierCase;
    this.logicalOperatorNewline = logicalOperatorNewline;
    this.linesBetweenQueries = linesBetweenQueries;
    this.denseOperators = denseOperators;
    this.newlineBeforeSemicolon = newlineBeforeSemicolon;
    this.skipWhitespaceNearBlockParentheses = skipWhitespaceNearBlockParentheses;
  }

  /**
   * Returns a new empty Builder.
   *
   * @return A new empty Builder
   */
  public static FormatConfigBuilder builder() {
    return new FormatConfigBuilder();
  }

  /** FormatConfigBuilder */
  public static class FormatConfigBuilder {
    private String indent = DEFAULT_INDENT;
    private int maxColumnLength = DEFAULT_COLUMN_MAX_LENGTH;
    private Params params;
    private String keywordCase = "preserve";
    private String dataTypeCase = "preserve";
    private String functionCase = "preserve";
    private String identifierCase = "preserve";
    private String logicalOperatorNewline = "before";
    private Integer linesBetweenQueries;
    private boolean denseOperators;
    private boolean newlineBeforeSemicolon;
    private boolean skipWhitespaceNearBlockParentheses;

    FormatConfigBuilder() {}

    public FormatConfigBuilder tabWidth(int tabWidth) {
      if (tabWidth < 1) {
        throw new IllegalArgumentException("tabWidth must be greater than zero.");
      }
      this.indent = " ".repeat(tabWidth);
      return this;
    }

    public FormatConfigBuilder useTabs(boolean useTabs) {
      if (useTabs) {
        this.indent = "\t";
      }
      return this;
    }

    /**
     * @param maxColumnLength Maximum length to treat inline block as one line
     * @return This
     */
    public FormatConfigBuilder maxColumnLength(int maxColumnLength) {
      this.maxColumnLength = maxColumnLength;
      return this;
    }

    /**
     * @param params Collection of params for placeholder replacement
     * @return This
     */
    public FormatConfigBuilder params(Params params) {
      this.params = params;
      return this;
    }

    /**
     * @param params Collection of params for placeholder replacement
     * @return This
     */
    public FormatConfigBuilder params(Map<String, ?> params) {
      return params(Params.of(params));
    }

    /**
     * @param params Collection of params for placeholder replacement
     * @return This
     */
    public FormatConfigBuilder params(List<?> params) {
      return params(Params.of(params));
    }

    public FormatConfigBuilder keywordCase(String keywordCase) {
      this.keywordCase = normalizeCase(keywordCase, "keywordCase");
      return this;
    }

    public FormatConfigBuilder dataTypeCase(String dataTypeCase) {
      this.dataTypeCase = normalizeCase(dataTypeCase, "dataTypeCase");
      return this;
    }

    public FormatConfigBuilder functionCase(String functionCase) {
      this.functionCase = normalizeCase(functionCase, "functionCase");
      return this;
    }

    public FormatConfigBuilder identifierCase(String identifierCase) {
      this.identifierCase = normalizeCase(identifierCase, "identifierCase");
      return this;
    }

    public FormatConfigBuilder logicalOperatorNewline(String logicalOperatorNewline) {
      String normalized = logicalOperatorNewline.toLowerCase(Locale.ROOT);
      if (!normalized.equals("before") && !normalized.equals("after")) {
        throw new IllegalArgumentException("logicalOperatorNewline must be preserve, before or after.");
      }
      this.logicalOperatorNewline = normalized;
      return this;
    }

    /**
     * @param linesBetweenQueries How many line breaks between queries
     * @return This
     */
    public FormatConfigBuilder linesBetweenQueries(int linesBetweenQueries) {
      this.linesBetweenQueries = linesBetweenQueries;
      return this;
    }

    public FormatConfigBuilder denseOperators(boolean denseOperators) {
      this.denseOperators = denseOperators;
      return this;
    }

    public FormatConfigBuilder newlineBeforeSemicolon(boolean newlineBeforeSemicolon) {
      this.newlineBeforeSemicolon = newlineBeforeSemicolon;
      return this;
    }

    /**
     * @param skipWhitespaceNearBlockParentheses skip adding whitespace before and after block
     *     Parentheses
     * @return This
     */
    public FormatConfigBuilder skipWhitespaceNearBlockParentheses(
        boolean skipWhitespaceNearBlockParentheses) {
      this.skipWhitespaceNearBlockParentheses = skipWhitespaceNearBlockParentheses;
      return this;
    }

    /**
     * Returns an instance of FormatConfig created from the fields set on this builder.
     *
     * @return FormatConfig
     */
    public FormatConfig build() {
      return new FormatConfig(
          this.indent,
          this.maxColumnLength,
          this.params,
          this.keywordCase,
          this.dataTypeCase,
          this.functionCase,
          this.identifierCase,
          this.logicalOperatorNewline,
          this.linesBetweenQueries,
          this.denseOperators,
          this.newlineBeforeSemicolon,
          this.skipWhitespaceNearBlockParentheses);
    }

    private String normalizeCase(String value, String field) {
      String normalized = value.toLowerCase(Locale.ROOT);
      if (!normalized.equals("preserve") && !normalized.equals("upper") && !normalized.equals("lower")) {
        throw new IllegalArgumentException(field + " must be preserve, upper or lower.");
      }
      return normalized;
    }
  }
}
