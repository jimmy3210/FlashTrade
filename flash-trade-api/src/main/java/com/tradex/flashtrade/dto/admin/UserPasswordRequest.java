package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "修改用户密码请求")
public class UserPasswordRequest {

    @Schema(description = "新密码")
    @NotBlank
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
