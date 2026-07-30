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
import com.tradex.flashtrade.fix.model.FixPositionReport;
import com.tradex.flashtrade.fix.model.FixTradeReport;

import quickfix.FieldMap;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.Account;
import quickfix.field.AccountType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.ClearingBusinessDate;
import quickfix.field.Commission;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.HandlInst;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.LongQty;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MarketDepth;
import quickfix.field.NoMDEntries;
import quickfix.field.NoPositions;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.PosMaintRptID;
import quickfix.field.PosReqID;
import quickfix.field.PosReqType;
import quickfix.field.PosType;
import quickfix.field.Price;
import quickfix.field.ShortQty;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TotalNumPosReports;
import quickfix.field.TradeReportID;
import quickfix.field.TradeRequestID;
import quickfix.field.TradeRequestType;
import quickfix.field.TransactTime;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.MarketDataRequest;
import quickfix.fix50sp2.NewOrderSingle;
import quickfix.fix50sp2.OrderCancelReplaceRequest;
import quickfix.fix50sp2.OrderCancelRequest;
import quickfix.fix50sp2.OrderStatusRequest;
import quickfix.fix50sp2.RequestForPositions;
import quickfix.fix50sp2.TradeCaptureReportRequest;

/**
 * FIX 5.0 SP2 メッセージアダプター（FIXT.1.1 トランスポート）
 *
 * FIX 5.0 では BeginString=FIXT.1.1 を使用し、
 * ApplVerID (Tag 1128) でアプリケーションバージョンを指定する。
 * Fix44MessageAdapter と同等の機能を FIX 5.0 SP2 クラスで実装。
 */
public class Fix50MessageAdapter implements FixMessageAdapter {

    private static final Logger log = LoggerFactory.getLogger(Fix50MessageAdapter.class);

    @Override
    public String getSupportedVersion() {
        return "FIX.5.0SP2";
    }

    // =========================================================================
    // 注文系メッセージ
    // =========================================================================

