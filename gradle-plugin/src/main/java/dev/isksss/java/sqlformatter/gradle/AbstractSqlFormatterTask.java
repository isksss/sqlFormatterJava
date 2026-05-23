package dev.isksss.java.sqlformatter.gradle;

import dev.isksss.java.sqlformatter.core.ErrorPolicy;
import dev.isksss.java.sqlformatter.core.FormatterConfig;
import dev.isksss.java.sqlformatter.core.SqlFormatterService;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

public abstract class AbstractSqlFormatterTask extends DefaultTask {
    private final SqlFormatterService formatter = new SqlFormatterService();

    @Input
    public abstract ListProperty<String> getSourcePaths();

    @Input
    public abstract ListProperty<String> getIncludes();

    @Input
    public abstract ListProperty<String> getExcludes();

    @Input
    @Optional
    public abstract Property<String> getDialect();

    @Input
    @Optional
    public abstract Property<String> getIndent();

    @Input
    @Optional
    public abstract Property<Boolean> getUppercase();

    @Input
    @Optional
    public abstract Property<Integer> getLinesBetweenQueries();

    @Input
    @Optional
    public abstract Property<Integer> getMaxColumnLength();

    @Input
    @Optional
    public abstract Property<ErrorPolicy> getErrorPolicy();

    @Input
    @Optional
    public abstract Property<String> getCharset();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSqlFiles();

    @Internal
    protected SqlFormatterService getFormatter() {
        return formatter;
    }

    protected List<File> selectedSqlFiles() {
        List<String> sourcePaths = getSourcePaths().getOrElse(List.of());
        if (sourcePaths.isEmpty()) {
            throw new GradleException("sqlFormatter.files.from must select at least one source path.");
        }
        return getSqlFiles().getFiles().stream().sorted().toList();
    }

    @Internal
    protected FormatterConfig getFormatterConfig() {
        return new FormatterConfig(
                getDialect().getOrNull(),
                getIndent().getOrNull(),
                getUppercase().getOrNull(),
                getLinesBetweenQueries().getOrNull(),
                getMaxColumnLength().getOrNull(),
                getErrorPolicy().getOrNull(),
                getCharset().getOrNull());
    }

    @Internal
    protected Charset getSqlCharset() {
        try {
            return Charset.forName(getFormatterConfig().mergeOver(FormatterConfig.defaults()).charset());
        } catch (RuntimeException exception) {
            throw new GradleException("Unsupported sqlFormatter charset.", exception);
        }
    }
}
