package com.tradex.flashtrade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * 订单修改历史表 - 记录订单修改（Replace Order）操作
 * 
 * etrade 的 Replace Order API 会生成一个新的订单（新的 externalOrderId），
 * 本表用于记录修改历史，维护原订单和新订单的关联关系。
 * 
 * 修改流程：
 * 1. 用户要求修改订单（改价格、改数量等）
 * 2. 系统调用 Replace Order API，获得新的 orderId
 * 3. 创建 OrderModification 记录
 * 4. 旧订单被标记为 REPLACED，新订单创建为 PENDING
 */
@Entity
@Table(
    name = "order_modifications",
    indexes = {
        @Index(name = "idx_mod_order_id", columnList = "order_id"),
        @Index(name = "idx_mod_sequence", columnList = "order_id,modification_sequence"),
        @Index(name = "idx_mod_status", columnList = "status"),
        @Index(name = "idx_mod_modified_at", columnList = "modified_at")
    }
)
public class OrderModification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 原订单 ID（本地数据库）
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 修改序列号（同一订单可能多次修改）
     * 例如：1st modification = 1, 2nd modification = 2
     */
    @Column(name = "modification_sequence", nullable = false)
    private Integer modificationSequence;

    /**
     * 修改类型
     * PRICE_CHANGE: 改变订单价格
     * QUANTITY_CHANGE: 改变订单数量
     * BOTH: 同时改变价格和数量
     * TIME_IN_FORCE_CHANGE: 改变时间类型
     */
    @Column(name = "modification_type", nullable = false, length = 50)
    private String modificationType;

    // ===== 修改前的值 =====
    @Column(name = "original_price", precision = 15, scale = 4)
    private BigDecimal originalPrice;

    @Column(name = "original_quantity", precision = 15, scale = 4)
    private BigDecimal originalQuantity;

    @Column(name = "original_time_in_force", length = 20)
    private String originalTimeInForce;

    // ===== 修改后的值 =====
    @Column(name = "new_price", precision = 15, scale = 4)
    private BigDecimal newPrice;

    @Column(name = "new_quantity", precision = 15, scale = 4)
    private BigDecimal newQuantity;

    @Column(name = "new_time_in_force", length = 20)
    private String newTimeInForce;

    // ===== 修改结果 =====
    /**
     * 修改后得到的新外部订单 ID（来自 etrade）
     * 因为 etrade Replace Order 生成新的 orderId
     */
    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    /**
     * 新订单 ID（本地数据库）
     * 修改成功后，会创建一个新的 Order 记录，此字段记录其 ID
     */
    @Column(name = "new_order_id")
    private Long newOrderId;

    /**
     * 修改状态
     * PENDING: 修改请求已提交，等待响应
     * SUCCESS: 修改成功，新订单已创建
     * FAILED: 修改失败
     * CANCELLED: 修改被取消
     */
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String status = "PENDING";

    /**
     * 失败原因或错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * etrade 返回的错误代码
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    // ===== 时间戳 =====
    /**
     * 修改请求发起时间
     */
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    /**
     * 修改请求被处理的时间（收到响应时）
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Business Logic Methods =====

    /**
     * 检查修改是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * 检查修改是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 检查修改是否仍在处理中
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * 获取处理耗时（毫秒）
     */
    public Long getProcessingTimeMs() {
        if (modifiedAt == null || processedAt == null) {
            return null;
        }
        return java.time.Duration.between(modifiedAt, processedAt).toMillis();
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Integer getModificationSequence() { return modificationSequence; }
    public void setModificationSequence(Integer modificationSequence) { this.modificationSequence = modificationSequence; }

    public String getModificationType() { return modificationType; }
    public void setModificationType(String modificationType) { this.modificationType = modificationType; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(BigDecimal originalQuantity) { this.originalQuantity = originalQuantity; }

    public String getOriginalTimeInForce() { return originalTimeInForce; }
    public void setOriginalTimeInForce(String originalTimeInForce) { this.originalTimeInForce = originalTimeInForce; }

    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }

    public BigDecimal getNewQuantity() { return newQuantity; }
    public void setNewQuantity(BigDecimal newQuantity) { this.newQuantity = newQuantity; }

    public String getNewTimeInForce() { return newTimeInForce; }
    public void setNewTimeInForce(String newTimeInForce) { this.newTimeInForce = newTimeInForce; }

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public Long getNewOrderId() { return newOrderId; }
    public void setNewOrderId(Long newOrderId) { this.newOrderId = newOrderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "OrderModification{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", modificationSequence=" + modificationSequence +
                ", modificationType='" + modificationType + '\'' +
                ", status='" + status + '\'' +
                ", modifiedAt=" + modifiedAt +
                '}';
    }
}
