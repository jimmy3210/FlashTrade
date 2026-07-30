package com.tradex.flashtrade.fix.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.SessionID;

/**
 * FIX 監査ログ
 *
 * 全 FIX メッセージ（送受信）を構造化ログとして記録する。
 * セキュリティ上の理由から Tag 554（Password）はマスクする。
 */
public class FixAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("fix.audit");



    /**
     * 受信メッセージを監査ログに記録する
     *
     * @param message   受信した FIX メッセージ
     * @param sessionId セッション ID
     */
    public void logInbound(Message message, SessionID sessionId) {
        String raw = maskSensitiveFields(message.toString());
        auditLog.info("INBOUND | session={} | version={} | msgType={} | msg={}",
                formatSessionId(sessionId),
                sessionId.getBeginString(),
                extractMsgType(message),
                raw);
    }

    /**
     * 送信メッセージを監査ログに記録する
     *
     * @param message   送信する FIX メッセージ
     * @param sessionId セッション ID
     */
    public void logOutbound(Message message, SessionID sessionId) {
        String raw = maskSensitiveFields(message.toString());
        auditLog.info("OUTBOUND | session={} | version={} | msgType={} | msg={}",
                formatSessionId(sessionId),
                sessionId.getBeginString(),
                extractMsgType(message),
                raw);
    }

    /**
     * セッション接続/切断イベントを記録する
     *
     * @param event     "LOGON" または "LOGOUT"
     * @param sessionId セッション ID
     */
    public void logSessionEvent(String event, SessionID sessionId) {
        auditLog.info("SESSION_EVENT | event={} | session={} | version={}",
                event,
                formatSessionId(sessionId),
                sessionId.getBeginString());
    }

    /**
     * エラーを監査ログに記録する
     *
     * @param errorMessage エラーメッセージ
     * @param sessionId    セッション ID
     * @param cause        原因例外（nullable）
     */
    public void logError(String errorMessage, SessionID sessionId, Throwable cause) {
        if (cause != null) {
            auditLog.error("ERROR | session={} | message={}", formatSessionId(sessionId), errorMessage, cause);
        } else {
            auditLog.error("ERROR | session={} | message={}", formatSessionId(sessionId), errorMessage);
        }
    }

    // -------------------------------------------------------------------------
    // プライベートヘルパー
    // -------------------------------------------------------------------------

    /**
     * センシティブフィールド（Password）をマスクする
     * FIX メッセージの SOH（\u0001）区切りを考慮
     */
    private String maskSensitiveFields(String rawMessage) {
        if (rawMessage == null) {
            return "";
        }
        // Tag 554 (Password) をマスク: \u0001554=VALUE\u0001 → \u0001554=***\u0001
        return rawMessage.replaceAll("\u0001554=[^\u0001]*", "\u0001554=***");
    }

    private String formatSessionId(SessionID sessionId) {
        return sessionId.getSenderCompID() + "->" + sessionId.getTargetCompID();
    }

    private String extractMsgType(Message message) {
        try {
            return message.getHeader().getString(quickfix.field.MsgType.FIELD);
        } catch (quickfix.FieldNotFound e) {
            return "UNKNOWN";
        }
    }
}
