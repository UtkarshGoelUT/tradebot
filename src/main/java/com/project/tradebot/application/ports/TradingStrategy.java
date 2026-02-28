package com.project.tradebot.application.ports;

import com.project.tradebot.domain.model.TradeContext;
import com.project.tradebot.domain.model.TradeSignal;
import java.util.List;

public interface TradingStrategy {
    List<TradeSignal> generateSignals(TradeContext context);
    String getName();
}
