package dev.isksss.java.sqlformatter.gradle;

import java.util.Arrays;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

public abstract class SqlFormatterFileSpec {
    private final ListProperty<String> sources;
    private final ListProperty<String> includes;
    private final ListProperty<String> excludes;

    @Inject
    public SqlFormatterFileSpec(ObjectFactory objects) {
        sources = objects.listProperty(String.class);
        includes = objects.listProperty(String.class).convention(Arrays.asList("**/*.sql"));
        excludes = objects.listProperty(String.class).convention(Arrays.asList());
    }

    public ListProperty<String> getSources() {
        return sources;
    }

    public ListProperty<String> getIncludes() {
        return includes;
    }

    public ListProperty<String> getExcludes() {
        return excludes;
    }

    public void from(Object... paths) {
        sources.addAll(toStrings(paths));
    }

    public void include(String... patterns) {
        includes.addAll(Arrays.asList(patterns));
    }

    public void exclude(String... patterns) {
        excludes.addAll(Arrays.asList(patterns));
    }

    private Iterable<String> toStrings(Object[] values) {
        return Stream.of(values).map(Object::toString).toList();
    }
}
