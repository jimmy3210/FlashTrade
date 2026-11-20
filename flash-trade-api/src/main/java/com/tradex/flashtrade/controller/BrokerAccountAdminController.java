package com.tradex.flashtrade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.admin.BrokerAccountDto;
import com.tradex.flashtrade.dto.admin.BrokerAccountRequest;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.BrokerAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/broker-accounts")
@Tag(name = "券商账户管理", description = "管理员券商账户 CRUD 接口")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'SUPER_ADMIN')")
public class BrokerAccountAdminController {

    private final BrokerAccountService brokerAccountService;

    public BrokerAccountAdminController(BrokerAccountService brokerAccountService) {
        this.brokerAccountService = brokerAccountService;
    }

    @GetMapping
    @Operation(summary = "券商账户列表", description = "查询所有用户的券商账户")
    public ResponseEntity<ApiResponse<List<BrokerAccountDto>>> listBrokerAccounts() {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.listAccounts()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询券商账户", description = "根据券商账户 ID 返回详细信息")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> getBrokerAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.getAccount(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "创建券商账户", description = "新建用户券商交易账户并关联 API 环境")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> createBrokerAccount(
            @Valid @RequestBody BrokerAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.createAccount(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新券商账户", description = "更新券商账户信息和 API 环境配置")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> updateBrokerAccount(
            @PathVariable Long id,
            @Valid @RequestBody BrokerAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.updateAccount(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "停用券商账户", description = "将指定券商账户状态标记为 CLOSED")
    public ResponseEntity<ApiResponse<Void>> deleteBrokerAccount(@PathVariable Long id) {
        brokerAccountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/user/{userId}/current")
    @Operation(summary = "获取用户当前绑定的券商账户", description = "查询指定用户当前绑定的券商账户信息")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> getCurrentAccountForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.getCurrentAccountForUser(userId)));
    }

    @PostMapping("/user/{userId}/bind/{accountId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "用户切换/绑定券商账户", description = "为用户设置当前绑定的券商账户（同一账户只能绑定一个用户）")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> setCurrentAccountForUser(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(brokerAccountService.setCurrentAccountForUser(userId, accountId)));
    }
}
