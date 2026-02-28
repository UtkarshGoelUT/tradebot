package com.project.tradebot.application.ports;

import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;

public interface Broker {
    Portfolio getPortfolio();
    Order placeOrder(Order order);
    double getBalance(String asset);
    String getName();
}
