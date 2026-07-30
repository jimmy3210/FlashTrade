package com.tradex.flashtrade.broker.etrade.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.broker.etrade.entity.OrderPreview;

@Repository
public interface OrderPreviewRepository extends JpaRepository<OrderPreview, Long> {

    /**
     * 通过 previewId 查找预览（必须指定 broker_name 以确保唯一性）
     */
    Optional<OrderPreview> findByBrokerNameAndPreviewId(String brokerName, String previewId);

    /**
     * 查找用户在某个账户下的所有有效（未过期、未使用）预览
     */
    List<OrderPreview> findByUserIdAndAccountIdOrderByCreatedAtDesc(Long userId, String accountId);

    /**
     * 查找特定订单关联的预览
     */
    Optional<OrderPreview> findByUsedForOrderId(Long orderId);

    /**
     * 查找所有已过期的预览（用于清理）
     */
    @Query("SELECT p FROM OrderPreview p WHERE p.expiresAt IS NOT NULL " +
            "AND p.expiresAt < CURRENT_TIMESTAMP")
    List<OrderPreview> findAllExpiredPreviews();

    /**
     * 查找未被使用的过期预览
     */
    @Query("SELECT p FROM OrderPreview p WHERE p.expiresAt IS NOT NULL " +
            "AND p.expiresAt < CURRENT_TIMESTAMP " +
            "AND p.usedForOrderId IS NULL")
    List<OrderPreview> findUnusedExpiredPreviews();

    /**
     * 查找用户最近的预览记录（用于列表展示）
     */
    @Query("SELECT p FROM OrderPreview p WHERE p.userId = :userId " +
            "AND p.accountId = :accountId " +
            "ORDER BY p.createdAt DESC LIMIT 10")
    List<OrderPreview> findRecentPreviews(Long userId, String accountId);

    /**
     * 删除过期的预览（清理任务）
     */
    long deleteByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * 统计用户待确认的预览数量
     */
    @Query("SELECT COUNT(p) FROM OrderPreview p WHERE p.userId = :userId " +
            "AND p.usedForOrderId IS NULL " +
            "AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    long countPendingPreviewsForUser(Long userId);
}
