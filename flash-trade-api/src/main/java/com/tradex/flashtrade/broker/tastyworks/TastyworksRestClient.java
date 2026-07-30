package com.tradex.flashtrade.broker.tastyworks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrdersResponse;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPositionsResponse;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * Tastyworks REST API HTTP 客户端
 * <p>
 * 使用 Bearer Session Token（{@link TastyworksTokenService}）认证，
 * Token 过期（401）时自动重新登录后重试一次。
 * </p>
 */
@Component
public class TastyworksRestClient {

    private static final Logger log         = LoggerFactory.getLogger(TastyworksRestClient.class);
    private static final String BROKER_NAME = "TASTYWORKS";

    private final TastyworksProperties props;
    private final TastyworksTokenService tokenService;
    private final BrokerConfigService brokerConfigService;

    public TastyworksRestClient(TastyworksProperties props,
                                TastyworksTokenService tokenService,
                                BrokerConfigService brokerConfigService) {
        this.props = props;
        this.tokenService = tokenService;
        this.brokerConfigService = brokerConfigService;
    }

    private RestClient buildRestClient() {
        String baseUrl = brokerConfigService.getEffectiveBaseUrl("TASTYWORKS", props.getBaseUrl());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    // -------------------------------------------------------------------------
    // Positions
    // -------------------------------------------------------------------------

    public TastyworksPositionsResponse getPositions(String accountNumber) {
        log.debug("Tastyworks getPositions accountNumber={}", accountNumber);
        String path = "/accounts/" + accountNumber + "/positions";
        try {
            return buildRestClient().get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .body(TastyworksPositionsResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                log.warn("Tastyworks: 401 Session 已过期，重新登录后重试");
                tokenService.clearToken();
                try {
                    return buildRestClient().get()
                            .uri(path)
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .body(TastyworksPositionsResponse.class);
                } catch (RestClientResponseException retryEx) {
                    throw toBrokerException(retryEx);
                }
            }
            throw toBrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    public TastyworksOrdersResponse getOrders(String accountNumber) {
        log.debug("Tastyworks getOrders accountNumber={}", accountNumber);
        String path = "/accounts/" + accountNumber + "/orders";
        try {
            return buildRestClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("per-page", 100)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .body(TastyworksOrdersResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                log.warn("Tastyworks: 401 Session 已过期，重新登录后重试");
                tokenService.clearToken();
                try {
                    return buildRestClient().get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(path)
                                    .queryParam("per-page", 100)
                                    .build())
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .body(TastyworksOrdersResponse.class);
                } catch (RestClientResponseException retryEx) {
                    throw toBrokerException(retryEx);
                }
            }
            throw toBrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private BrokerApiException toBrokerException(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new BrokerApiException(BROKER_NAME, status, "Tastyworks API 速率限制");
        }
        log.error("Tastyworks API 响应异常 status={} body={}", status, ex.getResponseBodyAsString());
        return new BrokerApiException(BROKER_NAME, status, "Tastyworks API 错误: " + ex.getMessage());
    }
}
