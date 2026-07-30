package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;

/**
 * 券商侧单条持仓记录
 */
public class BrokerPosition {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedPnl;

    public BrokerPosition() {}

    private BrokerPosition(Builder b) {
        this.symbol        = b.symbol;
        this.quantity      = b.quantity;
        this.averageCost   = b.averageCost;
        this.currentPrice  = b.currentPrice;
        this.marketValue   = b.marketValue;
        this.unrealizedPnl = b.unrealizedPnl;
    }

    public static Builder builder() { return new Builder(); }

    public String getSymbol()              { return symbol; }
    public BigDecimal getQuantity()        { return quantity; }
    public BigDecimal getAverageCost()     { return averageCost; }
    public BigDecimal getCurrentPrice()    { return currentPrice; }
    public BigDecimal getMarketValue()     { return marketValue; }
    public BigDecimal getUnrealizedPnl()   { return unrealizedPnl; }

    public static class Builder {
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averageCost;
        private BigDecimal currentPrice;
        private BigDecimal marketValue;
        private BigDecimal unrealizedPnl;

        public Builder symbol(String v)             { this.symbol        = v; return this; }
        public Builder quantity(BigDecimal v)       { this.quantity      = v; return this; }
        public Builder averageCost(BigDecimal v)    { this.averageCost   = v; return this; }
        public Builder currentPrice(BigDecimal v)   { this.currentPrice  = v; return this; }
        public Builder marketValue(BigDecimal v)    { this.marketValue   = v; return this; }
        public Builder unrealizedPnl(BigDecimal v)  { this.unrealizedPnl = v; return this; }
        public BrokerPosition build()               { return new BrokerPosition(this); }
    }
}
