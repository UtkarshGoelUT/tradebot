package com.project.tradebot.api;

import com.project.tradebot.application.service.BrokerService;
import com.project.tradebot.domain.model.Order;
import com.project.tradebot.domain.model.Portfolio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Place an order via a broker")
    public Order placeOrder(
            @RequestParam(defaultValue = "CoinDCXBroker") String brokerName,
            @RequestBody Order order) {
        return brokerService.placeOrder(brokerName, order);
    }
}
