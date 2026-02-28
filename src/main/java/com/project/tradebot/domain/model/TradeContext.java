package com.project.tradebot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeContext {
    private List<News> recentNews;
    private Portfolio portfolio;
    private Map<String, Double> marketPrices; // symbol -> price
}
