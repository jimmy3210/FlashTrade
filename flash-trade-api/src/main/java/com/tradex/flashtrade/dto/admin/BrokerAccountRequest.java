package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "创建 / 更新券商账户请求")
public class BrokerAccountRequest {

    @Schema(description = "用户 ID")
    @NotNull
    private Long userId;

    @Schema(description = "券商名称或编码")
    @NotBlank
    private String brokerName;

    @Schema(description = "券商账户号")
    @NotBlank
    private String accountNo;

    @Schema(description = "账户类型，CASH 或 MARGIN")
    private String accountType;

    @Schema(description = "账户状态，ACTIVE/SUSPENDED/CLOSED")
    private String status;

    @Schema(description = "开发者账号 ID，可选")
    private Long devAccountId;

    @Schema(description = "API 环境，LIVE 或 SANDBOX")
    private String apiEnvironment;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDevAccountId() { return devAccountId; }
    public void setDevAccountId(Long devAccountId) { this.devAccountId = devAccountId; }

    public String getApiEnvironment() { return apiEnvironment; }
    public void setApiEnvironment(String apiEnvironment) { this.apiEnvironment = apiEnvironment; }
}
