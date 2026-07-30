package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tastyworks 单条持仓记录
 * <pre>
 * GET /accounts/{account-number}/positions → data.items[]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksPosition {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("instrument-type")
    private String instrumentType;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("quantity-direction")
    private String quantityDirection;  // Long / Short

    @JsonProperty("average-open-price")
    private String averageOpenPrice;

    @JsonProperty("close-price")
    private String closePrice;

    @JsonProperty("multiplier")
    private Integer multiplier;

    @JsonProperty("cost-effect")
    private String costEffect;          // Debit / Credit

    public String getSymbol()             { return symbol; }
    public String getInstrumentType()     { return instrumentType; }
    public String getQuantity()           { return quantity; }
    public String getQuantityDirection()  { return quantityDirection; }
    public String getAverageOpenPrice()   { return averageOpenPrice; }
    public String getClosePrice()         { return closePrice; }
    public Integer getMultiplier()        { return multiplier; }
    public String getCostEffect()         { return costEffect; }

    public void setSymbol(String v)             { this.symbol            = v; }
    public void setInstrumentType(String v)     { this.instrumentType    = v; }
    public void setQuantity(String v)           { this.quantity          = v; }
    public void setQuantityDirection(String v)  { this.quantityDirection = v; }
    public void setAverageOpenPrice(String v)   { this.averageOpenPrice  = v; }
    public void setClosePrice(String v)         { this.closePrice        = v; }
    public void setMultiplier(Integer v)        { this.multiplier        = v; }
    public void setCostEffect(String v)         { this.costEffect        = v; }
}
