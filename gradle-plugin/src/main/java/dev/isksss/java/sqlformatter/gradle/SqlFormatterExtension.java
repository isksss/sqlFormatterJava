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

    /**
     * スペースインデントの幅。
     *
     * @return インデント幅プロパティ
     */
    public abstract Property<Integer> getTabWidth();

    /**
     * インデントにタブ文字を使うか。
     *
     * @return タブ利用フラグプロパティ
     */
    public abstract Property<Boolean> getUseTabs();

    /**
     * SQLキーワードの大文字小文字。
     *
     * @return キーワードケースプロパティ
     */
    public abstract Property<String> getKeywordCase();

    /**
     * データ型名の大文字小文字。
     *
     * @return データ型ケースプロパティ
     */
    public abstract Property<String> getDataTypeCase();

    /**
     * 関数名の大文字小文字。
     *
     * @return 関数名ケースプロパティ
     */
    public abstract Property<String> getFunctionCase();

    /**
     * 非引用識別子の大文字小文字。
     *
     * @return 識別子ケースプロパティ
     */
    public abstract Property<String> getIdentifierCase();

    /**
     * 論理演算子の改行位置。
     *
     * @return 論理演算子改行位置プロパティ
     */
    public abstract Property<String> getLogicalOperatorNewline();

    /**
     * 括弧内式を1行に保つ最大幅。
     *
     * @return 式幅プロパティ
     */
    public abstract Property<Integer> getExpressionWidth();

    /**
     * 複数SQL文の間に入れる空行数。
     *
     * @return 空行数プロパティ
     */
    public abstract Property<Integer> getLinesBetweenQueries();

    /**
     * 演算子の周囲の空白を詰めるか。
     *
     * @return 演算子空白制御プロパティ
     */
    public abstract Property<Boolean> getDenseOperators();

    /**
     * セミコロンを独立行に置くか。
     *
     * @return セミコロン改行プロパティ
     */
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
