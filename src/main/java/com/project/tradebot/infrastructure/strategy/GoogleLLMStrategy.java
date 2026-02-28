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

@Component
@Slf4j
public class GoogleLLMStrategy implements TradingStrategy {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${google.llm.model:gemini-3-flash-preview}")
    private String modelName;

    @Value("${google.llm.api-key:}")
    private String apiKey;

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    public GoogleLLMStrategy(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TradeSignal> generateSignals(TradeContext context) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Google LLM API Key is missing. Skipping strategy.");
            return new ArrayList<>();
        }

        log.info("Generating signals using Google LLM model: {} for news items: {}", modelName, context.getRecentNews().size());

        String prompt = buildPrompt(context);
        String url = String.format(API_URL_TEMPLATE, modelName);

        try {
            GeminiRequest request = new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));

            GeminiResponse response = webClient.post()
                    .uri(url)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                String responseText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                log.debug("Google LLM Response: {}", responseText);
                return parseSignals(responseText);
            }
        } catch (Exception e) {
            log.error("Error calling Google LLM API: {}", e.getMessage());
        }

        return new ArrayList<>();
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
            log.warn("Failed to parse Google LLM response as JSON: {}. Response text: {}", e.getMessage(), text);
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "GoogleLLMStrategy";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeminiRequest {
        private List<Content> contents;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;
    }
}
