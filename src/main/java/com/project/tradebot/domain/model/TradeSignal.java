package com.project.tradebot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignal {
    public enum SignalType { BUY, SELL, HOLD }

    private String symbol;
    private SignalType type;
    private double confidence; // 0.0 to 1.0
    private String reason;
}
