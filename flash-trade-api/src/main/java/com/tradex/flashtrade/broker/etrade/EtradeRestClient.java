package com.tradex.flashtrade.broker.etrade;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrderRequestEntry;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrdersResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePlaceOrderRequest;
import com.tradex.flashtrade.broker.etrade.dto.EtradePlaceOrderResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePortfolioResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePreviewOrderRequest;
import com.tradex.flashtrade.broker.etrade.dto.EtradePreviewOrderResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradeTransactionResponse;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * E*TRADE REST API HTTP 客户端
 * <p>
 * 实现 OAuth 1.0a 请求签名，调用 E*TRADE Trader API。
 * </p>
 */
@Component
public class EtradeRestClient {

    private static final Logger log = LoggerFactory.getLogger(EtradeRestClient.class);

    private static final String BROKER_NAME    = "ETRADE";
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String OAUTH_VERSION  = "1.0";

    private final EtradeProperties   props;
    private final EtradeTokenService tokenService;
    private final BrokerConfigService brokerConfigService;

    public EtradeRestClient(EtradeProperties props, EtradeTokenService tokenService,
                            BrokerConfigService brokerConfigService) {
        this.props              = props;
        this.tokenService       = tokenService;
        this.brokerConfigService = brokerConfigService;
    }

