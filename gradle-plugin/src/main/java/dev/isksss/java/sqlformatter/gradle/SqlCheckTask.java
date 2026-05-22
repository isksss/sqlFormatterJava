package dev.isksss.java.sqlformatter.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Checks current SQL source file contents.")
public abstract class SqlCheckTask extends AbstractSqlFormatterTask {
    @TaskAction
    public void check() {
        List<File> unformatted = new ArrayList<>();
        for (File file : selectedSqlFiles()) {
            try {
                String input = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                if (!input.equals(getFormatter().format(input, getFormatterConfig()))) {
                    unformatted.add(file);
                }
            } catch (IOException exception) {
                throw new GradleException("Failed to check " + file + ".", exception);
            }
        }
        if (!unformatted.isEmpty()) {
            throw new GradleException("SQL files need formatting: " + unformatted);
        }
    }
}
