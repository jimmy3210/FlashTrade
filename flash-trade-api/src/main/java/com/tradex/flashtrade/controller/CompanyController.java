package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.admin.CompanyCreateRequest;
import com.tradex.flashtrade.dto.admin.CompanyDto;
import com.tradex.flashtrade.dto.admin.CompanyStatusRequest;
import com.tradex.flashtrade.dto.admin.CompanyUpdateRequest;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/companies")
@Tag(name = "公司管理", description = "公司/租户管理接口")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'SUPER_ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(summary = "公司列表", description = "查询公司/租户（支持分页与名称搜索 q）")
    public ResponseEntity<ApiResponse<com.tradex.flashtrade.dto.admin.CompanyListResponse>> listCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success(companyService.listCompanies(page, size, q)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询公司", description = "按 ID 查询公司详情")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompany(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "新增公司", description = "创建公司/租户")
    public ResponseEntity<ApiResponse<CompanyDto>> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.createCompany(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新公司", description = "修改公司名称或状态")
    public ResponseEntity<ApiResponse<CompanyDto>> updateCompany(@PathVariable Long id,
                                                               @Valid @RequestBody CompanyUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.updateCompany(id, request)));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "切换公司状态", description = "启用/停用公司")
    public ResponseEntity<ApiResponse<Void>> updateCompanyStatus(@PathVariable Long id,
                                                                @Valid @RequestBody CompanyStatusRequest request) {
        companyService.setCompanyStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "删除公司", description = "删除公司及其关联组")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
