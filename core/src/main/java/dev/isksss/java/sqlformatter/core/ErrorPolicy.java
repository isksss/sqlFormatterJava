package dev.isksss.java.sqlformatter.core;

/**
 * SQL整形に失敗した場合の扱い。
 */
public enum ErrorPolicy {
    /**
     * 整形に失敗した場合、例外を外へ出さず入力SQLをそのまま返す。
     */
    KEEP_INPUT,

    /**
     * 整形に失敗した場合、{@link SqlFormattingException} を送出する。
     */
    THROW
}
