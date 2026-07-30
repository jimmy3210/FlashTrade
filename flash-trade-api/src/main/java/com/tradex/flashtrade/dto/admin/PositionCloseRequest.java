package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "平仓请求")
public class PositionCloseRequest {

    @Schema(description = "用户 ID")
    @NotNull
    private Long userId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
