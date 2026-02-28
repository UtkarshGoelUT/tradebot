package com.project.tradebot.application.ports;

import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;
import java.util.List;

public interface Broker {
    Portfolio getPortfolio();
    Order placeOrder(Order order);
    List<Order> placeOrders(List<Order> orders); // New method for batch execution
    double getBalance(String asset);
    String getName();
}
