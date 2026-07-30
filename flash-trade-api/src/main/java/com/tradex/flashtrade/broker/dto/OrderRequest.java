package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;

import com.tradex.flashtrade.entity.OrderSide;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 下单请求 DTO */
public class OrderRequest {

    @NotBlank(message = "股票代码不能为空")
    private String    symbol;

    @NotNull(message = "买卖方向不能为空")
    private OrderSide side;           // BUY / SELL

    @NotNull(message = "委托类型不能为空")
    private OrderType orderType;      // MARKET / LIMIT

    @Min(value = 1, message = "委托数量至少为 1")
    private int       quantity;

    @DecimalMin(value = "0.01", message = "限价必须 > 0")
    private BigDecimal limitPrice;    // LIMIT 单时必填

    @DecimalMin(value = "0.00", message = "止损价必须 >= 0")
    private BigDecimal stopPrice;     // STOP / STOP_LIMIT 单时可选

    private String timeInForce = "DAY";   // DAY / GTC / IOC / FOK

    private String clientOrderId;  // 幂等 ID（客户端生成，防重复下单）

    public OrderRequest() {}

    public String     getSymbol()        { return symbol; }
    public OrderSide  getSide()          { return side; }
    public OrderType  getOrderType()     { return orderType; }
    public int        getQuantity()      { return quantity; }
    public BigDecimal getLimitPrice()    { return limitPrice; }
    public BigDecimal getStopPrice()     { return stopPrice; }
    public String     getTimeInForce()   { return timeInForce; }
    public String     getClientOrderId() { return clientOrderId; }

    public void setSymbol(String v)        { this.symbol = v; }
    public void setSide(OrderSide v)       { this.side = v; }
    public void setOrderType(OrderType v)  { this.orderType = v; }
    public void setQuantity(int v)         { this.quantity = v; }
    public void setLimitPrice(BigDecimal v){ this.limitPrice = v; }
    public void setStopPrice(BigDecimal v) { this.stopPrice = v; }
    public void setTimeInForce(String v)   { this.timeInForce = v; }
    public void setClientOrderId(String v) { this.clientOrderId = v; }

    public enum OrderType { MARKET, LIMIT }
}
