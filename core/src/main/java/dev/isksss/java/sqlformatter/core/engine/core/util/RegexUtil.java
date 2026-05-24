package dev.isksss.java.sqlformatter.core.engine.core.util;

import dev.isksss.java.sqlformatter.core.engine.languages.StringLiteral;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Utility methods for building tokenizer regular expressions. */
public class RegexUtil {

  /** Creates a regex utility instance. */
  public RegexUtil() {}

  private static final String ESCAPE_REGEX =
      Stream.of("^", "$", "\\", ".", "*", "+", "*", "?", "(", ")", "[", "]", "{", "}", "|")
          .map(spChr -> "(\\" + spChr + ")")
          .collect(Collectors.joining("|"));
  public static final Pattern ESCAPE_REGEX_PATTERN = Pattern.compile(ESCAPE_REGEX);

  /**
   * Escapes a string for use inside a regular expression.
   *
   * @param s string to escape
   * @return escaped string
   */
  public static String escapeRegExp(String s) {
    return ESCAPE_REGEX_PATTERN.matcher(s).replaceAll("\\\\$0");
  }

  /**
   * Creates a regular expression for SQL operators.
   *
   * @param multiLetterOperators multi-character operators
   * @return operator regular expression
   */
  public static String createOperatorRegex(JSLikeList<String> multiLetterOperators) {
    return String.format(
        "^(%s|.)",
        Util.sortByLengthDesc(multiLetterOperators).map(RegexUtil::escapeRegExp).join("|"));
  }

  /**
   * Creates a regular expression for line comments.
   *
   * @param lineCommentTypes line comment prefixes
   * @return line comment regular expression
   */
  public static String createLineCommentRegex(JSLikeList<String> lineCommentTypes) {
    return String.format(
        "^((?:%s).*?)(?:\r\n|\r|\n|$)", lineCommentTypes.map(RegexUtil::escapeRegExp).join("|"));
  }

  /**
   * Creates a regular expression for reserved words.
   *
   * @param reservedWords reserved words
   * @return reserved word regular expression
   */
  public static String createReservedWordRegex(JSLikeList<String> reservedWords) {
    if (reservedWords.isEmpty()) {
      return "^\b$";
    }
    String reservedWordsPattern =
        Util.sortByLengthDesc(reservedWords).join("|").replaceAll(" ", "\\\\s+");
    return "(?i)" + "^(" + reservedWordsPattern + ")\\b";
  }

  /**
   * Creates a regular expression for plain words.
   *
   * @param specialChars additional word characters
   * @return word regular expression
   */
  public static String createWordRegex(JSLikeList<String> specialChars) {
    return "^([\\p{IsAlphabetic}\\p{Mc}\\p{Me}\\p{Mn}\\p{Nd}\\p{Pc}\\p{IsJoin_Control}"
        + specialChars.join("")
        + "]+)";
  }

  /**
   * Creates a regular expression for string literals.
   *
   * @param stringTypes supported string literal types
   * @return string literal regular expression
   */
  public static String createStringRegex(JSLikeList<String> stringTypes) {
    return "^(" + createStringPattern(stringTypes) + ")";
  }

  // This enables the following string patterns:
  // 1. backtick quoted string using `` to escape
  // 2. square bracket quoted string (SQL Server) using ]] to escape
  // 3. double quoted string using "" or \" to escape
  // 4. single quoted string using '' or \' to escape
  // 5. national character quoted string using N'' or N\' to escape
  /**
   * Creates the inner pattern for string literals.
   *
   * @param stringTypes supported string literal types
   * @return string literal pattern
   */
  public static String createStringPattern(JSLikeList<String> stringTypes) {
    return stringTypes.map(StringLiteral::get).join("|");
  }

  /**
   * Creates a regular expression for parentheses tokens.
   *
   * @param parens parentheses tokens
   * @return parenthesis regular expression
   */
  public static String createParenRegex(JSLikeList<String> parens) {
    return "(?i)^(" + parens.map(RegexUtil::escapeParen).join("|") + ")";
  }

  /**
   * Escapes a parenthesis token.
   *
   * @param paren parenthesis token
   * @return escaped parenthesis pattern
   */
  public static String escapeParen(String paren) {
    if (paren.length() == 1) {
      // A single punctuation character
      return RegexUtil.escapeRegExp(paren);
    } else {
      // longer word
      return "\\b" + paren + "\\b";
    }
  }

  /**
   * Creates a placeholder regular expression pattern.
   *
   * @param types placeholder marker types
   * @param pattern placeholder key pattern
   * @return placeholder pattern, or {@code null} when no types are configured
   */
  public static Pattern createPlaceholderRegexPattern(JSLikeList<String> types, String pattern) {
    if (types.isEmpty()) {
      return null;
    }
    String typesRegex = types.map(RegexUtil::escapeRegExp).join("|");

    return Pattern.compile(String.format("^((?:%s)(?:%s))", typesRegex, pattern));
  }
}
