package com.analyzer.event_analyzer.repository;

import com.analyzer.event_analyzer.model.EventAnalytics;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import java.time.Instant;

public interface EventAnalyticsRepository extends ReactiveMongoRepository<EventAnalytics, String> {
    Flux<EventAnalytics> findByEventTypeAndPeriodStartGreaterThanEqual(String eventType, Instant start);
}