package dev.isksss.java.sqlformatter.core.engine.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Configurations for formatting. */
public class FormatConfig {

  /** Default indentation string. */
  public static final String DEFAULT_INDENT = "  ";

  /** Default maximum column length for inline expressions. */
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

  /** Builds {@link FormatConfig} instances. */
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

    /** Creates an empty builder with default values. */
    FormatConfigBuilder() {}

    /**
     * Sets the number of spaces per indentation level.
     *
     * @param tabWidth number of spaces per indentation level
     * @return this builder
     */
    public FormatConfigBuilder tabWidth(int tabWidth) {
      if (tabWidth < 1) {
        throw new IllegalArgumentException("tabWidth must be greater than zero.");
      }
      this.indent = " ".repeat(tabWidth);
      return this;
    }

    /**
     * Enables tab indentation when requested.
     *
     * @param useTabs whether tab characters should be used for indentation
     * @return this builder
     */
    public FormatConfigBuilder useTabs(boolean useTabs) {
      if (useTabs) {
        this.indent = "\t";
      }
      return this;
    }

    /**
     * Sets the maximum column length for inline blocks.
     *
     * @param maxColumnLength Maximum length to treat inline block as one line
     * @return this builder
     */
    public FormatConfigBuilder maxColumnLength(int maxColumnLength) {
      this.maxColumnLength = maxColumnLength;
      return this;
    }

    /**
     * Sets placeholder parameters.
     *
     * @param params Collection of params for placeholder replacement
     * @return this builder
     */
    public FormatConfigBuilder params(Params params) {
      this.params = params;
      return this;
    }

    /**
     * Sets named placeholder parameters.
     *
     * @param params Collection of params for placeholder replacement
     * @return this builder
     */
    public FormatConfigBuilder params(Map<String, ?> params) {
      return params(Params.of(params));
    }

    /**
     * Sets indexed placeholder parameters.
     *
     * @param params Collection of params for placeholder replacement
     * @return this builder
     */
    public FormatConfigBuilder params(List<?> params) {
      return params(Params.of(params));
    }

    /**
     * Sets the case conversion mode for SQL keywords.
     *
     * @param keywordCase keyword case mode
     * @return this builder
     */
    public FormatConfigBuilder keywordCase(String keywordCase) {
      this.keywordCase = normalizeCase(keywordCase, "keywordCase");
      return this;
    }

    /**
     * Sets the case conversion mode for data types.
     *
     * @param dataTypeCase data type case mode
     * @return this builder
     */
    public FormatConfigBuilder dataTypeCase(String dataTypeCase) {
      this.dataTypeCase = normalizeCase(dataTypeCase, "dataTypeCase");
      return this;
    }

    /**
     * Sets the case conversion mode for function names.
     *
     * @param functionCase function name case mode
     * @return this builder
     */
    public FormatConfigBuilder functionCase(String functionCase) {
      this.functionCase = normalizeCase(functionCase, "functionCase");
      return this;
    }

    /**
     * Sets the case conversion mode for identifiers.
     *
     * @param identifierCase identifier case mode
     * @return this builder
     */
    public FormatConfigBuilder identifierCase(String identifierCase) {
      this.identifierCase = normalizeCase(identifierCase, "identifierCase");
      return this;
    }

    /**
     * Sets whether logical operators are placed before or after line breaks.
     *
     * @param logicalOperatorNewline logical operator line break position
     * @return this builder
     */
    public FormatConfigBuilder logicalOperatorNewline(String logicalOperatorNewline) {
      String normalized = logicalOperatorNewline.toLowerCase(Locale.ROOT);
      if (!normalized.equals("before") && !normalized.equals("after")) {
        throw new IllegalArgumentException("logicalOperatorNewline must be preserve, before or after.");
      }
      this.logicalOperatorNewline = normalized;
      return this;
    }

    /**
     * Sets blank lines between formatted queries.
     *
     * @param linesBetweenQueries How many line breaks between queries
     * @return this builder
     */
    public FormatConfigBuilder linesBetweenQueries(int linesBetweenQueries) {
      this.linesBetweenQueries = linesBetweenQueries;
      return this;
    }

    /**
     * Sets whether spaces around operators are removed.
     *
     * @param denseOperators whether operator spacing should be compact
     * @return this builder
     */
    public FormatConfigBuilder denseOperators(boolean denseOperators) {
      this.denseOperators = denseOperators;
      return this;
    }

    /**
     * Sets whether semicolons are placed on their own line.
     *
     * @param newlineBeforeSemicolon whether semicolons should start a new line
     * @return this builder
     */
    public FormatConfigBuilder newlineBeforeSemicolon(boolean newlineBeforeSemicolon) {
      this.newlineBeforeSemicolon = newlineBeforeSemicolon;
      return this;
    }

    /**
     * Sets whether block parentheses suppress surrounding whitespace.
     *
     * @param skipWhitespaceNearBlockParentheses skip adding whitespace before and after block
     *     Parentheses
     * @return this builder
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

    /**
     * Normalizes and validates a case conversion mode.
     *
     * @param value case conversion mode
     * @param field field name used in validation errors
     * @return normalized case conversion mode
     */
    private String normalizeCase(String value, String field) {
      String normalized = value.toLowerCase(Locale.ROOT);
      if (!normalized.equals("preserve") && !normalized.equals("upper") && !normalized.equals("lower")) {
        throw new IllegalArgumentException(field + " must be preserve, upper or lower.");
      }
      return normalized;
    }
  }
}
