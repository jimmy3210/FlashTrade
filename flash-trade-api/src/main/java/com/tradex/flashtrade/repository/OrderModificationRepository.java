package com.tradex.flashtrade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.OrderModification;

@Repository
public interface OrderModificationRepository extends JpaRepository<OrderModification, Long> {

    /**
     * 查找订单的所有修改历史
     */
    List<OrderModification> findByOrderIdOrderByModificationSequenceAsc(Long orderId);

    /**
     * 查找订单最后一次修改
     */
    @Query("SELECT m FROM OrderModification m WHERE m.orderId = :orderId " +
            "ORDER BY m.modificationSequence DESC LIMIT 1")
    OrderModification findLastModificationByOrderId(Long orderId);

    /**
     * 查找所有失败的修改
     */
    List<OrderModification> findByStatusOrderByModifiedAtDesc(String status);

    /**
     * 查找未处理的修改（等待响应中）
     */
    @Query("SELECT m FROM OrderModification m WHERE m.status = 'PENDING' " +
            "ORDER BY m.modifiedAt ASC")
    List<OrderModification> findPendingModifications();

    /**
     * 查找特定外部订单 ID 对应的修改
     */
    @Query("SELECT m FROM OrderModification m WHERE m.externalOrderId = :externalOrderId " +
            "ORDER BY m.modificationSequence DESC")
    List<OrderModification> findByExternalOrderId(String externalOrderId);

    /**
     * 统计订单的修改次数
     */
    @Query("SELECT COUNT(m) FROM OrderModification m WHERE m.orderId = :orderId")
    long countModificationsByOrderId(Long orderId);

    /**
     * 查找某个类型的所有修改（用于分析）
     */
    @Query("SELECT m FROM OrderModification m WHERE m.modificationType = :type " +
            "ORDER BY m.modifiedAt DESC LIMIT 50")
    List<OrderModification> findRecentModificationsByType(String type);
}
