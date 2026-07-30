package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.admin.UserAdminDto;
import com.tradex.flashtrade.dto.admin.UserCreateRequest;
import com.tradex.flashtrade.dto.admin.UserPasswordRequest;
import com.tradex.flashtrade.dto.admin.UserStatusRequest;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "管理员用户管理接口")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final AdminUserService adminUserService;

    public UserAdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "查询用户列表", description = "返回系统用户及其默认券商账户信息")
    public ResponseEntity<ApiResponse<List<UserAdminDto>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.listUsers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询单个用户", description = "根据用户 ID 返回用户和默认券商账户信息")
    public ResponseEntity<ApiResponse<UserAdminDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUser(id)));
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "新建用户并关联一个默认券商账户")
    public ResponseEntity<ApiResponse<UserAdminDto>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.createUser(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "修改用户信息、默认券商账户或密码")
    public ResponseEntity<ApiResponse<UserAdminDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.updateUser(id, request)));
    }

    @PostMapping("/status")
    @Operation(summary = "切换用户启用状态", description = "根据 deleteFlag 对用户启用或停用")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @Valid @RequestBody UserStatusRequest request) {
        adminUserService.setEnabled(request.getUserId(), request.getDeleteFlag() == 0);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/password")
    @Operation(summary = "修改用户密码", description = "更新指定用户的登录密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordRequest request) {
        adminUserService.changePassword(id, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
