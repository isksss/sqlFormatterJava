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

/**
 * SQL整形タスクで共通利用する入力設定と補助処理を持つ基底タスク。
 */
public abstract class AbstractSqlFormatterTask extends DefaultTask {
    private final SqlFormatterService formatter = new SqlFormatterService();

    /**
     * SQL整形タスクの共通基底を作成する。
     */
    public AbstractSqlFormatterTask() {}

    /**
     * SQLファイル探索の起点パス。
     *
     * @return 起点パス一覧
     */
    @Input
    public abstract ListProperty<String> getSourcePaths();

    /**
     * 整形対象に含めるファイルパターン。
     *
     * @return includeパターン一覧
     */
    @Input
    public abstract ListProperty<String> getIncludes();

    /**
     * 整形対象から除外するファイルパターン。
     *
     * @return excludeパターン一覧
     */
    @Input
    public abstract ListProperty<String> getExcludes();

    /**
     * SQL方言。
     *
     * @return SQL方言プロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getDialect();

    /**
     * スペースインデントの幅。
     *
     * @return インデント幅プロパティ
     */
    @Input
    @Optional
    public abstract Property<Integer> getTabWidth();

    /**
     * インデントにタブ文字を使うか。
     *
     * @return タブ利用フラグプロパティ
     */
    @Input
    @Optional
    public abstract Property<Boolean> getUseTabs();

    /**
     * SQLキーワードの大文字小文字。
     *
     * @return キーワードケースプロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getKeywordCase();

    /**
     * データ型名の大文字小文字。
     *
     * @return データ型ケースプロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getDataTypeCase();

    /**
     * 関数名の大文字小文字。
     *
     * @return 関数名ケースプロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getFunctionCase();

    /**
     * 非引用識別子の大文字小文字。
     *
     * @return 識別子ケースプロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getIdentifierCase();

    /**
     * 論理演算子の改行位置。
     *
     * @return 論理演算子改行位置プロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getLogicalOperatorNewline();

    /**
     * 括弧内式を1行に保つ最大幅。
     *
     * @return 式幅プロパティ
     */
    @Input
    @Optional
    public abstract Property<Integer> getExpressionWidth();

    /**
     * 複数SQL文の間に入れる空行数。
     *
     * @return 空行数プロパティ
     */
    @Input
    @Optional
    public abstract Property<Integer> getLinesBetweenQueries();

    /**
     * 演算子の周囲の空白を詰めるか。
     *
     * @return 演算子空白制御プロパティ
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDenseOperators();

    /**
     * セミコロンを独立行に置くか。
     *
     * @return セミコロン改行プロパティ
     */
    @Input
    @Optional
    public abstract Property<Boolean> getNewlineBeforeSemicolon();

    /**
     * 整形失敗時の扱い。
     *
     * @return エラー方針プロパティ
     */
    @Input
    @Optional
    public abstract Property<ErrorPolicy> getErrorPolicy();

    /**
     * SQLファイルの読み書きに使う文字セット。
     *
     * @return 文字セット名プロパティ
     */
    @Input
    @Optional
    public abstract Property<String> getCharset();

    /**
     * Gradleが追跡する整形対象SQLファイル。
     *
     * @return SQLファイル集合
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSqlFiles();

    /**
     * SQL整形サービスを返す。
     *
     * @return SQL整形サービス
     */
    @Internal
    protected SqlFormatterService getFormatter() {
        return formatter;
    }

    /**
     * 実際に処理するSQLファイルを返す。
     *
     * @return 選択済みSQLファイル一覧
     */
    protected List<File> selectedSqlFiles() {
        List<String> sourcePaths = getSourcePaths().getOrElse(List.of());
        if (sourcePaths.isEmpty()) {
            throw new GradleException("sqlFormatter.files.from must select at least one source path.");
        }
        return getSqlFiles().getFiles().stream().sorted().toList();
    }

    /**
     * タスク入力から整形設定を作成する。
     *
     * @return 整形設定
     */
    @Internal
    protected FormatterConfig getFormatterConfig() {
        return new FormatterConfig(
                getDialect().getOrNull(),
                getTabWidth().getOrNull(),
                getUseTabs().getOrNull(),
                getKeywordCase().getOrNull(),
                getDataTypeCase().getOrNull(),
                getFunctionCase().getOrNull(),
                getIdentifierCase().getOrNull(),
                getLogicalOperatorNewline().getOrNull(),
                getExpressionWidth().getOrNull(),
                getLinesBetweenQueries().getOrNull(),
                getDenseOperators().getOrNull(),
                getNewlineBeforeSemicolon().getOrNull(),
                getErrorPolicy().getOrNull(),
                getCharset().getOrNull());
    }

    /**
     * SQLファイルの読み書きに使う文字セットを返す。
     *
     * @return 文字セット
     */
    @Internal
    protected Charset getSqlCharset() {
        try {
            return Charset.forName(getFormatterConfig().mergeOver(FormatterConfig.defaults()).charset());
        } catch (RuntimeException exception) {
            throw new GradleException("Unsupported sqlFormatter charset.", exception);
        }
    }
}
