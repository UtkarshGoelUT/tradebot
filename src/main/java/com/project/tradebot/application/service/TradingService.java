package com.project.tradebot.application.service;

import com.project.tradebot.application.ports.Broker;
import com.project.tradebot.application.ports.MarketData;
import com.project.tradebot.application.ports.NewsSource;
import com.project.tradebot.application.ports.TradingStrategy;
import com.project.tradebot.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private final Map<String, NewsSource> newsSources;
    private final Map<String, TradingStrategy> strategies;
    private final Map<String, Broker> brokers;
    private final Map<String, MarketData> marketDataImplementations;

    @Value("${trading.max-allocation-per-trade-inr:5000.0}")
    private double maxAllocationPerTradeInr;

    @Value("${trading.min-confidence-threshold:0.7}")
    private double minConfidenceThreshold;

    public List<Order> executeFullPipeline(String sourceName, String strategyName, String brokerName, String marketDataName) {
        log.info("Starting trading pipeline with Source: {}, Strategy: {}, Broker: {}, MarketData: {}", 
                sourceName, strategyName, brokerName, marketDataName);

        NewsSource source = newsSources.get(sourceName);
        TradingStrategy strategy = strategies.get(strategyName);
        Broker broker = brokers.get(brokerName);
        MarketData marketData = marketDataImplementations.get(marketDataName);

        if (source == null || strategy == null || broker == null || marketData == null) {
            throw new IllegalArgumentException("Invalid component names provided");
        }

        // 1. Fetch News
        List<News> news = source.fetchNews();
        log.info("Fetched {} news items", news.size());

        // 2. Get Portfolio
        Portfolio portfolio = broker.getPortfolio();
        log.info("Current portfolio assets: {}", portfolio.getBalances().keySet());

        // 3. Get Market Data for relevant symbols (INR markets)
        Set<String> symbolsToFetch = identifyRelevantSymbols(portfolio, news);
        Map<String, Double> marketPrices = marketData.getPrices(symbolsToFetch);
        log.info("Fetched prices for {} symbols", marketPrices.size());

        // 4. Create Context
        TradeContext context = TradeContext.builder()
                .recentNews(news)
                .portfolio(portfolio)
                .marketPrices(marketPrices)
                .build();

        // 5. Generate Signals
        List<TradeSignal> signals = strategy.generateSignals(context);
        log.info("Generated {} trade signals", signals.size());

        // 6. Execute Trades Judiciously via Batch API
        List<Order> ordersToExecute = prepareOrders(signals, marketPrices, portfolio);
        if (ordersToExecute.isEmpty()) {
            log.info("No trades met the criteria for execution.");
            return new ArrayList<>();
        }

        return broker.placeOrders(ordersToExecute);
    }

    private Set<String> identifyRelevantSymbols(Portfolio portfolio, List<News> news) {
        Set<String> symbols = new HashSet<>();
        portfolio.getBalances().keySet().forEach(s -> {
            if (!s.equals("INR") && !s.equals("USDT")) {
                symbols.add(s + "INR");
            }
        });
        symbols.add("BTCINR");
        symbols.add("ETHINR");
        news.forEach(n -> {
            String title = n.getTitle().toUpperCase();
            if (title.contains("BTC") || title.contains("BITCOIN")) symbols.add("BTCINR");
            if (title.contains("ETH") || title.contains("ETHEREUM")) symbols.add("ETHINR");
            if (title.contains("SOL") || title.contains("SOLANA")) symbols.add("SOLINR");
        });
        return symbols;
    }

    private List<Order> prepareOrders(List<TradeSignal> signals, Map<String, Double> prices, Portfolio portfolio) {
        List<Order> orders = new ArrayList<>();
        double currentInrBalance = portfolio.getBalances().getOrDefault("INR", 0.0);

        for (TradeSignal signal : signals) {
            if (signal.getConfidence() < minConfidenceThreshold) continue;

            String exchangeSymbol = signal.getSymbol().endsWith("INR") ? signal.getSymbol() : signal.getSymbol() + "INR";
            Double price = prices.get(exchangeSymbol);

            if (price == null || price <= 0) continue;

            if (signal.getType() == TradeSignal.SignalType.BUY) {
                double targetSpend = Math.min(maxAllocationPerTradeInr * signal.getConfidence(), currentInrBalance * 0.95);
                if (targetSpend < 100) continue;

                orders.add(Order.builder()
                        .symbol(exchangeSymbol)
                        .type(Order.OrderType.BUY)
                        .quantity(targetSpend / price)
                        .price(price)
                        .build());
                currentInrBalance -= targetSpend;

            } else if (signal.getType() == TradeSignal.SignalType.SELL) {
                String baseAsset = exchangeSymbol.replace("INR", "");
                double availableAsset = portfolio.getBalances().getOrDefault(baseAsset, 0.0);
                if (availableAsset > 0) {
                    orders.add(Order.builder()
                            .symbol(exchangeSymbol)
                            .type(Order.OrderType.SELL)
                            .quantity(availableAsset * signal.getConfidence())
                            .price(price)
                            .build());
                }
            }
        }
        return orders;
    }
}
