package com.analyzer.event_analyzer.service;

import com.analyzer.event_analyzer.model.Event;
import com.analyzer.event_analyzer.model.EventAnalytics;
import com.analyzer.event_analyzer.repository.EventAnalyticsRepository;
import com.analyzer.event_analyzer.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final EventRepository eventRepository;
    private final EventAnalyticsRepository analyticsRepository;

    public Flux<EventAnalytics> getAnalyticsForPeriod(String eventType, int lastHours) {
        Instant start = Instant.now().minus(lastHours, ChronoUnit.HOURS);
        return analyticsRepository.findByEventTypeAndPeriodStartGreaterThanEqual(eventType, start);
    }

    @Scheduled(fixedRate = 300000) // Esegui ogni 5 minuti
    public void computeHourlyAnalytics() {
        Instant hourAgo = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);

        // Raggruppa per tipo di evento
        Map<String, Flux<Event>> eventsByType = new HashMap<>();

        Flux<Event> recentEvents = eventRepository.findByTypeAndTimestampBetween(null, hourAgo, now);

        recentEvents.groupBy(Event::getType)
                .flatMap(group -> {
                    String eventType = group.key();

                    return group.collectList()
                            .flatMap(events -> {
                                // Calcola le metriche aggregate
                                Map<String, Long> countBySource = new HashMap<>();

                                for (Event event : events) {
                                    countBySource.merge(event.getSource(), 1L, Long::sum);
                                }

                                EventAnalytics analytics = new EventAnalytics();
                                analytics.setEventType(eventType);
                                analytics.setPeriodStart(hourAgo);
                                analytics.setPeriodEnd(now);
                                analytics.setCount((long) events.size());
                                analytics.setCountBySource(countBySource);

                                return analyticsRepository.save(analytics);
                            });
                }).subscribe();
    }
}

