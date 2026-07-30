package com.tradex.flashtrade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.OrderFill;

@Repository
public interface OrderFillRepository extends JpaRepository<OrderFill, Long> {

    /**
     * 查找订单的所有成交记录
     */
    List<OrderFill> findByOrderIdOrderByFillSequenceAsc(Long orderId);

    /**
     * 查找订单最后一笔成交
     */
    @Query("SELECT f FROM OrderFill f WHERE f.orderId = :orderId " +
            "ORDER BY f.fillSequence DESC LIMIT 1")
    Optional<OrderFill> findLastFillByOrderId(Long orderId);

    /**
     * 通过外部成交 ID 查找成交记录
     */
    Optional<OrderFill> findByOrderIdAndExternalFillId(Long orderId, String externalFillId);

    /**
     * 获取订单的累积成交量
     */
    @Query("SELECT COALESCE(SUM(f.fillQuantity), 0) FROM OrderFill f WHERE f.orderId = :orderId")
    BigDecimal sumFilledQuantityByOrderId(Long orderId);

    /**
     * 获取订单的累积成交金额
     */
    @Query("SELECT COALESCE(SUM(f.fillValue), 0) FROM OrderFill f WHERE f.orderId = :orderId")
    BigDecimal sumFilledValueByOrderId(Long orderId);

    /**
     * 获取订单的累积佣金
     */
    @Query("SELECT COALESCE(SUM(f.fillCommission), 0) FROM OrderFill f WHERE f.orderId = :orderId")
    BigDecimal sumCommissionByOrderId(Long orderId);

    /**
     * 获取订单的平均成交价格
     */
    @Query("SELECT COALESCE(SUM(f.fillValue), 0) / NULLIF(SUM(f.fillQuantity), 0) " +
            "FROM OrderFill f WHERE f.orderId = :orderId")
    BigDecimal getAverageFillPriceByOrderId(Long orderId);

    /**
     * 查找时间段内的所有成交（用于报表）
     */
    List<OrderFill> findByFilledAtBetweenOrderByFilledAtDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找用户在时间段内的所有成交（需要关联 Order 表）
     */
    @Query("SELECT f FROM OrderFill f " +
            "JOIN Order o ON f.orderId = o.id " +
            "WHERE o.userId = :userId " +
            "AND f.filledAt BETWEEN :startTime AND :endTime " +
            "ORDER BY f.filledAt DESC")
    List<OrderFill> findUserTradesInPeriod(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找市场价格异常的成交（滑点分析）
     * 用于分析执行质量
     */
    @Query("SELECT f FROM OrderFill f WHERE f.orderId = :orderId " +
            "AND ABS(f.fillPrice - f.marketPrice) / f.marketPrice > :slippageThreshold")
    List<OrderFill> findAbnormalSlippageFills(Long orderId, BigDecimal slippageThreshold);

    /**
     * 删除旧的成交记录（归档/清理）
     */
    long deleteByFilledAtBefore(LocalDateTime dateTime);

    /**
     * 统计订单成交笔数
     */
    @Query("SELECT COUNT(f) FROM OrderFill f WHERE f.orderId = :orderId")
    long countFillsByOrderId(Long orderId);

    /**
     * 检查订单是否有任何成交
     */
    @Query("SELECT COUNT(f) > 0 FROM OrderFill f WHERE f.orderId = :orderId")
    boolean existsAnyFillForOrder(Long orderId);
}