    @Override
    public Message toFixNewOrderSingle(FixInternalOrder order, SessionID sessionId) {
        NewOrderSingle msg = new NewOrderSingle(
                new ClOrdID(order.clOrdId()),
                new Side(order.side()),
                new TransactTime(),
                new OrdType(order.orderType())
        );

        msg.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION));
        msg.set(new Symbol(order.symbol()));
        msg.set(new OrderQty(order.quantity().doubleValue()));
        msg.set(new TimeInForce(order.timeInForce()));

        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }
        if (order.price() != null && order.orderType() == OrdType.LIMIT) {
            msg.set(new Price(order.price().doubleValue()));
        }

        log.debug("FIX 5.0 NewOrderSingle 生成: clOrdId={}, symbol={}", order.clOrdId(), order.symbol());
        return msg;
    }

    @Override
    public Message toFixCancelRequest(FixInternalOrder order, SessionID sessionId) {
        OrderCancelRequest msg = new OrderCancelRequest();
        msg.set(new OrigClOrdID(order.origClOrdId()));
        msg.set(new ClOrdID(order.clOrdId()));
        msg.set(new Symbol(order.symbol()));
        msg.set(new Side(order.side()));
        msg.set(new TransactTime());
        msg.set(new OrderQty(order.quantity().doubleValue()));

        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }

        log.debug("FIX 5.0 OrderCancelRequest 生成: clOrdId={}", order.clOrdId());
        return msg;
    }

    @Override
    public Message toFixCancelReplaceRequest(FixInternalOrder order, SessionID sessionId) {
        OrderCancelReplaceRequest msg = new OrderCancelReplaceRequest();
        msg.set(new OrigClOrdID(order.origClOrdId()));
        msg.set(new ClOrdID(order.clOrdId()));
        msg.set(new Symbol(order.symbol()));
        msg.set(new Side(order.side()));
        msg.set(new OrdType(order.orderType()));
        msg.set(new TransactTime());
        msg.set(new OrderQty(order.quantity().doubleValue()));
        msg.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION));
        msg.set(new TimeInForce(order.timeInForce()));

        if (order.account() != null && !order.account().isBlank()) {
            msg.set(new Account(order.account()));
        }
        if (order.price() != null && order.orderType() == OrdType.LIMIT) {
            msg.set(new Price(order.price().doubleValue()));
        }

        log.debug("FIX 5.0 OrderCancelReplaceRequest 生成: clOrdId={}", order.clOrdId());
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

        log.debug("FIX 5.0 OrderStatusRequest 生成: clOrdId={}", clOrdId);
        return msg;
    }

    // =========================================================================
    // ExecutionReport
    // =========================================================================

    @Override
    public FixExecutionReport toExecutionReport(Message message, SessionID sessionId) {
        try {
            ExecutionReport execReport = (ExecutionReport) message;

            String execId    = execReport.getExecID().getValue();
            String clOrdId   = execReport.getClOrdID().getValue();
            String orderId   = execReport.getOrderID().getValue();
            String symbol    = execReport.getSymbol().getValue();
            char   side      = execReport.getSide().getValue();
            char   ordStatus = execReport.getOrdStatus().getValue();
            char   execType  = execReport.getExecType().getValue();

            BigDecimal ordQty    = safeGetDecimal(message, OrderQty.FIELD);
            BigDecimal cumQty    = safeGetDecimal(message, CumQty.FIELD);
            BigDecimal leavesQty = safeGetDecimal(message, LeavesQty.FIELD);
            BigDecimal lastQty   = safeGetDecimal(message, LastQty.FIELD);
            BigDecimal lastPx    = safeGetDecimal(message, LastPx.FIELD);
            BigDecimal avgPx     = safeGetDecimal(message, AvgPx.FIELD);

            String account = safeGetString(message, Account.FIELD);
            String text    = safeGetString(message, Text.FIELD);

            return new FixExecutionReport(
                    execId, clOrdId, orderId, symbol, side,
                    FixOrderStatus.fromFixValue(ordStatus), execType,
                    ordQty, cumQty, leavesQty, lastQty, lastPx, avgPx,
                    account, text, "FIX.5.0SP2", Instant.now()
            );
        } catch (Exception e) {
            log.error("FIX 5.0 ExecutionReport 変換エラー: {}", e.getMessage(), e);
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
            new MarketDepth(1)
        );

        MarketDataRequest.NoMDEntryTypes bidGroup = new MarketDataRequest.NoMDEntryTypes();
        bidGroup.set(new MDEntryType(MDEntryType.BID));
        msg.addGroup(bidGroup);

        MarketDataRequest.NoMDEntryTypes offerGroup = new MarketDataRequest.NoMDEntryTypes();
        offerGroup.set(new MDEntryType(MDEntryType.OFFER));
        msg.addGroup(offerGroup);

        MarketDataRequest.NoMDEntryTypes tradeGroup = new MarketDataRequest.NoMDEntryTypes();
        tradeGroup.set(new MDEntryType(MDEntryType.TRADE));
        msg.addGroup(tradeGroup);

        for (String symbol : symbols) {
            MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
            symGroup.set(new Symbol(symbol));
            msg.addGroup(symGroup);
        }

        log.debug("FIX 5.0 MarketDataRequest 生成: reqId={}, symbols={}", reqId, symbols);
        return msg;
    }

    @Override
    public List<FixMarketDataEntry> parseMarketDataMessage(Message message, SessionID sessionId) {
        List<FixMarketDataEntry> entries = new ArrayList<>();
        try {
            String symbol = safeGetString(message, Symbol.FIELD);
            if (symbol == null) symbol = "UNKNOWN";

            int noMDEntries = safeGetInt(message, NoMDEntries.FIELD);
            for (int i = 1; i <= noMDEntries; i++) {
                try {
                    quickfix.Group group = new quickfix.Group(NoMDEntries.FIELD, MDEntryType.FIELD);
                    message.getGroup(i, group);

                    char entryType = group.getChar(MDEntryType.FIELD);
                    BigDecimal price = safeGetDecimal(group, MDEntryPx.FIELD);
                    BigDecimal size  = safeGetDecimal(group, MDEntrySize.FIELD);

                    entries.add(new FixMarketDataEntry(symbol, entryType, price, size, "FIX.5.0SP2", Instant.now()));
                } catch (Exception e) {
                    log.warn("FIX 5.0 MarketData エントリ {} 解析エラー: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("FIX 5.0 MarketData メッセージ解析エラー: {}", e.getMessage(), e);
        }
        return entries;
    }

    // =========================================================================
    // 成交（TradeCaptureReport）
    // =========================================================================

    @Override
    public Message toTradeCaptureReportRequest(String reqId, String accountId, SessionID sessionId) {
        TradeCaptureReportRequest msg = new TradeCaptureReportRequest(
                new TradeRequestID(reqId),
                new TradeRequestType(TradeRequestType.ALL_TRADES)
        );

        if (accountId != null && !accountId.isBlank()) {
            msg.setString(Account.FIELD, accountId);
        }

        log.debug("FIX 5.0 TradeCaptureReportRequest 生成: reqId={}", reqId);
        return msg;
    }

    @Override
    public FixTradeReport parseTradeCaptureReport(Message message, SessionID sessionId) {
        try {
            String tradeReportId = safeGetString(message, TradeReportID.FIELD);
            String execId        = safeGetString(message, ExecID.FIELD);
            String orderId       = safeGetString(message, OrderID.FIELD);
            String clOrdId       = safeGetString(message, ClOrdID.FIELD);
            String symbol        = safeGetString(message, Symbol.FIELD);
            int    sideInt       = safeGetInt(message, Side.FIELD);

            BigDecimal lastQty   = safeGetDecimal(message, LastQty.FIELD);
            BigDecimal lastPx    = safeGetDecimal(message, LastPx.FIELD);
            BigDecimal commission = safeGetDecimal(message, Commission.FIELD);
            String account       = safeGetString(message, Account.FIELD);

            return new FixTradeReport(
                    tradeReportId != null ? tradeReportId : "UNKNOWN",
                    execId != null ? execId : "UNKNOWN",
                    orderId != null ? orderId : "NONE",
                    clOrdId != null ? clOrdId : "UNKNOWN",
                    symbol != null ? symbol : "UNKNOWN",
                    (char) sideInt,
                    lastQty, lastPx, commission, account,
                    Instant.now(), "FIX.5.0SP2", Instant.now()
            );
        } catch (Exception e) {
            log.error("FIX 5.0 TradeCaptureReport 変換エラー: {}", e.getMessage(), e);
            throw new RuntimeException("TradeCaptureReport 変換失敗", e);
        }
    }

    // =========================================================================
    // 在仓（PositionReport）
    // =========================================================================

    @Override
    public Message toRequestForPositions(String reqId, String accountId, SessionID sessionId) {
        RequestForPositions msg = new RequestForPositions();
        msg.set(new PosReqID(reqId));
        msg.set(new PosReqType(PosReqType.POSITIONS));
        msg.set(new Account(accountId));
        msg.set(new AccountType(1));
        msg.set(new TransactTime());
        msg.set(new ClearingBusinessDate(
                new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date())));

        log.debug("FIX 5.0 RequestForPositions 生成: reqId={}, account={}", reqId, accountId);
        return msg;
    }

    @Override
    public FixPositionReport parsePositionReport(Message message, SessionID sessionId) {
        try {
            String posMaintRptId = safeGetString(message, PosMaintRptID.FIELD);
            String posReqId      = safeGetString(message, PosReqID.FIELD);
            String account       = safeGetString(message, Account.FIELD);
            String symbol        = safeGetString(message, Symbol.FIELD);
            int totalNumReports  = safeGetInt(message, TotalNumPosReports.FIELD);

            BigDecimal longQty  = BigDecimal.ZERO;
            BigDecimal shortQty = BigDecimal.ZERO;

            int noPositions = safeGetInt(message, NoPositions.FIELD);
            for (int i = 1; i <= noPositions; i++) {
                try {
                    quickfix.Group posGroup = new quickfix.Group(NoPositions.FIELD, PosType.FIELD);
                    message.getGroup(i, posGroup);
                    String posType = safeGetString(posGroup, PosType.FIELD);
                    if ("TOT".equals(posType) || "NET".equals(posType)) {
                        longQty  = safeGetDecimal(posGroup, LongQty.FIELD);
                        shortQty = safeGetDecimal(posGroup, ShortQty.FIELD);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("PositionGroup {} 解析スキップ: {}", i, e.getMessage());
                }
            }

            return new FixPositionReport(
                    posMaintRptId != null ? posMaintRptId : "UNKNOWN",
                    posReqId != null ? posReqId : "UNKNOWN",
                    account, symbol,
                    longQty, shortQty,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    totalNumReports,
                    "FIX.5.0SP2", Instant.now()
            );
        } catch (Exception e) {
            log.error("FIX 5.0 PositionReport 変換エラー: {}", e.getMessage(), e);
            throw new RuntimeException("PositionReport 変換失敗", e);
        }
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

    private int safeGetInt(FieldMap msg, int tag) {
        try {
            return msg.getInt(tag);
        } catch (quickfix.FieldNotFound e) {
            return 0;
        }
    }
}
