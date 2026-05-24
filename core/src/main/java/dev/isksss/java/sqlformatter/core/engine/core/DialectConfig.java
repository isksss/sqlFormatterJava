package dev.isksss.java.sqlformatter.core.engine.core;

import dev.isksss.java.sqlformatter.core.engine.core.util.Util;
import java.util.Arrays;
import java.util.List;

/** SQL dialect-specific tokenizer and formatter configuration. */
public class DialectConfig {
  public final List<String> lineCommentTypes;
  public final List<String> reservedTopLevelWords;
  public final List<String> reservedTopLevelWordsNoIndent;
  public final List<String> reservedNewlineWords;
  public final List<String> reservedWords;
  public final List<String> dataTypes;
  public final List<String> specialWordChars;
  public final List<String> stringTypes;
  public final List<String> openParens;
  public final List<String> closeParens;
  public final List<String> indexedPlaceholderTypes;
  public final List<String> namedPlaceholderTypes;
  public final List<String> operators;

  /** Creates a dialect configuration from the provided keyword and token lists. */
  DialectConfig(
      List<String> lineCommentTypes,
      List<String> reservedTopLevelWords,
      List<String> reservedNewlineWords,
      List<String> reservedTopLevelWordsNoIndent,
      List<String> reservedWords,
      List<String> dataTypes,
      List<String> specialWordChars,
      List<String> stringTypes,
      List<String> openParens,
      List<String> closeParens,
      List<String> indexedPlaceholderTypes,
      List<String> namedPlaceholderTypes,
      List<String> operators) {
    this.lineCommentTypes = Util.nullToEmpty(lineCommentTypes);
    this.reservedTopLevelWords = Util.nullToEmpty(reservedTopLevelWords);
    this.reservedTopLevelWordsNoIndent = Util.nullToEmpty(reservedTopLevelWordsNoIndent);
    this.reservedNewlineWords = Util.nullToEmpty(reservedNewlineWords);
    this.reservedWords = Util.nullToEmpty(reservedWords);
    this.dataTypes = Util.nullToEmpty(dataTypes);
    this.specialWordChars = Util.nullToEmpty(specialWordChars);
    this.stringTypes = Util.nullToEmpty(stringTypes);
    this.openParens = Util.nullToEmpty(openParens);
    this.closeParens = Util.nullToEmpty(closeParens);
    this.indexedPlaceholderTypes = Util.nullToEmpty(indexedPlaceholderTypes);
    this.namedPlaceholderTypes = Util.nullToEmpty(namedPlaceholderTypes);
    this.operators = Util.nullToEmpty(operators);
  }

  /**
   * Returns a copy with line comment marker types replaced.
   *
   * @param lineCommentTypes line comment marker types
   * @return copied configuration
   */
  public DialectConfig withLineCommentTypes(List<String> lineCommentTypes) {
    return this.toBuilder().lineCommentTypes(lineCommentTypes).build();
  }

  /**
   * Returns a copy with additional line comment marker types.
   *
   * @param lineCommentTypes line comment marker types to append
   * @return copied configuration
   */
  public DialectConfig plusLineCommentTypes(String... lineCommentTypes) {
    return this.plusLineCommentTypes(Arrays.asList(lineCommentTypes));
  }

  /**
   * Returns a copy with additional line comment marker types.
   *
   * @param lineCommentTypes line comment marker types to append
   * @return copied configuration
   */
  public DialectConfig plusLineCommentTypes(List<String> lineCommentTypes) {
    return this.toBuilder()
        .lineCommentTypes(Util.concat(this.lineCommentTypes, lineCommentTypes))
        .build();
  }

  /**
   * Returns a copy with top-level reserved words replaced.
   *
   * @param reservedTopLevelWords top-level reserved words
   * @return copied configuration
   */
  public DialectConfig withReservedTopLevelWords(List<String> reservedTopLevelWords) {
    return this.toBuilder().reservedTopLevelWords(reservedTopLevelWords).build();
  }

