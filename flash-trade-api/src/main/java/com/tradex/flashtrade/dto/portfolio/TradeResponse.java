package com.tradex.flashtrade.dto.portfolio;

import com.tradex.flashtrade.entity.OrderSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "成交记录")
public class TradeResponse {

    @Schema(description = "成交 ID", example = "1")
    private Long id;
    @Schema(description = "账户 ID", example = "dev-account-001")
    private String accountId;
    @Schema(description = "股票代码", example = "AAPL")
    private String symbol;
    @Schema(description = "买卖方向", example = "BUY")
    private OrderSide side;
    @Schema(description = "成交数量", example = "100")
    private BigDecimal quantity;
    @Schema(description = "成交价格", example = "150.00")
    private BigDecimal price;
    @Schema(description = "交易佣金", example = "1.50")
    private BigDecimal commission;
    @Schema(description = "券商名称", example = "SCHWAB")
    private String brokerName;
    @Schema(description = "外部成交编号")
    private String externalTradeId;
    @Schema(description = "关联订单 ID")
    private Long orderId;
    @Schema(description = "成交时间")
    private LocalDateTime tradedAt;

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TradeResponse r = new TradeResponse();
        public Builder id(Long v)                  { r.id = v; return this; }
        public Builder accountId(String v)         { r.accountId = v; return this; }
        public Builder symbol(String v)            { r.symbol = v; return this; }
        public Builder side(OrderSide v)           { r.side = v; return this; }
        public Builder quantity(BigDecimal v)      { r.quantity = v; return this; }
        public Builder price(BigDecimal v)         { r.price = v; return this; }
        public Builder commission(BigDecimal v)    { r.commission = v; return this; }
        public Builder brokerName(String v)        { r.brokerName = v; return this; }
        public Builder externalTradeId(String v)   { r.externalTradeId = v; return this; }
        public Builder orderId(Long v)             { r.orderId = v; return this; }
        public Builder tradedAt(LocalDateTime v)   { r.tradedAt = v; return this; }
        public TradeResponse build()               { return r; }
    }

    // Getters
    public Long getId()              { return id; }
    public String getAccountId()     { return accountId; }
    public String getSymbol()        { return symbol; }
    public OrderSide getSide()       { return side; }
    public BigDecimal getQuantity()  { return quantity; }
    public BigDecimal getPrice()     { return price; }
    public BigDecimal getCommission(){ return commission; }
    public String getBrokerName()    { return brokerName; }
    public String getExternalTradeId(){ return externalTradeId; }
    public Long getOrderId()         { return orderId; }
    public LocalDateTime getTradedAt(){ return tradedAt; }
}
