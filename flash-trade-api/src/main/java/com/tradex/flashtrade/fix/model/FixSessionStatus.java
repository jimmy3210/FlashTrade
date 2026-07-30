package com.tradex.flashtrade.fix.model;

/**
 * FIX セッションの状態を表す Enum
 */
public enum FixSessionStatus {

    /** 接続前（未起動） */
    DISCONNECTED,

    /** ログオン中（接続確立、認証待ち） */
    LOGGING_ON,

    /** ログオン済み（セッション確立済み、通常稼働） */
    LOGGED_ON,

    /** ログアウト中（切断処理中） */
    LOGGING_OUT,

    /** エラー状態 */
    ERROR
}
