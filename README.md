# sqlFormatterJava

Java SQL formatter core, Gradle plugin, and native CLI. The formatter engine is
[`com.github.vertical-blank:sql-formatter`](https://github.com/vertical-blank/sql-formatter).

## Gradle plugin

Apply the plugin and configure both the SQL files and formatter options in
`build.gradle`.

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
    indent = '    '
    uppercase = true
    linesBetweenQueries = 1
    maxColumnLength = 100
}
```

`files.from` is required. Includes default to `**/*.sql`; an explicit include
can add more patterns. Supported options are intentionally limited to the
formatter engine API: `dialect`, `indent`, `uppercase`, `linesBetweenQueries`,
and `maxColumnLength`.

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
  "indent": "    ",
  "uppercase": true,
  "linesBetweenQueries": 1,
  "maxColumnLength": 100
}
```

Command line options override JSON config fields.

```bash
sql-formatter-java --help
sql-formatter-java --config sql-formatter.json --uppercase true query.sql
```

## Development

```bash
./gradlew test
```

Native executables require a GraalVM JDK with Native Image support.
