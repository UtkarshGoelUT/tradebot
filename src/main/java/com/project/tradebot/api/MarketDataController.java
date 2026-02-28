package com.project.tradebot.api;

import com.project.tradebot.application.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Endpoints for fetching real-time crypto prices")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/prices")
    @Operation(summary = "Get prices for multiple symbols from a specific provider")
    public Map<String, Double> getPrices(
            @RequestParam(defaultValue = "CoinDCXMarketData") String provider,
            @RequestParam Set<String> symbols) {
        return marketDataService.getPrices(provider, symbols);
    }

    @GetMapping("/price")
    @Operation(summary = "Get current price for a single symbol from a specific provider")
    public double getPrice(
            @RequestParam(defaultValue = "CoinDCXMarketData") String provider,
            @RequestParam String symbol) {
        return marketDataService.getPrice(provider, symbol);
    }

    @GetMapping("/providers")
    @Operation(summary = "Get list of available market data providers")
    public Set<String> getProviders() {
        return marketDataService.getAvailableProviders();
    }
}
