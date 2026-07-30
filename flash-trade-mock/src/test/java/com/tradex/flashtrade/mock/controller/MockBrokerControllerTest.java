package com.tradex.flashtrade.mock.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.tradex.flashtrade.mock.service.BrokerMockService;
import com.tradex.flashtrade.mock.service.DefaultMockStrategy;
import com.tradex.flashtrade.mock.service.EtradeMockStrategy;
import com.tradex.flashtrade.mock.service.SchwabMockStrategy;
import com.tradex.flashtrade.mock.service.TastytradeMockStrategy;
import com.tradex.flashtrade.mock.service.TastyworksMockStrategy;

@WebMvcTest(MockBrokerController.class)
@Import({BrokerMockService.class, EtradeMockStrategy.class, SchwabMockStrategy.class,
        TastytradeMockStrategy.class, TastyworksMockStrategy.class, DefaultMockStrategy.class})
class MockBrokerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void positionsEndpointReturnsDemoData() throws Exception {
        mockMvc.perform(get("/mock/accounts/demo-001/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("XYZ"));
    }

    @Test
    void brokerSpecificRoutesReturnDifferentDemoData() throws Exception {
        mockMvc.perform(get("/mock/etrade/accounts/demo-001/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[1].symbol").value("AMZN"));

        mockMvc.perform(get("/mock/schwab/accounts/demo-001/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("MSFT"))
                .andExpect(jsonPath("$[1].symbol").value("NVDA"));
    }

    @Test
    void watchlistEndpointReturnsDemoData() throws Exception {
        mockMvc.perform(get("/mock/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void placeOrderEndpointReturnsPendingOrder() throws Exception {
        String payload = "{\"symbol\":\"AAPL\",\"side\":\"BUY\",\"orderType\":\"LIMIT\",\"quantity\":10,\"limitPrice\":185.25}";

        mockMvc.perform(post("/mock/accounts/demo-001/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
