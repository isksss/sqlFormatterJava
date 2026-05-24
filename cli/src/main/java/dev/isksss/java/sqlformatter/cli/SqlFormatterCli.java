package dev.isksss.java.sqlformatter.cli;

import dev.isksss.java.sqlformatter.core.FormatterConfig;
import dev.isksss.java.sqlformatter.core.SqlFormatterService;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * 標準入力またはSQLファイルを読み取り、整形結果を標準出力へ書き出すCLIエントリポイント。
 */
public final class SqlFormatterCli {
    private final JsonConfigLoader configLoader;
    private final SqlFormatterService formatter;

    SqlFormatterCli(JsonConfigLoader configLoader, SqlFormatterService formatter) {
        this.configLoader = configLoader;
        this.formatter = formatter;
    }

    /**
     * CLIを実行する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        int exitCode = new SqlFormatterCli(new JsonConfigLoader(), new SqlFormatterService())
                .run(args, System.in, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    int run(String[] args, InputStream stdin, PrintStream stdout, PrintStream stderr) {
        try {
            CliArguments arguments = CliArguments.parse(args);
            FormatterConfig config = loadConfig(arguments).mergeOver(FormatterConfig.defaults());
            Charset charset = charset(config.charset());
            String sql = arguments.sqlFile() == null
                    ? new String(stdin.readAllBytes(), charset)
                    : Files.readString(arguments.sqlFile(), charset);
            stdout.print(format(arguments, sql, config));
            return 0;
        } catch (HelpRequestedException exception) {
            stdout.print(usage());
            return 0;
        } catch (IllegalArgumentException | IOException exception) {
            stderr.println("sql-formatter-java: " + exception.getMessage());
            stderr.print(usage());
            return 2;
        } catch (RuntimeException exception) {
            stderr.println("sql-formatter-java: " + exception.getMessage());
            return 1;
        }
    }

    private FormatterConfig loadConfig(CliArguments arguments) throws IOException {
        FormatterConfig fileConfig = arguments.configFile() == null
                ? FormatterConfig.defaults()
                : configLoader.load(arguments.configFile());
        return arguments.overrides().mergeOver(fileConfig);
    }

    private Charset charset(String name) {
        try {
            return Charset.forName(name);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unsupported charset: " + name, exception);
        }
    }

    private String usage() {
        return """
                Usage: sql-formatter-java [OPTIONS] [SQL_FILE]
                  --config FILE                  JSON formatter config
                  --rust-core FILE               Rust formatter core executable
                  --dialect NAME                 SQL dialect
                  --tab-width COUNT              Spaces per indentation level
                  --use-tabs BOOLEAN             Use tab characters for indentation
                  --keyword-case CASE            preserve, upper, or lower
                  --data-type-case CASE          preserve, upper, or lower
                  --function-case CASE           preserve, upper, or lower
                  --identifier-case CASE         preserve, upper, or lower
                  --logical-operator-newline POS before or after
                  --expression-width COUNT       Max inline expression length
                  --lines-between-queries COUNT  Blank lines between queries
                  --dense-operators BOOLEAN      Remove spaces around operators
                  --newline-before-semicolon BOOLEAN
                  --error-policy POLICY          keep-input or throw
                  --charset NAME                 SQL input charset
                Without SQL_FILE, SQL is read from stdin.
                """;
    }

    private String format(CliArguments arguments, String sql, FormatterConfig config) {
        if (arguments.rustCorePath() == null) {
            return formatter.format(sql, config);
        }
        String previous = System.getProperty("sqlFormatter.rustCorePath");
        try {
            System.setProperty("sqlFormatter.rustCorePath", arguments.rustCorePath().toString());
            return formatter.format(sql, config);
        } finally {
            if (previous == null) {
                System.clearProperty("sqlFormatter.rustCorePath");
            } else {
                System.setProperty("sqlFormatter.rustCorePath", previous);
            }
        }
    }
}
