package com.tradex.flashtrade.controller.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单的券商 REST Mock 控制器。
 * <p>示例: GET /mock/SCHWAB/market?symbols=AAPL,MSFT
 */
@RestController
@RequestMapping("/mock")
public class MockBrokerController {

    private static final Logger log = LoggerFactory.getLogger(MockBrokerController.class);

    @GetMapping("/{broker}/market")
    public ResponseEntity<List<Map<String, Object>>> marketSnapshot(
            @PathVariable String broker,
            @RequestParam(required = false, name = "symbols") String symbolsCsv) {

        List<String> symbols = new ArrayList<>();
        if (symbolsCsv != null && !symbolsCsv.isBlank()) {
            for (String s : symbolsCsv.split(",")) {
                if (!s.isBlank()) symbols.add(s.trim().toUpperCase());
            }
        }

        List<Map<String, Object>> resp = new ArrayList<>();
        for (String sym : symbols) {
            double base = 10 + Math.abs(sym.hashCode()) % 490; // 10..499
            double delta = (Math.random() - 0.5) * base * 0.03; // ±1.5%
            BigDecimal last = BigDecimal.valueOf(base + delta).setScale(2, RoundingMode.HALF_UP);
            BigDecimal bid = last.subtract(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ask = last.add(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
            double changePercent = (delta / base) * 100.0;

            Map<String, Object> item = new HashMap<>();
            item.put("broker", broker.toUpperCase());
            item.put("symbol", sym);
            item.put("lastPrice", last);
            item.put("bidPrice", bid);
            item.put("askPrice", ask);
            item.put("changePercent", String.format("%.2f", changePercent));
            item.put("timestamp", System.currentTimeMillis());
            resp.add(item);
        }

        log.debug("Mock market snapshot for broker={} symbols={} returned {} items", broker, symbols, resp.size());
        return ResponseEntity.ok(resp);
    }
}
