package dev.isksss.java.sqlformatter.core.engine.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Token produced by the SQL tokenizer. */
public class Token {
  public final TokenTypes type;
  public final String value;
  public final String regex;
  public final String whitespaceBefore;
  public final String key;

  /**
   * Creates a token.
   *
   * @param type token type
   * @param value token text
   * @param regex matching regular expression
   * @param whitespaceBefore whitespace before the token in the source
   * @param key placeholder key
   */
  public Token(TokenTypes type, String value, String regex, String whitespaceBefore, String key) {
    this.type = type;
    this.value = value;
    this.regex = regex;
    this.whitespaceBefore = whitespaceBefore;
    this.key = key;
  }

  /**
   * Creates a token without a placeholder key.
   *
   * @param type token type
   * @param value token text
   * @param regex matching regular expression
   * @param whitespaceBefore whitespace before the token in the source
   */
  public Token(TokenTypes type, String value, String regex, String whitespaceBefore) {
    this(type, value, regex, whitespaceBefore, null);
  }

  /**
   * Creates a token without source whitespace metadata.
   *
   * @param type token type
   * @param value token text
   * @param regex matching regular expression
   */
  public Token(TokenTypes type, String value, String regex) {
    this(type, value, regex, null);
  }

  /**
   * Creates a token with only type and value.
   *
   * @param type token type
   * @param value token text
   */
  public Token(TokenTypes type, String value) {
    this(type, value, null, null);
  }

  /**
   * Returns a copy with source whitespace metadata.
   *
   * @param whitespaceBefore whitespace before the token
   * @return copied token
   */
  public Token withWhitespaceBefore(String whitespaceBefore) {
    return new Token(this.type, this.value, this.regex, whitespaceBefore, this.key);
  }

  /**
   * Returns a copy with a placeholder key.
   *
   * @param key placeholder key
   * @return copied token
   */
  public Token withKey(String key) {
    return new Token(this.type, this.value, this.regex, this.whitespaceBefore, key);
  }

  @Override
  public String toString() {
    return "type: " + type + ", value: [" + value + "], regex: /" + regex + "/" + ", key: " + key;
  }

  private static final Pattern AND =
      Pattern.compile("^AND$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern OR =
      Pattern.compile("^OR$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern XOR =
      Pattern.compile("^XOR$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern BETWEEN =
      Pattern.compile("^BETWEEN$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern LIMIT =
      Pattern.compile("^LIMIT$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SET =
      Pattern.compile("^SET$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern BY =
      Pattern.compile("^BY$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern WINDOW =
      Pattern.compile("^WINDOW$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern END =
      Pattern.compile("^END$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static Function<Token, Boolean> isToken(TokenTypes type, Pattern pattern) {
    return token -> token.type == type && pattern.matcher(token.value).matches();
  }

  /**
   * Returns whether the token is {@code AND}.
   *
   * @param token token
   * @return whether the token is {@code AND}
   */
  public static boolean isAnd(Token token) {
    return isAnd(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code AND}.
   *
   * @param token optional token
   * @return whether the token is {@code AND}
   */
  public static boolean isAnd(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_NEWLINE, AND)).orElse(false);
  }

  /**
   * Returns whether the token is {@code OR}.
   *
   * @param token token
   * @return whether the token is {@code OR}
   */
  public static boolean isOr(Token token) {
    return isOr(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code OR}.
   *
   * @param token optional token
   * @return whether the token is {@code OR}
   */
  public static boolean isOr(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_NEWLINE, OR)).orElse(false);
  }

  /**
   * Returns whether the token is {@code XOR}.
   *
   * @param token token
   * @return whether the token is {@code XOR}
   */
  public static boolean isXor(Token token) {
    return isXor(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code XOR}.
   *
   * @param token optional token
   * @return whether the token is {@code XOR}
   */
  public static boolean isXor(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_NEWLINE, XOR)).orElse(false);
  }

  /**
   * Returns whether the token is {@code BETWEEN}.
   *
   * @param token token
   * @return whether the token is {@code BETWEEN}
   */
  public static boolean isBetween(Token token) {
    return isBetween(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code BETWEEN}.
   *
   * @param token optional token
   * @return whether the token is {@code BETWEEN}
   */
  public static boolean isBetween(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED, BETWEEN)).orElse(false);
  }

  /**
   * Returns whether the token is {@code LIMIT}.
   *
   * @param token token
   * @return whether the token is {@code LIMIT}
   */
  public static boolean isLimit(Token token) {
    return isLimit(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code LIMIT}.
   *
   * @param token optional token
   * @return whether the token is {@code LIMIT}
   */
  public static boolean isLimit(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_TOP_LEVEL, LIMIT)).orElse(false);
  }

  /**
   * Returns whether the token is {@code SET}.
   *
   * @param token token
   * @return whether the token is {@code SET}
   */
  public static boolean isSet(Token token) {
    return isSet(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code SET}.
   *
   * @param token optional token
   * @return whether the token is {@code SET}
   */
  public static boolean isSet(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_TOP_LEVEL, SET)).orElse(false);
  }

  /**
   * Returns whether the token is {@code BY}.
   *
   * @param token token
   * @return whether the token is {@code BY}
   */
  public static boolean isBy(Token token) {
    return isBy(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code BY}.
   *
   * @param token optional token
   * @return whether the token is {@code BY}
   */
  public static boolean isBy(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED, BY)).orElse(false);
  }

  /**
   * Returns whether the token is {@code WINDOW}.
   *
   * @param token token
   * @return whether the token is {@code WINDOW}
   */
  public static boolean isWindow(Token token) {
    return isWindow(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code WINDOW}.
   *
   * @param token optional token
   * @return whether the token is {@code WINDOW}
   */
  public static boolean isWindow(Optional<Token> token) {
    return token.map(isToken(TokenTypes.RESERVED_TOP_LEVEL, WINDOW)).orElse(false);
  }

  /**
   * Returns whether the token is {@code END}.
   *
   * @param token token
   * @return whether the token is {@code END}
   */
  public static boolean isEnd(Token token) {
    return isEnd(Optional.ofNullable(token));
  }

  /**
   * Returns whether the token is {@code END}.
   *
   * @param token optional token
   * @return whether the token is {@code END}
   */
  public static boolean isEnd(Optional<Token> token) {
    return token.map(isToken(TokenTypes.CLOSE_PAREN, END)).orElse(false);
  }
}
