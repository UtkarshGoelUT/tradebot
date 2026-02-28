package com.project.tradebot.infrastructure.broker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tradebot.application.ports.Broker;
import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CoinDCXBroker implements Broker {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiSecret;
    private final String portfolioPath;
    private final String orderPath;
    private final String marketDetailsUrl;
    
    private final Map<String, Integer> marketPrecisions = new ConcurrentHashMap<>();

    public CoinDCXBroker(
            WebClient.Builder webClientBuilder, 
            ObjectMapper objectMapper,
            @Value("${coindcx.api.key:}") String apiKey,
            @Value("${coindcx.api.secret:}") String apiSecret,
            @Value("${coindcx.api.base-url:https://api.coindcx.com}") String baseUrl,
            @Value("${coindcx.api.portfolio-path:/exchange/v1/users/balances}") String portfolioPath,
            @Value("${coindcx.api.order-path:/exchange/v1/orders/create_multiple}") String orderPath,
            @Value("${coindcx.api.market-details-url:/exchange/v1/market_details}") String marketDetailsUrl) {
        
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.portfolioPath = portfolioPath;
        this.orderPath = orderPath;
        this.marketDetailsUrl = marketDetailsUrl;
    }

    private void loadMarketDetails() {
        if (!marketPrecisions.isEmpty()) return;
        try {
            List<CoinDCXMarketDetail> details = webClient.get()
                    .uri(marketDetailsUrl)
                    .retrieve()
                    .bodyToFlux(CoinDCXMarketDetail.class)
                    .collectList()
                    .block();
            
            if (details != null) {
                details.forEach(d -> {
                    if (d.getSymbol() != null) {
                        marketPrecisions.put(d.getSymbol(), d.getTargetCurrencyPrecision());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to load CoinDCX market details: {}", e.getMessage());
        }
    }

    @Override
    public Portfolio getPortfolio() {
        if (isMissingCredentials()) return getMockPortfolio();
        try {
            long timestamp = System.currentTimeMillis();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", timestamp);

            String jsonBody = objectMapper.writeValueAsString(body);
            String signature = generateSignature(jsonBody);

            List<CoinDCXBalance> balances = webClient.post()
                    .uri(portfolioPath)
                    .header("X-AUTH-APIKEY", apiKey)
                    .header("X-AUTH-SIGNATURE", signature)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToFlux(CoinDCXBalance.class)
                    .collectList()
                    .block();

            if (balances != null) {
                Map<String, Double> balanceMap = balances.stream()
                        .filter(b -> b.getBalance() > 0)
                        .collect(Collectors.toMap(CoinDCXBalance::getCurrency, CoinDCXBalance::getBalance));
                return Portfolio.builder().balances(balanceMap).totalValueInUsd(0.0).build();
            }
        } catch (Exception e) {
            log.error("Error fetching CoinDCX portfolio: {}", e.getMessage());
        }
        return getMockPortfolio();
    }

    @Override
    public Order placeOrder(Order order) {
        List<Order> result = placeOrders(List.of(order));
        return result.isEmpty() ? order : result.get(0);
    }

    @Override
    public List<Order> placeOrders(List<Order> orders) {
        if (isMissingCredentials()) {
            log.warn("Credentials missing. Mocking batch orders.");
            orders.forEach(o -> {
                o.setStatus(Order.OrderStatus.EXECUTED);
                o.setOrderId("MOCK-" + UUID.randomUUID());
            });
            return orders;
        }

        loadMarketDetails();
        long timestamp = System.currentTimeMillis();

        try {
            List<Map<String, Object>> orderList = new ArrayList<>();
            for (Order order : orders) {
                int precision = marketPrecisions.getOrDefault(order.getSymbol(), 8);
                
                BigDecimal qty = BigDecimal.valueOf(order.getQuantity())
                        .setScale(precision, RoundingMode.HALF_DOWN)
                        .stripTrailingZeros();

                Map<String, Object> oMap = new LinkedHashMap<>();
                oMap.put("side", order.getType().toString().toLowerCase());
                oMap.put("order_type", "market_order");
                oMap.put("market", order.getSymbol());
                oMap.put("total_quantity", qty);
                oMap.put("timestamp", timestamp);
                oMap.put("ecode", "I");
                oMap.put("client_order_id", UUID.randomUUID().toString().replace("-", ""));
                orderList.add(oMap);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orders", orderList);

            String jsonBody = objectMapper.writeValueAsString(body);
            String signature = generateSignature(jsonBody);

            log.info("CoinDCX Batch Order Payload: {}", jsonBody);

            String rawResponse = webClient.post()
                    .uri(orderPath)
                    .header("X-AUTH-APIKEY", apiKey)
                    .header("X-AUTH-SIGNATURE", signature)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp -> 
                        resp.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("CoinDCX Batch Order Error (400): {}", errorBody);
                            return Mono.error(new RuntimeException(errorBody));
                        })
                    )
                    .bodyToMono(String.class)
                    .block();

            CoinDCXOrderListResponse response = objectMapper.readValue(rawResponse, CoinDCXOrderListResponse.class);

            if (response != null && response.getOrders() != null) {
                for (int i = 0; i < Math.min(orders.size(), response.getOrders().size()); i++) {
                    CoinDCXOrderInfo info = response.getOrders().get(i);
                    orders.get(i).setOrderId(info.getId());
                    orders.get(i).setStatus(Order.OrderStatus.EXECUTED);
                }
                log.info("Successfully executed {}/{} batch orders", response.getOrders().size(), orders.size());
            }
        } catch (Exception e) {
            log.error("Error executing batch orders: {}", e.getMessage());
            orders.forEach(o -> o.setStatus(Order.OrderStatus.FAILED));
        }

        return orders;
    }

    @Override
    public double getBalance(String asset) {
        return getPortfolio().getBalances().getOrDefault(asset, 0.0);
    }

    @Override
    public String getName() {
        return "CoinDCXBroker";
    }

    private boolean isMissingCredentials() {
        return apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty();
    }

    private String generateSignature(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Portfolio getMockPortfolio() {
        Map<String, Double> balances = new HashMap<>();
        balances.put("BTC", 0.0);
        balances.put("INR", 10000.0);
        return Portfolio.builder().balances(balances).totalValueInUsd(120.0).build();
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CoinDCXBalance {
        private String currency;
        private double balance;
        @JsonProperty("locked_balance")
        private double lockedBalance;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CoinDCXOrderListResponse {
        private List<CoinDCXOrderInfo> orders;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CoinDCXOrderInfo {
        private String id;
        @JsonProperty("client_order_id")
        private String clientOrderId;
        private String status;
        private String market;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CoinDCXMarketDetail {
        @JsonProperty("symbol")
        private String symbol;
        @JsonProperty("target_currency_precision")
        private int targetCurrencyPrecision;
    }
}
