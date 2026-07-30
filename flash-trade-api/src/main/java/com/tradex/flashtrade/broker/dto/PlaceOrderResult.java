package com.tradex.flashtrade.broker.dto;

/** 下单 / 撤单操作结果 DTO */
public class PlaceOrderResult {

    private boolean success;
    private String  externalOrderId;  // 券商侧订单 ID
    private String  message;

    public PlaceOrderResult() {}

    private PlaceOrderResult(Builder b) {
        this.success         = b.success;
        this.externalOrderId = b.externalOrderId;
        this.message         = b.message;
    }

    public static Builder builder() { return new Builder(); }

    public boolean isSuccess()          { return success; }
    public String  getExternalOrderId() { return externalOrderId; }
    public String  getMessage()         { return message; }

    public void setSuccess(boolean v)          { this.success = v; }
    public void setExternalOrderId(String v)   { this.externalOrderId = v; }
    public void setMessage(String v)           { this.message = v; }

    public static class Builder {
        private boolean success;
        private String  externalOrderId;
        private String  message;

        public Builder success(boolean v)          { this.success = v;         return this; }
        public Builder externalOrderId(String v)   { this.externalOrderId = v; return this; }
        public Builder message(String v)           { this.message = v;         return this; }
        public PlaceOrderResult build()            { return new PlaceOrderResult(this); }
    }
}
