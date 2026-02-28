package com.project.tradebot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {
    private String title;
    private String description;
    private String source;
    private LocalDateTime timestamp;
    private String sentiment; // e.g., POSITIVE, NEGATIVE, NEUTRAL
}
