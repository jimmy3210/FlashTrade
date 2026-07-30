package com.tradex.flashtrade.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.broker.dto.BrokerWatchlistItem;
import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.dto.watchlist.WatchlistItemResponse;
import com.tradex.flashtrade.entity.Stock;
import com.tradex.flashtrade.entity.Watchlist;
import com.tradex.flashtrade.repository.StockRepository;
import com.tradex.flashtrade.repository.WatchlistRepository;

@Service
public class WatchlistService {

    private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);

    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final UserLookupService userLookupService;
    private final MarketDataService marketDataService;
    private final BrokerService brokerService;
    private final BrokerSyncProperties brokerSyncProperties;

    public WatchlistService(WatchlistRepository watchlistRepository,
                            StockRepository stockRepository,
                            StockService stockService,
                            UserLookupService userLookupService,
                            MarketDataService marketDataService,
                            BrokerService brokerService,
                            BrokerSyncProperties brokerSyncProperties) {
        this.watchlistRepository = watchlistRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.userLookupService = userLookupService;
        this.marketDataService = marketDataService;
        this.brokerService = brokerService;
        this.brokerSyncProperties = brokerSyncProperties;
    }

    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getWatchlist(String username) {
        Long userId = userLookupService.getUserIdByUsername(username);
        try {
            syncRemoteWatchlistIfAvailable(userId);
        } catch (Exception ex) {
            log.warn("同步远程 Watchlist 失败 userId={} : {}", userId, ex.getMessage());
        }
        return watchlistRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WatchlistItemResponse addToWatchlist(String username, String symbol) {
        Long userId = userLookupService.getUserIdByUsername(username);
        Stock stock = stockService.validateAndRegister(symbol);
        String normalizedSymbol = stock.getStockCode();

        Optional<Watchlist> existing = watchlistRepository.findByUserIdAndSymbol(userId, normalizedSymbol);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(userId);
        watchlist.setSymbol(normalizedSymbol);
        watchlist.setSortOrder(0);
        Watchlist saved = watchlistRepository.save(watchlist);
        return toResponse(saved);
    }

    @Transactional
    public void removeFromWatchlist(String username, String symbol) {
        Long userId = userLookupService.getUserIdByUsername(username);
        watchlistRepository.deleteByUserIdAndSymbol(userId, symbol.toUpperCase());
    }

    private WatchlistItemResponse toResponse(Watchlist watchlist) {
        return stockRepository.findByStockCode(watchlist.getSymbol())
                .map(this::toResponse)
                .orElseGet(() -> new WatchlistItemResponse(
                watchlist.getSymbol(),
                watchlist.getSymbol(),
                "US",
                true,
                null, null, null, null, 0L));
    }

    private void syncRemoteWatchlistIfAvailable(Long userId) {
        brokerSyncProperties.getAccounts().stream()
                .filter(mapping -> mapping.getUserId() != null && mapping.getUserId().equals(userId))
                .filter(mapping -> "TASTYTRADE".equalsIgnoreCase(mapping.getBrokerName()))
                .findFirst()
                .ifPresent(mapping -> {
                    List<BrokerWatchlistItem> remoteItems = brokerService.getWatchlist(mapping.getBrokerName(), mapping.getAccountId());
                    for (BrokerWatchlistItem item : remoteItems) {
                        String normalizedSymbol = stockService.validateAndRegister(item.getSymbol()).getStockCode();
                        if (!watchlistRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
                            Watchlist watchlist = new Watchlist();
                            watchlist.setUserId(userId);
                            watchlist.setBrokerName(mapping.getBrokerName().toUpperCase());
                            watchlist.setSymbol(normalizedSymbol);
                            watchlist.setSortOrder(0);
                            watchlistRepository.save(watchlist);
                        }
                    }
                });
    }

    private WatchlistItemResponse toResponse(Stock stock) {
        // 尝试从缓存读取行情
        var opt = marketDataService.get(stock.getStockCode());
        if (opt.isPresent()) {
            var md = opt.get();
            Double change = null; // 若需计算变化率，可在数据源处提供
            return new WatchlistItemResponse(
                stock.getStockCode(),
                stock.getStockName(),
                stock.getExchange(),
                stock.isActive(),
                md.getLastPrice(),
                md.getBidPrice(),
                md.getAskPrice(),
                change,
                md.getTimestamp());
        }

        return new WatchlistItemResponse(
            stock.getStockCode(),
            stock.getStockName(),
            stock.getExchange(),
            stock.isActive(),
            null, null, null, null, 0L);
    }
}
