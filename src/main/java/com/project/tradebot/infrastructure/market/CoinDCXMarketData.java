package com.project.tradebot.infrastructure.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.tradebot.application.ports.MarketData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CoinDCXMarketData implements MarketData {

    private final WebClient webClient;
    private final String tickerUrl;

    public CoinDCXMarketData(
            WebClient.Builder webClientBuilder,
            @Value("${coindcx.api.base-url:https://api.coindcx.com}") String baseUrl,
            @Value("${coindcx.api.ticker-url:/exchange/ticker}") String tickerUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.tickerUrl = tickerUrl;
    }

    @Override
    public Map<String, Double> getPrices(Set<String> symbols) {
        log.info("Fetching market prices for {} symbols from CoinDCX", symbols != null ? symbols.size() : 0);

        if (symbols == null) return Collections.emptyMap();

        try {
            List<CoinDCXTicker> tickers = webClient.get()
                    .uri(tickerUrl)
                    .retrieve()
                    .bodyToFlux(CoinDCXTicker.class)
                    .collectList()
                    .block();

            if (tickers != null) {
                return tickers.stream()
                        .filter(t -> t.getMarket() != null && (symbols.isEmpty() || symbols.contains(t.getMarket())))
                        .filter(t -> t.getLastPrice() != null) // Avoid parsing null prices
                        .collect(Collectors.toMap(
                                CoinDCXTicker::getMarket,
                                t -> {
                                    try {
                                        return Double.parseDouble(t.getLastPrice());
                                    } catch (NumberFormatException e) {
                                        log.warn("Failed to parse price for {}: {}", t.getMarket(), t.getLastPrice());
                                        return 0.0;
                                    }
                                },
                                (existing, replacement) -> existing
                        ));
            }
        } catch (Exception e) {
            log.error("Error fetching market prices from CoinDCX: {}. Message: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        return Collections.emptyMap();
    }

    @Override
    public double getPrice(String symbol) {
        if (symbol == null) return 0.0;
        Map<String, Double> prices = getPrices(Set.of(symbol));
        return prices.getOrDefault(symbol, 0.0);
    }

    @Override
    public String getName() {
        return "CoinDCXMarketData";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoinDCXTicker {
        private String market;
        @JsonProperty("last_price")
        private String lastPrice;
    }
}
