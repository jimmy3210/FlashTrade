package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * E*TRADE 订单委托明细（OrderDetail）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeOrderDetail {

    @JsonProperty("product")
    private EtradeInstrument product;

    @JsonProperty("orderedQuantity")
    private Double orderedQuantity;

    @JsonProperty("filledQuantity")
    private Double filledQuantity;

    @JsonProperty("averageExecutionPrice")
    private Double averageExecutionPrice;

    public EtradeInstrument getProduct()         { return product; }
    public Double getOrderedQuantity()           { return orderedQuantity; }
    public Double getFilledQuantity()            { return filledQuantity; }
    public Double getAverageExecutionPrice()     { return averageExecutionPrice; }

    public void setProduct(EtradeInstrument v)       { this.product                = v; }
    public void setOrderedQuantity(Double v)         { this.orderedQuantity        = v; }
    public void setFilledQuantity(Double v)          { this.filledQuantity         = v; }
    public void setAverageExecutionPrice(Double v)   { this.averageExecutionPrice  = v; }
}