  /**
   * Returns a copy with additional top-level reserved words.
   *
   * @param reservedTopLevelWords top-level reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedTopLevelWords(String... reservedTopLevelWords) {
    return this.plusReservedTopLevelWords(Arrays.asList(reservedTopLevelWords));
  }

  /**
   * Returns a copy with additional top-level reserved words.
   *
   * @param reservedTopLevelWords top-level reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedTopLevelWords(List<String> reservedTopLevelWords) {
    return this.toBuilder()
        .reservedTopLevelWords(Util.concat(this.reservedTopLevelWords, reservedTopLevelWords))
        .build();
  }

  /**
   * Returns a copy with newline reserved words replaced.
   *
   * @param reservedNewlineWords reserved words that force a newline
   * @return copied configuration
   */
  public DialectConfig withReservedNewlineWords(List<String> reservedNewlineWords) {
    return this.toBuilder().reservedNewlineWords(reservedNewlineWords).build();
  }

  /**
   * Returns a copy with additional newline reserved words.
   *
   * @param reservedNewlineWords reserved words that force a newline
   * @return copied configuration
   */
  public DialectConfig plusReservedNewlineWords(String... reservedNewlineWords) {
    return this.plusReservedNewlineWords(Arrays.asList(reservedNewlineWords));
  }

  /**
   * Returns a copy with additional newline reserved words.
   *
   * @param reservedNewlineWords reserved words that force a newline
   * @return copied configuration
   */
  public DialectConfig plusReservedNewlineWords(List<String> reservedNewlineWords) {
    return this.toBuilder()
        .reservedNewlineWords(Util.concat(this.reservedNewlineWords, reservedNewlineWords))
        .build();
  }

  /**
   * Returns a copy with non-indenting top-level reserved words replaced.
   *
   * @param reservedTopLevelWordsNoIndent non-indenting top-level reserved words
   * @return copied configuration
   */
  public DialectConfig withReservedTopLevelWordsNoIndent(
      List<String> reservedTopLevelWordsNoIndent) {
    return this.toBuilder().reservedTopLevelWordsNoIndent(reservedTopLevelWordsNoIndent).build();
  }

  /**
   * Returns a copy with additional non-indenting top-level reserved words.
   *
   * @param reservedTopLevelWordsNoIndent non-indenting top-level reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedTopLevelWordsNoIndent(String... reservedTopLevelWordsNoIndent) {
    return this.plusReservedTopLevelWordsNoIndent(Arrays.asList(reservedTopLevelWordsNoIndent));
  }

  /**
   * Returns a copy with additional non-indenting top-level reserved words.
   *
   * @param reservedTopLevelWordsNoIndent non-indenting top-level reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedTopLevelWordsNoIndent(
      List<String> reservedTopLevelWordsNoIndent) {
    return this.toBuilder()
        .reservedTopLevelWordsNoIndent(
            Util.concat(this.reservedTopLevelWordsNoIndent, reservedTopLevelWordsNoIndent))
        .build();
  }

  /**
   * Returns a copy with reserved words replaced.
   *
   * @param reservedWords reserved words
   * @return copied configuration
   */
  public DialectConfig withReservedWords(List<String> reservedWords) {
    return this.toBuilder().reservedWords(reservedWords).build();
  }

  /**
   * Returns a copy with additional reserved words.
   *
   * @param reservedWords reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedWords(String... reservedWords) {
    return this.plusReservedWords(Arrays.asList(reservedWords));
  }

  /**
   * Returns a copy with additional reserved words.
   *
   * @param reservedWords reserved words to append
   * @return copied configuration
   */
  public DialectConfig plusReservedWords(List<String> reservedWords) {
    return this.toBuilder().reservedWords(Util.concat(this.reservedWords, reservedWords)).build();
  }

  /**
   * Returns a copy with data types replaced.
   *
   * @param dataTypes data types
   * @return copied configuration
   */
  public DialectConfig withDataTypes(List<String> dataTypes) {
    return this.toBuilder().dataTypes(dataTypes).build();
  }

  /**
   * Returns a copy with additional data types.
   *
   * @param dataTypes data types to append
   * @return copied configuration
   */
  public DialectConfig plusDataTypes(String... dataTypes) {
    return this.plusDataTypes(Arrays.asList(dataTypes));
  }

  /**
   * Returns a copy with additional data types.
   *
   * @param dataTypes data types to append
   * @return copied configuration
   */
  public DialectConfig plusDataTypes(List<String> dataTypes) {
    return this.toBuilder().dataTypes(Util.concat(this.dataTypes, dataTypes)).build();
  }

