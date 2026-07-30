package com.tradex.flashtrade.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应 Token 信息")
public class LoginResponse {

    @Schema(description = "JWT 访问令牌")
    private String accessToken;
    @Schema(description = "JWT 刷新令牌")
    private String refreshToken;
    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType;
    @Schema(description = "过期时间（秒）", example = "3600")
    private long expiresIn;

    public LoginResponse() {}

    private LoginResponse(Builder builder) {
        this.accessToken  = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenType    = builder.tokenType;
        this.expiresIn    = builder.expiresIn;
    }

    public static Builder builder() { return new Builder(); }

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType()    { return tokenType; }
    public long   getExpiresIn()    { return expiresIn; }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long   expiresIn;

        public Builder accessToken(String v)  { this.accessToken  = v; return this; }
        public Builder refreshToken(String v) { this.refreshToken = v; return this; }
        public Builder tokenType(String v)    { this.tokenType    = v; return this; }
        public Builder expiresIn(long v)      { this.expiresIn    = v; return this; }
        public LoginResponse build()          { return new LoginResponse(this); }
    }
}
