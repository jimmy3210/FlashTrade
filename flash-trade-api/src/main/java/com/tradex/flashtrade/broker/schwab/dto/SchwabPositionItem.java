package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schwab 单条持仓记录
 * <pre>
 * GET /accounts/{accountNumber}/positions → securitiesAccount.positions[]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabPositionItem {

    /** 多头数量 */
    @JsonProperty("longQuantity")
    private Double longQuantity;

    /** 空头数量 */
    @JsonProperty("shortQuantity")
    private Double shortQuantity;

    /** 持仓平均成本价 */
    @JsonProperty("averagePrice")
    private Double averagePrice;

    /** 当日盈亏 */
    @JsonProperty("currentDayProfitLoss")
    private Double currentDayProfitLoss;

    /** 当日盈亏百分比 */
    @JsonProperty("currentDayProfitLossPercentage")
    private Double currentDayProfitLossPercentage;

    /** 持仓市场价值 */
    @JsonProperty("marketValue")
    private Double marketValue;

    /** 多头长期浮动盈亏 */
    @JsonProperty("longOpenProfitLoss")
    private Double longOpenProfitLoss;

    /** 金融工具详情 */
    @JsonProperty("instrument")
    private SchwabInstrument instrument;

    public Double getLongQuantity()                    { return longQuantity; }
    public Double getShortQuantity()                   { return shortQuantity; }
    public Double getAveragePrice()                    { return averagePrice; }
    public Double getCurrentDayProfitLoss()            { return currentDayProfitLoss; }
    public Double getCurrentDayProfitLossPercentage()  { return currentDayProfitLossPercentage; }
    public Double getMarketValue()                     { return marketValue; }
    public Double getLongOpenProfitLoss()              { return longOpenProfitLoss; }
    public SchwabInstrument getInstrument()            { return instrument; }

    public void setLongQuantity(Double v)                   { this.longQuantity                   = v; }
    public void setShortQuantity(Double v)                  { this.shortQuantity                  = v; }
    public void setAveragePrice(Double v)                   { this.averagePrice                   = v; }
    public void setCurrentDayProfitLoss(Double v)           { this.currentDayProfitLoss           = v; }
    public void setCurrentDayProfitLossPercentage(Double v) { this.currentDayProfitLossPercentage = v; }
    public void setMarketValue(Double v)                    { this.marketValue                    = v; }
    public void setLongOpenProfitLoss(Double v)             { this.longOpenProfitLoss             = v; }
    public void setInstrument(SchwabInstrument v)           { this.instrument                     = v; }
}
