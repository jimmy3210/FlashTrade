package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员用户列表 DTO")
public class UserAdminDto {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "账户名")
    private String accountUserId;

    @Schema(description = "券商名称")
    private String brokerName;

    @Schema(description = "用户角色")
    private String role;

    @Schema(description = "启用状态，0=启用，1=停用")
    private Integer deleteFlag;

    public UserAdminDto() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAccountUserId() { return accountUserId; }
    public void setAccountUserId(String accountUserId) { this.accountUserId = accountUserId; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getDeleteFlag() { return deleteFlag; }
    public void setDeleteFlag(Integer deleteFlag) { this.deleteFlag = deleteFlag; }
}
