package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tastyworks 单条委托订单
 * <pre>
 * GET /accounts/{account-number}/orders → data.items[]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksOrder {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("account-number")
    private String accountNumber;

    @JsonProperty("time-in-force")
    private String timeInForce;    // Day / GTC / GTD

    @JsonProperty("order-type")
    private String orderType;      // Limit / Market / Stop / Stop Limit

    @JsonProperty("size")
    private String size;

    @JsonProperty("underlying-symbol")
    private String underlyingSymbol;

    @JsonProperty("underlying-instrument-type")
    private String underlyingInstrumentType;

    @JsonProperty("price")
    private String price;

    @JsonProperty("price-effect")
    private String priceEffect;   // Debit / Credit

    @JsonProperty("status")
    private String status;        // Received / Live / Filled / Cancelled / Rejected

    @JsonProperty("received-at")
    private String receivedAt;   // ISO-8601

    @JsonProperty("updated-at")
    private Long updatedAt;      // epoch millis

    @JsonProperty("legs")
    private List<TastyworksOrderLeg> legs;

    public Long getId()                         { return id; }
    public String getAccountNumber()            { return accountNumber; }
    public String getTimeInForce()              { return timeInForce; }
    public String getOrderType()                { return orderType; }
    public String getSize()                     { return size; }
    public String getUnderlyingSymbol()         { return underlyingSymbol; }
    public String getUnderlyingInstrumentType() { return underlyingInstrumentType; }
    public String getPrice()                    { return price; }
    public String getPriceEffect()              { return priceEffect; }
    public String getStatus()                   { return status; }
    public String getReceivedAt()               { return receivedAt; }
    public Long getUpdatedAt()                  { return updatedAt; }
    public List<TastyworksOrderLeg> getLegs()   { return legs; }

    public void setId(Long v)                         { this.id                         = v; }
    public void setAccountNumber(String v)            { this.accountNumber              = v; }
    public void setTimeInForce(String v)              { this.timeInForce                = v; }
    public void setOrderType(String v)                { this.orderType                  = v; }
    public void setSize(String v)                     { this.size                       = v; }
    public void setUnderlyingSymbol(String v)         { this.underlyingSymbol           = v; }
    public void setUnderlyingInstrumentType(String v) { this.underlyingInstrumentType   = v; }
    public void setPrice(String v)                    { this.price                      = v; }
    public void setPriceEffect(String v)              { this.priceEffect                = v; }
    public void setStatus(String v)                   { this.status                     = v; }
    public void setReceivedAt(String v)               { this.receivedAt                 = v; }
    public void setUpdatedAt(Long v)                  { this.updatedAt                  = v; }
    public void setLegs(List<TastyworksOrderLeg> v)   { this.legs                       = v; }
}
