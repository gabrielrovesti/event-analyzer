package com.analyzer.event_analyzer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "event_analytics")
public class EventAnalytics {
    @Id
    private String id;
    private String eventType;
    private Instant periodStart;
    private Instant periodEnd;
    private Long count;
    private Map<String, Long> countBySource;
    private Map<String, Double> averagesByMetric;
    private Map<String, Object> additionalMetrics;
}