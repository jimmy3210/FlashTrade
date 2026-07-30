package com.tradex.flashtrade.repository;

import com.tradex.flashtrade.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByAccountIdOrderByTradedAtDesc(String accountId, Pageable pageable);

    List<Trade> findByAccountIdAndTradedAtBetweenOrderByTradedAtDesc(
            String accountId, LocalDateTime from, LocalDateTime to);

    List<Trade> findByOrderId(Long orderId);

    /** 按用户查询成交列表（不分页） */
    List<Trade> findByUserIdOrderByTradedAtDesc(Long userId);

    /** 按用户 + 券商查询成交列表 */
    List<Trade> findByUserIdAndBrokerNameOrderByTradedAtDesc(Long userId, String brokerName);

    /** Upsert 用：按外部 tradeId + 券商名查询 */
    Optional<Trade> findByExternalTradeIdAndBrokerName(String externalTradeId, String brokerName);

    /** 聚合用：按用户 + 账户查询全部成交 */
    List<Trade> findByUserIdAndAccountIdOrderByTradedAtAsc(Long userId, String accountId);

    /** 快照用：按账户 + 日期范围查询 */
    List<Trade> findByAccountIdAndTradedAtBetweenOrderByTradedAtAsc(
            String accountId, java.time.LocalDateTime from, java.time.LocalDateTime to);
}
