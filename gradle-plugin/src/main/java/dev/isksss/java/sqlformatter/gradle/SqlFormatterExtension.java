package dev.isksss.java.sqlformatter.gradle;

import dev.isksss.java.sqlformatter.core.ErrorPolicy;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * {@code sqlFormatter} Gradle拡張の設定モデル。
 */
public abstract class SqlFormatterExtension {
    private final SqlFormatterFileSpec files;

    /**
     * Gradleのオブジェクトファクトリを使って拡張を作成する。
     *
     * @param objects Gradleのオブジェクトファクトリ
     */
    @Inject
    public SqlFormatterExtension(ObjectFactory objects) {
        files = objects.newInstance(SqlFormatterFileSpec.class);
    }

    /**
     * SQLファイル選択設定を返す。
     *
     * @return SQLファイル選択設定
     */
    public SqlFormatterFileSpec getFiles() {
        return files;
    }

    /**
     * SQLファイル選択設定を構成する。
     *
     * @param action SQLファイル選択設定へ適用するアクション
     */
    public void files(Action<? super SqlFormatterFileSpec> action) {
        action.execute(files);
    }

    /**
     * SQL方言。
     *
     * @return SQL方言プロパティ
     */
    public abstract Property<String> getDialect();

    public abstract Property<Integer> getTabWidth();

    public abstract Property<Boolean> getUseTabs();

    public abstract Property<String> getKeywordCase();

    public abstract Property<String> getDataTypeCase();

    public abstract Property<String> getFunctionCase();

    public abstract Property<String> getIdentifierCase();

    public abstract Property<String> getLogicalOperatorNewline();

    public abstract Property<Integer> getExpressionWidth();

    /**
     * 複数SQL文の間に入れる空行数。
     *
     * @return 空行数プロパティ
     */
    public abstract Property<Integer> getLinesBetweenQueries();

    public abstract Property<Boolean> getDenseOperators();

    public abstract Property<Boolean> getNewlineBeforeSemicolon();

    /**
     * 整形失敗時の扱い。
     *
     * @return エラー方針プロパティ
     */
    public abstract Property<ErrorPolicy> getErrorPolicy();

    /**
     * SQLファイルの読み書きに使う文字セット。
     *
     * @return 文字セット名プロパティ
     */
    public abstract Property<String> getCharset();
}
