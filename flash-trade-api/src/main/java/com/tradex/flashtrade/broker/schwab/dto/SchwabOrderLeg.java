package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schwab 订单委托腿（orderLegCollection 中的元素）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabOrderLeg {

    /** 委托方向: BUY / SELL / BUY_TO_COVER / SELL_SHORT */
    @JsonProperty("instruction")
    private String instruction;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("instrument")
    private SchwabInstrument instrument;

    public String getInstruction()       { return instruction; }
    public Double getQuantity()          { return quantity; }
    public SchwabInstrument getInstrument() { return instrument; }

    public void setInstruction(String v)          { this.instruction  = v; }
    public void setQuantity(Double v)             { this.quantity     = v; }
    public void setInstrument(SchwabInstrument v) { this.instrument   = v; }
}
