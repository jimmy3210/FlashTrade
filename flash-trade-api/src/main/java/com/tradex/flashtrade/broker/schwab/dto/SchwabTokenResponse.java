package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schwab OAuth2 Token 端点响应
 * <pre>
 * POST https://api.schwabapi.com/v1/oauth/token
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    /** Access Token 有效期（秒），嘉信默认 1800 秒（30分钟） */
    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("id_token")
    private String idToken;

    public String getAccessToken()   { return accessToken; }
    public String getRefreshToken()  { return refreshToken; }
    public String getTokenType()     { return tokenType; }
    public int getExpiresIn()        { return expiresIn; }
    public String getScope()         { return scope; }
    public String getIdToken()       { return idToken; }

    public void setAccessToken(String v)  { this.accessToken  = v; }
    public void setRefreshToken(String v) { this.refreshToken = v; }
    public void setTokenType(String v)    { this.tokenType    = v; }
    public void setExpiresIn(int v)       { this.expiresIn    = v; }
    public void setScope(String v)        { this.scope        = v; }
    public void setIdToken(String v)      { this.idToken      = v; }
}
