package com.project.tradebot.infrastructure.news;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.tradebot.application.ports.NewsSource;
import com.project.tradebot.domain.model.News;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CryptoNewsScraper implements NewsSource {

    private final WebClient webClient;

    @Value("${news.api.url:https://min-api.cryptocompare.com/data/v2/news/?lang=EN}")
    private String newsApiUrl;

    public CryptoNewsScraper(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public List<News> fetchNews() {
        log.info("Fetching latest crypto news from: {}", newsApiUrl);

        try {
            CryptoCompareResponse response = webClient.get()
                    .uri(newsApiUrl)
                    .retrieve()
                    .bodyToMono(CryptoCompareResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return response.getData().stream()
                        .map(this::mapToDomain)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error fetching news from CryptoCompare: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    private News mapToDomain(CryptoCompareNewsData data) {
        return News.builder()
                .title(data.getTitle())
                .description(data.getBody())
                .source(data.getSource())
                .timestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(data.getPublishedOn()), ZoneId.systemDefault()))
                .sentiment("NEUTRAL") // API doesn't provide sentiment directly in this endpoint
                .build();
    }

    @Override
    public String getName() {
        return "CryptoNewsScraper";
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CryptoCompareResponse {
        @JsonProperty("Data")
        private List<CryptoCompareNewsData> data;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CryptoCompareNewsData {
        private String title;
        private String body;
        private String source;
        @JsonProperty("published_on")
        private long publishedOn;
    }
}
