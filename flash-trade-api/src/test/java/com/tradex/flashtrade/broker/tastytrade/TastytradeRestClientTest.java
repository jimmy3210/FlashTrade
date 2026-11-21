package com.tradex.flashtrade.broker.tastytrade;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.service.BrokerConfigService;

class TastytradeRestClientTest {

    private BrokerConfigService brokerConfigService() {
        return new BrokerConfigService(null, null, null, null, null, new StandardEnvironment(), "MOCK");
    }

    @Test
    void toBrokerException_extractsMessageFromBody_messageField() throws Exception {
        String body = "{\"message\":\"invalid api key\"}";
        WebClientResponseException ex = WebClientResponseException.create(401, "Unauthorized", HttpHeaders.EMPTY, body.getBytes(), null);

        TastytradeRestClient client = new TastytradeRestClient(null, new TastytradeProperties(), brokerConfigService());
        Method m = TastytradeRestClient.class.getDeclaredMethod("toBrokerException", WebClientResponseException.class);
        m.setAccessible(true);
        BrokerApiException be = (BrokerApiException) m.invoke(client, ex);
        assertEquals(401, be.getStatusCode());
        assertTrue(be.getMessage().contains("invalid api key"));
    }

    @Test
    void toBrokerException_extractsFirstErrorFromErrorsArray() throws Exception {
        String body = "{\"errors\":[\"bad request\", \"other\"]}";
        WebClientResponseException ex = WebClientResponseException.create(400, "Bad Request", HttpHeaders.EMPTY, body.getBytes(), null);

        TastytradeRestClient client = new TastytradeRestClient(null, new TastytradeProperties(), brokerConfigService());
        Method m = TastytradeRestClient.class.getDeclaredMethod("toBrokerException", WebClientResponseException.class);
        m.setAccessible(true);
        BrokerApiException be = (BrokerApiException) m.invoke(client, ex);
        assertEquals(400, be.getStatusCode());
        assertTrue(be.getMessage().contains("bad request"));
    }
}
