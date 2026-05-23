# sqlFormatterJava

Java SQL formatter core, Gradle plugin, and native CLI. The formatter engine is
implemented in this repository and supports `postgresql`, `mysql`, and `sqlite`.

## Gradle plugin

Apply the plugin and configure SQL file selection in `build.gradle`. Formatter
options can be omitted; the default is standard SQL, two-space indentation, and
preserved casing.

```groovy
plugins {
    id 'dev.isksss.java.sql-formatter'
}

sqlFormatter {
    files {
        from 'src/main/resources/sql'
        include '**/*.sql'
        exclude '**/generated/**'
    }

    dialect = 'postgresql'
    tabWidth = 4
    useTabs = false
    keywordCase = 'upper'
    dataTypeCase = 'upper'
    functionCase = 'preserve'
    identifierCase = 'preserve'
    logicalOperatorNewline = 'before'
    expressionWidth = 80
    linesBetweenQueries = 1
    denseOperators = false
    newlineBeforeSemicolon = false
    errorPolicy = dev.isksss.java.sqlformatter.core.ErrorPolicy.KEEP_INPUT
    charset = 'UTF-8'
}
```

`files.from` is required. Includes default to `**/*.sql`; an explicit include
can add more patterns. Formatting options follow the same shape as the CLI JSON
config. `errorPolicy` controls whether formatting errors keep the SQL input or
throw. `charset` controls Gradle SQL file reads and writes.

```bash
./gradlew sqlFormat
./gradlew sqlCheck
```

`sqlFormat` writes selected SQL files in place. `sqlCheck` does not modify files
and fails when any selected SQL file is not formatted.

## Native CLI

The CLI reads an optional SQL file or stdin and writes formatted SQL to stdout.

```bash
./gradlew :cli:nativeCompile
./cli/build/native/nativeCompile/sql-formatter-java --config sql-formatter.json query.sql
cat query.sql | ./cli/build/native/nativeCompile/sql-formatter-java --dialect postgresql
```

Windows builds produce `sql-formatter-java.exe`.

```json
{
  "dialect": "postgresql",
  "tabWidth": 4,
  "useTabs": false,
  "keywordCase": "upper",
  "dataTypeCase": "upper",
  "functionCase": "preserve",
  "identifierCase": "preserve",
  "logicalOperatorNewline": "before",
  "expressionWidth": 80,
  "linesBetweenQueries": 1,
  "denseOperators": false,
  "newlineBeforeSemicolon": false,
  "errorPolicy": "keep-input",
  "charset": "UTF-8"
}
```

Command line options override JSON config fields.

```bash
sql-formatter-java --help
sql-formatter-java --config sql-formatter.json --keyword-case upper query.sql
```

## Development

```bash
./gradlew test
```

Native executables require a GraalVM JDK with Native Image support.
