package com.tradex.flashtrade.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.dto.admin.CancelOrderRequest;
import com.tradex.flashtrade.dto.admin.MarkOrderRequest;
import com.tradex.flashtrade.dto.admin.OrderAdminDto;
import com.tradex.flashtrade.dto.admin.OrderQueryFilter;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单管理 Service
 * 提供订单查询、标记、取消等功能
 */
@Service
@Slf4j
public class OrderAdminService {

    private static final Map<String, String> ORDER_SORT_PROPERTIES = Map.of(
            "created_at", "createdAt",
            "updated_at", "updatedAt",
            "quantity", "quantity",
            "price", "limitPrice",
            "id", "id",
            "symbol", "symbol");
    
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogger auditLogger;
    
    /**
     * 分页查询订单列表
     */
    public Page<OrderAdminDto> queryOrders(int page, int pageSize, OrderQueryFilter filter) {
        log.info("📋 Admin querying orders: page={}, pageSize={}, userId={}, status={}, symbol={}, riskFlag={}", 
                page, pageSize, filter.getUserId(), filter.getStatus(), filter.getSymbol(), filter.getRiskFlag());
        
        try {
            // 构建排序条件
            Direction direction = "asc".equalsIgnoreCase(filter.getSortOrder()) 
                ? Direction.ASC : Direction.DESC;
            String requestedSort = filter.getSortBy();
            String sortProperty = ORDER_SORT_PROPERTIES.getOrDefault(
                    requestedSort == null ? "" : requestedSort, "createdAt");
            Sort sort = Sort.by(direction, sortProperty);
            Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
            
            // 构建查询条件
            Specification<Order> spec = (root, query, cb) -> {
                java.util.List<Predicate> predicates = new java.util.ArrayList<>();
                
                if (filter.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
                }
                
                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    try {
                        OrderStatus statusEnum = OrderStatus.valueOf(filter.getStatus());
                        predicates.add(cb.equal(root.get("status"), statusEnum));
                    } catch (IllegalArgumentException ex) {
                        // ignore invalid enum value
                    }
                }
                
                if (filter.getSymbol() != null && !filter.getSymbol().isEmpty()) {
                    predicates.add(cb.like(root.get("symbol"), "%" + filter.getSymbol() + "%"));
                }
                
                if (filter.getSide() != null && !filter.getSide().isEmpty()) {
                    try {
                        OrderSide sideEnum = OrderSide.valueOf(filter.getSide());
                        predicates.add(cb.equal(root.get("side"), sideEnum));
                    } catch (IllegalArgumentException ex) {
                        // ignore invalid enum value
                    }
                }
                
                if (filter.getRiskFlag() != null && !filter.getRiskFlag().isEmpty()) {
                    predicates.add(cb.equal(root.get("riskFlag"), filter.getRiskFlag()));
                }
                
                if (filter.getStartDate() != null) {
                    LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
                }
                
                if (filter.getEndDate() != null) {
                    LocalDateTime endDateTime = filter.getEndDate().atTime(LocalTime.MAX);
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            Page<Order> orderPage = orderRepository.findAll(spec, pageable);
            
            log.info("✅ Orders queried successfully: page={}, totalElements={}, totalPages={}", 
                    page, orderPage.getTotalElements(), orderPage.getTotalPages());
            
            return orderPage.map(this::convertToAdminDto);
            
        } catch (Exception e) {
            log.error("❌ Error querying orders: {}", e.getMessage(), e);
                auditLogger.logSystemEvent("ORDER_QUERY_ERROR", "HIGH", "OrderAdminService",
                    "订单查询失败: " + e.getMessage(), null, null, null, "query_orders");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "查询订单失败");
        }
    }
    
    /**
     * 获取订单详情
     */
    public OrderAdminDto getOrderDetail(Long orderId) {
        log.info("📄 Fetching order detail: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> {
                log.error("❌ Order not found: orderId={}", orderId);
                return new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
            });
        
        return convertToAdminDto(order);
    }
    
    /**
     * 标记订单（风险标记或备注）
     */
    @Transactional
    public OrderAdminDto markOrder(Long orderId, MarkOrderRequest request, Long adminId) {
        log.warn("📝 Admin marking order: orderId={}, riskFlag={}, adminId={}, timestamp={}", 
                orderId, request.getRiskFlag(), adminId, LocalDateTime.now());
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> {
                log.error("❌ Order not found for marking: orderId={}", orderId);
                return new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
            });
        
        String oldFlag = order.getRiskFlag();
        String oldNote = order.getAdminNote();
        
        order.setRiskFlag(request.getRiskFlag());
        order.setAdminNote(request.getAdminNote());
        order.setAdminId(adminId);
        order.setAdminActionTime(LocalDateTime.now());
        
        Order updated = orderRepository.save(order);
        
        log.info("✅ Order marked successfully: orderId={}, oldFlag={}, newFlag={}, oldNote={}, newNote={}", 
                orderId, oldFlag, request.getRiskFlag(), oldNote, request.getAdminNote());
        
        // 记录操作日志
        try {
            auditLogger.logOperation(adminId, null, "MARK_ORDER", "ORDER", orderId, 
                    String.valueOf(orderId),
                    java.util.Map.of("riskFlag", oldFlag, "adminNote", oldNote),
                    java.util.Map.of("riskFlag", request.getRiskFlag(), "adminNote", request.getAdminNote()),
                    "标记订单: " + oldFlag + " -> " + request.getRiskFlag(),
                    null, null, null, 200, null);
        } catch (Exception e) {
            log.error("Failed to log operation", e);
        }
        
        return convertToAdminDto(updated);
    }
    
    /**
     * 撤单结果核验能力完成前拒绝取消请求。
     */
    public OrderAdminDto cancelOrder(Long orderId, CancelOrderRequest request, Long adminId) {
        log.warn("Admin order cancellation rejected pending broker-side verification: orderId={}, adminId={}",
                orderId, adminId);
        throw new BusinessException(ErrorCode.INVALID_STATE,
                "订单撤单已暂停，待补齐券商侧执行结果核验和审计能力");
    }
    
    /**
     * 转换为 Admin DTO
     */
    private OrderAdminDto convertToAdminDto(Order order) {
        User user = order.getUser();
        
        return OrderAdminDto.builder()
            .id(order.getId())
            .userId(user != null ? user.getId() : null)
            .username(user != null ? user.getUsername() : null)
            .brokerAccountId(order.getBrokerAccountId())
            .brokerName(order.getBrokerName())
            .symbol(order.getSymbol())
            .side(order.getSide() != null ? order.getSide().toString() : null)
            .quantity(order.getQuantity() != null ? order.getQuantity().intValue() : null)
            .orderType(order.getOrderType())
            .limitPrice(order.getLimitPrice())
            .status(order.getStatus() != null ? order.getStatus().toString() : null)
            .filledQuantity(order.getFilledQuantity() != null ? order.getFilledQuantity().intValue() : null)
            .averagePrice(order.getAveragePrice())
            .totalValue(order.getTotalValue())
            .commission(order.getCommission())
            .externalOrderId(order.getExternalOrderId())
            .previewId(order.getPreviewId())
            .riskFlag(order.getRiskFlag())
            .adminNote(order.getAdminNote())
            .adminId(order.getAdminId())
            .adminActionTime(order.getAdminActionTime())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