  /**
   * Returns a copy with special word characters replaced.
   *
   * @param specialWordChars special word characters
   * @return copied configuration
   */
  public DialectConfig withSpecialWordChars(List<String> specialWordChars) {
    return this.toBuilder().specialWordChars(specialWordChars).build();
  }

  /**
   * Returns a copy with additional special word characters.
   *
   * @param specialWordChars special word characters to append
   * @return copied configuration
   */
  public DialectConfig plusSpecialWordChars(String... specialWordChars) {
    return this.plusSpecialWordChars(Arrays.asList(specialWordChars));
  }

  /**
   * Returns a copy with additional special word characters.
   *
   * @param specialWordChars special word characters to append
   * @return copied configuration
   */
  public DialectConfig plusSpecialWordChars(List<String> specialWordChars) {
    return this.toBuilder()
        .specialWordChars(Util.concat(this.specialWordChars, specialWordChars))
        .build();
  }

  /**
   * Returns a copy with string literal types replaced.
   *
   * @param stringTypes string literal types
   * @return copied configuration
   */
  public DialectConfig withStringTypes(List<String> stringTypes) {
    return this.toBuilder().stringTypes(stringTypes).build();
  }

  /**
   * Returns a copy with additional string literal types.
   *
   * @param stringTypes string literal types to append
   * @return copied configuration
   */
  public DialectConfig plusStringTypes(String... stringTypes) {
    return this.plusStringTypes(Arrays.asList(stringTypes));
  }

  /**
   * Returns a copy with additional string literal types.
   *
   * @param stringTypes string literal types to append
   * @return copied configuration
   */
  public DialectConfig plusStringTypes(List<String> stringTypes) {
    return this.toBuilder().stringTypes(Util.concat(this.stringTypes, stringTypes)).build();
  }

  /**
   * Returns a copy with opening parentheses replaced.
   *
   * @param openParens opening parentheses
   * @return copied configuration
   */
  public DialectConfig withOpenParens(List<String> openParens) {
    return this.toBuilder().openParens(openParens).build();
  }

  /**
   * Returns a copy with additional opening parentheses.
   *
   * @param openParens opening parentheses to append
   * @return copied configuration
   */
  public DialectConfig plusOpenParens(String... openParens) {
    return this.plusOpenParens(Arrays.asList(openParens));
  }

  /**
   * Returns a copy with additional opening parentheses.
   *
   * @param openParens opening parentheses to append
   * @return copied configuration
   */
  public DialectConfig plusOpenParens(List<String> openParens) {
    return this.toBuilder().openParens(Util.concat(this.openParens, openParens)).build();
  }

  /**
   * Returns a copy with closing parentheses replaced.
   *
   * @param closeParens closing parentheses
   * @return copied configuration
   */
  public DialectConfig withCloseParens(List<String> closeParens) {
    return this.toBuilder().closeParens(closeParens).build();
  }

  /**
   * Returns a copy with additional closing parentheses.
   *
   * @param closeParens closing parentheses to append
   * @return copied configuration
   */
  public DialectConfig plusCloseParens(String... closeParens) {
    return this.plusCloseParens(Arrays.asList(closeParens));
  }

  /**
   * Returns a copy with additional closing parentheses.
   *
   * @param closeParens closing parentheses to append
   * @return copied configuration
   */
  public DialectConfig plusCloseParens(List<String> closeParens) {
    return this.toBuilder().closeParens(Util.concat(this.closeParens, closeParens)).build();
  }

  /**
   * Returns a copy with indexed placeholder types replaced.
   *
   * @param indexedPlaceholderTypes indexed placeholder types
   * @return copied configuration
   */
  public DialectConfig withIndexedPlaceholderTypes(List<String> indexedPlaceholderTypes) {
    return this.toBuilder().indexedPlaceholderTypes(indexedPlaceholderTypes).build();
  }

  /**
   * Returns a copy with additional indexed placeholder types.
   *
   * @param indexedPlaceholderTypes indexed placeholder types to append
   * @return copied configuration
   */
  public DialectConfig plusIndexedPlaceholderTypes(String... indexedPlaceholderTypes) {
    return this.plusIndexedPlaceholderTypes(Arrays.asList(indexedPlaceholderTypes));
  }

