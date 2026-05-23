package dev.isksss.java.sqlformatter.core.engine.languages;

import java.util.Arrays;
import java.util.List;

final class SqlDialectWords {
  static final List<String> COMMON_DATA_TYPES =
      Arrays.asList(
          "BIGINT",
          "BINARY",
          "BLOB",
          "BOOLEAN",
          "CHAR",
          "CHARACTER",
          "DATE",
          "DEC",
          "DECIMAL",
          "DOUBLE",
          "FLOAT",
          "INT",
          "INTEGER",
          "NUMERIC",
          "REAL",
          "SMALLINT",
          "TEXT",
          "TIME",
          "TIMESTAMP",
          "VARCHAR");

  static final List<String> POSTGRESQL_DATA_TYPES =
      Arrays.asList(
          "BIGSERIAL",
          "BYTEA",
          "CIDR",
          "INET",
          "JSON",
          "JSONB",
          "MONEY",
          "SERIAL",
          "TIMESTAMPTZ",
          "UUID",
          "XML");

  static final List<String> MYSQL_DATA_TYPES =
      Arrays.asList(
          "DATETIME",
          "ENUM",
          "JSON",
          "LONGBLOB",
          "LONGTEXT",
          "MEDIUMBLOB",
          "MEDIUMINT",
          "MEDIUMTEXT",
          "SET",
          "TINYBLOB",
          "TINYINT",
          "TINYTEXT",
          "UNSIGNED",
          "YEAR");

  static final List<String> SQLITE_DATA_TYPES =
      Arrays.asList("ANY", "NONE");

  private SqlDialectWords() {}
}
