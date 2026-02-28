package com.project.tradebot.api;

import com.project.tradebot.application.service.TradingService;
import com.project.tradebot.domain.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
@Tag(name = "Trade Execution", description = "Endpoints for full pipeline execution")
public class TradeController {

    private final TradingService tradingService;

    @PostMapping("/execute")
    @Operation(summary = "Execute full trading pipeline (News -> Market Data -> Strategy -> Execution)")
    public List<Order> execute(
            @RequestParam(defaultValue = "CryptoNewsScraper") String source,
            @RequestParam(defaultValue = "OllamaLLMStrategy") String strategy,
            @RequestParam(defaultValue = "CoinDCXBroker") String broker,
            @RequestParam(defaultValue = "CoinDCXMarketData") String marketData) {
        return tradingService.executeFullPipeline(source, strategy, broker, marketData);
    }
}
