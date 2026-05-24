package dev.isksss.java.sqlformatter.core.engine.languages;

import dev.isksss.java.sqlformatter.core.engine.core.AbstractFormatter;
import dev.isksss.java.sqlformatter.core.engine.core.FormatConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Supported SQL dialects. */
public enum Dialect {
  /** DB2 dialect. */
  Db2(Db2Formatter::new),
  /** MariaDB dialect. */
  MariaDb(MariaDbFormatter::new),
  /** MySQL dialect. */
  MySql(MySqlFormatter::new),
  /** Couchbase N1QL dialect. */
  N1ql(N1qlFormatter::new),
  /** Oracle PL/SQL dialect. */
  PlSql(PlSqlFormatter::new, "pl/sql"),
  /** PostgreSQL dialect. */
  PostgreSql(PostgreSqlFormatter::new),
  /** Amazon Redshift dialect. */
  Redshift(RedshiftFormatter::new),
  /** SQLite dialect. */
  SQLite(SqliteFormatter::new, "sqlite"),
  /** Spark SQL dialect. */
  SparkSql(SparkSqlFormatter::new, "spark"),
  /** Standard SQL dialect. */
  StandardSql(StandardSqlFormatter::new, "sql"),
  /** Microsoft T-SQL dialect. */
  TSql(TSqlFormatter::new),
  ;

  /** Formatter factory for this dialect. */
  public final Function<FormatConfig, AbstractFormatter> func;

  /** Additional names accepted for this dialect. */
  public final List<String> aliases;

  Dialect(Function<FormatConfig, AbstractFormatter> func, String... aliases) {
    this.func = func;
    this.aliases = Arrays.asList(aliases);
  }

  private boolean matches(String name) {
    return this.name().equalsIgnoreCase(name)
        || this.aliases.stream().anyMatch(s -> s.equalsIgnoreCase(name));
  }

  /**
   * Resolves a dialect by name or alias.
   *
   * @param name dialect name or alias
   * @return matching dialect, when supported
   */
  public static Optional<Dialect> nameOf(String name) {
    return Arrays.stream(values()).filter(d -> d.matches(name)).findFirst();
  }
}
