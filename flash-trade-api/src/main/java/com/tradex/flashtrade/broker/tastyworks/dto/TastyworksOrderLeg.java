package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tastyworks 订单的单个腿（Leg）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksOrderLeg {

    @JsonProperty("instrument-type")
    private String instrumentType;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("remaining-quantity")
    private String remainingQuantity;

    @JsonProperty("action")
    private String action;  // Buy to Open / Sell to Close / Sell to Open / Buy to Close

    @JsonProperty("fills")
    private java.util.List<TastyworksFill> fills;

    public String getInstrumentType()      { return instrumentType; }
    public String getSymbol()              { return symbol; }
    public String getQuantity()            { return quantity; }
    public String getRemainingQuantity()   { return remainingQuantity; }
    public String getAction()              { return action; }
    public java.util.List<TastyworksFill> getFills() { return fills; }

    public void setInstrumentType(String v)    { this.instrumentType    = v; }
    public void setSymbol(String v)            { this.symbol            = v; }
    public void setQuantity(String v)          { this.quantity          = v; }
    public void setRemainingQuantity(String v) { this.remainingQuantity = v; }
    public void setAction(String v)            { this.action            = v; }
    public void setFills(java.util.List<TastyworksFill> v) { this.fills = v; }
}
