package com.tradex.flashtrade.broker.etrade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 订单预览表 - 存储 etrade Order Preview（Dry Run）的结果
 * 
 * etrade 的订单流程强制要求：
 * 1. 先调用 Preview API（Dry Run）获取 previewId
 * 2. 用户确认预览结果（费用、保证金等）
 * 3. 再调用 Place Order API 提交订单
 * 
 * 本表用于存储第一步的结果，支持用户确认和过期管理。
 */
@Entity
@Table(
    name = "order_previews",
    indexes = {
        @Index(name = "idx_preview_user_id", columnList = "user_id"),
        @Index(name = "idx_preview_account_id", columnList = "account_id"),
        @Index(name = "idx_preview_expires_at", columnList = "expires_at"),
        @Index(name = "idx_preview_created_at", columnList = "created_at"),
        @Index(name = "idx_preview_order_id", columnList = "used_for_order_id")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uk_preview_id",
        columnNames = {"broker_name", "preview_id"}
    )
)
public class OrderPreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    /**
     * 券商返回的预览 ID - etrade 返回，用于后续下单
     * 必须是 UNIQUE 的，因为下单时需要指定这个 previewId
     */
    @Column(name = "preview_id", nullable = false, length = 100)
    private String previewId;

    // ===== 订单基本信息 =====
    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;  // BUY / SELL

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "order_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'MARKET'")
    private String orderType = "MARKET";  // MARKET / LIMIT / STOP / STOP_LIMIT

    @Column(precision = 15, scale = 4)
    private BigDecimal price;  // 限价单的价格

    @Column(name = "stop_price", precision = 15, scale = 4)
    private BigDecimal stopPrice;  // 止损价

    @Column(name = "time_in_force", nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'DAY'")
    private String timeInForce = "DAY";  // DAY / GTC / IOC / FOK

    // ===== 预估费用（来自 Preview API） =====
    @Column(name = "estimated_commission", precision = 15, scale = 4)
    private BigDecimal estimatedCommission;  // 预估佣金

    @Column(name = "estimated_tax", precision = 15, scale = 4)
    private BigDecimal estimatedTax;  // 预估税费

    @Column(name = "estimated_fees", precision = 15, scale = 4)
    private BigDecimal estimatedFees;  // 预估其他费用

    @Column(name = "estimated_total", precision = 15, scale = 4)
    private BigDecimal estimatedTotal;  // 预估总费用

    // ===== 保证金要求（Margin 账户） =====
    @Column(name = "required_margin", precision = 15, scale = 4)
    private BigDecimal requiredMargin;  // 该订单所需保证金

    @Column(name = "available_margin", precision = 15, scale = 4)
    private BigDecimal availableMargin;  // 完成订单后剩余可用保证金

    @Column(name = "margin_buy_power", precision = 15, scale = 4)
    private BigDecimal marginBuyPower;  // 保证金购买力

    // ===== 风险和警告信息 =====
    /**
     * 风险警告列表（JSON 格式）
     * 例如：[
     *   {"code": "MARGIN_RISK", "message": "Order would require more margin than available"},
     *   {"code": "IPO_WARNING", "message": "IPO 股票可能受限"}
     * ]
     */
    @Column(name = "warnings", columnDefinition = "JSON")
    private String warnings;

    /**
     * 是否可以下单（基于预览结果）
     * false 可能表示：余额不足、参数错误、风险过高等
     */
    @Column(name = "can_place", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean canPlace = true;

    // ===== 关联关系 =====
    /**
     * 该预览对应的订单 ID（用户确认后，下单时填充）
     * NULL 表示还未被使用
     */
    @Column(name = "used_for_order_id")
    private Long usedForOrderId;

    // ===== 时间戳 =====
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 预览过期时间
     * etrade 的预览通常 30-60 秒后过期，之后需要重新进行 preview
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 预览被使用的时间（下单时填充）
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Business Logic Methods =====

    /**
     * 检查预览是否已过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查预览是否已被使用
     */
    public boolean isUsed() {
        return usedForOrderId != null && usedAt != null;
    }

    /**
     * 检查预览是否仍然有效（未过期且未使用）
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * 标记预览为已使用
     */
    public void markAsUsed(Long orderId) {
        this.usedForOrderId = orderId;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 获取总成本（订单金额 + 佣金 + 税费）
     */
    public BigDecimal getTotalCost() {
        BigDecimal cost = estimatedTotal != null ? estimatedTotal : BigDecimal.ZERO;
        if (estimatedCommission != null) {
            cost = cost.add(estimatedCommission);
        }
        if (estimatedTax != null) {
            cost = cost.add(estimatedTax);
        }
        if (estimatedFees != null) {
            cost = cost.add(estimatedFees);
        }
        return cost;
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getPreviewId() { return previewId; }
    public void setPreviewId(String previewId) { this.previewId = previewId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getStopPrice() { return stopPrice; }
    public void setStopPrice(BigDecimal stopPrice) { this.stopPrice = stopPrice; }

    public String getTimeInForce() { return timeInForce; }
    public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }

    public BigDecimal getEstimatedCommission() { return estimatedCommission; }
    public void setEstimatedCommission(BigDecimal estimatedCommission) { this.estimatedCommission = estimatedCommission; }

    public BigDecimal getEstimatedTax() { return estimatedTax; }
    public void setEstimatedTax(BigDecimal estimatedTax) { this.estimatedTax = estimatedTax; }

    public BigDecimal getEstimatedFees() { return estimatedFees; }
    public void setEstimatedFees(BigDecimal estimatedFees) { this.estimatedFees = estimatedFees; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public BigDecimal getRequiredMargin() { return requiredMargin; }
    public void setRequiredMargin(BigDecimal requiredMargin) { this.requiredMargin = requiredMargin; }

    public BigDecimal getAvailableMargin() { return availableMargin; }
    public void setAvailableMargin(BigDecimal availableMargin) { this.availableMargin = availableMargin; }

    public BigDecimal getMarginBuyPower() { return marginBuyPower; }
    public void setMarginBuyPower(BigDecimal marginBuyPower) { this.marginBuyPower = marginBuyPower; }

    public String getWarnings() { return warnings; }
    public void setWarnings(String warnings) { this.warnings = warnings; }

    public Boolean getCanPlace() { return canPlace; }
    public void setCanPlace(Boolean canPlace) { this.canPlace = canPlace; }

    public Long getUsedForOrderId() { return usedForOrderId; }
    public void setUsedForOrderId(Long usedForOrderId) { this.usedForOrderId = usedForOrderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    @Override
    public String toString() {
        return "OrderPreview{" +
                "id=" + id +
                ", previewId='" + previewId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                ", estimatedTotal=" + estimatedTotal +
                ", isExpired=" + isExpired() +
                '}';
    }
}
