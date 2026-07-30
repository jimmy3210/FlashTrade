package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schwab 交易转移项（成交记录中的资产明细）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabTransferItem {

    @JsonProperty("instrument")
    private SchwabInstrument instrument;

    /** 成交数量（正=买入，负=卖出） */
    @JsonProperty("amount")
    private Double amount;

    /** 成交金额 */
    @JsonProperty("cost")
    private Double cost;

    /** 成交价格 */
    @JsonProperty("price")
    private Double price;

    /** 费用类型: COMMISSION / SEC_FEE / STR_FEE / ... */
    @JsonProperty("feeType")
    private String feeType;

    /** 持仓效果: OPENING / CLOSING / AUTOMATIC */
    @JsonProperty("positionEffect")
    private String positionEffect;

    public SchwabInstrument getInstrument() { return instrument; }
    public Double getAmount()               { return amount; }
    public Double getCost()                 { return cost; }
    public Double getPrice()               { return price; }
    public String getFeeType()              { return feeType; }
    public String getPositionEffect()       { return positionEffect; }

    public void setInstrument(SchwabInstrument v)  { this.instrument     = v; }
    public void setAmount(Double v)                { this.amount         = v; }
    public void setCost(Double v)                  { this.cost           = v; }
    public void setPrice(Double v)                 { this.price          = v; }
    public void setFeeType(String v)               { this.feeType        = v; }
    public void setPositionEffect(String v)        { this.positionEffect = v; }
}
