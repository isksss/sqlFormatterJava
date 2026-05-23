package dev.isksss.java.sqlformatter.core.engine.languages;

import dev.isksss.java.sqlformatter.core.engine.core.DialectConfig;
import dev.isksss.java.sqlformatter.core.engine.core.FormatConfig;
import dev.isksss.java.sqlformatter.core.engine.core.util.Util;
import java.util.Arrays;
import java.util.Collections;

public class SqliteFormatter extends StandardSqlFormatter {
  public SqliteFormatter(FormatConfig cfg) {
    super(cfg);
  }

  @Override
  public DialectConfig dialectConfig() {
    return super.dialectConfig()
        .plusReservedTopLevelWords("INSERT OR REPLACE INTO", "INSERT OR IGNORE INTO")
        .plusReservedWords("CONFLICT", "GLOB", "INDEXED", "REGEXP", "ROWID", "WITHOUT")
        .plusDataTypes(Util.concat(SqlDialectWords.COMMON_DATA_TYPES, SqlDialectWords.SQLITE_DATA_TYPES))
        .withNamedPlaceholderTypes(Collections.singletonList(":"))
        .withLineCommentTypes(Arrays.asList("--"))
        .plusOperators("->", "->>", "||");
  }
}
