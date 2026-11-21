package com.tradex.flashtrade.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "修改用户密码请求")
public class UserPasswordRequest {

    @Schema(description = "新密码")
    @NotBlank
    @Size(min = 6, max = 100, message = "密码长度须在 6~100 字符之间")
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
