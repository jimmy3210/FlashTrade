package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Schwab 订单对象
 * <pre>
 * GET /accounts/{accountNumber}/orders
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabOrderResponse {

    @JsonProperty("orderId")
    private Long orderId;

    /** 订单类型: MARKET / LIMIT / STOP / STOP_LIMIT */
    @JsonProperty("orderType")
    private String orderType;

    /** 委托状态: AWAITING_PARENT_ORDER / AWAITING_CONDITION / AWAITING_RECALL /
     *  AWAITING_REVIEW / ACCEPTED / PENDING_ACTIVATION / QUEUED / WORKING /
     *  REJECTED / PENDING_CANCEL / CANCELED / PENDING_REPLACE / REPLACED /
     *  FILLED / EXPIRED / NEW / AWAITING_RELEASE_TIME / PENDING_ACKNOWLEDGEMENT /
     *  PENDING_RECALL / UNKNOWN */
    @JsonProperty("status")
    private String status;

    /** 委托总数量 */
    @JsonProperty("quantity")
    private Double quantity;

    /** 已成交数量 */
    @JsonProperty("filledQuantity")
    private Double filledQuantity;

    /** 剩余数量 */
    @JsonProperty("remainingQuantity")
    private Double remainingQuantity;

    /** 限价（LIMIT/STOP_LIMIT 时有值） */
    @JsonProperty("price")
    private Double price;

    /** 委托提交时间（ISO-8601） */
    @JsonProperty("enteredTime")
    private String enteredTime;

    /** 委托结束时间（已成交/已撤销） */
    @JsonProperty("closeTime")
    private String closeTime;

    /** 委托腿列表（通常含1条，复杂策略含多条） */
    @JsonProperty("orderLegCollection")
    private List<SchwabOrderLeg> orderLegCollection;

    public Long getOrderId()                          { return orderId; }
    public String getOrderType()                      { return orderType; }
    public String getStatus()                         { return status; }
    public Double getQuantity()                       { return quantity; }
    public Double getFilledQuantity()                 { return filledQuantity; }
    public Double getRemainingQuantity()              { return remainingQuantity; }
    public Double getPrice()                          { return price; }
    public String getEnteredTime()                    { return enteredTime; }
    public String getCloseTime()                      { return closeTime; }
    public List<SchwabOrderLeg> getOrderLegCollection() { return orderLegCollection; }

    public void setOrderId(Long v)                              { this.orderId             = v; }
    public void setOrderType(String v)                         { this.orderType            = v; }
    public void setStatus(String v)                            { this.status               = v; }
    public void setQuantity(Double v)                          { this.quantity             = v; }
    public void setFilledQuantity(Double v)                    { this.filledQuantity       = v; }
    public void setRemainingQuantity(Double v)                 { this.remainingQuantity    = v; }
    public void setPrice(Double v)                             { this.price                = v; }
    public void setEnteredTime(String v)                       { this.enteredTime          = v; }
    public void setCloseTime(String v)                         { this.closeTime            = v; }
    public void setOrderLegCollection(List<SchwabOrderLeg> v)  { this.orderLegCollection   = v; }
}
