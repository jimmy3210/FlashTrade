package com.tradex.flashtrade.fix.adapter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tradex.flashtrade.fix.model.FixExecutionReport;
import com.tradex.flashtrade.fix.model.FixInternalOrder;
import com.tradex.flashtrade.fix.model.FixMarketDataEntry;
import com.tradex.flashtrade.fix.model.FixOrderStatus;

import quickfix.FieldMap;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.HandlInst;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MarketDepth;
import quickfix.field.NoMDEntries;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;
import quickfix.fix42.OrderStatusRequest;

/**
 * FIX 4.2 メッセージアダプター
 *
 * FIX 4.2 固有のフィールドマッピングを実装する。
 * ※ TradeCaptureReport / PositionReport は FIX 4.2 では未定義のため、
 *    それらを必要とするメソッドは UnsupportedOperationException をスローする。
 */
public class Fix42MessageAdapter implements FixMessageAdapter {

    private static final Logger log = LoggerFactory.getLogger(Fix42MessageAdapter.class);

    @Override
    public String getSupportedVersion() {
        return "FIX.4.2";
    }

    // =========================================================================
    // 注文系メッセージ
    // =========================================================================

    @Override
    public Message toFixNewOrderSingle(FixInternalOrder order, SessionID sessionId) {
        NewOrderSingle msg = new NewOrderSingle(
                new ClOrdID(order.clOrdId()),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION),
                new Symbol(order.symbol()),
                new Side(order.side()),
                new TransactTime(),
                new OrdType(order.orderType())
        );

