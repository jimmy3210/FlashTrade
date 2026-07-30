package com.tradex.flashtrade.service;

import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.dto.portfolio.PositionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * 每日盘后快照服务
 * <p>
 * 在每个交易日收盘后（美东时间 17:00 周一至周五），
 * 将当日聚合结果持久化到以下三张表：
 * <ul>
 *   <li>{@code t_daily_pnl_stock}  — 每只股票的每日盈亏快照</li>
 *   <li>{@code t_daily_pnl_account} — 每个账户的每日汇总</li>
 *   <li>{@code t_position_history}  — 隔夜持仓滚存（供次日计算基准）</li>
 * </ul>
 *
 * <p>隔夜持仓处理规则：unrealizedPnl 每次按当前市价实时重算，不从历史累加。
 */
@Service
public class DailySnapshotService {

    private static final Logger log = LoggerFactory.getLogger(DailySnapshotService.class);

    private final JdbcTemplate               jdbc;
    private final BrokerSyncProperties       syncProperties;
    private final PositionAggregatorService  aggregatorService;

    public DailySnapshotService(JdbcTemplate jdbc,
                                BrokerSyncProperties syncProperties,
                                PositionAggregatorService aggregatorService) {
        this.jdbc              = jdbc;
        this.syncProperties    = syncProperties;
        this.aggregatorService = aggregatorService;
    }

    /**
     * 每个交易日 17:00（美东时间）执行每日快照。
     * 可通过 {@code app.snapshot.cron} 覆盖默认 cron 表达式。
     */
    @Scheduled(cron = "${app.snapshot.cron:0 0 17 * * MON-FRI}",
               zone  = "${app.snapshot.timezone:America/New_York}")
    @Transactional
    public void takeDailySnapshot() {
        LocalDate today = LocalDate.now();
        log.info("开始执行每日盘后快照，日期={}", today);

        for (BrokerSyncProperties.AccountMapping mapping : syncProperties.getAccounts()) {
            try {
                snapshotAccount(mapping, today);
            } catch (Exception e) {
                log.error("账户 [{}] 快照失败: {}", mapping.getAccountId(), e.getMessage(), e);
            }
        }
        log.info("每日盘后快照完成，日期={}", today);
    }

    // -------------------------------------------------------------------------
    // 内部逻辑
    // -------------------------------------------------------------------------

    private void snapshotAccount(BrokerSyncProperties.AccountMapping mapping, LocalDate date) {
        Long userId = mapping.getUserId();
        String accountId  = mapping.getAccountId();
        String brokerName = mapping.getBrokerName();

        // 调用聚合器获取当日资产视图
        List<PositionView> views = aggregatorService.aggregateByAccount(userId, accountId);

        if (views.isEmpty()) {
            log.debug("账户 [{}] 无持仓，跳过快照", accountId);
            return;
        }

        Date sqlDate = Date.valueOf(date);

        // 写入 t_daily_pnl_stock
        for (PositionView v : views) {
            jdbc.update("""
                    INSERT INTO t_daily_pnl_stock
                        (broker_account_id, broker_name, symbol, snapshot_date,
                         net_quantity, total_quantity, avg_cost,
                         realized_pnl, unrealized_pnl, last_price, market_value, cost_value)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        net_quantity   = VALUES(net_quantity),
                        total_quantity = VALUES(total_quantity),
                        avg_cost       = VALUES(avg_cost),
                        realized_pnl   = VALUES(realized_pnl),
                        unrealized_pnl = VALUES(unrealized_pnl),
                        last_price     = VALUES(last_price),
                        market_value   = VALUES(market_value),
                        cost_value     = VALUES(cost_value)
                    """,
                    accountId, brokerName, v.getSymbol(), sqlDate,
                    v.getNetQuantity(), v.getTotalQuantity(), v.getAvgCost(),
                    v.getRealizedPnl(), v.getUnrealizedPnl(),
                    v.getLastPrice(), v.getMarketValue(), v.getCostValue()
            );

            // 写入隔夜持仓快照（只记录 netQuantity > 0 的仓位）
            if (v.getNetQuantity() > 0) {
                jdbc.update("""
                        INSERT INTO t_position_history
                            (broker_account_id, broker_name, symbol, snapshot_date,
                             net_quantity, avg_cost, realized_pnl)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            net_quantity = VALUES(net_quantity),
                            avg_cost     = VALUES(avg_cost),
                            realized_pnl = VALUES(realized_pnl)
                        """,
                        accountId, brokerName, v.getSymbol(), sqlDate,
                        v.getNetQuantity(), v.getAvgCost(), v.getRealizedPnl()
                );
            }
        }

        // 汇总写入 t_daily_pnl_account
        double totalPnl      = views.stream().mapToDouble(v -> v.getTotalPnl().doubleValue()).sum();
        double totalRealized = views.stream().mapToDouble(v -> v.getRealizedPnl().doubleValue()).sum();
        double totalMV       = views.stream().mapToDouble(v -> v.getMarketValue().doubleValue()).sum();
        double totalCost     = views.stream().mapToDouble(v -> v.getCostValue().doubleValue()).sum();
        long   posCount      = views.stream().filter(v -> v.getNetQuantity() > 0).count();

        jdbc.update("""
                INSERT INTO t_daily_pnl_account
                    (broker_account_id, broker_name, snapshot_date,
                     total_pnl, total_realized_pnl, total_market_value, total_cost, position_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    total_pnl          = VALUES(total_pnl),
                    total_realized_pnl = VALUES(total_realized_pnl),
                    total_market_value = VALUES(total_market_value),
                    total_cost         = VALUES(total_cost),
                    position_count     = VALUES(position_count)
                """,
                accountId, brokerName, sqlDate,
                totalPnl, totalRealized, totalMV, totalCost, posCount
        );

        log.info("账户 [{}] 快照完成，股票数={}", accountId, views.size());
    }
}
