package com.project.tradebot.infrastructure.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tradebot.application.ports.TradingStrategy;
import com.project.tradebot.domain.model.TradeContext;
import com.project.tradebot.domain.model.TradeSignal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OllamaStrategy implements TradingStrategy {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.model:llama3.2}")
    private String modelName;

    @Value("${ollama.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    public OllamaStrategy(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TradeSignal> generateSignals(TradeContext context) {
        log.info("Generating signals using real Ollama model: {} for news items: {}", modelName, context.getRecentNews().size());

        String prompt = buildPrompt(context);
        
        try {
            OllamaRequest request = OllamaRequest.builder()
                    .model(modelName)
                    .prompt(prompt)
                    .stream(false)
                    .build();

            OllamaResponse response = webClient.post()
                    .uri(ollamaUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .block();

            if (response != null && response.getResponse() != null) {
                String responseText = response.getResponse();
                log.debug("Ollama Response: {}", responseText);
                return parseSignals(responseText);
            }
        } catch (Exception e) {
            log.error("Error calling Ollama API: {}. Falling back to demo strategy logic.", e.getMessage());
        }

        // Fallback to demo logic if Ollama is unavailable
        return fallbackLogic(context);
    }

    private String buildPrompt(TradeContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert crypto trading analyst. Analyze the following context to generate trade signals.\n\n");
        sb.append("Current Portfolio: ").append(context.getPortfolio()).append("\n");
        sb.append("Market Prices: ").append(context.getMarketPrices()).append("\n\n");
        sb.append("Recent News:\n");
        context.getRecentNews().forEach(n -> 
            sb.append("- Title: ").append(n.getTitle())
              .append("\n  Description: ").append(n.getDescription())
              .append("\n  Sentiment: ").append(n.getSentiment()).append("\n")
        );
        sb.append("\nRespond ONLY with a JSON list of objects. Each object MUST have fields: ");
        sb.append("\"symbol\", \"type\" (must be one of BUY, SELL, HOLD), \"confidence\" (float 0.0 to 1.0), and \"reason\".\n");
        sb.append("Format Example: [{\"symbol\": \"BTC\", \"type\": \"BUY\", \"confidence\": 0.9, \"reason\": \"Strong positive sentiment in recent news.\"}]\n");
        return sb.toString();
    }

    private List<TradeSignal> parseSignals(String text) {
        try {
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start != -1 && end != -1) {
                String jsonPart = text.substring(start, end + 1);
                return objectMapper.readValue(jsonPart, new TypeReference<List<TradeSignal>>() {});
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse Ollama response as JSON: {}. Response text: {}", e.getMessage(), text);
        }
        return new ArrayList<>();
    }

    private List<TradeSignal> fallbackLogic(TradeContext context) {
        return context.getRecentNews().stream()
                .filter(n -> "POSITIVE".equalsIgnoreCase(n.getSentiment()))
                .map(n -> TradeSignal.builder()
                        .symbol("BTC")
                        .type(TradeSignal.SignalType.BUY)
                        .confidence(0.7)
                        .reason("Fallback: News sentiment analysis: " + n.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "OllamaLLMStrategy";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaResponse {
        private String response;
    }
}
