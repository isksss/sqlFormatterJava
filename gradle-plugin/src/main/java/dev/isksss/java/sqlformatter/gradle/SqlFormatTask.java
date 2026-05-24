package dev.isksss.java.sqlformatter.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

/**
 * 選択されたSQLファイルをその場で整形するGradleタスク。
 */
@DisableCachingByDefault(because = "Formats SQL source files in place.")
public abstract class SqlFormatTask extends AbstractSqlFormatterTask {
    /**
     * SQL整形タスクを作成する。
     */
    public SqlFormatTask() {}

    /**
     * SQLファイルを整形し、差分がある場合だけ書き戻す。
     */
    @TaskAction
    public void format() {
        for (File file : selectedSqlFiles()) {
            try {
                var charset = getSqlCharset();
                String input = Files.readString(file.toPath(), charset);
                String formatted = formatSql(input);
                if (!input.equals(formatted)) {
                    Files.writeString(file.toPath(), formatted, charset);
                }
            } catch (IOException exception) {
                throw new GradleException("Failed to format " + file + ".", exception);
            }
        }
    }

}
