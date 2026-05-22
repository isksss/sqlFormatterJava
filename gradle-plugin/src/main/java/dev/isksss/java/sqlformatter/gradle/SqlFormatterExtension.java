package dev.isksss.java.sqlformatter.gradle;

import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class SqlFormatterExtension {
    private final SqlFormatterFileSpec files;

    @Inject
    public SqlFormatterExtension(ObjectFactory objects) {
        files = objects.newInstance(SqlFormatterFileSpec.class);
    }

    public SqlFormatterFileSpec getFiles() {
        return files;
    }

    public void files(Action<? super SqlFormatterFileSpec> action) {
        action.execute(files);
    }

    public abstract Property<String> getDialect();

    public abstract Property<String> getIndent();

    public abstract Property<Boolean> getUppercase();

    public abstract Property<Integer> getLinesBetweenQueries();

    public abstract Property<Integer> getMaxColumnLength();
}
