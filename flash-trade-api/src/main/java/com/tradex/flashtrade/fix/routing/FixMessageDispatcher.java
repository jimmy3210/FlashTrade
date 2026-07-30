package com.tradex.flashtrade.fix.routing;

import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.fix.model.FixExecutionReport;
import com.tradex.flashtrade.fix.model.FixMarketDataEntry;
import com.tradex.flashtrade.fix.model.FixPositionReport;
import com.tradex.flashtrade.fix.model.FixTradeReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FIX メッセージディスパッチャー
 *
 * FIX Gateway から内部サービス層への通知を担当する。
 * リスナーパターンにより、複数のサービスが FIX メッセージを受信できる。
 *
 * サポートするリスナー:
 * - ExecutionReportListener : 約定回報（注文確認・約定・キャンセル）
 * - MarketDataListener      : 行情データ（Bid / Ask / Trade）
 * - TradeReportListener     : 成交回報（TradeCaptureReport, FIX 4.4+）
 * - PositionReportListener  : 在仓回報（PositionReport, FIX 4.4+）
 * - SessionEventListener    : セッション接続・切断イベント
 */
public class FixMessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(FixMessageDispatcher.class);

    private final List<ExecutionReportListener> executionReportListeners = new CopyOnWriteArrayList<>();
    private final List<MarketDataListener>      marketDataListeners      = new CopyOnWriteArrayList<>();
    private final List<TradeReportListener>     tradeReportListeners     = new CopyOnWriteArrayList<>();
    private final List<PositionReportListener>  positionReportListeners  = new CopyOnWriteArrayList<>();
    private final List<SessionEventListener>    sessionEventListeners    = new CopyOnWriteArrayList<>();

    // -------------------------------------------------------------------------
    // リスナー登録 / 解除
    // -------------------------------------------------------------------------

    public void addExecutionReportListener(ExecutionReportListener l) {
        executionReportListeners.add(l);
        log.info("ExecutionReport リスナー登録: {}", l.getClass().getSimpleName());
    }
    public void removeExecutionReportListener(ExecutionReportListener l) { executionReportListeners.remove(l); }

    public void addMarketDataListener(MarketDataListener l) {
        marketDataListeners.add(l);
        log.info("MarketData リスナー登録: {}", l.getClass().getSimpleName());
    }
    public void removeMarketDataListener(MarketDataListener l) { marketDataListeners.remove(l); }

    public void addTradeReportListener(TradeReportListener l) {
        tradeReportListeners.add(l);
        log.info("TradeReport リスナー登録: {}", l.getClass().getSimpleName());
    }
    public void removeTradeReportListener(TradeReportListener l) { tradeReportListeners.remove(l); }

    public void addPositionReportListener(PositionReportListener l) {
        positionReportListeners.add(l);
        log.info("PositionReport リスナー登録: {}", l.getClass().getSimpleName());
    }
    public void removePositionReportListener(PositionReportListener l) { positionReportListeners.remove(l); }

    public void addSessionEventListener(SessionEventListener l)    { sessionEventListeners.add(l); }
    public void removeSessionEventListener(SessionEventListener l) { sessionEventListeners.remove(l); }

    // -------------------------------------------------------------------------
    // ディスパッチメソッド
    // -------------------------------------------------------------------------

    public void dispatchExecutionReport(FixExecutionReport report) {
        log.info("ExecutionReport dispatch: clOrdId={}, status={}", report.clOrdId(), report.orderStatus());
        for (ExecutionReportListener l : executionReportListeners) {
            try { l.onExecutionReport(report); }
            catch (Exception e) { log.error("ExecutionReport リスナーエラー [{}]: {}", l.getClass().getSimpleName(), e.getMessage(), e); }
        }
    }

    /**
     * MarketData エントリを各リスナーにディスパッチする
     *
     * @param reqId   MDReqID（Tag 262）
     * @param entries 行情エントリリスト（1 銘柄分）
     */
    public void dispatchMarketData(String reqId, List<FixMarketDataEntry> entries) {
        if (entries.isEmpty()) return;
        String symbol = entries.get(0).symbol();
        log.debug("MarketData dispatch: reqId={}, symbol={}, entries={}", reqId, symbol, entries.size());
        for (MarketDataListener l : marketDataListeners) {
            try { l.onMarketData(reqId, entries); }
            catch (Exception e) { log.error("MarketData リスナーエラー [{}]: {}", l.getClass().getSimpleName(), e.getMessage(), e); }
        }
    }

    public void dispatchTradeReport(FixTradeReport report) {
        log.info("TradeReport dispatch: tradeReportId={}, symbol={}", report.tradeReportId(), report.symbol());
        for (TradeReportListener l : tradeReportListeners) {
            try { l.onTradeReport(report); }
            catch (Exception e) { log.error("TradeReport リスナーエラー [{}]: {}", l.getClass().getSimpleName(), e.getMessage(), e); }
        }
    }

    public void dispatchPositionReport(FixPositionReport report) {
        log.info("PositionReport dispatch: posReqId={}, symbol={}", report.posReqId(), report.symbol());
        for (PositionReportListener l : positionReportListeners) {
            try { l.onPositionReport(report); }
            catch (Exception e) { log.error("PositionReport リスナーエラー [{}]: {}", l.getClass().getSimpleName(), e.getMessage(), e); }
        }
    }

    public void dispatchSessionLogon(SessionID sessionId) {
        log.info("セッション Logon: {}", sessionId);
        for (SessionEventListener l : sessionEventListeners) {
            try { l.onSessionLogon(sessionId); }
            catch (Exception e) { log.error("SessionEvent リスナーエラー [Logon]: {}", e.getMessage(), e); }
        }
    }

    public void dispatchSessionLogout(SessionID sessionId) {
        log.warn("セッション Logout: {}", sessionId);
        for (SessionEventListener l : sessionEventListeners) {
            try { l.onSessionLogout(sessionId); }
            catch (Exception e) { log.error("SessionEvent リスナーエラー [Logout]: {}", e.getMessage(), e); }
        }
    }

    // -------------------------------------------------------------------------
    // リスナーインタフェース
    // -------------------------------------------------------------------------

    /** FIX ExecutionReport 受信リスナー（注文確認・約定・キャンセル） */
    public interface ExecutionReportListener {
        void onExecutionReport(FixExecutionReport report);
    }

    /**
     * FIX MarketData 受信リスナー（行情）
     * MarketDataSnapshotFullRefresh (W) および MarketDataIncrementalRefresh (X) の両方に対応。
     */
    public interface MarketDataListener {
        /**
         * @param reqId   MDReqID（リクエストと紐付けるため使用）
         * @param entries 受信した MarketData エントリリスト（1 銘柄分）
         */
        void onMarketData(String reqId, List<FixMarketDataEntry> entries);
    }

    /** FIX TradeCaptureReport 受信リスナー（成交, FIX 4.4+） */
    public interface TradeReportListener {
        void onTradeReport(FixTradeReport report);
    }

    /** FIX PositionReport 受信リスナー（在仓, FIX 4.4+） */
    public interface PositionReportListener {
        void onPositionReport(FixPositionReport report);
    }

    /** FIX セッションイベントリスナー */
    public interface SessionEventListener {
        void onSessionLogon(SessionID sessionId);
        void onSessionLogout(SessionID sessionId);
    }
}
