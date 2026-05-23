package dev.isksss.java.sqlformatter.core;

/**
 * SQL整形に利用する設定。
 *
 * @param dialect SQL方言。nullまたは空文字の場合は内部エンジンの既定方言を利用する
 * @param indent インデント文字列。nullの場合は内部エンジンの既定値を利用する
 * @param uppercase キーワードなどを大文字化するか。nullの場合は内部エンジンの既定値を利用する
 * @param linesBetweenQueries 複数SQL文の間に入れる空行数。nullの場合は内部エンジンの既定値を利用する
 * @param maxColumnLength 折返しの目安となる最大桁数。nullの場合は内部エンジンの既定値を利用する
 * @param errorPolicy 整形失敗時の扱い
 * @param charset CLIやGradleプラグインでSQLファイルを読み書きするときの文字セット
 */
public record FormatterConfig(
        String dialect,
        String indent,
        Boolean uppercase,
        Integer linesBetweenQueries,
        Integer maxColumnLength,
        ErrorPolicy errorPolicy,
        String charset) {

    /**
     * エラー方針と文字セットを指定しない整形設定を作成する。
     *
     * @param dialect SQL方言
     * @param indent インデント文字列
     * @param uppercase キーワードなどを大文字化するか
     * @param linesBetweenQueries 複数SQL文の間に入れる空行数
     * @param maxColumnLength 折返しの目安となる最大桁数
     */
    public FormatterConfig(
            String dialect, String indent, Boolean uppercase, Integer linesBetweenQueries, Integer maxColumnLength) {
        this(dialect, indent, uppercase, linesBetweenQueries, maxColumnLength, null, null);
    }

    /**
     * アプリケーション共通の既定設定を返す。
     *
     * @return 失敗時は入力を保持し、文字セットはUTF-8を使う既定設定
     */
    public static FormatterConfig defaults() {
        return new FormatterConfig(null, null, null, null, null, ErrorPolicy.KEEP_INPUT, "UTF-8");
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
                choose(indent, fallback.indent),
                choose(uppercase, fallback.uppercase),
                choose(linesBetweenQueries, fallback.linesBetweenQueries),
                choose(maxColumnLength, fallback.maxColumnLength),
                choose(errorPolicy, fallback.errorPolicy),
                choose(charset, fallback.charset));
    }

    private static <T> T choose(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
