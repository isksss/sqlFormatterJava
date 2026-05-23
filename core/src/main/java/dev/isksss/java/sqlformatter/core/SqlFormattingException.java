package dev.isksss.java.sqlformatter.core;

/**
 * SQL整形に失敗したことを表す例外。
 */
public final class SqlFormattingException extends RuntimeException {
    /**
     * 指定されたメッセージと原因で例外を作成する。
     *
     * @param message 失敗内容を表すメッセージ
     * @param cause 原因となった例外
     */
    public SqlFormattingException(String message, Throwable cause) {
        super(message, cause);
    }
}
