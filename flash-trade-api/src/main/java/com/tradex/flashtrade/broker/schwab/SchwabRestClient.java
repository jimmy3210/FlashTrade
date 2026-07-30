package com.tradex.flashtrade.broker.schwab;

import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.broker.schwab.dto.SchwabAccountPositions;
import com.tradex.flashtrade.broker.schwab.dto.SchwabOrderResponse;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTransaction;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;

/**
 * 嘉信证券（Schwab）REST API HTTP 客户端
 * <p>
 * 封装所有对 {@code https://api.schwabapi.com/trader/v1} 的 HTTP 调用，
 * 统一处理认证头注入和 HTTP 状态码转换。
 * </p>
 */
@Component
public class SchwabRestClient {

    private static final Logger log = LoggerFactory.getLogger(SchwabRestClient.class);

    private static final String BROKER_NAME = "SCHWAB";

    private final SchwabProperties props;
    private final SchwabTokenService tokenService;
    private final BrokerConfigService brokerConfigService;

    public SchwabRestClient(SchwabProperties props, SchwabTokenService tokenService,
                            BrokerConfigService brokerConfigService) {
        this.props = props;
        this.tokenService = tokenService;
        this.brokerConfigService = brokerConfigService;
    }

    private RestClient buildRestClient() {
        String baseUrl = brokerConfigService.getEffectiveBaseUrl("SCHWAB", props.getBaseUrl());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInitializer(req -> req.getHeaders().add(HttpHeaders.ACCEPT, "application/json"))
                .build();
    }

    // -------------------------------------------------------------------------
    // Positions
    // -------------------------------------------------------------------------

    /**
     * 获取账户持仓列表
     *
     * @param accountId 嘉信账户编号
     * @return 持仓响应包装列表（每个元素含 securitiesAccount.positions）
     * @throws BrokerApiException HTTP 4xx/5xx 时抛出
     */
    public List<SchwabAccountPositions> getPositions(String accountId) {
        String token = tokenService.getValidAccessToken(accountId);
        log.debug("Schwab getPositions accountId={}", accountId);
        try {
            return buildRestClient().get()
                    .uri("/accounts/{accountNumber}/positions", accountId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SchwabAccountPositions>>() {});
        } catch (RestClientResponseException ex) {
            throw tobrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    /**
     * 获取账户委托订单列表
     *
     * @param accountId 嘉信账户编号
     * @return 订单列表
     */
    public List<SchwabOrderResponse> getOrders(String accountId) {
        String token = tokenService.getValidAccessToken(accountId);
        log.debug("Schwab getOrders accountId={}", accountId);
        try {
            return buildRestClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accounts/{accountNumber}/orders")
                            .queryParam("fromEnteredTime",
                                    java.time.LocalDate.now().minus(Duration.ofDays(90)).toString())
                            .queryParam("toEnteredTime",
                                    java.time.LocalDate.now().toString())
                            .build(accountId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SchwabOrderResponse>>() {});
        } catch (RestClientResponseException ex) {
            throw tobrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Transactions（成交记录）
    // -------------------------------------------------------------------------

    /**
     * 获取账户成交记录（仅 TRADE 类型）
     *
     * @param accountId 嘉信账户编号
     * @return 成交记录列表
     */
    public List<SchwabTransaction> getTransactions(String accountId) {
        String token = tokenService.getValidAccessToken(accountId);
        log.debug("Schwab getTransactions accountId={}", accountId);
        try {
            return buildRestClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accounts/{accountNumber}/transactions")
                            .queryParam("types", "TRADE")
                            .queryParam("startDate",
                                    java.time.LocalDate.now().minus(Duration.ofDays(90)).toString())
                            .queryParam("endDate",
                                    java.time.LocalDate.now().toString())
                            .build(accountId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SchwabTransaction>>() {});
        } catch (RestClientResponseException ex) {
            throw tobrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // 内部工具
    // -------------------------------------------------------------------------

    private BrokerApiException tobrokerException(RestClientResponseException ex) {
        int statusCode = ex.getStatusCode().value();
        String msg = ex.getMessage();
        log.error("Schwab API 响应异常 status={} body={}", statusCode, ex.getResponseBodyAsString());

        if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            return new BrokerApiException(BROKER_NAME, statusCode, "Access Token 无效或已过期");
        }
        if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new BrokerApiException(BROKER_NAME, statusCode, "已触发 Schwab API 速率限制");
        }
        return new BrokerApiException(BROKER_NAME, statusCode, "Schwab API 错误: " + msg);
    }
}
