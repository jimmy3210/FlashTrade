package com.tradex.flashtrade.dto.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 资产视图 DTO（聚合计算结果）
 * <p>
 * 由 PositionAggregatorService 根据 trades + orders + 实时行情计算得出。
 * 不直接存储在数据库中，缓存于 Redis（TTL 2s）。
 */
@Schema(description = "资产视图（聚合计算结果）")
public class PositionView {

    @Schema(description = "账户 ID")
    private String     accountId;

    @Schema(description = "券商名称")
    private String     brokerName;

    @Schema(description = "股票代码", example = "AAPL")
    private String     symbol;

    @Schema(description = "当前在仓净股数（已成交 BUY - SELL）", example = "100")
    private int        netQuantity;

    @Schema(description = "未成交挂单买入量", example = "50")
    private int        pendingBuy;

    @Schema(description = "未成交挂单卖出量", example = "30")
    private int        pendingSell;

    @Schema(description = "可卖出量 = netQuantity - pendingSell", example = "70")
    private int        availableQuantity;

    @Schema(description = "当日成交总股数（买 + 卖，用于手续费统计）", example = "150")
    private int        totalQuantity;

    @Schema(description = "在仓加权平均成本", example = "150.00")
    private BigDecimal avgCost;

    @Schema(description = "当日已实现盈亏", example = "500.00")
    private BigDecimal realizedPnl;

    @Schema(description = "当前在仓浮动盈亏 = (lastPrice - avgCost) * netQuantity", example = "200.00")
    private BigDecimal unrealizedPnl;

    @Schema(description = "总盈亏 = realizedPnl + unrealizedPnl", example = "700.00")
    private BigDecimal totalPnl;

    @Schema(description = "最新市场价格（来自行情缓存）", example = "152.00")
    private BigDecimal lastPrice;

    @Schema(description = "市值 = netQuantity × lastPrice", example = "15200.00")
    private BigDecimal marketValue;

    @Schema(description = "持仓成本 = netQuantity × avgCost", example = "15000.00")
    private BigDecimal costValue;

    @Schema(description = "最后更新时间（毫秒时间戳）")
    private long       updateTime;

    public PositionView() {}

    // ---- Getters ----

    public String     getAccountId()         { return accountId; }
    public String     getBrokerName()         { return brokerName; }
    public String     getSymbol()             { return symbol; }
    public int        getNetQuantity()        { return netQuantity; }
    public int        getPendingBuy()         { return pendingBuy; }
    public int        getPendingSell()        { return pendingSell; }
    public int        getAvailableQuantity()  { return availableQuantity; }
    public int        getTotalQuantity()      { return totalQuantity; }
    public BigDecimal getAvgCost()            { return avgCost; }
    public BigDecimal getRealizedPnl()        { return realizedPnl; }
    public BigDecimal getUnrealizedPnl()      { return unrealizedPnl; }
    public BigDecimal getTotalPnl()           { return totalPnl; }
    public BigDecimal getLastPrice()          { return lastPrice; }
    public BigDecimal getMarketValue()        { return marketValue; }
    public BigDecimal getCostValue()          { return costValue; }
    public long       getUpdateTime()         { return updateTime; }

    // ---- Setters ----

    public void setAccountId(String v)          { this.accountId = v; }
    public void setBrokerName(String v)          { this.brokerName = v; }
    public void setSymbol(String v)              { this.symbol = v; }
    public void setNetQuantity(int v)            { this.netQuantity = v; }
    public void setPendingBuy(int v)             { this.pendingBuy = v; }
    public void setPendingSell(int v)            { this.pendingSell = v; }
    public void setAvailableQuantity(int v)      { this.availableQuantity = v; }
    public void setTotalQuantity(int v)          { this.totalQuantity = v; }
    public void setAvgCost(BigDecimal v)         { this.avgCost = v; }
    public void setRealizedPnl(BigDecimal v)     { this.realizedPnl = v; }
    public void setUnrealizedPnl(BigDecimal v)   { this.unrealizedPnl = v; }
    public void setTotalPnl(BigDecimal v)        { this.totalPnl = v; }
    public void setLastPrice(BigDecimal v)       { this.lastPrice = v; }
    public void setMarketValue(BigDecimal v)     { this.marketValue = v; }
    public void setCostValue(BigDecimal v)       { this.costValue = v; }
    public void setUpdateTime(long v)            { this.updateTime = v; }
}
