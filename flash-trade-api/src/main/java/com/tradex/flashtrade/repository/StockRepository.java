package com.tradex.flashtrade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradex.flashtrade.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByStockCode(String stockCode);

    Optional<Stock> findByStockCodeAndActiveTrue(String stockCode);

    List<Stock> findByStockCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseAndActiveTrue(
            String stockCode, String stockName, Pageable pageable);
}
