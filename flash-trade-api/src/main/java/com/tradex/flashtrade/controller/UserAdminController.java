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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "管理员用户管理接口")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "创建用户", description = "新建交易用户并关联有效公司及默认券商账户")
    public ResponseEntity<ApiResponse<UserAdminDto>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.createUser(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新用户", description = "修改用户信息、默认券商账户或密码")
    public ResponseEntity<ApiResponse<UserAdminDto>> updateUser(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UserCreateRequest request) {
        assertCanManageTarget(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(adminUserService.updateUser(id, request)));
    }

    @PostMapping("/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "切换用户启用状态", description = "根据 deleteFlag 对用户启用或停用")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            Authentication authentication,
            @Valid @RequestBody UserStatusRequest request) {
        assertCanManageTarget(request.getUserId(), authentication);
        adminUserService.setEnabled(request.getUserId(), request.getDeleteFlag() == 0);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "修改用户密码", description = "更新指定用户的登录密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UserPasswordRequest request) {
        assertCanManageTarget(id, authentication);
        adminUserService.changePassword(id, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void assertCanManageTarget(Long userId, Authentication authentication) {
        UserAdminDto target = adminUserService.getUser(userId);
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
        if ("ROLE_SUPER_ADMIN".equals(target.getRole()) && !isSuperAdmin) {
            throw new AccessDeniedException("Only a super-admin can manage another super-admin");
        }
    }
}
