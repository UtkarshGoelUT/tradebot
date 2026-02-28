package com.project.tradebot.application.ports;

import com.project.tradebot.domain.model.News;
import java.util.List;

public interface NewsSource {
    List<News> fetchNews();
    String getName();
}
