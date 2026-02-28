package com.project.tradebot.application.ports;

import java.util.Map;
import java.util.Set;

public interface MarketData {
    Map<String, Double> getPrices(Set<String> symbols);
    double getPrice(String symbol);
    String getName();
}
