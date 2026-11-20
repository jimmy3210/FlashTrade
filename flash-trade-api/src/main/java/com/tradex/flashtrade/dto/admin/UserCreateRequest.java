package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "创建 / 更新用户请求")
public class UserCreateRequest {

    @Schema(description = "用户名")
    @NotBlank
    @Size(max = 50, message = "用户名最多 50 个字符")
    private String username;

    @Schema(description = "账户名")
    @NotBlank
    private String accountUserId;

    @Schema(description = "所属券商")
    @NotBlank
    private String brokerName;

    @Schema(description = "所属公司 ID")
    @NotNull
    @Positive
    private Long companyId;

    @Schema(description = "密码（新建必填，编辑时若不修改可留空）")
    @Size(min = 6, max = 100, message = "密码长度须在 6~100 字符之间")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAccountUserId() { return accountUserId; }
    public void setAccountUserId(String accountUserId) { this.accountUserId = accountUserId; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
