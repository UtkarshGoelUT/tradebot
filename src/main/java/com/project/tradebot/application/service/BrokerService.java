package com.project.tradebot.application.service;

import com.project.tradebot.application.ports.Broker;
import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrokerService {
    private final Map<String, Broker> brokers;

    public Portfolio getPortfolio(String brokerName) {
        Broker broker = brokers.get(brokerName);
        if (broker == null) throw new IllegalArgumentException("Broker not found");
        return broker.getPortfolio();
    }

    public Order placeOrder(String brokerName, Order order) {
        Broker broker = brokers.get(brokerName);
        if (broker == null) throw new IllegalArgumentException("Broker not found");
        return broker.placeOrder(order);
    }
}