  /**
   * Returns a copy with additional indexed placeholder types.
   *
   * @param indexedPlaceholderTypes indexed placeholder types to append
   * @return copied configuration
   */
  public DialectConfig plusIndexedPlaceholderTypes(List<String> indexedPlaceholderTypes) {
    return this.toBuilder()
        .indexedPlaceholderTypes(Util.concat(this.indexedPlaceholderTypes, indexedPlaceholderTypes))
        .build();
  }

  /**
   * Returns a copy with named placeholder types replaced.
   *
   * @param namedPlaceholderTypes named placeholder types
   * @return copied configuration
   */
  public DialectConfig withNamedPlaceholderTypes(List<String> namedPlaceholderTypes) {
    return this.toBuilder().namedPlaceholderTypes(namedPlaceholderTypes).build();
  }

  /**
   * Returns a copy with additional named placeholder types.
   *
   * @param namedPlaceholderTypes named placeholder types to append
   * @return copied configuration
   */
  public DialectConfig plusNamedPlaceholderTypes(String... namedPlaceholderTypes) {
    return this.plusNamedPlaceholderTypes(Arrays.asList(namedPlaceholderTypes));
  }

  /**
   * Returns a copy with additional named placeholder types.
   *
   * @param namedPlaceholderTypes named placeholder types to append
   * @return copied configuration
   */
  public DialectConfig plusNamedPlaceholderTypes(List<String> namedPlaceholderTypes) {
    return this.toBuilder()
        .namedPlaceholderTypes(Util.concat(this.namedPlaceholderTypes, namedPlaceholderTypes))
        .build();
  }

  /**
   * Returns a copy with operators replaced.
   *
   * @param Operators operators
   * @return copied configuration
   */
  public DialectConfig withOperators(List<String> Operators) {
    return this.toBuilder().operators(Operators).build();
  }

  /**
   * Returns a copy with additional operators.
   *
   * @param operators operators to append
   * @return copied configuration
   */
  public DialectConfig plusOperators(String... operators) {
    return this.plusOperators(Arrays.asList(operators));
  }

  /**
   * Returns a copy with additional operators.
   *
   * @param operators operators to append
   * @return copied configuration
   */
  public DialectConfig plusOperators(List<String> operators) {
    return this.toBuilder().operators(Util.concat(this.operators, operators)).build();
  }

  /**
   * Returns a builder initialized from this configuration.
   *
   * @return initialized builder
   */
  public DialectConfigBuilder toBuilder() {
    return DialectConfig.builder()
        .reservedWords(this.reservedWords)
        .reservedTopLevelWords(this.reservedTopLevelWords)
        .reservedTopLevelWordsNoIndent(this.reservedTopLevelWordsNoIndent)
        .reservedNewlineWords(this.reservedNewlineWords)
        .dataTypes(this.dataTypes)
        .stringTypes(this.stringTypes)
        .openParens(this.openParens)
        .closeParens(this.closeParens)
        .indexedPlaceholderTypes(this.indexedPlaceholderTypes)
        .namedPlaceholderTypes(this.namedPlaceholderTypes)
        .lineCommentTypes(this.lineCommentTypes)
        .specialWordChars(this.specialWordChars)
        .operators(this.operators);
  }

  /**
   * Returns a new empty builder.
   *
   * @return new builder
   */
  public static DialectConfigBuilder builder() {
    return new DialectConfigBuilder();
  }

  /** Builds {@link DialectConfig} instances. */
  public static class DialectConfigBuilder {
    private List<String> lineCommentTypes;
    private List<String> reservedTopLevelWords;
    private List<String> reservedNewlineWords;
    private List<String> reservedTopLevelWordsNoIndent;
    private List<String> reservedWords;
    private List<String> dataTypes;
    private List<String> specialWordChars;
    private List<String> stringTypes;
    private List<String> openParens;
    private List<String> closeParens;
    private List<String> indexedPlaceholderTypes;
    private List<String> namedPlaceholderTypes;
    private List<String> operators;

    /** Creates an empty builder. */
    DialectConfigBuilder() {}

