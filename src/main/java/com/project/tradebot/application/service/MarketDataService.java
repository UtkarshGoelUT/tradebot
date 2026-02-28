package com.project.tradebot.application.service;

import com.project.tradebot.application.ports.MarketData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MarketDataService {
    private final Map<String, MarketData> marketDataImplementations;

    public Map<String, Double> getPrices(String providerName, Set<String> symbols) {
        MarketData provider = marketDataImplementations.get(providerName);
        if (provider == null) throw new IllegalArgumentException("Market Data Provider not found: " + providerName);
        return provider.getPrices(symbols);
    }

    public double getPrice(String providerName, String symbol) {
        MarketData provider = marketDataImplementations.get(providerName);
        if (provider == null) throw new IllegalArgumentException("Market Data Provider not found: " + providerName);
        return provider.getPrice(symbol);
    }

    public Set<String> getAvailableProviders() {
        return marketDataImplementations.keySet();
    }
}
