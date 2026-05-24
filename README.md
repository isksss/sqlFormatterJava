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
    rustCorePath = 'target/debug/sql-formatter-core'
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

To route formatting through the experimental Rust core, pass the executable
path explicitly.

```bash
cargo build -p sql-formatter-core
sql-formatter-java --rust-core target/debug/sql-formatter-core query.sql
```

## Development

```bash
./gradlew check
cargo test
cd vscode-extension && npm ci && npm run compile:with-wasm
```

Native executables require a GraalVM JDK with Native Image support.

`./gradlew check` runs the Java/Gradle plugin tests and the Rust core test
matrix. The Rust matrix covers supported dialect names and formatter option
combinations for indentation, casing, logical operator placement, expression
width, query spacing, dense operators, and semicolon placement.

To verify every existing Java test through the Rust backend, build the Rust
executable and set `SQL_FORMATTER_RUST_CORE`.

```bash
cargo build -p sql-formatter-core
SQL_FORMATTER_RUST_CORE="$PWD/target/debug/sql-formatter-core" ./gradlew test --rerun-tasks
```

CLI and Gradle plugin tests that require an explicit Rust executable use
`SQL_FORMATTER_TEST_RUST_CORE`.

```bash
SQL_FORMATTER_TEST_RUST_CORE="$PWD/target/debug/sql-formatter-core" \
  ./gradlew :cli:test :gradle-plugin:test --tests '*RustCoreBackend*' --rerun-tasks
```

## CI and release

GitHub Actions runs the following checks on pull requests and pushes to `main`.

- `./gradlew check --rerun-tasks`
- `cargo build -p sql-formatter-core`
- Rust backend integration tests for CLI and Gradle plugin
- full Java test suite with `SQL_FORMATTER_RUST_CORE`
- VS Code extension compile with bundled Wasm

Pushing a tag named `v*` runs the Gradle Plugin Portal publish workflow. Set
these repository secrets before publishing.

- `GRADLE_PUBLISH_KEY`
- `GRADLE_PUBLISH_SECRET`

The published Gradle plugin version is derived from the tag by removing the
leading `v`.

## Portable Rust/Wasm core

The repository now contains an experimental Rust formatter core in
`rust-core`. It is not yet a full replacement for the Java core; it establishes
the shared API boundary and Wasm build path for VS Code and future Java
integration.

```bash
cargo test
cargo build -p sql-formatter-core --target wasm32-unknown-unknown --release
```

## VS Code extension

The experimental VS Code extension lives in `vscode-extension`. It registers a
SQL document/range formatter and loads `dist/wasm/sql_formatter_core.wasm` when
present. In development, it falls back to a TypeScript shim if the Wasm file is
not built yet.

```bash
cd vscode-extension
npm install
npm run compile:with-wasm
```
