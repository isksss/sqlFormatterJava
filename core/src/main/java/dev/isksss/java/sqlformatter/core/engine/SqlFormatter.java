package dev.isksss.java.sqlformatter.core.engine;

import dev.isksss.java.sqlformatter.core.engine.core.AbstractFormatter;
import dev.isksss.java.sqlformatter.core.engine.core.DialectConfig;
import dev.isksss.java.sqlformatter.core.engine.core.FormatConfig;
import dev.isksss.java.sqlformatter.core.engine.languages.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Entry point for formatting SQL with the embedded formatter engine. */
public class SqlFormatter {
  /** Creates a SQL formatter entry point instance. */
  public SqlFormatter() {}

  /**
   * FormatConfig whitespaces in a query to make it easier to read.
   *
   * @param query sql
   * @param cfg FormatConfig
   * @return Formatted query
   */
  public static String format(String query, FormatConfig cfg) {
    return standard().format(query, cfg);
  }

  /**
   * Formats a Standard SQL query with indexed placeholder parameters.
   *
   * @param query SQL query
   * @param params indexed placeholder parameters
   * @return formatted query
   */
  public static String format(String query, List<?> params) {
    return standard().format(query, params);
  }

  /**
   * Formats a Standard SQL query with named placeholder parameters.
   *
   * @param query SQL query
   * @param params named placeholder parameters
   * @return formatted query
   */
  public static String format(String query, Map<String, ?> params) {
    return standard().format(query, params);
  }

  /**
   * Formats a Standard SQL query with default settings.
   *
   * @param query SQL query
   * @return formatted query
   */
  public static String format(String query) {
    return standard().format(query);
  }

  /**
   * Creates a formatter by extending the Standard SQL dialect configuration.
   *
   * @param operator dialect configuration customizer
   * @return formatter
   */
  public static Formatter extend(UnaryOperator<DialectConfig> operator) {
    return standard().extend(operator);
  }

  /**
   * Creates a Standard SQL formatter.
   *
   * @return formatter
   */
  public static Formatter standard() {
    return of(Dialect.StandardSql);
  }

  /**
   * Creates a formatter for a dialect name or alias.
   *
   * @param name dialect name or alias
   * @return formatter
   */
  public static Formatter of(String name) {
    return Dialect.nameOf(name)
        .map(Formatter::new)
        .orElseThrow(() -> new RuntimeException("Unsupported SQL dialect: " + name));
  }

  /**
   * Creates a formatter for a dialect.
   *
   * @param dialect dialect
   * @return formatter
   */
  public static Formatter of(Dialect dialect) {
    return new Formatter(dialect);
  }

  /** Formatter bound to a specific dialect factory. */
  public static class Formatter {

    private final Function<FormatConfig, AbstractFormatter> underlying;

    private Formatter(Function<FormatConfig, AbstractFormatter> underlying) {
      this.underlying = underlying;
    }

    private Formatter(Dialect dialect) {
      this(dialect.func);
    }

    /**
     * Formats a query with an explicit format configuration.
     *
     * @param query SQL query
     * @param cfg format configuration
     * @return formatted query
     */
    public String format(String query, FormatConfig cfg) {
      return this.underlying.apply(cfg).format(query);
    }

    /**
     * Formats a query with indexed placeholder parameters.
     *
     * @param query SQL query
     * @param params indexed placeholder parameters
     * @return formatted query
     */
    public String format(String query, List<?> params) {
      return format(query, FormatConfig.builder().params(params).build());
    }

    /**
     * Formats a query with named placeholder parameters.
     *
     * @param query SQL query
     * @param params named placeholder parameters
     * @return formatted query
     */
    public String format(String query, Map<String, ?> params) {
      return format(query, FormatConfig.builder().params(params).build());
    }

    /**
     * Formats a query with default settings.
     *
     * @param query SQL query
     * @return formatted query
     */
    public String format(String query) {
      return format(query, FormatConfig.builder().build());
    }

    /**
     * Creates a formatter with a customized dialect configuration.
     *
     * @param operator dialect configuration customizer
     * @return formatter
     */
    public Formatter extend(UnaryOperator<DialectConfig> operator) {
      return new Formatter(
          cfg ->
              new AbstractFormatter(cfg) {
                @Override
                public DialectConfig dialectConfig() {
                  return operator.apply(Formatter.this.underlying.apply(cfg).dialectConfig());
                }
              });
    }
  }
}
