package com.tradex.flashtrade.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "认证失败，请先登录"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "无效的请求参数"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "请求参数校验失败"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token 已过期"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token 无效或已被吊销"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token 已过期"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "无权访问"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "资源不存在"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "请求频率过高，请稍后重试"),
    BROKER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "券商服务暂时不可用"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "未找到资源"),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "状态不允许的操作"),
    ORDER_RESULT_UNCONFIRMED(HttpStatus.CONFLICT, "订单提交结果尚未确认，请先查询订单状态"),
    STOCK_NOT_FOUND(HttpStatus.BAD_REQUEST, "股票代码不存在或无效"),
    STOCK_NOT_WHITELISTED(HttpStatus.BAD_REQUEST, "该股票代码未在白名单中，无法操作"),
    ACCOUNT_RISK_BLOCKED(HttpStatus.FORBIDDEN, "账户已被风控禁止，无法下单"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "指定账户不存在");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return this.name(); }
    public String getMessage() { return message; }
}
