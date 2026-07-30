package com.tradex.flashtrade.fix.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tradex.flashtrade.fix.config.FixGatewayProperties;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MsgType;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

/**
 * FIX テスト用 Acceptor サーバー（Phase 1 開発専用）
 *
 * QuickFIX/J の Executor サンプルをベースに実装した簡易 FIX サーバー。
 * NewOrderSingle を受信すると自動的に ExecutionReport を返送する。
 *
 * <b>開発・テスト環境のみ使用可</b>。本番環境では起動しないこと。
 * （flashtrade.fix.test-server.enabled=true の場合のみ有効）
 */
public class FixTestAcceptor implements Application {

    private static final Logger log = LoggerFactory.getLogger(FixTestAcceptor.class);

    private final FixGatewayProperties.TestServerConfig config;
    private Acceptor acceptor;
    private final java.util.concurrent.atomic.AtomicLong execIdSequence = new java.util.concurrent.atomic.AtomicLong(1);

    public FixTestAcceptor(FixGatewayProperties.TestServerConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void startServer() {
        if (!config.enabled()) {
            log.info("FIX テストサーバーは無効化されています");
            return;
        }
        try {
            log.info("FIX テストサーバー起動: port={}, version={}", config.port(), config.fixVersion());

            SessionSettings settings = loadTestSettings();
            MessageStoreFactory storeFactory = new MemoryStoreFactory();
            LogFactory logFactory = new ScreenLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();

            acceptor = new SocketAcceptor(this, storeFactory, settings, logFactory, messageFactory);
            acceptor.start();

            log.info("FIX テストサーバー起動完了: port={}", config.port());
        } catch (ConfigError | RuntimeError e) {
            log.error("FIX テストサーバー起動失敗: {}", e.getMessage(), e);
            throw new RuntimeException("FIX テストサーバー起動失敗", e);
        }
    }

    @PreDestroy
    public void stopServer() {
        if (acceptor != null) {
            log.info("FIX テストサーバー停止");
            acceptor.stop(true);
        }
    }

    // -------------------------------------------------------------------------
    // QuickFIX/J Application コールバック
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("[TestAcceptor] セッション作成: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("[TestAcceptor] クライアント接続: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("[TestAcceptor] クライアント切断: {}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        // テストサーバー側は特別な管理メッセージ処理不要
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // ログオン認証は不要（テスト用のため全接続を受け入れる）
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // 送信前処理不要
    }

    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            log.debug("[TestAcceptor] 受信: msgType={}, session={}", msgType, sessionId);

            switch (msgType) {
                case MsgType.ORDER_SINGLE             -> handleNewOrderSingle(message, sessionId);
                case MsgType.ORDER_CANCEL_REQUEST     -> handleOrderCancelRequest(message, sessionId);
                case MsgType.ORDER_CANCEL_REPLACE_REQUEST -> handleOrderCancelReplaceRequest(message, sessionId);
                case MsgType.ORDER_STATUS_REQUEST     -> handleOrderStatusRequest(message, sessionId);
                case MsgType.MARKET_DATA_REQUEST      -> handleMarketDataRequest(message, sessionId);
                default -> log.debug("[TestAcceptor] 未処理メッセージ: {}", msgType);
            }
        } catch (Exception e) {
            log.error("[TestAcceptor] メッセージ処理エラー: {}", e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // ビジネスロジック（テスト用の疑似処理）
    // -------------------------------------------------------------------------

    /**
     * NewOrderSingle 受信 → 即時約定の ExecutionReport を返送
     */
    private void handleNewOrderSingle(Message request, SessionID sessionId) throws FieldNotFound {
        String clOrdId = request.getString(ClOrdID.FIELD);
        String symbol = request.getString(Symbol.FIELD);
        char side = request.getChar(Side.FIELD);
        double orderQty = request.getDouble(OrderQty.FIELD);
        double price = safeGetDouble(request, Price.FIELD, 100.0);

        log.info("[TestAcceptor] 注文受信: clOrdId={}, symbol={}, side={}, qty={}, price={}",
                clOrdId, symbol, side, orderQty, price);

        String orderId = "ORD-" + System.currentTimeMillis();
        String execId = "EXEC-" + execIdSequence.getAndIncrement();

        // 1. NEW ExecutionReport 送信
        sendExecutionReport(sessionId, execId + "-N", clOrdId, orderId, symbol, side,
                OrdStatus.NEW, ExecType.NEW, orderQty, 0.0, orderQty, 0.0, 0.0, price);

        // 2. FILLED ExecutionReport 送信（疑似即時約定）
        String execIdFilled = "EXEC-" + execIdSequence.getAndIncrement();
        sendExecutionReport(sessionId, execIdFilled, clOrdId, orderId, symbol, side,
                OrdStatus.FILLED, ExecType.FILL, orderQty, orderQty, 0.0, orderQty, price, price);

        log.info("[TestAcceptor] 注文疑似約定完了: clOrdId={}, orderId={}", clOrdId, orderId);
    }

    /**
     * OrderCancelRequest 受信 → キャンセル ExecutionReport を返送
     */
    private void handleOrderCancelRequest(Message request, SessionID sessionId) throws FieldNotFound {
        String clOrdId = request.getString(ClOrdID.FIELD);
        String origClOrdId = request.getString(OrigClOrdID.FIELD);
        String symbol = request.getString(Symbol.FIELD);
        char side = request.getChar(Side.FIELD);
        double orderQty = request.getDouble(OrderQty.FIELD);

        log.info("[TestAcceptor] キャンセル要求受信: clOrdId={}, origClOrdId={}", clOrdId, origClOrdId);

        String orderId = "ORD-CANCEL-" + System.currentTimeMillis();
        String execId = "EXEC-" + execIdSequence.getAndIncrement();

        sendExecutionReport(sessionId, execId, clOrdId, orderId, symbol, side,
                OrdStatus.CANCELED, ExecType.CANCELED, orderQty, 0.0, 0.0, 0.0, 0.0, 0.0);

        log.info("[TestAcceptor] キャンセル完了: clOrdId={}", clOrdId);
    }

    /**
     * OrderCancelReplaceRequest (G) 受信 → REPLACED ExecutionReport を返送
     */
    private void handleOrderCancelReplaceRequest(Message request, SessionID sessionId) throws FieldNotFound {
        String clOrdId     = request.getString(ClOrdID.FIELD);
        String origClOrdId = request.getString(OrigClOrdID.FIELD);
        String symbol      = request.getString(Symbol.FIELD);
        char   side        = request.getChar(Side.FIELD);
        double orderQty    = request.getDouble(OrderQty.FIELD);
        double price       = safeGetDouble(request, Price.FIELD, 100.0);

        log.info("[TestAcceptor] 注文修正要求受信: clOrdId={}, origClOrdId={}, newQty={}, newPrice={}",
                clOrdId, origClOrdId, orderQty, price);

        String orderId = "ORD-REPLACE-" + System.currentTimeMillis();
        String execId  = "EXEC-" + execIdSequence.getAndIncrement();

        sendExecutionReport(sessionId, execId, clOrdId, orderId, symbol, side,
                OrdStatus.REPLACED, ExecType.REPLACED, orderQty, 0.0, orderQty, 0.0, price, price);

        log.info("[TestAcceptor] 注文修正完了: clOrdId={}", clOrdId);
    }

    /**
     * OrderStatusRequest (H) 受信 → ExecType=ORDER_STATUS (I) の ExecutionReport を返送
     */
    private void handleOrderStatusRequest(Message request, SessionID sessionId) throws FieldNotFound {
        String clOrdId = request.getString(ClOrdID.FIELD);
        String symbol  = safeGetString(request, Symbol.FIELD, "UNKNOWN");
        char   side    = safeGetChar(request, Side.FIELD, Side.BUY);

        log.info("[TestAcceptor] 注文照会受信: clOrdId={}, symbol={}", clOrdId, symbol);

        String orderId = "ORD-" + clOrdId;
        String execId  = "EXEC-STATUS-" + execIdSequence.getAndIncrement();

        // ExecType='I' (ORDER_STATUS) は FIX 4.2 では '5' に相当
        sendExecutionReport(sessionId, execId, clOrdId, orderId, symbol, side,
                OrdStatus.NEW, '5' /* ExecType.ORDER_STATUS */ , 100.0, 0.0, 100.0, 0.0, 0.0, 0.0);

        log.info("[TestAcceptor] 注文照会回答送信: clOrdId={}", clOrdId);
    }

    /**
     * MarketDataRequest (V) 受信 → 各銘柄の MarketDataSnapshotFullRefresh (W) を返送
     */
    private void handleMarketDataRequest(Message request, SessionID sessionId) throws FieldNotFound {
        String reqId = request.getString(MDReqID.FIELD);
        log.info("[TestAcceptor] MarketData リクエスト受信: reqId={}", reqId);

        // NoRelatedSym グループから銘柄を取得
        int numRelated = request.getInt(NoRelatedSym.FIELD);
        for (int i = 1; i <= numRelated; i++) {
            quickfix.fix42.MarketDataRequest.NoRelatedSym symbolGroup =
                    new quickfix.fix42.MarketDataRequest.NoRelatedSym();
            request.getGroup(i, symbolGroup);
            String sym = symbolGroup.getString(Symbol.FIELD);

            // モック価格（銘柄ハッシュで分散）
            double mockMid = 100.0 + (Math.abs(sym.hashCode()) % 900);
            double bid     = mockMid - 0.05;
            double ask     = mockMid + 0.05;
            double last    = mockMid;

            sendMarketDataSnapshot(sessionId, reqId, sym, bid, ask, last);
            log.debug("[TestAcceptor] MarketData 送信: symbol={}, bid={}, ask={}", sym, bid, ask);
        }
    }

    private void sendMarketDataSnapshot(SessionID sessionId, String reqId,
                                        String symbol, double bid, double ask, double last) {
        try {
            MarketDataSnapshotFullRefresh snapshot = new MarketDataSnapshotFullRefresh();
            snapshot.set(new MDReqID(reqId));
            snapshot.set(new Symbol(symbol));

            // Bid エントリ
            MarketDataSnapshotFullRefresh.NoMDEntries bidGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
            bidGroup.set(new MDEntryType(MDEntryType.BID));
            bidGroup.set(new MDEntryPx(bid));
            bidGroup.set(new MDEntrySize(1000));
            snapshot.addGroup(bidGroup);

            // Offer エントリ
            MarketDataSnapshotFullRefresh.NoMDEntries askGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
            askGroup.set(new MDEntryType(MDEntryType.OFFER));
            askGroup.set(new MDEntryPx(ask));
            askGroup.set(new MDEntrySize(1000));
            snapshot.addGroup(askGroup);

            // Trade エントリ
            MarketDataSnapshotFullRefresh.NoMDEntries tradeGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
            tradeGroup.set(new MDEntryType(MDEntryType.TRADE));
            tradeGroup.set(new MDEntryPx(last));
            tradeGroup.set(new MDEntrySize(500));
            snapshot.addGroup(tradeGroup);

            Session.sendToTarget(snapshot, sessionId);

        } catch (SessionNotFound e) {
            log.error("[TestAcceptor] MarketData 送信失敗: {}", sessionId);
        }
    }

    private char safeGetChar(Message msg, int tag, char defaultValue) {
        try { return msg.getChar(tag); } catch (FieldNotFound e) { return defaultValue; }
    }

    private String safeGetString(Message msg, int tag, String defaultValue) {
        try { return msg.getString(tag); } catch (FieldNotFound e) { return defaultValue; }
    }

    private void sendExecutionReport(
            SessionID sessionId,
            String execId,
            String clOrdId,
            String orderId,
            String symbol,
            char side,
            char ordStatus,
            char execType,
            double ordQty,
            double cumQty,
            double leavesQty,
            double lastShares,
            double lastPx,
            double avgPx
    ) {
        try {
            ExecutionReport report = new ExecutionReport(
                    new OrderID(orderId),
                    new ExecID(execId),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(execType),
                    new OrdStatus(ordStatus),
                    new Symbol(symbol),
                    new Side(side),
                    new LeavesQty(leavesQty),
                    new CumQty(cumQty),
                    new AvgPx(avgPx)
            );

            report.set(new ClOrdID(clOrdId));
            report.set(new OrderQty(ordQty));

            if (lastShares > 0) {
                report.set(new LastShares(lastShares));
                report.set(new LastPx(lastPx));
            }

            Session.sendToTarget(report, sessionId);
            log.debug("[TestAcceptor] ExecutionReport 送信: execId={}, ordStatus={}", execId, ordStatus);

        } catch (SessionNotFound e) {
            log.error("[TestAcceptor] ExecutionReport 送信失敗: セッション不明: {}", sessionId);
        }
    }

    private double safeGetDouble(Message msg, int tag, double defaultValue) {
        try {
            return msg.getDouble(tag);
        } catch (FieldNotFound e) {
            return defaultValue;
        }
    }

    // -------------------------------------------------------------------------
    // 設定ファイル生成（動的生成でファイル不要）
    // -------------------------------------------------------------------------

    private SessionSettings loadTestSettings() throws ConfigError {
        String settingsContent = buildTestAcceptorConfig();
        try (java.io.StringReader reader = new java.io.StringReader(settingsContent)) {
            return new SessionSettings(new java.io.ByteArrayInputStream(settingsContent.getBytes()));
        } catch (Exception e) {
            throw new ConfigError("テストサーバー設定生成失敗: " + e.getMessage());
        }
    }

    private String buildTestAcceptorConfig() {
        return """
                [DEFAULT]
                ConnectionType=acceptor
                ResetOnLogon=Y
                HeartBtInt=30
                StartTime=00:00:00
                EndTime=00:00:00
                UseDataDictionary=Y
                ValidateFieldsOutOfOrder=N

                [SESSION]
                BeginString=%s
                SenderCompID=TEST_SERVER
                TargetCompID=FLASHTRADE
                SocketAcceptPort=%d
                """.formatted(config.fixVersion(), config.port());
    }
}
