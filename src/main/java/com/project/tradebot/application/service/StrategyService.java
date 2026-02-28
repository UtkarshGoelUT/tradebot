package com.project.tradebot.application.service;

import com.project.tradebot.application.ports.TradingStrategy;
import com.project.tradebot.domain.model.TradeContext;
import com.project.tradebot.domain.model.TradeSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StrategyService {
    private final Map<String, TradingStrategy> strategies;

    public List<TradeSignal> runStrategy(String strategyName, TradeContext context) {
        TradingStrategy strategy = strategies.get(strategyName);
        if (strategy == null) throw new IllegalArgumentException("Strategy not found");
        return strategy.generateSignals(context);
    }
}
