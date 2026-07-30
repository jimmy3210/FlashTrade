package com.tradex.flashtrade.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.dto.stock.StockResponse;
import com.tradex.flashtrade.entity.Stock;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.StockRepository;

/**
 * 股票白名单服务（TODO-06）
 * <p>
 * 负责 symbol 校验 + 自动注册：
 * <ol>
 *   <li>查询 {@code t_stock} 是否已存在且为活跃状态（is_active=true）</li>
 *   <li>若已存在且活跃 → 直接返回</li>
 *   <li>若已存在但停用 → 拒绝（抛出 STOCK_NOT_WHITELISTED）</li>
 *   <li>若不存在 → 校验 symbol 格式 → 调用券商 API 验证真实性 → 自动插入</li>
 *   <li>若券商也无法找到 → 拒绝（抛出 STOCK_NOT_FOUND）</li>
 * </ol>
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    /** 美股 symbol 格式：1~5 位大写字母，ETF 允许最多 6 位，特殊如 BRK.B 允许带点 */
    private static final java.util.regex.Pattern SYMBOL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Z]{1,5}([.][A-Z])?$");

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * 校验 symbol 并在 t_stock 中注册（若不存在）。
     *
     * @param symbol 股票代码（大小写不敏感，内部统一转为大写）
     * @return 对应的 {@link Stock} 实体
     * @throws BusinessException STOCK_NOT_FOUND / STOCK_NOT_WHITELISTED
     */
    @Transactional
    public Stock validateAndRegister(String symbol) {
        String code = symbol.toUpperCase().trim();

        Optional<Stock> existing = stockRepository.findByStockCode(code);

        if (existing.isPresent()) {
            Stock stock = existing.get();
            if (!stock.isActive()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_WHITELISTED,
                        "股票代码 [%s] 已停用，不允许操作".formatted(code));
            }
            return stock;
        }

        // 新 symbol：先校验格式
        if (!SYMBOL_PATTERN.matcher(code).matches()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_FOUND,
                    "股票代码格式无效: %s".formatted(code));
        }

        // 尝试通过任意券商 API 验证真实性（Mock 模式下跳过）
        boolean verified = verifyWithBroker(code);
        if (!verified) {
            throw new BusinessException(ErrorCode.STOCK_NOT_FOUND,
                    "券商无法找到股票代码: %s".formatted(code));
        }

        // 自动插入白名单
        Stock stock = new Stock();
        stock.setStockCode(code);
        stock.setStockName(code);          // 名称后续可由行情更新
        stock.setExchange("US");
        stock.setActive(true);
        Stock saved = stockRepository.save(stock);
        log.info("股票代码 [{}] 已自动注册到白名单", code);
        return saved;
    }

    /**
     * 查询 symbol 是否在白名单内（不触发自动注册）。
     */
    public boolean isWhitelisted(String symbol) {
        return stockRepository.findByStockCodeAndActiveTrue(symbol.toUpperCase()).isPresent();
    }

    /**
     * 搜索股票：先按代码/名称模糊匹配白名单，若 query 具有合法 symbol 格式且未命中，则尝试自动注册。
     */
    public List<StockResponse> searchStocks(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String q = query.trim();
        List<Stock> stocks = stockRepository.findByStockCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseAndActiveTrue(
                q, q, PageRequest.of(0, 50));

        if (stocks.isEmpty() && SYMBOL_PATTERN.matcher(q.toUpperCase()).matches()) {
            try {
                Stock registered = validateAndRegister(q);
                stocks = List.of(registered);
            } catch (BusinessException ex) {
                // ignore 未找到 / 未白名单情况，返回空结果
            }
        }

        return stocks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(stock.getStockCode(), stock.getStockName(), stock.getExchange(), stock.isActive());
    }

    // -------------------------------------------------------------------------
    // 内部方法
    // -------------------------------------------------------------------------

    /**
     * 通过券商 API 验证 symbol 是否真实存在。
     * <p>
     * 现阶段：格式校验通过即视为存在（Mock 模式）。
     * 真实模式下应调用一个支持 getBatchMarketData 的账户实际查询。
     */
    private boolean verifyWithBroker(String code) {
        // 真实模式扩展点：调用 brokerClients 中任一实现进行验证
        // 现阶段 Mock 模式下，格式正确即视为存在
        log.debug("symbol [{}] 格式校验通过，视为存在（可扩展为真实 API 调用）", code);
        return true;
    }
}
