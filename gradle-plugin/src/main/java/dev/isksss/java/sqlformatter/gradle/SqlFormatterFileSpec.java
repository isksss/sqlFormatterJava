package dev.isksss.java.sqlformatter.gradle;

import java.util.Arrays;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

/**
 * Gradleプラグインで整形対象SQLファイルを選択するための設定。
 */
public abstract class SqlFormatterFileSpec {
    private final ListProperty<String> sources;
    private final ListProperty<String> includes;
    private final ListProperty<String> excludes;

    /**
     * Gradleのオブジェクトファクトリを使ってファイル選択設定を作成する。
     *
     * @param objects Gradleのオブジェクトファクトリ
     */
    @Inject
    public SqlFormatterFileSpec(ObjectFactory objects) {
        sources = objects.listProperty(String.class);
        includes = objects.listProperty(String.class).convention(Arrays.asList("**/*.sql"));
        excludes = objects.listProperty(String.class).convention(Arrays.asList());
    }

    /**
     * SQLファイル探索の起点パス。
     *
     * @return 起点パス一覧
     */
    public ListProperty<String> getSources() {
        return sources;
    }

    /**
     * 整形対象に含めるファイルパターン。
     *
     * @return includeパターン一覧
     */
    public ListProperty<String> getIncludes() {
        return includes;
    }

    /**
     * 整形対象から除外するファイルパターン。
     *
     * @return excludeパターン一覧
     */
    public ListProperty<String> getExcludes() {
        return excludes;
    }

    /**
     * SQLファイル探索の起点パスを追加する。
     *
     * @param paths 起点パス
     */
    public void from(Object... paths) {
        sources.addAll(toStrings(paths));
    }

    /**
     * 整形対象に含めるファイルパターンを追加する。
     *
     * @param patterns includeパターン
     */
    public void include(String... patterns) {
        includes.addAll(Arrays.asList(patterns));
    }

    /**
     * 整形対象から除外するファイルパターンを追加する。
     *
     * @param patterns excludeパターン
     */
    public void exclude(String... patterns) {
        excludes.addAll(Arrays.asList(patterns));
    }

    private Iterable<String> toStrings(Object[] values) {
        return Stream.of(values).map(Object::toString).toList();
    }
}
