package com.project.tradebot.api;

import com.project.tradebot.application.service.BrokerService;
import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/broker")
@RequiredArgsConstructor
@Tag(name = "Broker", description = "Endpoints for broker interactions")
public class BrokerController {

    private final BrokerService brokerService;

    @GetMapping("/portfolio")
    @Operation(summary = "Get current portfolio from a broker")
    public Portfolio getPortfolio(@RequestParam(defaultValue = "CoinDCXBroker") String brokerName) {
        return brokerService.getPortfolio(brokerName);
    }

    @PostMapping("/order")
    @Operation(summary = "Place a single order via a broker")
    public Order placeOrder(
            @RequestParam(defaultValue = "CoinDCXBroker") String brokerName,
            @RequestBody Order order) {
        return brokerService.placeOrder(brokerName, order);
    }

    @PostMapping("/orders")
    @Operation(summary = "Place multiple orders in a batch via a broker")
    public List<Order> placeOrders(
            @RequestParam(defaultValue = "CoinDCXBroker") String brokerName,
            @RequestBody List<Order> orders) {
        return brokerService.placeOrders(brokerName, orders);
    }
}
