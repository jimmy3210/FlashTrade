package com.tradex.flashtrade.service;

import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.dto.broker.BrokerConfigRequest;
import com.tradex.flashtrade.dto.broker.BrokerConfigResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BrokerConfigServiceTest {

    @Test
    void databaseProductionConfigCannotDisableMockWhenDeploymentAllowsOnlyMock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Environment environment = environmentWithProfile(false);
        BrokerConfigService service = service(jdbc, environment, "MOCK");
        when(jdbc.query(anyString(), anyRowMapper(), eq("ETRADE")))
                .thenReturn(List.of(new BrokerConfigResponse(
                        "ETRADE", "PRODUCTION", "https://example.invalid", false, null)));

        assertThat(service.getEffectiveMode("ETRADE")).isEqualTo(BrokerMode.MOCK);
        BrokerConfigResponse config = service.getBrokerConfig("ETRADE");
        assertThat(config.getMode()).isEqualTo("MOCK");
        assertThat(config.isMockEnabled()).isTrue();
        assertThat(config.getBaseUrl()).isNull();
    }

    @Test
    void developmentAndTestProfilesAlwaysUseMockEvenIfExternalApisAreEnabled() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BrokerConfigService service = service(
                jdbc, environmentWithProfile(true), "MOCK,SANDBOX,PRODUCTION");

        assertThat(service.getEffectiveMode("SCHWAB")).isEqualTo(BrokerMode.MOCK);
    }

    @Test
    void allowedModesMustIncludeMockAsSafeFallback() {
        assertThatThrownBy(() -> service(
                mock(JdbcTemplate.class), environmentWithProfile(false), "SANDBOX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must include MOCK");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SANDBOX", "PRODUCTION"})
    void runtimeConfigCannotEnableExternalModeBeforeExternalApisAreOpened(String mode) {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BrokerConfigService service = service(jdbc, environmentWithProfile(false), "MOCK");
        BrokerConfigRequest request = new BrokerConfigRequest();
        request.setMode(mode);
        request.setMockEnabled(false);

        assertThatThrownBy(() -> service.updateBrokerConfig("ETRADE", request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE);
        verifyNoInteractions(jdbc);
    }

    @Test
    void sandboxModeIsEffectiveOnlyWhenDeploymentAllowsIt() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BrokerConfigService service = service(
                jdbc, environmentWithProfile(false), "MOCK,SANDBOX");
        when(jdbc.query(anyString(), anyRowMapper(), eq("ETRADE")))
                .thenReturn(List.of(new BrokerConfigResponse(
                        "ETRADE", "SANDBOX", "https://example.invalid", false, null)));

        assertThat(service.getEffectiveMode("ETRADE")).isEqualTo(BrokerMode.SANDBOX);
        assertThat(service.getBrokerConfig("ETRADE").getBaseUrl()).isNull();
    }

    @Test
    void inconsistentMockFlagFallsBackToMock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BrokerConfigService service = service(
                jdbc, environmentWithProfile(false), "MOCK,SANDBOX");
        when(jdbc.query(anyString(), anyRowMapper(), eq("ETRADE")))
                .thenReturn(List.of(new BrokerConfigResponse(
                        "ETRADE", "SANDBOX", "https://example.invalid", true, null)));

        assertThat(service.getEffectiveMode("ETRADE")).isEqualTo(BrokerMode.MOCK);
    }

    @Test
    void runtimeConfigCannotChangeApiBaseUrl() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BrokerConfigService service = service(jdbc, environmentWithProfile(false), "MOCK");
        BrokerConfigRequest request = new BrokerConfigRequest();
        request.setMode("MOCK");
        request.setMockEnabled(true);
        request.setBaseUrl("http://127.0.0.1:8080/metadata");

        assertThatThrownBy(() -> service.updateBrokerConfig("ETRADE", request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE);
        verifyNoInteractions(jdbc);
    }

    private BrokerConfigService service(
            JdbcTemplate jdbc,
            Environment environment,
            String allowedModes) {
        return new BrokerConfigService(
                jdbc,
                new EtradeProperties(),
                new SchwabProperties(),
                new TastytradeProperties(),
                new TastyworksProperties(),
                environment,
                allowedModes);
    }

    private Environment environmentWithProfile(boolean restricted) {
        Environment environment = mock(Environment.class);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(restricted);
        return environment;
    }

    @SuppressWarnings("unchecked")
    private RowMapper<BrokerConfigResponse> anyRowMapper() {
        return any(RowMapper.class);
    }
}
