package com.project.tradebot.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.tradebot.application.ports.Broker;
import com.project.tradebot.application.ports.MarketData;
import com.project.tradebot.application.ports.NewsSource;
import com.project.tradebot.application.ports.TradingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TradingConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        // Crucial: This prevents scientific notation like 1E+2 in JSON numbers
        mapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
        return mapper;
    }

    @Bean
    public Map<String, NewsSource> newsSources(List<NewsSource> sources) {
        return sources.stream().collect(Collectors.toMap(NewsSource::getName, Function.identity()));
    }

    @Bean
    public Map<String, TradingStrategy> strategies(List<TradingStrategy> strategies) {
        return strategies.stream().collect(Collectors.toMap(TradingStrategy::getName, Function.identity()));
    }

    @Bean
    public Map<String, Broker> brokers(List<Broker> brokers) {
        return brokers.stream().collect(Collectors.toMap(Broker::getName, Function.identity()));
    }

    @Bean
    public Map<String, MarketData> marketDataImplementations(List<MarketData> services) {
        return services.stream().collect(Collectors.toMap(MarketData::getName, Function.identity()));
    }
}
