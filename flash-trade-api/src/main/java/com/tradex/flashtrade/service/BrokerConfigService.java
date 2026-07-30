package com.tradex.flashtrade.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.dto.broker.BrokerConfigRequest;
import com.tradex.flashtrade.dto.broker.BrokerConfigResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;

@Service
public class BrokerConfigService {

    private static final Set<String> VALID_MODES = Set.of("MOCK", "SANDBOX", "PRODUCTION");

    private final JdbcTemplate jdbc;
    private final Map<String, DefaultBrokerConfig> defaultConfigs;

    public BrokerConfigService(
            JdbcTemplate jdbc,
            EtradeProperties etradeProperties,
            SchwabProperties schwabProperties,
            TastytradeProperties tastytradeProperties,
            TastyworksProperties tastyworksProperties) {
        this.jdbc = jdbc;
        this.defaultConfigs = Map.of(
                "ETRADE", buildDefaultConfig(etradeProperties),
                "SCHWAB", buildDefaultConfig(schwabProperties),
                "TASTYTRADE", buildDefaultConfig(tastytradeProperties),
                "TASTYWORKS", buildDefaultConfig(tastyworksProperties)
        );
    }

    public List<BrokerConfigResponse> listBrokerConfigs() {
        return defaultConfigs.keySet().stream()
                .sorted()
                .map(this::getBrokerConfig)
                .collect(Collectors.toList());
    }

    public BrokerConfigResponse getBrokerConfig(String brokerName) {
        String normalized = normalizeBrokerName(brokerName);
        validateBrokerName(normalized);
        BrokerConfigResponse config = queryBrokerConfig(normalized);
        if (config != null) {
            return config;
        }
        DefaultBrokerConfig defaults = defaultConfigs.get(normalized);
        String mode = defaults.mockEnabled ? "MOCK" : "PRODUCTION";
        return new BrokerConfigResponse(normalized, mode,
                defaults.baseUrl, defaults.mockEnabled, null);
    }

    @Transactional
    public BrokerConfigResponse updateBrokerConfig(String brokerName, BrokerConfigRequest request) {
        String normalized = normalizeBrokerName(brokerName);
        validateBrokerName(normalized);
        String mode = normalizeMode(request.getMode());
        if (!VALID_MODES.contains(mode)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "Unsupported broker mode: " + request.getMode());
        }
        DefaultBrokerConfig defaults = defaultConfigs.get(normalized);
        if (defaults == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE, "Unsupported broker: " + normalized);
        }
        boolean mockEnabled = request.getMockEnabled() != null
                ? request.getMockEnabled()
                : "MOCK".equals(mode);
        String baseUrl = request.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = defaults.baseUrl;
        }
        String sql = "INSERT INTO t_broker_runtime_config (broker_name, mode, base_url, mock_enabled) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE mode = VALUES(mode), base_url = VALUES(base_url), mock_enabled = VALUES(mock_enabled)";
        jdbc.update(sql, normalized, mode, baseUrl, mockEnabled ? 1 : 0);
        return getBrokerConfig(normalized);
    }

    public boolean isMockEnabled(String brokerName, boolean defaultValue) {
        BrokerConfigResponse config = queryBrokerConfig(normalizeBrokerName(brokerName));
        return config != null ? config.isMockEnabled() : defaultValue;
    }

    public String getEffectiveBaseUrl(String brokerName, String defaultBaseUrl) {
        BrokerConfigResponse config = queryBrokerConfig(normalizeBrokerName(brokerName));
        return config != null && config.getBaseUrl() != null && !config.getBaseUrl().isBlank()
                ? config.getBaseUrl()
                : defaultBaseUrl;
    }

    private BrokerConfigResponse queryBrokerConfig(String brokerName) {
        String normalized = normalizeBrokerName(brokerName);
        String sql = "SELECT broker_name, mode, base_url, mock_enabled, updated_at "
                + "FROM t_broker_runtime_config WHERE broker_name = ?";
        List<BrokerConfigResponse> rows = jdbc.query(sql, this::mapRow, normalized);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private BrokerConfigResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        String brokerName = rs.getString("broker_name");
        String mode = rs.getString("mode");
        String baseUrl = rs.getString("base_url");
        boolean mockEnabled = rs.getBoolean("mock_enabled");
        String updatedAt = rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime().toString()
                : null;
        return new BrokerConfigResponse(brokerName, mode, baseUrl, mockEnabled, updatedAt);
    }

    private void validateBrokerName(String brokerName) {
        if (brokerName == null || !defaultConfigs.containsKey(brokerName)) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "Unsupported broker: " + brokerName);
        }
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return "PRODUCTION";
        }
        return mode.trim().toUpperCase();
    }

    private String normalizeBrokerName(String brokerName) {
        return brokerName == null ? null : brokerName.trim().toUpperCase();
    }

    private DefaultBrokerConfig buildDefaultConfig(Object properties) {
        if (properties instanceof EtradeProperties etradeProperties) {
            return new DefaultBrokerConfig(etradeProperties.getBaseUrl(), etradeProperties.isMockEnabled());
        }
        if (properties instanceof SchwabProperties schwabProperties) {
            return new DefaultBrokerConfig(schwabProperties.getBaseUrl(), schwabProperties.isMockEnabled());
        }
        if (properties instanceof TastytradeProperties tastytradeProperties) {
            return new DefaultBrokerConfig(tastytradeProperties.getBaseUrl(), tastytradeProperties.isMockEnabled());
        }
        if (properties instanceof TastyworksProperties tastyworksProperties) {
            return new DefaultBrokerConfig(tastyworksProperties.getBaseUrl(), tastyworksProperties.isMockEnabled());
        }
        return new DefaultBrokerConfig(null, false);
    }

    private static final class DefaultBrokerConfig {
        private final String baseUrl;
        private final boolean mockEnabled;

        private DefaultBrokerConfig(String baseUrl, boolean mockEnabled) {
            this.baseUrl = baseUrl;
            this.mockEnabled = mockEnabled;
        }
    }
}
