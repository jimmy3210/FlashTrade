package com.tradex.flashtrade.fix.broker;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.fix.adapter.FixMessageAdapter;
import com.tradex.flashtrade.fix.core.FixGatewayService;
import com.tradex.flashtrade.fix.core.FixSessionManager;
import com.tradex.flashtrade.fix.model.FixExecutionReport;
import com.tradex.flashtrade.fix.model.FixInternalOrder;
import com.tradex.flashtrade.fix.model.FixMarketDataEntry;
import com.tradex.flashtrade.fix.model.FixPositionReport;
import com.tradex.flashtrade.fix.model.FixTradeReport;
import com.tradex.flashtrade.fix.routing.FixMessageDispatcher;
import com.tradex.flashtrade.fix.routing.FixVersionRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * BrokerClient の FIX プロトコル実装（全機能版）
 *
 * 対応機能：
 *   発注 / キャンセル / 注文照会 / 注文一覧 / 成交一覧 / 行情 / 在仓照会
 *
 * FIX バージョン別対応：
 *   FIX.4.2       : 発注・キャンセル・修正・注文照会・行情
 *   FIX.4.4 / 5.0SP2: 上記 + TradeCaptureReport (成交) + PositionReport (在仓)
 */
public class FixBrokerClient implements
        BrokerClient,
        FixMessageDispatcher.ExecutionReportListener,
        FixMessageDispatcher.MarketDataListener,
        FixMessageDispatcher.TradeReportListener,
        FixMessageDispatcher.PositionReportListener {

    private static final Logger log = LoggerFactory.getLogger(FixBrokerClient.class);

    private static final int ORDER_TIMEOUT_SECONDS       = 30;
    private static final int MARKET_DATA_TIMEOUT_SECONDS = 10;
    private static final int POSITION_TIMEOUT_SECONDS    = 15;
    private static final int TRADE_TIMEOUT_SECONDS       = 15;

    private final String brokerName;
    private final String sessionKey;
    private final String fixVersion;
    private final SessionID sessionId;
    private final FixVersionRouter versionRouter;
    private final FixGatewayService gatewayService;

    // -------------------------------------------------------------------------
    // インメモリ状態管理
    // -------------------------------------------------------------------------

    /** clOrdId -> 現在の注文状態 */
    private final ConcurrentHashMap<String, BrokerOrder> orderStateByClOrdId = new ConcurrentHashMap<>();
    /** ブローカー OrderID (Tag 37) -> clOrdId マッピング */
    private final ConcurrentHashMap<String, String> orderIdToClOrdId = new ConcurrentHashMap<>();
    /** clOrdId -> 元の FixInternalOrder（キャンセル・修正時参照用） */
    private final ConcurrentHashMap<String, FixInternalOrder> sentOrders = new ConcurrentHashMap<>();
    /** 成交履歴（ExecutionReport 由来） */
    private final CopyOnWriteArrayList<BrokerTrade> tradeHistory = new CopyOnWriteArrayList<>();

    // -------------------------------------------------------------------------
    // 非同期 Future マップ
    // -------------------------------------------------------------------------

    /** clOrdId -> 初回 ACK/REJECTED 待ち（placeOrder 用） */
    private final ConcurrentHashMap<String, CompletableFuture<FixExecutionReport>> pendingOrderFutures
            = new ConcurrentHashMap<>();
    /** cancelClOrdId -> キャンセル回報待ち */
    private final ConcurrentHashMap<String, CompletableFuture<FixExecutionReport>> pendingCancelFutures
            = new ConcurrentHashMap<>();
    /** orderId -> 注文照会待ち */
    private final ConcurrentHashMap<String, CompletableFuture<FixExecutionReport>> pendingStatusFutures
            = new ConcurrentHashMap<>();
    /** MDReqID -> 行情リクエスト */
    private final ConcurrentHashMap<String, PendingMarketDataRequest> pendingMarketData
            = new ConcurrentHashMap<>();
    /** TradeRequestID -> 成交回報アキュムレータ */
    private final ConcurrentHashMap<String, ResponseAccumulator<FixTradeReport>> pendingTradeRequests
            = new ConcurrentHashMap<>();
    /** PosReqID -> 在仓回報アキュムレータ */
    private final ConcurrentHashMap<String, ResponseAccumulator<FixPositionReport>> pendingPositionRequests
            = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // コンストラクタ
    // -------------------------------------------------------------------------

    public FixBrokerClient(
            String brokerName,
            String sessionKey,
            String fixVersion,
            SessionID sessionId,
            FixVersionRouter versionRouter,
            FixGatewayService gatewayService,
            FixSessionManager sessionManager,
            FixMessageDispatcher dispatcher
    ) {
        this.brokerName    = brokerName;
        this.sessionKey    = sessionKey;
        this.fixVersion    = fixVersion;
        this.sessionId     = sessionId;
        this.versionRouter = versionRouter;
        this.gatewayService = gatewayService;

        dispatcher.addExecutionReportListener(this);
        dispatcher.addMarketDataListener(this);
        dispatcher.addTradeReportListener(this);
        dispatcher.addPositionReportListener(this);

        log.info("FixBrokerClient 初期化: broker={}, session={}, version={}", brokerName, sessionKey, fixVersion);
    }

    // =========================================================================
    // BrokerClient 実装
    // =========================================================================

    @Override
    public String getBrokerName() { return brokerName; }

    // ---------- 発注 ----------

    @Override
    public PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        String clOrdId = generateClOrdId();

        FixInternalOrder order = FixInternalOrder.newOrder(
                clOrdId, request.getSymbol(),
                toFixSide(request.getSide().name()),
                toFixOrdType(request.getOrderType().name()),
                BigDecimal.valueOf(request.getQuantity()),
                request.getLimitPrice(),
                TimeInForce.DAY,
                accountId, sessionKey, fixVersion
        );

        CompletableFuture<FixExecutionReport> future = new CompletableFuture<>();
        pendingOrderFutures.put(clOrdId, future);
        sentOrders.put(clOrdId, order);

        try {
            sendFixMessage(versionRouter.resolve(sessionId).toFixNewOrderSingle(order, sessionId));
            log.info("発注送信: broker={}, clOrdId={}, symbol={}", brokerName, clOrdId, request.getSymbol());

            FixExecutionReport report = future.get(ORDER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return toPlaceOrderResult(report);

        } catch (TimeoutException e) {
            pendingOrderFutures.remove(clOrdId);
            return PlaceOrderResult.builder().success(false).message("注文タイムアウト").build();
        } catch (Exception e) {
            pendingOrderFutures.remove(clOrdId);
            log.error("発注エラー: {}", e.getMessage(), e);
            return PlaceOrderResult.builder().success(false).message(e.getMessage()).build();
        }
    }

    // ---------- キャンセル ----------

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        String origClOrdId = orderIdToClOrdId.get(externalOrderId);
        if (origClOrdId == null) {
            throw new IllegalArgumentException("注文 ID 未発見: " + externalOrderId);
        }
        FixInternalOrder orig = sentOrders.get(origClOrdId);
        if (orig == null) {
            throw new IllegalStateException("元注文データなし: origClOrdId=" + origClOrdId);
        }

        String cancelClOrdId = generateClOrdId();
        FixInternalOrder cancelOrder = FixInternalOrder.cancelOrder(
                cancelClOrdId, origClOrdId,
                orig.symbol(), orig.side(), orig.quantity(),
                accountId, sessionKey, fixVersion
        );

        CompletableFuture<FixExecutionReport> future = new CompletableFuture<>();
        pendingCancelFutures.put(cancelClOrdId, future);
        sentOrders.put(cancelClOrdId, cancelOrder);

        try {
            sendFixMessage(versionRouter.resolve(sessionId).toFixCancelRequest(cancelOrder, sessionId));
            log.info("キャンセル送信: broker={}, cancelClOrdId={}", brokerName, cancelClOrdId);
            future.get(ORDER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingCancelFutures.remove(cancelClOrdId);
            throw new RuntimeException("キャンセルタイムアウト", e);
        } catch (Exception e) {
            pendingCancelFutures.remove(cancelClOrdId);
            throw new RuntimeException("キャンセルエラー: " + e.getMessage(), e);
        }
    }

    // ---------- 注文照会 ----------

    @Override
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        // キャッシュ確認
        String clOrdId = orderIdToClOrdId.get(externalOrderId);
        if (clOrdId != null) {
            BrokerOrder cached = orderStateByClOrdId.get(clOrdId);
            if (cached != null) return cached;
        }

        // OrderStatusRequest 送信
        String reqClOrdId = (clOrdId != null) ? clOrdId : generateClOrdId();
        FixInternalOrder orig = (clOrdId != null) ? sentOrders.get(clOrdId) : null;

        CompletableFuture<FixExecutionReport> future = new CompletableFuture<>();
        pendingStatusFutures.put(externalOrderId, future);

        try {
            sendFixMessage(versionRouter.resolve(sessionId).toOrderStatusRequest(
                    reqClOrdId, externalOrderId,
                    orig != null ? orig.symbol() : "",
                    orig != null ? orig.side() : Side.BUY,
                    accountId, sessionId));
            log.info("注文照会送信: broker={}, orderId={}", brokerName, externalOrderId);

            FixExecutionReport report = future.get(ORDER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            BrokerOrder result = toBrokerOrder(report);
            orderStateByClOrdId.put(report.clOrdId(), result);
            return result;
        } catch (TimeoutException e) {
            pendingStatusFutures.remove(externalOrderId);
            throw new RuntimeException("注文照会タイムアウト", e);
        } catch (Exception e) {
            pendingStatusFutures.remove(externalOrderId);
            throw new RuntimeException("注文照会エラー: " + e.getMessage(), e);
        }
    }

    // ---------- 注文一覧 ----------

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        return new ArrayList<>(orderStateByClOrdId.values());
    }

    // ---------- 成交一覧 ----------

    @Override
    public List<BrokerTrade> getTrades(String accountId) {
        // FIX 4.4+ は TradeCaptureReport でサーバー側成交を取得
        if (!"FIX.4.2".equals(fixVersion)) {
            String reqId = generateClOrdId();
            ResponseAccumulator<FixTradeReport> acc = new ResponseAccumulator<>();
            pendingTradeRequests.put(reqId, acc);
            try {
                sendFixMessage(versionRouter.resolve(sessionId).toTradeCaptureReportRequest(reqId, accountId, sessionId));
                List<FixTradeReport> reports = acc.future.get(TRADE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                return reports.stream().map(this::toApiBrokerTrade).collect(Collectors.toList());
            } catch (Exception e) {
                pendingTradeRequests.remove(reqId);
                log.warn("TradeCaptureReport 取得失敗: {} — ローカルキャッシュ返却", e.getMessage());
            }
        }
        return new ArrayList<>(tradeHistory);
    }

    // ---------- 行情 ----------

    @Override
    public Map<String, MarketData> getBatchMarketData(String accountId, List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) return new HashMap<>();

        String reqId = generateClOrdId();
        PendingMarketDataRequest pending = new PendingMarketDataRequest(Set.copyOf(symbols));
        pendingMarketData.put(reqId, pending);

        try {
            // '1' = Snapshot
            sendFixMessage(versionRouter.resolve(sessionId).toMarketDataRequest(symbols, reqId, '1', sessionId));
            log.info("MarketData リクエスト送信: broker={}, reqId={}, symbols={}", brokerName, reqId, symbols);
            return pending.future.get(MARKET_DATA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingMarketData.remove(reqId);
            log.warn("MarketData タイムアウト: {} / {} 銘柄受信済み", pending.received.size(), symbols.size());
            return new HashMap<>(pending.received);
        } catch (Exception e) {
            pendingMarketData.remove(reqId);
            log.error("MarketData エラー: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    // ---------- 在仓照会 ----------

    @Override
    public BrokerPortfolio getPortfolio(String accountId) {
        if ("FIX.4.2".equals(fixVersion)) {
            throw new UnsupportedOperationException(
                    brokerName + ": FIX 4.2 は PositionReport 未対応。REST API を使用してください。");
        }

        String reqId = generateClOrdId();
        ResponseAccumulator<FixPositionReport> acc = new ResponseAccumulator<>();
        pendingPositionRequests.put(reqId, acc);

        try {
            sendFixMessage(versionRouter.resolve(sessionId).toRequestForPositions(reqId, accountId, sessionId));
            log.info("RequestForPositions 送信: broker={}, reqId={}", brokerName, reqId);
            List<FixPositionReport> reports = acc.future.get(POSITION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return convertToBrokerPortfolio(accountId, reports);
        } catch (TimeoutException e) {
            pendingPositionRequests.remove(reqId);
            return convertToBrokerPortfolio(accountId, acc.getReceived());
        } catch (Exception e) {
            pendingPositionRequests.remove(reqId);
            throw new RuntimeException("在仓照会エラー: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Listener 実装
    // =========================================================================

    @Override
    public void onExecutionReport(FixExecutionReport report) {
        String clOrdId = report.clOrdId();
        String orderId = report.orderId();
        char execType  = report.execType();
        FixMessageAdapter adapter = versionRouter.resolve(sessionId);

        // OrderID マッピング登録
        if (orderId != null && !orderId.isBlank()) {
            orderIdToClOrdId.put(orderId, clOrdId);
        }

        // 注文状態更新
        orderStateByClOrdId.put(clOrdId, toBrokerOrder(report));

        // 約定時: 成交履歴に追加
        if (adapter.isFillExecType(execType)
                && report.lastQty() != null
                && report.lastQty().compareTo(BigDecimal.ZERO) > 0) {
            tradeHistory.add(BrokerTrade.builder()
                    .externalTradeId(report.execId())
                    .externalOrderId(orderId)
                    .symbol(report.symbol())
                    .side(sideCharToString(report.side()))
                    .quantity(report.lastQty())
                    .price(report.lastPx())
                    .tradedAt(LocalDateTime.ofInstant(report.receivedAt(), ZoneId.systemDefault()))
                    .build());
        }

        // Future 完了
        CompletableFuture<FixExecutionReport> sf = pendingStatusFutures.remove(orderId);
        if (sf != null) { sf.complete(report); return; }

        CompletableFuture<FixExecutionReport> cf = pendingCancelFutures.remove(clOrdId);
        if (cf != null) { cf.complete(report); return; }

        if (adapter.isInitialAckExecType(execType)) {
            CompletableFuture<FixExecutionReport> of = pendingOrderFutures.remove(clOrdId);
            if (of != null) of.complete(report);
        }
    }

    @Override
    public void onMarketData(String reqId, List<FixMarketDataEntry> entries) {
        PendingMarketDataRequest pending = pendingMarketData.get(reqId);
        if (pending == null || entries.isEmpty()) return;

        String symbol = entries.get(0).symbol();
        BigDecimal bid = null, ask = null, last = null;
        long ts = System.currentTimeMillis();
        for (FixMarketDataEntry e : entries) {
            if (e.isBid()   && bid  == null) bid  = e.price();
            if (e.isOffer() && ask  == null) ask  = e.price();
            if (e.isTrade() && last == null) last = e.price();
            ts = e.receivedAt().toEpochMilli();
        }
        MarketData data = MarketData.builder()
                .symbol(symbol).bidPrice(bid).askPrice(ask)
                .lastPrice(last != null ? last : (bid != null ? bid : ask))
                .timestamp(ts).build();

        pending.onReceive(symbol, data);
        if (!pending.future.isDone() && pending.received.keySet().containsAll(pending.expectedSymbols)) {
            pending.future.complete(new HashMap<>(pending.received));
            pendingMarketData.remove(reqId);
        }
    }

    @Override
    public void onTradeReport(FixTradeReport report) {
        for (ResponseAccumulator<FixTradeReport> acc : pendingTradeRequests.values()) {
            acc.add(report, 0);
        }
        tradeHistory.add(toApiBrokerTrade(report));
    }

    @Override
    public void onPositionReport(FixPositionReport report) {
        String posReqId = report.posReqId();
        if (posReqId == null) return;
        ResponseAccumulator<FixPositionReport> acc = pendingPositionRequests.get(posReqId);
        if (acc == null) return;
        acc.add(report, report.totalNumReports());
        if (acc.future.isDone()) pendingPositionRequests.remove(posReqId);
    }

    // =========================================================================
    // セッション制御
    // =========================================================================

    public void startSession()   { gatewayService.start(sessionKey); }
    public void stopSession()    { gatewayService.stop(sessionKey); }
    public boolean isConnected() { return gatewayService.isRunning(sessionKey); }

    // =========================================================================
    // プライベートヘルパー
    // =========================================================================

    private void sendFixMessage(Message message) throws SessionNotFound {
        if (!Session.sendToTarget(message, sessionId)) {
            throw new RuntimeException("FIX 送信失敗: セッション未接続: " + sessionId);
        }
    }

    private String generateClOrdId() {
        return "FT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private char toFixSide(String side) {
        return switch (side.toUpperCase()) {
            case "BUY"        -> Side.BUY;
            case "SELL"       -> Side.SELL;
            case "SELL_SHORT" -> Side.SELL_SHORT;
            default -> throw new IllegalArgumentException("未対応の売買区分: " + side);
        };
    }

    private char toFixOrdType(String orderType) {
        return switch (orderType.toUpperCase()) {
            case "MARKET" -> OrdType.MARKET;
            case "LIMIT"  -> OrdType.LIMIT;
            default -> throw new IllegalArgumentException("未対応の注文種別: " + orderType);
        };
    }

    private String sideCharToString(char side) {
        return switch (side) {
            case Side.BUY        -> "BUY";
            case Side.SELL       -> "SELL";
            case Side.SELL_SHORT -> "SELL_SHORT";
            default -> String.valueOf(side);
        };
    }

    private PlaceOrderResult toPlaceOrderResult(FixExecutionReport report) {
        return switch (report.orderStatus()) {
            case NEW, PARTIALLY_FILLED, FILLED -> PlaceOrderResult.builder()
                    .success(true).externalOrderId(report.orderId())
                    .message(report.orderStatus().name()).build();
            case REJECTED -> PlaceOrderResult.builder()
                    .success(false).message("注文拒否: " + report.text()).build();
            default -> PlaceOrderResult.builder()
                    .success(false).message("不明ステータス: " + report.orderStatus()).build();
        };
    }

    private BrokerOrder toBrokerOrder(FixExecutionReport report) {
        return BrokerOrder.builder()
                .externalOrderId(report.orderId())
                .symbol(report.symbol())
                .side(sideCharToString(report.side()))
                .quantity(report.ordQty())
                .price(report.avgPx() != null && report.avgPx().compareTo(BigDecimal.ZERO) > 0
                        ? report.avgPx() : report.lastPx())
                .status(report.orderStatus().name())
                .createdAt(LocalDateTime.ofInstant(report.receivedAt(), ZoneId.systemDefault()))
                .build();
    }

    private BrokerTrade toApiBrokerTrade(FixTradeReport report) {
        return BrokerTrade.builder()
                .externalTradeId(report.tradeReportId())
                .externalOrderId(report.orderId())
                .symbol(report.symbol())
                .side(sideCharToString(report.side()))
                .quantity(report.lastQty())
                .price(report.lastPx())
                .commission(report.commission())
                .tradedAt(LocalDateTime.ofInstant(report.tradeDate(), ZoneId.systemDefault()))
                .build();
    }

    private BrokerPortfolio convertToBrokerPortfolio(String accountId, List<FixPositionReport> reports) {
        List<BrokerPosition> positions = new ArrayList<>();
        for (FixPositionReport rpt : reports) {
            BigDecimal qty = rpt.netQty();
            if (qty.compareTo(BigDecimal.ZERO) == 0) continue;
            positions.add(BrokerPosition.builder().symbol(rpt.symbol()).quantity(qty).build());
        }
        return BrokerPortfolio.builder()
                .accountId(accountId).brokerName(brokerName)
                .totalMarketValue(BigDecimal.ZERO).totalUnrealizedPnl(BigDecimal.ZERO)
                .positions(positions).build();
    }

    // =========================================================================
    // 内部クラス
    // =========================================================================

    private static class PendingMarketDataRequest {
        final Set<String> expectedSymbols;
        final Map<String, MarketData> received = new ConcurrentHashMap<>();
        final CompletableFuture<Map<String, MarketData>> future = new CompletableFuture<>();

        PendingMarketDataRequest(Set<String> expectedSymbols) {
            this.expectedSymbols = expectedSymbols;
        }

        void onReceive(String symbol, MarketData data) { received.put(symbol, data); }
    }

    private static class ResponseAccumulator<T> {
        final CopyOnWriteArrayList<T> responses = new CopyOnWriteArrayList<>();
        volatile int expectedTotal = -1;
        final CompletableFuture<List<T>> future = new CompletableFuture<>();

        synchronized void add(T response, int totalNumReports) {
            if (future.isDone()) return;
            responses.add(response);
            if (totalNumReports > 0) expectedTotal = totalNumReports;
            if (expectedTotal > 0 && responses.size() >= expectedTotal) {
                future.complete(new ArrayList<>(responses));
            }
        }

        List<T> getReceived() { return new ArrayList<>(responses); }
    }
}
