package dev.isksss.java.sqlformatter.cli;

import dev.isksss.java.sqlformatter.core.FormatterConfig;
import dev.isksss.java.sqlformatter.core.SqlFormatterService;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class SqlFormatterCli {
    private final JsonConfigLoader configLoader;
    private final SqlFormatterService formatter;

    SqlFormatterCli(JsonConfigLoader configLoader, SqlFormatterService formatter) {
        this.configLoader = configLoader;
        this.formatter = formatter;
    }

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
            String sql = arguments.sqlFile() == null
                    ? new String(stdin.readAllBytes(), StandardCharsets.UTF_8)
                    : Files.readString(arguments.sqlFile(), StandardCharsets.UTF_8);
            stdout.print(formatter.format(sql, config));
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

    private String usage() {
        return """
                Usage: sql-formatter-java [OPTIONS] [SQL_FILE]
                  --config FILE                  JSON formatter config
                  --dialect NAME                 SQL dialect
                  --indent TEXT                  Indentation string
                  --uppercase BOOLEAN            Uppercase keywords
                  --lines-between-queries COUNT  Blank lines between queries
                  --max-column-length COUNT      Max inline column length
                Without SQL_FILE, SQL is read from stdin.
                """;
    }
}
