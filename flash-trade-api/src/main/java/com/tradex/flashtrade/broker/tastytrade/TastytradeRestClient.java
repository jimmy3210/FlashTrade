package com.tradex.flashtrade.broker.tastytrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeOrder;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeOrdersResponse;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradePositionsResponse;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeWatchlistResponse;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * Tastytrade REST 客户端（最小化实现）。
 */
@Component
public class TastytradeRestClient {

    private static final Logger log = LoggerFactory.getLogger(TastytradeRestClient.class);
    private static final String BROKER = "TASTYTRADE";

    private final TastytradeTokenService tokenService;
    private final TastytradeProperties props;
    private final BrokerConfigService brokerConfigService;

    public TastytradeRestClient(TastytradeTokenService tokenService,
                                TastytradeProperties props,
                                BrokerConfigService brokerConfigService) {
        this.tokenService = tokenService;
        this.props = props;
        this.brokerConfigService = brokerConfigService;
    }

    private WebClient buildWebClient() {
        String baseUrl = brokerConfigService.getEffectiveBaseUrl("TASTYTRADE", props.getBaseUrl());
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    public TastytradePositionsResponse getPositions(String accountNumber) {
        String path = "/accounts/" + accountNumber + "/positions";
        try {
            return buildWebClient().get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .bodyToMono(TastytradePositionsResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                tokenService.clearToken();
                try {
                    return buildWebClient().get()
                            .uri(path)
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .bodyToMono(TastytradePositionsResponse.class)
                            .block();
                } catch (WebClientResponseException retry) {
                    throw toBrokerException(retry);
                }
            }
            throw toBrokerException(ex);
        }
    }

    public TastytradeOrdersResponse getOrders(String accountNumber) {
        String path = "/accounts/" + accountNumber + "/orders";
        try {
            return buildWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("per_page", 100).build())
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .bodyToMono(TastytradeOrdersResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                tokenService.clearToken();
                try {
                    return buildWebClient().get()
                            .uri(uriBuilder -> uriBuilder.path(path).queryParam("per_page", 100).build())
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .bodyToMono(TastytradeOrdersResponse.class)
                            .block();
                } catch (WebClientResponseException retry) {
                    throw toBrokerException(retry);
                }
            }
            throw toBrokerException(ex);
        }
    }

    public TastytradeOrder placeOrder(String accountNumber, com.tradex.flashtrade.broker.dto.OrderRequest req) {
        String path = "/accounts/" + accountNumber + "/orders";
        var payload = new com.tradex.flashtrade.broker.tastytrade.dto.TastytradePlaceOrderRequest();
        payload.setSymbol(req.getSymbol());
        payload.setSide(req.getSide().name());
        payload.setType(req.getOrderType().name());
        payload.setQuantity(java.math.BigDecimal.valueOf(req.getQuantity()));
        if (req.getLimitPrice() != null) payload.setLimit_price(req.getLimitPrice());
        payload.setClient_order_id(req.getClientOrderId());

        try {
            return buildWebClient().post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(TastytradeOrder.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                tokenService.clearToken();
                try {
                    return buildWebClient().post()
                            .uri(path)
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(TastytradeOrder.class)
                            .block();
                } catch (WebClientResponseException retry) {
                    throw toBrokerException(retry);
                }
            }
            throw toBrokerException(ex);
        }
    }

    public boolean cancelOrder(String accountNumber, String externalOrderId) {
        String path = "/accounts/" + accountNumber + "/orders/" + externalOrderId;
        try {
            buildWebClient().delete()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                tokenService.clearToken();
                try {
                    buildWebClient().delete()
                            .uri(path)
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .toBodilessEntity()
                            .block();
                    return true;
                } catch (WebClientResponseException retry) {
                    throw toBrokerException(retry);
                }
            }
            throw toBrokerException(ex);
        }
    }

    public TastytradeWatchlistResponse getWatchlist(String accountNumber) {
        String path = "/watchlists";
        try {
            return buildWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("account_number", accountNumber).build())
                    .header(HttpHeaders.AUTHORIZATION, tokenService.getSessionToken())
                    .retrieve()
                    .bodyToMono(TastytradeWatchlistResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                tokenService.clearToken();
                try {
                    return buildWebClient().get()
                            .uri(uriBuilder -> uriBuilder.path(path).queryParam("account_number", accountNumber).build())
                            .header(HttpHeaders.AUTHORIZATION, tokenService.refreshToken())
                            .retrieve()
                            .bodyToMono(TastytradeWatchlistResponse.class)
                            .block();
                } catch (WebClientResponseException retry) {
                    throw toBrokerException(retry);
                }
            }
            throw toBrokerException(ex);
        }
    }

    private BrokerApiException toBrokerException(WebClientResponseException ex) {
        int status = ex.getRawStatusCode();
        String body = ex.getResponseBodyAsString();
        String errMsg = null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(body);
            if (node.has("message")) errMsg = node.get("message").asText();
            else if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                var first = node.get("errors").get(0);
                errMsg = first.isTextual() ? first.asText() : first.toString();
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        if (errMsg == null || errMsg.isBlank()) errMsg = ex.getMessage();
        log.error("Tastytrade API error status={} body={}", status, body);
        return new BrokerApiException(BROKER, status, "Tastytrade API 错误: " + errMsg);
    }
}
