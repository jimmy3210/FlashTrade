package com.tradex.flashtrade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradex.flashtrade.entity.Watchlist;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderBySortOrderAsc(Long userId);

    Optional<Watchlist> findByUserIdAndSymbol(Long userId, String symbol);

    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    void deleteByUserIdAndSymbol(Long userId, String symbol);
}
