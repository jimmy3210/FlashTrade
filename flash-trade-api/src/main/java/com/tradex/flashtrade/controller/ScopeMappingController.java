package com.tradex.flashtrade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.admin.AccountOwnerMappingRequest;
import com.tradex.flashtrade.dto.admin.CompanyMappingRequest;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchRequest;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchResultDto;
import com.tradex.flashtrade.dto.admin.ScopeMappingStatusDto;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.ScopeMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/company-mappings")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class ScopeMappingController {

    private final ScopeMappingService scopeMappingService;

    public ScopeMappingController(ScopeMappingService scopeMappingService) {
        this.scopeMappingService = scopeMappingService;
    }

    @GetMapping
    @Operation(summary = "查询旧数据映射状态", description = "仅平台管理员可查看未归属有效公司的用户和券商账户")
    public ResponseEntity<ApiResponse<ScopeMappingStatusDto>> status() {
        return ResponseEntity.ok(ApiResponse.success(scopeMappingService.status()));
    }

    @PostMapping("/users/{id}")
    @Operation(summary = "映射旧用户公司", description = "为旧用户分配有效启用公司并记录审计")
    public ResponseEntity<ApiResponse<Void>> mapUser(
            @PathVariable Long id,
            @Valid @RequestBody CompanyMappingRequest request) {
        scopeMappingService.mapUserToCompany(id, request.getCompanyId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/accounts/{id}")
    @Operation(summary = "映射旧券商账户所有者", description = "将旧账户归属给已映射有效公司的用户并记录审计")
    public ResponseEntity<ApiResponse<Void>> mapAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountOwnerMappingRequest request) {
        scopeMappingService.mapAccountOwner(id, request.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量映射旧数据", description = "逐项映射并返回每项成功或失败状态，不会因单项失败回滚其他成功项")
    public ResponseEntity<ApiResponse<ScopeMappingBatchResultDto>> batch(
            @Valid @RequestBody ScopeMappingBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scopeMappingService.batch(request)));
    }
}
