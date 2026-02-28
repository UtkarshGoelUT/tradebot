package com.project.tradebot.api;

import com.project.tradebot.application.service.NewsService;
import com.project.tradebot.domain.model.News;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "Endpoints for news scraping and retrieval")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "Fetch latest news from a source")
    public List<News> getNews(@RequestParam(defaultValue = "CryptoNewsScraper") String source) {
        return newsService.fetchNews(source);
    }

    @PostMapping("/scrape")
    @Operation(summary = "Trigger scraping from a source")
    public List<News> triggerScrape(@RequestParam(defaultValue = "CryptoNewsScraper") String source) {
        return newsService.fetchNews(source);
    }
}
