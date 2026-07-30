package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * E*TRADE 单条持仓记录
 * <pre>
 * GET /v1/accounts/{accountIdKey}/portfolio → PortfolioResponse.AccountPortfolio[].Position[]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePosition {

    @JsonProperty("positionId")
    private Long positionId;

    @JsonProperty("product")
    private EtradeInstrument product;

    /** 持仓数量 */
    @JsonProperty("quantity")
    private Double quantity;

    /** 持仓成本价 */
    @JsonProperty("costPerShare")
    private Double costPerShare;

    /** 当前市价 */
    @JsonProperty("currentPrice")
    private Double currentPrice;

    /** 市场总价值 */
    @JsonProperty("marketValue")
    private Double marketValue;

    /** 浮动盈亏 */
    @JsonProperty("totalGain")
    private Double totalGain;

    /** 持仓类型: LONG / SHORT */
    @JsonProperty("positionType")
    private String positionType;

    public Long getPositionId()       { return positionId; }
    public EtradeInstrument getProduct() { return product; }
    public Double getQuantity()       { return quantity; }
    public Double getCostPerShare()   { return costPerShare; }
    public Double getCurrentPrice()   { return currentPrice; }
    public Double getMarketValue()    { return marketValue; }
    public Double getTotalGain()      { return totalGain; }
    public String getPositionType()   { return positionType; }

    public void setPositionId(Long v)           { this.positionId   = v; }
    public void setProduct(EtradeInstrument v)  { this.product      = v; }
    public void setQuantity(Double v)           { this.quantity     = v; }
    public void setCostPerShare(Double v)       { this.costPerShare = v; }
    public void setCurrentPrice(Double v)       { this.currentPrice = v; }
    public void setMarketValue(Double v)        { this.marketValue  = v; }
    public void setTotalGain(Double v)          { this.totalGain    = v; }
    public void setPositionType(String v)       { this.positionType = v; }
}
