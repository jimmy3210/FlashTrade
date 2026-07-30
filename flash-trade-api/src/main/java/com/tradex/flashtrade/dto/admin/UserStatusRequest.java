package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "修改用户启用/停用状态请求")
public class UserStatusRequest {

    @Schema(description = "用户 ID")
    @NotNull
    private Long userId;

    @Schema(description = "删除标志，0=启用，1=停用")
    @NotNull
    private Integer deleteFlag;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getDeleteFlag() { return deleteFlag; }
    public void setDeleteFlag(Integer deleteFlag) { this.deleteFlag = deleteFlag; }
}
