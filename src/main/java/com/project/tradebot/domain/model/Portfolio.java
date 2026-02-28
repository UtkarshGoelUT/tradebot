package com.project.tradebot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private Map<String, Double> balances; // symbol -> amount
    private double totalValueInUsd;
}