        msg.set(new OrderQty(order.quantity().doubleValue()));
        msg.set(new TimeInForce(order.timeInForce()));

        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }
        if (order.price() != null && order.orderType() == OrdType.LIMIT) {
            msg.set(new Price(order.price().doubleValue()));
        }

        log.debug("FIX 4.2 NewOrderSingle 生成: clOrdId={}, symbol={}", order.clOrdId(), order.symbol());
        return msg;
    }

    @Override
    public Message toFixCancelRequest(FixInternalOrder order, SessionID sessionId) {
        OrderCancelRequest msg = new OrderCancelRequest(
                new OrigClOrdID(order.origClOrdId()),
                new ClOrdID(order.clOrdId()),
                new Symbol(order.symbol()),
                new Side(order.side()),
                new TransactTime()
        );

        msg.set(new OrderQty(order.quantity().doubleValue()));
        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }

        log.debug("FIX 4.2 OrderCancelRequest 生成: clOrdId={}, origClOrdId={}", order.clOrdId(), order.origClOrdId());
        return msg;
    }

    @Override
    public Message toFixCancelReplaceRequest(FixInternalOrder order, SessionID sessionId) {
        OrderCancelReplaceRequest msg = new OrderCancelReplaceRequest(
                new OrigClOrdID(order.origClOrdId()),
                new ClOrdID(order.clOrdId()),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION),
                new Symbol(order.symbol()),
                new Side(order.side()),
                new TransactTime(),
                new OrdType(order.orderType())
        );

        msg.set(new OrderQty(order.quantity().doubleValue()));
        msg.set(new TimeInForce(order.timeInForce()));

        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }
        if (order.price() != null && order.orderType() == OrdType.LIMIT) {
            msg.set(new Price(order.price().doubleValue()));
        }

        log.debug("FIX 4.2 OrderCancelReplaceRequest 生成: clOrdId={}, origClOrdId={}", order.clOrdId(), order.origClOrdId());
        return msg;
    }

    @Override
    public Message toOrderStatusRequest(String clOrdId, String orderId, String symbol,
                                        char side, String accountId, SessionID sessionId) {
        OrderStatusRequest msg = new OrderStatusRequest();
        msg.set(new ClOrdID(clOrdId));
        if (symbol != null && !symbol.isBlank()) {
            msg.set(new Symbol(symbol));
        }
        msg.set(new Side(side));

        if (orderId != null && !orderId.equals("NONE")) {
            msg.set(new OrderID(orderId));
        }
        if (accountId != null && !accountId.isBlank()) {
            msg.set(new Account(accountId));
        }

        log.debug("FIX 4.2 OrderStatusRequest 生成: clOrdId={}", clOrdId);
        return msg;
    }

    // =========================================================================
    // ExecutionReport
    // =========================================================================

    @Override
    public FixExecutionReport toExecutionReport(Message message, SessionID sessionId) {
        try {
            ExecutionReport execReport = (ExecutionReport) message;

            String execId       = execReport.getExecID().getValue();
            String clOrdId      = execReport.getClOrdID().getValue();
            String orderId      = execReport.getOrderID().getValue();
            String symbol       = execReport.getSymbol().getValue();
            char   side         = execReport.getSide().getValue();
            char   ordStatus    = execReport.getOrdStatus().getValue();
            char   execType     = execReport.getExecType().getValue();

            BigDecimal ordQty   = safeGetDecimal(message, OrderQty.FIELD);
            BigDecimal cumQty   = safeGetDecimal(message, CumQty.FIELD);
            BigDecimal leavesQty = safeGetDecimal(message, LeavesQty.FIELD);
            // FIX 4.2 は LastShares (Tag 32)
            BigDecimal lastQty  = safeGetDecimal(message, LastShares.FIELD);
            BigDecimal lastPx   = safeGetDecimal(message, LastPx.FIELD);
            BigDecimal avgPx    = safeGetDecimal(message, AvgPx.FIELD);

            String account = safeGetString(message, Account.FIELD);
            String text    = safeGetString(message, Text.FIELD);

            return new FixExecutionReport(
                    execId, clOrdId, orderId, symbol, side,
                    FixOrderStatus.fromFixValue(ordStatus), execType,
                    ordQty, cumQty, leavesQty, lastQty, lastPx, avgPx,
                    account, text, "FIX.4.2", Instant.now()
            );
        } catch (Exception e) {
            log.error("FIX 4.2 ExecutionReport 変換エラー: {}", e.getMessage(), e);
            throw new RuntimeException("ExecutionReport 変換失敗", e);
        }
    }

    // =========================================================================
    // 行情（MarketData）
    // =========================================================================

    @Override
    public Message toMarketDataRequest(List<String> symbols, String reqId,
                                       char subscriptionType, SessionID sessionId) {
        MarketDataRequest msg = new MarketDataRequest(
            new MDReqID(reqId),
            new SubscriptionRequestType(subscriptionType),
            new MarketDepth(1)   // トップ of ブック
        );

        // 要求エントリタイプ: Bid + Offer + Trade
        MarketDataRequest.NoMDEntryTypes bidGroup = new MarketDataRequest.NoMDEntryTypes();
        bidGroup.set(new MDEntryType(MDEntryType.BID));
        msg.addGroup(bidGroup);

        MarketDataRequest.NoMDEntryTypes offerGroup = new MarketDataRequest.NoMDEntryTypes();
        offerGroup.set(new MDEntryType(MDEntryType.OFFER));
        msg.addGroup(offerGroup);

        MarketDataRequest.NoMDEntryTypes tradeGroup = new MarketDataRequest.NoMDEntryTypes();
        tradeGroup.set(new MDEntryType(MDEntryType.TRADE));
        msg.addGroup(tradeGroup);

        // 対象銘柄
        for (String symbol : symbols) {
            MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
            symGroup.set(new Symbol(symbol));
            msg.addGroup(symGroup);
        }

        log.debug("FIX 4.2 MarketDataRequest 生成: reqId={}, symbols={}", reqId, symbols);
        return msg;
    }

    @Override
    public List<FixMarketDataEntry> parseMarketDataMessage(Message message, SessionID sessionId) {
        List<FixMarketDataEntry> entries = new ArrayList<>();
        try {
            // 銘柄は W メッセージのヘッダーに含まれる（Tag 55）
            String symbol = safeGetString(message, Symbol.FIELD);
            if (symbol == null) symbol = "UNKNOWN";

            int noMDEntries = 0;
            try {
                noMDEntries = message.getInt(NoMDEntries.FIELD);
            } catch (quickfix.FieldNotFound e) {
                log.warn("FIX 4.2 MarketData: NoMDEntries フィールドなし");
                return entries;
            }

            for (int i = 1; i <= noMDEntries; i++) {
                try {
                    quickfix.Group group = new quickfix.Group(NoMDEntries.FIELD, MDEntryType.FIELD);
                    message.getGroup(i, group);

                    char entryType = group.getChar(MDEntryType.FIELD);
                    BigDecimal price = safeGetDecimal(group, MDEntryPx.FIELD);
                    BigDecimal size  = safeGetDecimal(group, MDEntrySize.FIELD);

                    entries.add(new FixMarketDataEntry(symbol, entryType, price, size, "FIX.4.2", Instant.now()));
                } catch (quickfix.FieldNotFound | quickfix.FieldException e) {
                    log.warn("FIX 4.2 MarketData エントリ {} 解析エラー: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("FIX 4.2 MarketData メッセージ解析エラー: {}", e.getMessage(), e);
        }
        return entries;
    }

    // =========================================================================
    // ユーティリティ
    // =========================================================================

    private BigDecimal safeGetDecimal(FieldMap msg, int tag) {
        try {
            return BigDecimal.valueOf(msg.getDouble(tag));
        } catch (quickfix.FieldNotFound e) {
            return BigDecimal.ZERO;
        }
    }

    private String safeGetString(FieldMap msg, int tag) {
        try {
            return msg.getString(tag);
        } catch (quickfix.FieldNotFound e) {
            return null;
        }
    }
}
