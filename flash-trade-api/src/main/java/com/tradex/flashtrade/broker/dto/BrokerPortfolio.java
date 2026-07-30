package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 券商侧持仓聚合对象（经过数据映射后传入系统内部）
 */
public class BrokerPortfolio {

    private String accountId;
    private String brokerName;
    private BigDecimal totalMarketValue;
    private BigDecimal totalUnrealizedPnl;
    private List<BrokerPosition> positions;

    public BrokerPortfolio() {}

    private BrokerPortfolio(Builder b) {
        this.accountId         = b.accountId;
        this.brokerName        = b.brokerName;
        this.totalMarketValue  = b.totalMarketValue;
        this.totalUnrealizedPnl = b.totalUnrealizedPnl;
        this.positions         = b.positions;
    }

    public static Builder builder() { return new Builder(); }

    public String getAccountId()              { return accountId; }
    public String getBrokerName()             { return brokerName; }
    public BigDecimal getTotalMarketValue()   { return totalMarketValue; }
    public BigDecimal getTotalUnrealizedPnl() { return totalUnrealizedPnl; }
    public List<BrokerPosition> getPositions() { return positions; }

    public static class Builder {
        private String accountId;
        private String brokerName;
        private BigDecimal totalMarketValue;
        private BigDecimal totalUnrealizedPnl;
        private List<BrokerPosition> positions;

        public Builder accountId(String v)              { this.accountId          = v; return this; }
        public Builder brokerName(String v)             { this.brokerName         = v; return this; }
        public Builder totalMarketValue(BigDecimal v)   { this.totalMarketValue   = v; return this; }
        public Builder totalUnrealizedPnl(BigDecimal v) { this.totalUnrealizedPnl = v; return this; }
        public Builder positions(List<BrokerPosition> v) { this.positions         = v; return this; }
        public BrokerPortfolio build()                  { return new BrokerPortfolio(this); }
    }
}
