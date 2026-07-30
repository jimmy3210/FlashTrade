package com.tradex.flashtrade.fix.adapter;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.fix.model.FixExecutionReport;
import com.tradex.flashtrade.fix.model.FixInternalOrder;
import com.tradex.flashtrade.fix.model.FixMarketDataEntry;
import com.tradex.flashtrade.fix.model.FixPositionReport;
import com.tradex.flashtrade.fix.model.FixTradeReport;
import quickfix.Message;
import quickfix.SessionID;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * FIX バージョン別メッセージ変換統一インタフェース
 *
 * 各 FIX バージョン（4.2 / 4.4 / 5.0 SP2）がこのインタフェースを実装し、
 * バージョン固有のフィールド操作を隠蔽する。
 *
 * デフォルト実装:
 *   - FIX 4.2 対応外のメソッド（position, trade capture）は UnsupportedOperationException をスロー
 *   - 全バージョン共通の変換ユーティリティは default メソッドで提供
 */
public interface FixMessageAdapter {

    // =========================================================================
    // バージョン識別
    // =========================================================================

    /**
     * このアダプターが対応する FIX バージョンを返す
     * @return "FIX.4.2", "FIX.4.4", "FIX.5.0SP2" 等
     */
    String getSupportedVersion();

    // =========================================================================
    // 注文系メッセージ送信（全バージョン共通）
    // =========================================================================

    /**
     * 内部注文モデル → NewOrderSingle (MsgType=D) への変換
     */
    Message toFixNewOrderSingle(FixInternalOrder order, SessionID sessionId);

    /**
     * 内部注文モデル → OrderCancelRequest (MsgType=F) への変換
     */
    Message toFixCancelRequest(FixInternalOrder order, SessionID sessionId);

    /**
     * 内部注文モデル → OrderCancelReplaceRequest (MsgType=G) への変換
     * （注文修正: 数量・価格の変更）
     */
    Message toFixCancelReplaceRequest(FixInternalOrder order, SessionID sessionId);

    /**
     * OrderStatusRequest (MsgType=H) の生成
     * ExecType=I (Order Status) の ExecutionReport が返ってくる
     *
     * @param clOrdId   クライアント注文 ID（Tag 11）
     * @param orderId   ブローカー注文 ID（Tag 37, 不明の場合 "NONE"）
     * @param symbol    銘柄コード（Tag 55）
     * @param side      売買区分（Tag 54）
     * @param accountId 口座 ID（Tag 1）
     * @param sessionId セッション ID
     */
    Message toOrderStatusRequest(String clOrdId, String orderId, String symbol,
                                 char side, String accountId, SessionID sessionId);

    // =========================================================================
    // 注文系メッセージ受信
    // =========================================================================

    /**
     * ExecutionReport (MsgType=8) → 内部約定回報モデルへの変換
     */
    FixExecutionReport toExecutionReport(Message message, SessionID sessionId);

    /**
     * ExecutionReport → BrokerOrder への変換（デフォルト実装あり）
     */
    default BrokerOrder toBrokerOrder(FixExecutionReport report) {
        return BrokerOrder.builder()
                .externalOrderId(report.orderId())
                .symbol(report.symbol())
                .side(report.side() == '1' ? "BUY" : "SELL")
                .quantity(report.ordQty())
                .price(report.avgPx() != null && report.avgPx().signum() > 0
                        ? report.avgPx() : report.leavesQty())
                .status(report.orderStatus().name())
                .createdAt(LocalDateTime.ofInstant(report.receivedAt(), ZoneOffset.UTC))
                .build();
    }

    // =========================================================================
    // 行情（MarketData）メッセージ（全バージョン共通）
    // =========================================================================

    /**
     * MarketDataRequest (MsgType=V) の生成
     *
     * @param symbols          銘柄コードリスト
     * @param reqId            MDReqID (Tag 262)
     * @param subscriptionType '0'=snapshot, '1'=subscribe, '2'=unsubscribe
     * @param sessionId        セッション ID
     */
    Message toMarketDataRequest(List<String> symbols, String reqId,
                                char subscriptionType, SessionID sessionId);

