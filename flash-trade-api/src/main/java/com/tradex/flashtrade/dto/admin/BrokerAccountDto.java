package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "券商账户 DTO")
public class BrokerAccountDto {

    @Schema(description = "券商账户 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "券商 ID")
    private Long brokerId;

    @Schema(description = "券商编码")
    private String brokerCode;

    @Schema(description = "券商名称")
    private String brokerName;

    @Schema(description = "券商账户号")
    private String accountNo;

    @Schema(description = "账户类型，CASH 或 MARGIN")
    private String accountType;

    @Schema(description = "账户状态，ACTIVE/SUSPENDED/CLOSED")
    private String status;

    @Schema(description = "开发者账号 ID")
    private Long devAccountId;

    @Schema(description = "API 环境，LIVE 或 SANDBOX")
    private String apiEnvironment;

    @Schema(description = "当前绑定的用户名")
    private String currentBindingUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getBrokerId() { return brokerId; }
    public void setBrokerId(Long brokerId) { this.brokerId = brokerId; }

    public String getBrokerCode() { return brokerCode; }
    public void setBrokerCode(String brokerCode) { this.brokerCode = brokerCode; }

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

    public String getCurrentBindingUsername() { return currentBindingUsername; }
    public void setCurrentBindingUsername(String currentBindingUsername) { this.currentBindingUsername = currentBindingUsername; }
}
