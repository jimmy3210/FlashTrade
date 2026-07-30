package com.tradex.flashtrade.fix.core;

import com.tradex.flashtrade.fix.adapter.FixMessageAdapter;
import com.tradex.flashtrade.fix.audit.FixAuditLogger;
import com.tradex.flashtrade.fix.model.FixExecutionReport;
import com.tradex.flashtrade.fix.model.FixSessionStatus;
import com.tradex.flashtrade.fix.routing.FixMessageDispatcher;
import com.tradex.flashtrade.fix.routing.FixVersionRouter;
import com.tradex.flashtrade.fix.security.FixRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MsgType;

/**
 * QuickFIX/J Application インタフェース実装
 *
 * QuickFIX/J の全コールバックを受け取り、各コンポーネントへ委譲する。
 * このクラスは FIX セッションのライフサイクル全体を管理する。
 */
public class FixApplicationHandler implements Application {

    private static final Logger log = LoggerFactory.getLogger(FixApplicationHandler.class);

    private final FixVersionRouter versionRouter;
    private final FixMessageDispatcher dispatcher;
    private final FixAuditLogger auditLogger;
    private final FixRateLimiter rateLimiter;
    private final FixSessionManager sessionManager;

    public FixApplicationHandler(
            FixVersionRouter versionRouter,
            FixMessageDispatcher dispatcher,
            FixAuditLogger auditLogger,
            FixRateLimiter rateLimiter,
            FixSessionManager sessionManager
    ) {
        this.versionRouter = versionRouter;
        this.dispatcher = dispatcher;
        this.auditLogger = auditLogger;
        this.rateLimiter = rateLimiter;
        this.sessionManager = sessionManager;
    }

    // -------------------------------------------------------------------------
    // QuickFIX/J Application コールバック
    // -------------------------------------------------------------------------

    /**
     * セッション作成時のコールバック
     * セッションを SessionManager に登録する
     */
    @Override
    public void onCreate(SessionID sessionId) {
        sessionManager.registerSession(sessionId);
        log.info("FIX セッション作成: {}", sessionId);
    }

    /**
     * ログオン成功時のコールバック
     */
    @Override
    public void onLogon(SessionID sessionId) {
        sessionManager.updateStatus(sessionId, FixSessionStatus.LOGGED_ON);
        auditLogger.logSessionEvent("LOGON", sessionId);
        dispatcher.dispatchSessionLogon(sessionId);
        log.info("FIX セッション LOGON 完了: {}", sessionId);
    }

    /**
     * ログアウト時のコールバック
     */
    @Override
    public void onLogout(SessionID sessionId) {
        sessionManager.updateStatus(sessionId, FixSessionStatus.DISCONNECTED);
        auditLogger.logSessionEvent("LOGOUT", sessionId);
        dispatcher.dispatchSessionLogout(sessionId);
        rateLimiter.resetSession(sessionId);
        log.warn("FIX セッション LOGOUT: {}", sessionId);
    }

