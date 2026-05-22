package dev.isksss.java.sqlformatter.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Formats SQL source files in place.")
public abstract class SqlFormatTask extends AbstractSqlFormatterTask {
    @TaskAction
    public void format() {
        for (File file : selectedSqlFiles()) {
            try {
                String input = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String formatted = getFormatter().format(input, getFormatterConfig());
                if (!input.equals(formatted)) {
                    Files.writeString(file.toPath(), formatted, StandardCharsets.UTF_8);
                }
            } catch (IOException exception) {
                throw new GradleException("Failed to format " + file + ".", exception);
            }
        }
    }
}
