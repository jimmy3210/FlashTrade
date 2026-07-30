package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tastyworks 登录响应
 * <pre>
 * POST /sessions
 * 响应: { "data": { "session-token": "...", "user": { ... } } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksSessionResponse {

    @JsonProperty("data")
    private SessionData data;

    public SessionData getData() { return data; }
    public void setData(SessionData v) { this.data = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionData {

        @JsonProperty("session-token")
        private String sessionToken;

        @JsonProperty("user")
        private UserInfo user;

        public String getSessionToken() { return sessionToken; }
        public UserInfo getUser()       { return user; }

        public void setSessionToken(String v) { this.sessionToken = v; }
        public void setUser(UserInfo v)       { this.user          = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {

        @JsonProperty("email")
        private String email;

        @JsonProperty("username")
        private String username;

        @JsonProperty("external-id")
        private String externalId;

        public String getEmail()      { return email; }
        public String getUsername()   { return username; }
        public String getExternalId() { return externalId; }

        public void setEmail(String v)      { this.email      = v; }
        public void setUsername(String v)   { this.username   = v; }
        public void setExternalId(String v) { this.externalId = v; }
    }
}
