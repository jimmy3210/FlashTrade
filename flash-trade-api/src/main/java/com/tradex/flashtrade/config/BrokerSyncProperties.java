package com.tradex.flashtrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 券商账户映射配置
 * <p>
 * 在 application-*.yml 的 {@code app.broker-sync} 节中进行配置。
 * BrokerSyncService 定时执行时读取此配置，
 * 将各券商获取的数据同步到本地数据库。
 *
 * <pre>{@code
 * app:
 *   broker-sync:
 *     enabled: true
 *     interval-ms: 300000
 *     accounts:
 *       - userId: 1
 *         brokerName: SCHWAB
 *         accountId: schwab-account-001
 * }</pre>
 */
@Component
@ConfigurationProperties(prefix = "app.broker-sync")
public class BrokerSyncProperties {

    /** 同步功能启用/禁用标志（默认：true） */
    private boolean enabled = true;

    /** 同步间隔（毫秒），与 application.yml 中的 fixedDelay 对应 */
    private long intervalMs = 300_000;

    /** 用户 ↔ 券商账户映射列表 */
    private List<AccountMapping> accounts = new ArrayList<>();

    public boolean isEnabled()               { return enabled; }
    public void setEnabled(boolean enabled)  { this.enabled = enabled; }

    public long getIntervalMs()              { return intervalMs; }
    public void setIntervalMs(long v)        { this.intervalMs = v; }

    public List<AccountMapping> getAccounts()             { return accounts; }
    public void setAccounts(List<AccountMapping> accounts){ this.accounts = accounts; }

    public static class AccountMapping {
        /** 内部用户 ID（users.id） */
        private Long userId;
        /** 券商代码（SCHWAB / ETRADE / TASTYWORKS） */
        private String brokerName;
        /** 券商账户 ID */
        private String accountId;

        public Long getUserId()              { return userId; }
        public void setUserId(Long userId)   { this.userId = userId; }

        public String getBrokerName()             { return brokerName; }
        public void setBrokerName(String v)       { this.brokerName = v; }

        public String getAccountId()              { return accountId; }
        public void setAccountId(String accountId){ this.accountId = accountId; }
    }
}
