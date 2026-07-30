package com.tradex.flashtrade.dto.portfolio;

import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "委托订单信息")
public class OrderResponse {

    @Schema(description = "订单 ID", example = "1")
    private Long id;
    @Schema(description = "账户 ID", example = "dev-account-001")
    private String accountId;
    @Schema(description = "股票代码", example = "AAPL")
    private String symbol;
    @Schema(description = "买卖方向", example = "BUY")
    private OrderSide side;
    @Schema(description = "委托数量", example = "100")
    private BigDecimal quantity;
    @Schema(description = "委托价格", example = "150.00")
    private BigDecimal price;
    @Schema(description = "订单状态", example = "FILLED")
    private OrderStatus status;
    @Schema(description = "券商名称", example = "SCHWAB")
    private String brokerName;
    @Schema(description = "外部订单编号")
    private String externalOrderId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "成交时间")
    private LocalDateTime filledAt;

    public OrderResponse() {}

    private OrderResponse(Builder b) {
        this.id              = b.id;
        this.accountId       = b.accountId;
        this.symbol          = b.symbol;
        this.side            = b.side;
        this.quantity        = b.quantity;
        this.price           = b.price;
        this.status          = b.status;
        this.brokerName      = b.brokerName;
        this.externalOrderId = b.externalOrderId;
        this.createdAt       = b.createdAt;
        this.filledAt        = b.filledAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId()                   { return id; }
    public String getAccountId()          { return accountId; }
    public String getSymbol()             { return symbol; }
    public OrderSide getSide()            { return side; }
    public BigDecimal getQuantity()       { return quantity; }
    public BigDecimal getPrice()          { return price; }
    public OrderStatus getStatus()        { return status; }
    public String getBrokerName()         { return brokerName; }
    public String getExternalOrderId()    { return externalOrderId; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getFilledAt()    { return filledAt; }

    public static class Builder {
        private Long id;
        private String accountId;
        private String symbol;
        private OrderSide side;
        private BigDecimal quantity;
        private BigDecimal price;
        private OrderStatus status;
        private String brokerName;
        private String externalOrderId;
        private LocalDateTime createdAt;
        private LocalDateTime filledAt;

        public Builder id(Long v)                   { this.id              = v; return this; }
        public Builder accountId(String v)          { this.accountId       = v; return this; }
        public Builder symbol(String v)             { this.symbol          = v; return this; }
        public Builder side(OrderSide v)            { this.side            = v; return this; }
        public Builder quantity(BigDecimal v)       { this.quantity        = v; return this; }
        public Builder price(BigDecimal v)          { this.price           = v; return this; }
        public Builder status(OrderStatus v)        { this.status          = v; return this; }
        public Builder brokerName(String v)         { this.brokerName      = v; return this; }
        public Builder externalOrderId(String v)    { this.externalOrderId = v; return this; }
        public Builder createdAt(LocalDateTime v)   { this.createdAt       = v; return this; }
        public Builder filledAt(LocalDateTime v)    { this.filledAt        = v; return this; }
        public OrderResponse build()                { return new OrderResponse(this); }
    }
}
