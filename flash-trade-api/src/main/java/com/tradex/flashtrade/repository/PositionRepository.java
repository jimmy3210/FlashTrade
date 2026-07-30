package com.tradex.flashtrade.repository;

import com.tradex.flashtrade.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByUserIdOrderBySymbolAsc(Long userId);

    List<Position> findByUserIdAndAccountId(Long userId, String accountId);

    Optional<Position> findByUserIdAndAccountIdAndSymbolAndBrokerName(
            Long userId, String accountId, String symbol, String brokerName);

    List<Position> findByUserIdAndSymbolOrderBySymbolAsc(Long userId, String symbol);

    List<Position> findByUserIdAndAccountIdAndSymbolOrderBySymbolAsc(Long userId, String accountId, String symbol);

    List<Position> findByAccountId(String accountId);
}
