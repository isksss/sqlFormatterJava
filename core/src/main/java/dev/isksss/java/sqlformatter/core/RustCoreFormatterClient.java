package dev.isksss.java.sqlformatter.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

final class RustCoreFormatterClient {
    private static final String PATH_PROPERTY = "sqlFormatter.rustCorePath";
    private static final String PATH_ENV = "SQL_FORMATTER_RUST_CORE";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    Optional<String> format(String sql, FormatterConfig config) {
        Optional<Path> executable = executablePath();
        if (executable.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(run(executable.get(), sql, config));
        } catch (IOException | ExecutionException exception) {
            throw new SqlFormattingException("Failed to execute Rust formatter core.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SqlFormattingException("Interrupted while executing Rust formatter core.", exception);
        }
    }

    private Optional<Path> executablePath() {
        String configured = System.getProperty(PATH_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(PATH_ENV);
        }
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }
        Path path = Path.of(configured);
        if (!Files.isRegularFile(path) || !Files.isExecutable(path)) {
            throw new SqlFormattingException(
                    "Configured Rust formatter core is not executable: " + path,
                    new IllegalArgumentException(path.toString()));
        }
        return Optional.of(path);
    }

    private String run(Path executable, String sql, FormatterConfig config)
            throws IOException, InterruptedException, ExecutionException {
        Process process = new ProcessBuilder(arguments(executable, config)).start();
        CompletableFuture<String> stdout = readAsync(process.getInputStream());
        CompletableFuture<String> stderr = readAsync(process.getErrorStream());
        process.getOutputStream().write(sql.getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();
        boolean completed = process.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new SqlFormattingException("Rust formatter core timed out.", new IllegalStateException());
        }
        String stdoutText = stdout.get();
        String stderrText = stderr.get();
        if (process.exitValue() != 0) {
            throw new SqlFormattingException(
                    stderrText.isBlank() ? "Rust formatter core failed." : stderrText.trim(),
                    new IllegalStateException("exit code " + process.exitValue()));
        }
        return stdoutText;
    }

    private CompletableFuture<String> readAsync(java.io.InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private List<String> arguments(Path executable, FormatterConfig config) {
        FormatterConfig effective = config.mergeOver(FormatterConfig.defaults());
        List<String> args = new ArrayList<>();
        args.add(executable.toString());
        add(args, "--dialect", effective.dialect());
        add(args, "--tab-width", effective.tabWidth());
        add(args, "--use-tabs", effective.useTabs());
        add(args, "--keyword-case", effective.keywordCase());
        add(args, "--data-type-case", effective.dataTypeCase());
        add(args, "--function-case", effective.functionCase());
        add(args, "--identifier-case", effective.identifierCase());
        add(args, "--logical-operator-newline", effective.logicalOperatorNewline());
        add(args, "--expression-width", effective.expressionWidth());
        add(args, "--lines-between-queries", effective.linesBetweenQueries());
        add(args, "--dense-operators", effective.denseOperators());
        add(args, "--newline-before-semicolon", effective.newlineBeforeSemicolon());
        add(args, "--error-policy", errorPolicy(effective.errorPolicy()));
        return args;
    }

    private void add(List<String> args, String option, Object value) {
        if (value == null) {
            return;
        }
        args.add(option);
        args.add(value.toString());
    }

    private String errorPolicy(ErrorPolicy value) {
        return value == ErrorPolicy.THROW ? "throw" : "keep-input";
    }
}
