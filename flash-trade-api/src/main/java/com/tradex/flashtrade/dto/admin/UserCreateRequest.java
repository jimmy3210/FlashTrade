package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "创建 / 更新用户请求")
public class UserCreateRequest {

    @Schema(description = "用户名")
    @NotBlank
    private String username;

    @Schema(description = "账户名")
    @NotBlank
    private String accountUserId;

    @Schema(description = "所属券商")
    @NotBlank
    private String brokerName;

    @Schema(description = "密码（新建必填，编辑时若不修改可留空）")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAccountUserId() { return accountUserId; }
    public void setAccountUserId(String accountUserId) { this.accountUserId = accountUserId; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
