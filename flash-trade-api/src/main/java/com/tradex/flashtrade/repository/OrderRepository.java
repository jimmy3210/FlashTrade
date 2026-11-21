package com.tradex.flashtrade.repository;

import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, OrderStatus status, Pageable pageable);

    /** 不分页：获取全部委托（用于 paginated=false） */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 不分页：指定状态获取全部委托 */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    Optional<Order> findByExternalOrderIdAndBrokerName(
            String externalOrderId, String brokerName);

    Optional<Order> findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
            Long userId, String accountId, String brokerName, String clientOrderId);

    /** 聚合用：按用户 + 账户 + 状态列表查询委托 */
    List<Order> findByUserIdAndAccountIdAndStatusInOrderByCreatedAtDesc(
            Long userId, String accountId, List<OrderStatus> statuses);

}
