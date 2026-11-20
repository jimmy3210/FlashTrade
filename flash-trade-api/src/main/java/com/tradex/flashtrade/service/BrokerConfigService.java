package com.tradex.flashtrade.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.config.BrokerMode;
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

    private static final Logger log = LoggerFactory.getLogger(BrokerConfigService.class);
    private static final Set<BrokerMode> MOCK_ONLY = Set.of(BrokerMode.MOCK);

    private final JdbcTemplate jdbc;
    private final Map<String, DefaultBrokerConfig> defaultConfigs;
    private final Environment environment;
    private final Set<BrokerMode> configuredAllowedModes;

    public BrokerConfigService(
            JdbcTemplate jdbc,
            EtradeProperties etradeProperties,
            SchwabProperties schwabProperties,
            TastytradeProperties tastytradeProperties,
            TastyworksProperties tastyworksProperties,
            Environment environment,
            @Value("${app.broker.allowed-modes:MOCK}") String allowedModes) {
        this.jdbc = jdbc;
        this.environment = environment;
        this.configuredAllowedModes = parseAllowedModes(allowedModes);
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
        BrokerMode mode = resolveEffectiveMode(config, effectiveAllowedModes());
        return new BrokerConfigResponse(normalized, mode.name(), null,
                mode == BrokerMode.MOCK, config == null ? null : config.getUpdatedAt());
    }

    @Transactional
    public BrokerConfigResponse updateBrokerConfig(String brokerName, BrokerConfigRequest request) {
        String normalized = normalizeBrokerName(brokerName);
        validateBrokerName(normalized);
        BrokerMode mode = parseRequestedMode(request.getMode());
        if (!effectiveAllowedModes().contains(mode)) {
            throw new BusinessException(ErrorCode.INVALID_STATE,
                    "当前部署不允许券商模式: " + mode);
        }
        if (request.getMockEnabled() != null
                && request.getMockEnabled() != (mode == BrokerMode.MOCK)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,
                    "mode 与 mockEnabled 配置不一致");
        }
        DefaultBrokerConfig defaults = defaultConfigs.get(normalized);
        if (defaults == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE, "Unsupported broker: " + normalized);
        }
        if (request.getBaseUrl() != null && !request.getBaseUrl().isBlank()
                && !request.getBaseUrl().equals(defaults.baseUrl)) {
            throw new BusinessException(ErrorCode.INVALID_STATE,
                    "券商 API 地址由服务端环境配置管理");
        }
        boolean mockEnabled = mode == BrokerMode.MOCK;
        String sql = "INSERT INTO t_broker_runtime_config (broker_name, mode, base_url, mock_enabled) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE mode = VALUES(mode), base_url = VALUES(base_url), mock_enabled = VALUES(mock_enabled)";
        jdbc.update(sql, normalized, mode.name(), defaults.baseUrl, mockEnabled ? 1 : 0);
        return getBrokerConfig(normalized);
    }

    public BrokerMode getEffectiveMode(String brokerName) {
        String normalized = normalizeBrokerName(brokerName);
        validateBrokerName(normalized);
        Set<BrokerMode> allowedModes = effectiveAllowedModes();
        if (allowedModes.equals(MOCK_ONLY)) {
            return BrokerMode.MOCK;
        }
        return resolveEffectiveMode(queryBrokerConfig(normalized), allowedModes);
    }

    public String getEffectiveBaseUrl(String brokerName, String defaultBaseUrl) {
        validateBrokerName(normalizeBrokerName(brokerName));
        return defaultBaseUrl;
    }

    private Set<BrokerMode> effectiveAllowedModes() {
        if (environment.acceptsProfiles(Profiles.of("dev", "test"))) {
            return MOCK_ONLY;
        }
        return configuredAllowedModes;
    }

    private BrokerMode resolveEffectiveMode(
            BrokerConfigResponse config,
            Set<BrokerMode> allowedModes) {
        if (config == null) {
            return BrokerMode.MOCK;
        }
        if (config.getMode() == null) {
            log.warn("数据库中的券商模式为空，按 MOCK 处理 broker={}", config.getBrokerName());
            return BrokerMode.MOCK;
        }
        BrokerMode mode;
        try {
            mode = BrokerMode.valueOf(config.getMode().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("数据库中的券商模式无效，按 MOCK 处理 broker={} mode={}",
                    config.getBrokerName(), config.getMode());
            return BrokerMode.MOCK;
        }
        if (!allowedModes.contains(mode)) {
            log.warn("部署不允许当前数据库券商模式，按 MOCK 处理 broker={} mode={}",
                    config.getBrokerName(), mode);
            return BrokerMode.MOCK;
        }
        if (config.isMockEnabled() != (mode == BrokerMode.MOCK)) {
            log.warn("数据库中的券商模式与 Mock 标志不一致，按 MOCK 处理 broker={} mode={}",
                    config.getBrokerName(), mode);
            return BrokerMode.MOCK;
        }
        return mode;
    }

    private Set<BrokerMode> parseAllowedModes(String modes) {
        if (modes == null || modes.isBlank()) {
            return MOCK_ONLY;
        }
        EnumSet<BrokerMode> parsed = EnumSet.noneOf(BrokerMode.class);
        for (String value : modes.split(",")) {
            try {
                parsed.add(BrokerMode.valueOf(value.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid app.broker.allowed-modes value: " + value, ex);
            }
        }
        if (!parsed.contains(BrokerMode.MOCK)) {
            throw new IllegalArgumentException("app.broker.allowed-modes must include MOCK");
        }
        return Collections.unmodifiableSet(parsed);
    }

    private BrokerMode parseRequestedMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "券商模式不能为空");
        }
        try {
            return BrokerMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "Unsupported broker mode: " + mode);
        }
    }

    private BrokerConfigResponse queryBrokerConfig(String brokerName) {
        String normalized = normalizeBrokerName(brokerName);
        String sql = "SELECT broker_name, mode, mock_enabled, updated_at "
                + "FROM t_broker_runtime_config WHERE broker_name = ?";
        List<BrokerConfigResponse> rows = jdbc.query(sql, this::mapRow, normalized);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private BrokerConfigResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        String brokerName = rs.getString("broker_name");
        String mode = rs.getString("mode");
        boolean mockEnabled = rs.getBoolean("mock_enabled");
        String updatedAt = rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime().toString()
                : null;
        return new BrokerConfigResponse(brokerName, mode, null, mockEnabled, updatedAt);
    }

    private void validateBrokerName(String brokerName) {
        if (brokerName == null || !defaultConfigs.containsKey(brokerName)) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "Unsupported broker: " + brokerName);
        }
    }

    private String normalizeBrokerName(String brokerName) {
        return brokerName == null ? null : brokerName.trim().toUpperCase(Locale.ROOT);
    }

    private DefaultBrokerConfig buildDefaultConfig(Object properties) {
        if (properties instanceof EtradeProperties etradeProperties) {
            return new DefaultBrokerConfig(etradeProperties.getBaseUrl());
        }
        if (properties instanceof SchwabProperties schwabProperties) {
            return new DefaultBrokerConfig(schwabProperties.getBaseUrl());
        }
        if (properties instanceof TastytradeProperties tastytradeProperties) {
            return new DefaultBrokerConfig(tastytradeProperties.getBaseUrl());
        }
        if (properties instanceof TastyworksProperties tastyworksProperties) {
            return new DefaultBrokerConfig(tastyworksProperties.getBaseUrl());
        }
        return new DefaultBrokerConfig(null);
    }

    private static final class DefaultBrokerConfig {
        private final String baseUrl;

        private DefaultBrokerConfig(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
