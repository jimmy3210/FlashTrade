package com.tradex.flashtrade.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;

import com.tradex.flashtrade.dto.stock.StockResponse;
import com.tradex.flashtrade.entity.Stock;
import com.tradex.flashtrade.repository.StockRepository;

class StockServiceTest {

    private final StockRepository repository = Mockito.mock(StockRepository.class);
    private final StockService stockService = new StockService(repository);

    @Test
    void searchStocks_returnsMatchingWhitelistResults() {
        Stock stock = new Stock();
        stock.setStockCode("AAPL");
        stock.setStockName("Apple Inc.");
        stock.setExchange("NASDAQ");
        stock.setActive(true);

        Mockito.when(repository.findByStockCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseAndActiveTrue(
                        eq("AAPL"), eq("AAPL"), any(PageRequest.class)))
                .thenReturn(List.of(stock));

        List<StockResponse> result = stockService.searchStocks("AAPL");

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("Apple Inc.", result.get(0).getCompanyName());
    }
}
