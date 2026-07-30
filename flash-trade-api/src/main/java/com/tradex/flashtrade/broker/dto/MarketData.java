package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;

/** 行情快照：单只股票的实时价格数据 */
public class MarketData {

    private String     symbol;
    private BigDecimal lastPrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private long       timestamp;   // 毫秒时间戳

    public MarketData() {}

    private MarketData(Builder b) {
        this.symbol    = b.symbol;
        this.lastPrice = b.lastPrice;
        this.bidPrice  = b.bidPrice;
        this.askPrice  = b.askPrice;
        this.timestamp = b.timestamp;
    }

    public static Builder builder() { return new Builder(); }

    public String     getSymbol()    { return symbol; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public BigDecimal getBidPrice()  { return bidPrice; }
    public BigDecimal getAskPrice()  { return askPrice; }
    public long       getTimestamp() { return timestamp; }

    public void setSymbol(String symbol)       { this.symbol = symbol; }
    public void setLastPrice(BigDecimal v)     { this.lastPrice = v; }
    public void setBidPrice(BigDecimal v)      { this.bidPrice = v; }
    public void setAskPrice(BigDecimal v)      { this.askPrice = v; }
    public void setTimestamp(long timestamp)   { this.timestamp = timestamp; }

    public static class Builder {
        private String     symbol;
        private BigDecimal lastPrice;
        private BigDecimal bidPrice;
        private BigDecimal askPrice;
        private long       timestamp = System.currentTimeMillis();

        public Builder symbol(String v)       { this.symbol = v;    return this; }
        public Builder lastPrice(BigDecimal v){ this.lastPrice = v; return this; }
        public Builder bidPrice(BigDecimal v) { this.bidPrice = v;  return this; }
        public Builder askPrice(BigDecimal v) { this.askPrice = v;  return this; }
        public Builder timestamp(long v)      { this.timestamp = v; return this; }
        public MarketData build()             { return new MarketData(this); }
    }
}
