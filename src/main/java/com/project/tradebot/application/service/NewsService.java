package com.project.tradebot.application.service;

import com.project.tradebot.application.ports.NewsSource;
import com.project.tradebot.domain.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final Map<String, NewsSource> newsSources;

    public List<News> fetchNews(String sourceName) {
        NewsSource source = newsSources.get(sourceName);
        if (source == null) throw new IllegalArgumentException("Source not found");
        return source.fetchNews();
    }
}
