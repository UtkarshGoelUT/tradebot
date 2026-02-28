package com.project.tradebot.api;

import com.project.tradebot.application.service.StrategyService;
import com.project.tradebot.domain.model.TradeContext;
import com.project.tradebot.domain.model.TradeSignal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/strategy")
@RequiredArgsConstructor
@Tag(name = "Strategy", description = "Endpoints for trading strategy engine")
public class StrategyController {

    private final StrategyService strategyService;

    @PostMapping("/run")
    @Operation(summary = "Run a specific strategy with the provided context")
    public List<TradeSignal> runStrategy(
            @RequestParam(defaultValue = "GoogleLLMStrategy") String strategyName,
            @RequestBody TradeContext context) {
        return strategyService.runStrategy(strategyName, context);
    }
}
