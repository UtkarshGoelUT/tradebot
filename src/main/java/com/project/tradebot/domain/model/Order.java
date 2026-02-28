package com.project.tradebot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    public enum OrderType { BUY, SELL }
    public enum OrderStatus { PENDING, EXECUTED, FAILED }

    private String symbol;
    private OrderType type;
    private double quantity;
    private double price;
    private OrderStatus status;
    private String orderId;
}
