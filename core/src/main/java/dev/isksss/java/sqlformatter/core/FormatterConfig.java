package dev.isksss.java.sqlformatter.core;

/**
 * SQL整形に利用する設定。
 *
 * @param dialect SQL方言。nullまたは空文字の場合は内部エンジンの既定方言を利用する
 * @param tabWidth スペースインデントの幅
 * @param useTabs インデントにタブ文字を使うか
 * @param keywordCase キーワードの大文字小文字。preserve、upper、lowerを指定する
 * @param dataTypeCase データ型の大文字小文字。preserve、upper、lowerを指定する
 * @param functionCase 関数名の大文字小文字。preserve、upper、lowerを指定する
 * @param identifierCase 非引用識別子の大文字小文字。preserve、upper、lowerを指定する
 * @param logicalOperatorNewline 論理演算子の改行位置。before、afterを指定する
 * @param expressionWidth 括弧内式を1行に保つ最大幅
 * @param linesBetweenQueries 複数SQL文の間に入れる空行数。nullの場合は内部エンジンの既定値を利用する
 * @param denseOperators 演算子の周囲の空白を詰めるか
 * @param newlineBeforeSemicolon セミコロンを独立行に置くか
 * @param errorPolicy 整形失敗時の扱い
 * @param charset CLIやGradleプラグインでSQLファイルを読み書きするときの文字セット
 */
public record FormatterConfig(
        String dialect,
        Integer tabWidth,
        Boolean useTabs,
        String keywordCase,
        String dataTypeCase,
        String functionCase,
        String identifierCase,
        String logicalOperatorNewline,
        Integer expressionWidth,
        Integer linesBetweenQueries,
        Boolean denseOperators,
        Boolean newlineBeforeSemicolon,
        ErrorPolicy errorPolicy,
        String charset) {

    /**
     * エラー方針と文字セットを指定しない整形設定を作成する。
     */
    public FormatterConfig(
            String dialect,
            Integer tabWidth,
            Boolean useTabs,
            String keywordCase,
            String dataTypeCase,
            String functionCase,
            String identifierCase,
            String logicalOperatorNewline,
            Integer expressionWidth,
            Integer linesBetweenQueries,
            Boolean denseOperators,
            Boolean newlineBeforeSemicolon) {
        this(
                dialect,
                tabWidth,
                useTabs,
                keywordCase,
                dataTypeCase,
                functionCase,
                identifierCase,
                logicalOperatorNewline,
                expressionWidth,
                linesBetweenQueries,
                denseOperators,
                newlineBeforeSemicolon,
                null,
                null);
    }

    /**
     * アプリケーション共通の既定設定を返す。
     *
     * @return 失敗時は入力を保持し、文字セットはUTF-8を使う既定設定
     */
    public static FormatterConfig defaults() {
        return new FormatterConfig(
                null,
                2,
                false,
                "preserve",
                "preserve",
                "preserve",
                "preserve",
                "before",
                50,
                1,
                false,
                false,
                ErrorPolicy.KEEP_INPUT,
                "UTF-8");
    }

    /**
     * この設定を優先し、未指定項目だけフォールバック設定で補完する。
     *
     * @param fallback 未指定項目に利用する設定
     * @return 補完後の設定
     */
    public FormatterConfig mergeOver(FormatterConfig fallback) {
        return new FormatterConfig(
                choose(dialect, fallback.dialect),
                choose(tabWidth, fallback.tabWidth),
                choose(useTabs, fallback.useTabs),
                choose(keywordCase, fallback.keywordCase),
                choose(dataTypeCase, fallback.dataTypeCase),
                choose(functionCase, fallback.functionCase),
                choose(identifierCase, fallback.identifierCase),
                choose(logicalOperatorNewline, fallback.logicalOperatorNewline),
                choose(expressionWidth, fallback.expressionWidth),
                choose(linesBetweenQueries, fallback.linesBetweenQueries),
                choose(denseOperators, fallback.denseOperators),
                choose(newlineBeforeSemicolon, fallback.newlineBeforeSemicolon),
                choose(errorPolicy, fallback.errorPolicy),
                choose(charset, fallback.charset));
    }

    private static <T> T choose(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
