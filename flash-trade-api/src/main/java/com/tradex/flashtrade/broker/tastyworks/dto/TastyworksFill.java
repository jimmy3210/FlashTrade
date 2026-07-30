package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tastyworks 成交明细（Fill）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksFill {

    @JsonProperty("ext-group-fill-id")
    private String extGroupFillId;

    @JsonProperty("ext-exec-id")
    private String extExecId;

    @JsonProperty("fill-id")
    private String fillId;

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("fill-price")
    private String fillPrice;

    @JsonProperty("filled-at")
    private String filledAt;   // ISO-8601

    @JsonProperty("destination-venue")
    private String destinationVenue;

    public String getExtGroupFillId()   { return extGroupFillId; }
    public String getExtExecId()        { return extExecId; }
    public String getFillId()           { return fillId; }
    public String getQuantity()         { return quantity; }
    public String getFillPrice()        { return fillPrice; }
    public String getFilledAt()         { return filledAt; }
    public String getDestinationVenue() { return destinationVenue; }

    public void setExtGroupFillId(String v)   { this.extGroupFillId   = v; }
    public void setExtExecId(String v)        { this.extExecId        = v; }
    public void setFillId(String v)           { this.fillId           = v; }
    public void setQuantity(String v)         { this.quantity         = v; }
    public void setFillPrice(String v)        { this.fillPrice        = v; }
    public void setFilledAt(String v)         { this.filledAt         = v; }
    public void setDestinationVenue(String v) { this.destinationVenue = v; }
}