    /**
     * 管理メッセージ送信前のコールバック（Logon, Logout, HeartBeat 等）
     * Logon メッセージに認証情報を付与する処理はここで行う。
     * （認証情報は設定ファイルの TargetSubID / Username / Password から自動付与）
     */
    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            if (MsgType.LOGON.equals(msgType)) {
                log.debug("Logon メッセージ送信: {}", sessionId);
                // QuickFIX/J は設定ファイルの Username/Password を自動付与する
                // 追加のカスタムフィールドが必要な場合はここで設定する
            }
        } catch (FieldNotFound e) {
            log.warn("toAdmin: MsgType 取得失敗: {}", e.getMessage());
        }
    }

    /**
     * 管理メッセージ受信時のコールバック（HeartBeat, TestRequest 等）
     */
    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        auditLogger.logInbound(message, sessionId);
    }

    /**
     * アプリケーションメッセージ送信前のコールバック
     * レート制限チェックを行う
     */
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        try {
            rateLimiter.checkAndIncrement(sessionId);
            auditLogger.logOutbound(message, sessionId);
        } catch (FixRateLimiter.RateLimitExceededException e) {
            log.warn("レート制限超過によりメッセージ送信キャンセル: {}", e.getMessage());
            throw new DoNotSend();
        }
    }

    /**
     * アプリケーションメッセージ受信時のコールバック
     * バージョンルーティング → アダプター変換 → ディスパッチを行う
     *
     * 対応メッセージタイプ:
     *   8  = ExecutionReport
     *   W  = MarketDataSnapshotFullRefresh
     *   X  = MarketDataIncrementalRefresh
     *   AE = TradeCaptureReport (FIX 4.4+)
     *   AP = PositionReport     (FIX 4.4+)
     */
    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        auditLogger.logInbound(message, sessionId);

        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);

            switch (msgType) {
                case MsgType.EXECUTION_REPORT ->
                        handleExecutionReport(message, sessionId);
                case MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH,
                     MsgType.MARKET_DATA_INCREMENTAL_REFRESH ->
                        handleMarketData(message, sessionId);
                case "AE" ->  // TradeCaptureReport
                        handleTradeCaptureReport(message, sessionId);
                case "AP" ->  // PositionReport
                        handlePositionReport(message, sessionId);
                default ->
                        log.debug("未処理メッセージタイプ受信: msgType={}, session={}", msgType, sessionId);
            }
        } catch (FieldNotFound e) {
            log.error("fromApp: フィールド取得失敗: session={}, error={}", sessionId, e.getMessage());
        } catch (UnsupportedOperationException e) {
            log.error("fromApp: 未対応 FIX バージョン: session={}, error={}", sessionId, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // プライベートヘルパー
    // -------------------------------------------------------------------------

    private void handleExecutionReport(Message message, SessionID sessionId) {
        try {
            FixMessageAdapter adapter = versionRouter.resolve(sessionId);
            FixExecutionReport report = adapter.toExecutionReport(message, sessionId);
            dispatcher.dispatchExecutionReport(report);
        } catch (Exception e) {
            log.error("ExecutionReport 処理エラー: session={}, error={}", sessionId, e.getMessage(), e);
            auditLogger.logError("ExecutionReport 処理エラー: " + e.getMessage(), sessionId, e);
        }
    }

    private void handleMarketData(Message message, SessionID sessionId) {
        try {
            FixMessageAdapter adapter = versionRouter.resolve(sessionId);
            String reqId = adapter.extractMdReqId(message);
            java.util.List<com.tradex.flashtrade.fix.model.FixMarketDataEntry> entries =
                    adapter.parseMarketDataMessage(message, sessionId);
            if (!entries.isEmpty()) {
                dispatcher.dispatchMarketData(reqId, entries);
            }
        } catch (Exception e) {
            log.error("MarketData 処理エラー: session={}, error={}", sessionId, e.getMessage(), e);
            auditLogger.logError("MarketData 処理エラー: " + e.getMessage(), sessionId, e);
        }
    }

    private void handleTradeCaptureReport(Message message, SessionID sessionId) {
        try {
            FixMessageAdapter adapter = versionRouter.resolve(sessionId);
            com.tradex.flashtrade.fix.model.FixTradeReport report =
                    adapter.parseTradeCaptureReport(message, sessionId);
            dispatcher.dispatchTradeReport(report);
        } catch (UnsupportedOperationException e) {
            log.warn("TradeCaptureReport: FIX バージョン未対応: session={}", sessionId);
        } catch (Exception e) {
            log.error("TradeCaptureReport 処理エラー: session={}, error={}", sessionId, e.getMessage(), e);
            auditLogger.logError("TradeCaptureReport 処理エラー: " + e.getMessage(), sessionId, e);
        }
    }

    private void handlePositionReport(Message message, SessionID sessionId) {
        try {
            FixMessageAdapter adapter = versionRouter.resolve(sessionId);
            com.tradex.flashtrade.fix.model.FixPositionReport report =
                    adapter.parsePositionReport(message, sessionId);
            dispatcher.dispatchPositionReport(report);
        } catch (UnsupportedOperationException e) {
            log.warn("PositionReport: FIX バージョン未対応: session={}", sessionId);
        } catch (Exception e) {
            log.error("PositionReport 処理エラー: session={}, error={}", sessionId, e.getMessage(), e);
            auditLogger.logError("PositionReport 処理エラー: " + e.getMessage(), sessionId, e);
        }
    }
}

