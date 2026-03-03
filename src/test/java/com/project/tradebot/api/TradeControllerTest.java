package com.project.tradebot.api;

import com.project.tradebot.application.service.TradingService;
import com.project.tradebot.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradingService tradingService;

    @Test
    void execute_withDefaultParameters_returnsOrders() throws Exception {
        Order order = Order.builder()
                .symbol("BTCINR")
                .type(Order.OrderType.BUY)
                .quantity(0.01)
                .price(5000000.0)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(tradingService.executeFullPipeline(
                "CryptoNewsScraper",
                "OllamaLLMStrategy",
                "CoinDCXBroker",
                "CoinDCXMarketData"))
                .thenReturn(List.of(order));

        mockMvc.perform(post("/trade/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTCINR"))
                .andExpect(jsonPath("$[0].type").value("BUY"))
                .andExpect(jsonPath("$[0].quantity").value(0.01))
                .andExpect(jsonPath("$[0].price").value(5000000.0));
    }

    @Test
    void execute_withCustomParameters_returnsOrders() throws Exception {
        Order order = Order.builder()
                .symbol("ETHINR")
                .type(Order.OrderType.SELL)
                .quantity(0.5)
                .price(200000.0)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(tradingService.executeFullPipeline("CustomSource", "CustomStrategy", "CustomBroker", "CustomMarketData"))
                .thenReturn(List.of(order));

        mockMvc.perform(post("/trade/execute")
                        .param("source", "CustomSource")
                        .param("strategy", "CustomStrategy")
                        .param("broker", "CustomBroker")
                        .param("marketData", "CustomMarketData"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("ETHINR"))
                .andExpect(jsonPath("$[0].type").value("SELL"));
    }

    @Test
    void execute_withServiceException_returnsInternalServerError() throws Exception {
        when(tradingService.executeFullPipeline(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid component names provided"));

        mockMvc.perform(post("/trade/execute"))
                .andExpect(status().isInternalServerError());
    }
}