    private RestClient buildRestClient() {
        String baseUrl = brokerConfigService.getEffectiveBaseUrl("ETRADE", props.getBaseUrl());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    // -------------------------------------------------------------------------
    // Portfolio
    // -------------------------------------------------------------------------

    public EtradePortfolioResponse getPortfolio(String accountIdKey) {
        log.debug("E*TRADE getPortfolio accountIdKey={}", accountIdKey);
        String path = "/accounts/" + accountIdKey + "/portfolio";
        try {
            String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
            return buildRestClient().get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("GET", effectiveUrl + path, accountIdKey))
                    .retrieve()
                    .body(EtradePortfolioResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    public EtradeOrdersResponse getOrders(String accountIdKey) {
        log.debug("E*TRADE getOrders accountIdKey={}", accountIdKey);
        String path = "/accounts/" + accountIdKey + "/orders";
        try {
            String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
            return buildRestClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("count", 100)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("GET", effectiveUrl + path, accountIdKey))
                    .retrieve()
                    .body(EtradeOrdersResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Transactions
    // -------------------------------------------------------------------------

    public EtradeTransactionResponse getTransactions(String accountIdKey) {
        log.debug("E*TRADE getTransactions accountIdKey={}", accountIdKey);
        String path = "/accounts/" + accountIdKey + "/transactions";
        String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
        try {
            return buildRestClient().get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("GET", effectiveUrl + path, accountIdKey))
                    .retrieve()
                    .body(EtradeTransactionResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    public EtradePreviewOrderResponse previewOrder(String accountIdKey, com.tradex.flashtrade.broker.dto.OrderRequest request) {
        log.debug("E*TRADE previewOrder accountIdKey={} symbol={} qty={}", accountIdKey, request.getSymbol(), request.getQuantity());
        String path = "/order/v1/accounts/" + accountIdKey + "/orders/preview";
        String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
        var orderEntry = EtradeOrderRequestEntry.fromOrderRequest(request);
        var previewRequest = new EtradePreviewOrderRequest();
        var body = new EtradePreviewOrderRequest.PreviewOrderRequest();
        body.setOrderType("EQ");
        if (request.getClientOrderId() == null || request.getClientOrderId().isBlank()) {
            request.setClientOrderId(UUID.randomUUID().toString());
        }
        body.setClientOrderId(request.getClientOrderId());
        body.setOrder(List.of(orderEntry));
        previewRequest.setPreviewOrderRequest(body);
        try {
            return buildRestClient().post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("POST", effectiveUrl + path, accountIdKey))
                    .body(previewRequest)
                    .retrieve()
                    .body(EtradePreviewOrderResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    public EtradePlaceOrderResponse placeOrder(String accountIdKey, com.tradex.flashtrade.broker.dto.OrderRequest request, String previewId) {
        log.debug("E*TRADE placeOrder accountIdKey={} previewId={} symbol={} qty={}", accountIdKey, previewId, request.getSymbol(), request.getQuantity());
        String path = "/order/v1/accounts/" + accountIdKey + "/orders/place";
        String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
        var orderEntry = EtradeOrderRequestEntry.fromOrderRequest(request);
        var placeRequest = new EtradePlaceOrderRequest();
        var body = new EtradePlaceOrderRequest.PlaceOrderRequest();
        body.setOrderType("EQ");
        body.setClientOrderId(request.getClientOrderId());
        body.setPreviewId(previewId);
        body.setOrder(List.of(orderEntry));
        placeRequest.setPlaceOrderRequest(body);
        try {
            return buildRestClient().post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("POST", effectiveUrl + path, accountIdKey))
                    .body(placeRequest)
                    .retrieve()
                    .body(EtradePlaceOrderResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    public boolean cancelOrder(String accountIdKey, String orderId) {
        log.debug("E*TRADE cancelOrder accountIdKey={} orderId={}", accountIdKey, orderId);
        String path = "/order/v1/accounts/" + accountIdKey + "/orders/" + orderId;
        String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
        try {
            buildRestClient().delete()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("DELETE", effectiveUrl + path, accountIdKey))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return false;
            }
            throw toBrokerException(ex);
        }
    }

    public EtradeOrdersResponse getOrderById(String accountIdKey, String orderId) {
        log.debug("E*TRADE getOrderById accountIdKey={} orderId={}", accountIdKey, orderId);
        String path = "/order/v1/accounts/" + accountIdKey + "/orders/" + orderId;
        String effectiveUrl = brokerConfigService.getEffectiveBaseUrl(BROKER_NAME, props.getBaseUrl());
        try {
            return buildRestClient().get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION,
                            buildOAuthHeader("GET", effectiveUrl + path, accountIdKey))
                    .retrieve()
                    .body(EtradeOrdersResponse.class);
        } catch (RestClientResponseException ex) {
            throw toBrokerException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // OAuth 1.0a 签名
    // -------------------------------------------------------------------------

    /**
     * 构造 OAuth 1.0a Authorization Header。
     * <p>
     * 按 RFC 5849 规范生成 HMAC-SHA1 签名。
     * </p>
     */
    String buildOAuthHeader(String method, String url, String accountId) {
        String token       = tokenService.getAccessToken(accountId);
        String tokenSecret = tokenService.getTokenSecret(accountId);

        String nonce     = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        String signingKey = encode(props.getConsumerSecret()) + "&" + encode(tokenSecret);
        String baseString = method.toUpperCase()
                + "&" + encode(url)
                + "&" + encode(
                    "oauth_consumer_key=" + props.getConsumerKey()
                    + "&oauth_nonce=" + nonce
                    + "&oauth_signature_method=" + SIGNATURE_METHOD
                    + "&oauth_timestamp=" + timestamp
                    + "&oauth_token=" + token
                    + "&oauth_version=" + OAUTH_VERSION
                );

        String signature = hmacSha1(signingKey, baseString);

        return "OAuth "
                + "oauth_consumer_key=\"" + props.getConsumerKey() + "\","
                + "oauth_nonce=\"" + nonce + "\","
                + "oauth_signature=\"" + encode(signature) + "\","
                + "oauth_signature_method=\"" + SIGNATURE_METHOD + "\","
                + "oauth_timestamp=\"" + timestamp + "\","
                + "oauth_token=\"" + token + "\","
                + "oauth_version=\"" + OAUTH_VERSION + "\"";
    }

    private String hmacSha1(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(
                    mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new BrokerApiException(BROKER_NAME, 500, "OAuth 1.0a 签名生成失败: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private BrokerApiException toBrokerException(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            return new BrokerApiException(BROKER_NAME, status, "E*TRADE Access Token 无效或已过期");
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new BrokerApiException(BROKER_NAME, status, "E*TRADE API 速率限制");
        }
        log.error("E*TRADE API 响应异常 status={} body={}", status, ex.getResponseBodyAsString());
        return new BrokerApiException(BROKER_NAME, status, "E*TRADE API 错误: " + ex.getMessage());
    }
}
