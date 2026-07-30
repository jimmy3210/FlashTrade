package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * E*TRADE 订单对象
 * <pre>
 * GET /v1/accounts/{accountIdKey}/orders
 * 响应: { "OrdersResponse": { "Order": [...] } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeOrder {

    @JsonProperty("orderId")
    private Long orderId;

    /** 订单类型: EQ（股票）/ OPTN（期权）等 */
    @JsonProperty("orderType")
    private String orderType;

    /** 委托方向: BUY / SELL / BUY_TO_COVER / SELL_SHORT */
    @JsonProperty("orderAction")
    private String orderAction;

    /** 价格类型: MARKET / LIMIT / STOP / STOP_LIMIT */
    @JsonProperty("priceType")
    private String priceType;

    /** 限价 */
    @JsonProperty("limitPrice")
    private Double limitPrice;

    /** 委托数量 */
    @JsonProperty("orderedQuantity")
    private Double orderedQuantity;

    /** 已执行数量 */
    @JsonProperty("executedQuantity")
    private Double executedQuantity;

    /**
     * 订单状态: OPEN / EXECUTED / CANCEL_REQUESTED / CANCELLED /
     *           EXPIRED / REJECTED
     */
    @JsonProperty("status")
    private String status;

    /** 下单时间（Unix epoch 毫秒） */
    @JsonProperty("placedTime")
    private Long placedTime;

    /** 执行时间（Unix epoch 毫秒） */
    @JsonProperty("executedTime")
    private Long executedTime;

    /** 委托腿列表 */
    @JsonProperty("OrderDetail")
    private List<EtradeOrderDetail> orderDetails;

    public Long getOrderId()                          { return orderId; }
    public String getOrderType()                      { return orderType; }
    public String getOrderAction()                    { return orderAction; }
    public String getPriceType()                      { return priceType; }
    public Double getLimitPrice()                     { return limitPrice; }
    public Double getOrderedQuantity()                { return orderedQuantity; }
    public Double getExecutedQuantity()               { return executedQuantity; }
    public String getStatus()                         { return status; }
    public Long getPlacedTime()                       { return placedTime; }
    public Long getExecutedTime()                     { return executedTime; }
    public List<EtradeOrderDetail> getOrderDetails()  { return orderDetails; }

    public void setOrderId(Long v)                              { this.orderId          = v; }
    public void setOrderType(String v)                         { this.orderType         = v; }
    public void setOrderAction(String v)                       { this.orderAction       = v; }
    public void setPriceType(String v)                         { this.priceType         = v; }
    public void setLimitPrice(Double v)                        { this.limitPrice        = v; }
    public void setOrderedQuantity(Double v)                   { this.orderedQuantity   = v; }
    public void setExecutedQuantity(Double v)                  { this.executedQuantity  = v; }
    public void setStatus(String v)                            { this.status            = v; }
    public void setPlacedTime(Long v)                          { this.placedTime        = v; }
    public void setExecutedTime(Long v)                        { this.executedTime      = v; }
    public void setOrderDetails(List<EtradeOrderDetail> v)     { this.orderDetails      = v; }
}