    /**
     * MarketDataSnapshotFullRefresh (MsgType=W) または
     * MarketDataIncrementalRefresh (MsgType=X) → FixMarketDataEntry リストへの変換
     *
     * @param message   受信した MarketData メッセージ
     * @param sessionId セッション ID
     * @return 銘柄ごとのエントリリスト
     */
    List<FixMarketDataEntry> parseMarketDataMessage(Message message, SessionID sessionId);

    /**
     * 受信メッセージから MDReqID (Tag 262) を取得
     */
    default String extractMdReqId(Message message) {
        try {
            return message.getString(quickfix.field.MDReqID.FIELD);
        } catch (quickfix.FieldNotFound e) {
            return "UNKNOWN";
        }
    }

    // =========================================================================
    // 成交（TradeCaptureReport）メッセージ（FIX 4.4+ のみ）
    // =========================================================================

    /**
     * TradeCaptureReportRequest (MsgType=AD) の生成（FIX 4.4+ のみ）
     *
     * @param reqId     TradeRequestID (Tag 568)
     * @param accountId 口座 ID（Tag 1）
     * @param sessionId セッション ID
     */
    default Message toTradeCaptureReportRequest(String reqId, String accountId, SessionID sessionId) {
        throw new UnsupportedOperationException(
                getSupportedVersion() + ": TradeCaptureReportRequest は FIX 4.4 以降でのみ使用可能");
    }

    /**
     * TradeCaptureReport (MsgType=AE) → FixTradeReport への変換（FIX 4.4+ のみ）
     */
    default FixTradeReport parseTradeCaptureReport(Message message, SessionID sessionId) {
        throw new UnsupportedOperationException(
                getSupportedVersion() + ": TradeCaptureReport は FIX 4.4 以降でのみ使用可能");
    }

    /**
     * FixTradeReport → BrokerTrade への変換（デフォルト実装あり）
     */
    default BrokerTrade toBrokerTrade(FixTradeReport report) {
        return BrokerTrade.builder()
                .externalTradeId(report.tradeReportId())
                .externalOrderId(report.orderId())
                .symbol(report.symbol())
                .side(report.sideAsString())
                .quantity(report.lastQty())
                .price(report.lastPx())
                .commission(report.commission())
                .tradedAt(LocalDateTime.ofInstant(report.tradeDate(), ZoneOffset.UTC))
                .build();
    }

    // =========================================================================
    // 在仓（Position）メッセージ（FIX 4.4+ のみ）
    // =========================================================================

    /**
     * RequestForPositions (MsgType=AN) の生成（FIX 4.4+ のみ）
     *
     * @param reqId     PosReqID (Tag 710)
     * @param accountId 口座 ID（Tag 1）
     * @param sessionId セッション ID
     */
    default Message toRequestForPositions(String reqId, String accountId, SessionID sessionId) {
        throw new UnsupportedOperationException(
                getSupportedVersion() + ": RequestForPositions は FIX 4.4 以降でのみ使用可能");
    }

    /**
     * PositionReport (MsgType=AP) → FixPositionReport への変換（FIX 4.4+ のみ）
     */
    default FixPositionReport parsePositionReport(Message message, SessionID sessionId) {
        throw new UnsupportedOperationException(
                getSupportedVersion() + ": PositionReport は FIX 4.4 以降でのみ使用可能");
    }

    // =========================================================================
    // ユーティリティ（デフォルト実装）
    // =========================================================================

    /**
     * 受信メッセージから ClOrdID (Tag 11) を安全に取得
     */
    default String extractClOrdId(Message message) {
        try {
            return message.getString(quickfix.field.ClOrdID.FIELD);
        } catch (quickfix.FieldNotFound e) {
            return "UNKNOWN";
        }
    }

    /**
     * 実行タイプが約定（fill）か判定
     * FIX 4.2: '1'=PARTIAL_FILL, '2'=FILL
     * FIX 4.4+: 'F'=TRADE
     */
    default boolean isFillExecType(char execType) {
        return execType == '1' || execType == '2' || execType == 'F';
    }

    /**
     * 実行タイプが初期 ACK（NEW / REJECTED / PENDING_NEW）か判定
     */
    default boolean isInitialAckExecType(char execType) {
        return execType == '0' || execType == '8' || execType == 'A';
    }
}