    /**
     * Sets line comment marker types.
     *
     * @param lineCommentTypes line comment marker types
     * @return this builder
     */
    public DialectConfigBuilder lineCommentTypes(List<String> lineCommentTypes) {
      this.lineCommentTypes = lineCommentTypes;
      return this;
    }

    /**
     * Sets top-level reserved words.
     *
     * @param reservedTopLevelWords top-level reserved words
     * @return this builder
     */
    public DialectConfigBuilder reservedTopLevelWords(List<String> reservedTopLevelWords) {
      this.reservedTopLevelWords = reservedTopLevelWords;
      return this;
    }

    /**
     * Sets newline reserved words.
     *
     * @param reservedNewlineWords reserved words that force a newline
     * @return this builder
     */
    public DialectConfigBuilder reservedNewlineWords(List<String> reservedNewlineWords) {
      this.reservedNewlineWords = reservedNewlineWords;
      return this;
    }

    /**
     * Sets non-indenting top-level reserved words.
     *
     * @param reservedTopLevelWordsNoIndent non-indenting top-level reserved words
     * @return this builder
     */
    public DialectConfigBuilder reservedTopLevelWordsNoIndent(
        List<String> reservedTopLevelWordsNoIndent) {
      this.reservedTopLevelWordsNoIndent = reservedTopLevelWordsNoIndent;
      return this;
    }

    /**
     * Sets reserved words.
     *
     * @param reservedWords reserved words
     * @return this builder
     */
    public DialectConfigBuilder reservedWords(List<String> reservedWords) {
      this.reservedWords = reservedWords;
      return this;
    }

    /**
     * Sets data types.
     *
     * @param dataTypes data types
     * @return this builder
     */
    public DialectConfigBuilder dataTypes(List<String> dataTypes) {
      this.dataTypes = dataTypes;
      return this;
    }

    /**
     * Sets special word characters.
     *
     * @param specialWordChars special word characters
     * @return this builder
     */
    public DialectConfigBuilder specialWordChars(List<String> specialWordChars) {
      this.specialWordChars = specialWordChars;
      return this;
    }

    /**
     * Sets string literal types.
     *
     * @param stringTypes string literal types
     * @return this builder
     */
    public DialectConfigBuilder stringTypes(List<String> stringTypes) {
      this.stringTypes = stringTypes;
      return this;
    }

    /**
     * Sets opening parentheses.
     *
     * @param openParens opening parentheses
     * @return this builder
     */
    public DialectConfigBuilder openParens(List<String> openParens) {
      this.openParens = openParens;
      return this;
    }

    /**
     * Sets closing parentheses.
     *
     * @param closeParens closing parentheses
     * @return this builder
     */
    public DialectConfigBuilder closeParens(List<String> closeParens) {
      this.closeParens = closeParens;
      return this;
    }

    /**
     * Sets indexed placeholder types.
     *
     * @param indexedPlaceholderTypes indexed placeholder types
     * @return this builder
     */
    public DialectConfigBuilder indexedPlaceholderTypes(List<String> indexedPlaceholderTypes) {
      this.indexedPlaceholderTypes = indexedPlaceholderTypes;
      return this;
    }

    /**
     * Sets named placeholder types.
     *
     * @param namedPlaceholderTypes named placeholder types
     * @return this builder
     */
    public DialectConfigBuilder namedPlaceholderTypes(List<String> namedPlaceholderTypes) {
      this.namedPlaceholderTypes = namedPlaceholderTypes;
      return this;
    }

    /**
     * Sets operators.
     *
     * @param operators operators
     * @return this builder
     */
    public DialectConfigBuilder operators(List<String> operators) {
      this.operators = operators;
      return this;
    }

    /**
     * Builds a dialect configuration.
     *
     * @return dialect configuration
     */
    public DialectConfig build() {
      return new DialectConfig(
          lineCommentTypes,
          reservedTopLevelWords,
          reservedNewlineWords,
          reservedTopLevelWordsNoIndent,
          reservedWords,
          dataTypes,
          specialWordChars,
          stringTypes,
          openParens,
          closeParens,
          indexedPlaceholderTypes,
          namedPlaceholderTypes,
          operators);
    }
  }
}
