package dev.isksss.java.sqlformatter.core.engine.languages;

import dev.isksss.java.sqlformatter.core.engine.core.DialectConfig;

/** Provides dialect-specific tokenizer and formatting configuration. */
public interface DialectConfigurator {
  /**
   * Returns this formatter's dialect configuration.
   *
   * @return dialect configuration
   */
  DialectConfig dialectConfig();
}
