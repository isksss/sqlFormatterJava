package dev.isksss.java.sqlformatter.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

/**
 * SQL整形用のGradle拡張とタスクを登録するプラグイン。
 */
public final class SqlFormatterPlugin implements Plugin<Project> {
    /**
     * SQL整形用Gradleプラグインを作成する。
     */
    public SqlFormatterPlugin() {}

    /**
     * {@code sqlFormatter} 拡張、{@code sqlFormat} タスク、{@code sqlCheck} タスクを登録する。
     *
     * @param project 適用先のGradleプロジェクト
     */
    @Override
    public void apply(Project project) {
        SqlFormatterExtension extension =
                project.getExtensions().create("sqlFormatter", SqlFormatterExtension.class);

        project.getTasks().register("sqlFormat", SqlFormatTask.class, task -> {
            task.setGroup("formatting");
            task.setDescription("Formats configured SQL files in place.");
            configure(task, extension);
        });
        project.getTasks().register("sqlCheck", SqlCheckTask.class, task -> {
            task.setGroup("verification");
            task.setDescription("Checks that configured SQL files are formatted.");
            configure(task, extension);
        });

        project.afterEvaluate(ignored -> {
            FileTree sqlFiles = project.files(extension.getFiles().getSources().getOrElse(java.util.List.of()))
                    .getAsFileTree()
                    .matching(patterns -> {
                        patterns.include(extension.getFiles().getIncludes().get());
                        patterns.exclude(extension.getFiles().getExcludes().get());
                    });
            project.getTasks().withType(AbstractSqlFormatterTask.class).configureEach(task -> {
                task.getSqlFiles().from(sqlFiles);
            });
        });
    }

    private void configure(AbstractSqlFormatterTask task, SqlFormatterExtension extension) {
        task.getSourcePaths().set(extension.getFiles().getSources());
        task.getIncludes().set(extension.getFiles().getIncludes());
        task.getExcludes().set(extension.getFiles().getExcludes());
        task.getDialect().set(extension.getDialect());
        task.getIndent().set(extension.getIndent());
        task.getUppercase().set(extension.getUppercase());
        task.getLinesBetweenQueries().set(extension.getLinesBetweenQueries());
        task.getMaxColumnLength().set(extension.getMaxColumnLength());
        task.getErrorPolicy().set(extension.getErrorPolicy());
        task.getCharset().set(extension.getCharset());
    }
}
