package com.tradex.flashtrade.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "持仓信息")
public class PositionResponse {

    @Schema(description = "持仓 ID", example = "1")
    private Long id;
    @Schema(description = "账户 ID", example = "dev-account-001")
    private String accountId;
    @Schema(description = "股票代码", example = "AAPL")
    private String symbol;
    @Schema(description = "持仓数量", example = "100")
    private BigDecimal quantity;
    @Schema(description = "成本均价", example = "150.00")
    private BigDecimal averageCost;
    @Schema(description = "当前价格", example = "175.00")
    private BigDecimal currentPrice;
    @Schema(description = "市场价值", example = "17500.00")
    private BigDecimal marketValue;
    @Schema(description = "未实现盈亏", example = "2500.00")
    private BigDecimal unrealizedPnl;
    @Schema(description = "券商名称", example = "SCHWAB")
    private String brokerName;
    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;

    public PositionResponse() {}

    private PositionResponse(Builder b) {
        this.id             = b.id;
        this.accountId      = b.accountId;
        this.symbol         = b.symbol;
        this.quantity       = b.quantity;
        this.averageCost    = b.averageCost;
        this.currentPrice   = b.currentPrice;
        this.marketValue    = b.marketValue;
        this.unrealizedPnl  = b.unrealizedPnl;
        this.brokerName     = b.brokerName;
        this.updatedAt      = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId()                   { return id; }
    public String getAccountId()          { return accountId; }
    public String getSymbol()             { return symbol; }
    public BigDecimal getQuantity()       { return quantity; }
    public BigDecimal getAverageCost()    { return averageCost; }
    public BigDecimal getCurrentPrice()   { return currentPrice; }
    public BigDecimal getMarketValue()    { return marketValue; }
    public BigDecimal getUnrealizedPnl()  { return unrealizedPnl; }
    public String getBrokerName()         { return brokerName; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public static class Builder {
        private Long id;
        private String accountId;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averageCost;
        private BigDecimal currentPrice;
        private BigDecimal marketValue;
        private BigDecimal unrealizedPnl;
        private String brokerName;
        private LocalDateTime updatedAt;

        public Builder id(Long v)                   { this.id            = v; return this; }
        public Builder accountId(String v)          { this.accountId     = v; return this; }
        public Builder symbol(String v)             { this.symbol        = v; return this; }
        public Builder quantity(BigDecimal v)       { this.quantity      = v; return this; }
        public Builder averageCost(BigDecimal v)    { this.averageCost   = v; return this; }
        public Builder currentPrice(BigDecimal v)   { this.currentPrice  = v; return this; }
        public Builder marketValue(BigDecimal v)    { this.marketValue   = v; return this; }
        public Builder unrealizedPnl(BigDecimal v)  { this.unrealizedPnl = v; return this; }
        public Builder brokerName(String v)         { this.brokerName    = v; return this; }
        public Builder updatedAt(LocalDateTime v)   { this.updatedAt     = v; return this; }
        public PositionResponse build()             { return new PositionResponse(this); }
    }
}
