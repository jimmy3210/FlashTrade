package com.tradex.flashtrade.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.ApiResponse;
import com.tradex.flashtrade.dto.admin.CancelOrderRequest;
import com.tradex.flashtrade.dto.admin.MarkOrderRequest;
import com.tradex.flashtrade.dto.admin.OrderAdminDto;
import com.tradex.flashtrade.dto.admin.OrderQueryFilter;
import com.tradex.flashtrade.security.AuthService;
import com.tradex.flashtrade.service.admin.OrderAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单管理 Admin API Controller
 * 提供订单查询、标记、取消等 REST 端点
 */
@RestController
@RequestMapping("/admin/orders")
@Tag(name = "订单管理", description = "后端管理订单相关接口")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {
    
    @Autowired private OrderAdminService orderAdminService;
    @Autowired
    @Qualifier("securityAuthService")
    private AuthService authService;
    
    /**
     * 获取订单列表
     */
    @GetMapping
    @Operation(summary = "获取订单列表", description = "支持分页、搜索、过滤和排序")
    public ResponseEntity<ApiResponse<Page<OrderAdminDto>>> queryOrders(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "每页数量，最多 100", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "用户ID（可选）")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "订单状态（可选）")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "股票代码（可选）")
            @RequestParam(required = false) String symbol,
            
            @Parameter(description = "买卖方向：BUY/SELL（可选）")
            @RequestParam(required = false) String side,
            
            @Parameter(description = "风险标记：NORMAL/SUSPICIOUS/BLOCKED（可选）")
            @RequestParam(required = false) String riskFlag,
            
            @Parameter(description = "开始日期（可选），格式: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "结束日期（可选），格式: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "排序字段", example = "created_at")
            @RequestParam(defaultValue = "created_at") String sortBy,
            
            @Parameter(description = "排序顺序：asc/desc", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        log.info("📋 Admin querying orders: page={}, pageSize={}, userId={}, status={}", 
                page, pageSize, userId, status);
        
        OrderQueryFilter filter = OrderQueryFilter.builder()
            .userId(userId)
            .status(status)
            .symbol(symbol)
            .side(side)
            .riskFlag(riskFlag)
            .startDate(startDate)
            .endDate(endDate)
            .sortBy(sortBy)
            .sortOrder(sortOrder)
            .build();
        
        Page<OrderAdminDto> result = orderAdminService.queryOrders(page, pageSize, filter);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "获取指定订单的完整信息")
    public ResponseEntity<ApiResponse<OrderAdminDto>> getOrderDetail(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId) {
        
        log.info("📄 Fetching order detail: orderId={}", orderId);
        
        OrderAdminDto order = orderAdminService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * 标记订单（风险标记或备注）
     */
    @PutMapping("/{orderId}/mark")
    @Operation(summary = "标记订单", description = "为订单添加风险标记和管理员备注")
    public ResponseEntity<ApiResponse<OrderAdminDto>> markOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId,
            
            @RequestBody @Valid MarkOrderRequest request) {
        
        Long adminId = authService.getCurrentUserId();
        log.warn("📝 Admin marking order: orderId={}, riskFlag={}, adminId={}", 
                orderId, request.getRiskFlag(), adminId);
        
        OrderAdminDto result = orderAdminService.markOrder(orderId, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 取消订单
     */
    @DeleteMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "管理员取消异常订单（仅对待成交订单有效）")
    public ResponseEntity<ApiResponse<OrderAdminDto>> cancelOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId,
            
            @RequestBody @Valid CancelOrderRequest request) {
        
        Long adminId = authService.getCurrentUserId();
        log.warn("⚠️  Admin cancelling order: orderId={}, reason={}, adminId={}", 
                orderId, request.getReason(), adminId);
        
        OrderAdminDto result = orderAdminService.cancelOrder(orderId, request, adminId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result));
    }
}
