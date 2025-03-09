package com.analyzer.event_analyzer.service;

import com.analyzer.event_analyzer.model.Event;
import com.analyzer.event_analyzer.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventAggregationService {

    private final ReactiveMongoTemplate mongoTemplate;
    private final EventRepository eventRepository;

    /**
     * Aggrega eventi per tipo nel periodo specificato
     */
    public Flux<Map<String, Object>> aggregateEventsByType(Instant startTime, Instant endTime) {
        // Operazioni di aggregazione MongoDB
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("timestamp").gte(startTime).lte(endTime));

        GroupOperation groupOperation = Aggregation.group("type")
                .count().as("count")
                .sum("payload.value").as("totalValue")
                .avg("payload.value").as("averageValue")
                .max("timestamp").as("lastEventTime");

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("eventType")
                .andExclude("_id")
                .andInclude("count", "totalValue", "averageValue", "lastEventTime");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                projectionOperation);

        return mongoTemplate.aggregate(aggregation, "events", (Class<Map<String, Object>>) (Class<?>) Map.class);
    }

    /**
     * Analizza tendenze eventi per rilevare anomalie
     */
    public Flux<Map> analyzeEventTrends(String eventType, int periodDays) {
        Instant now = Instant.now();
        Instant startPeriod = now.minus(periodDays, ChronoUnit.DAYS);

        // Operazioni per aggregare per giorno
        MatchOperation matchByType = Aggregation.match(
                Criteria.where("type").is(eventType)
                        .and("timestamp").gte(startPeriod));

        // Aggrega per giorno
        AggregationOperation projectToYMD = Aggregation.project()
                .andExpression("year(timestamp)").as("year")
                .andExpression("month(timestamp)").as("month")
                .andExpression("dayOfMonth(timestamp)").as("day")
                .and("payload");

        GroupOperation groupByDay = Aggregation.group("year", "month", "day")
                .count().as("count")
                .avg("payload.value").as("avgValue")
                .sum("payload.value").as("totalValue");

        ProjectionOperation projectResults = Aggregation.project()
                .and("_id.year").as("year")
                .and("_id.month").as("month")
                .and("_id.day").as("day")
                .andExclude("_id")
                .andInclude("count", "avgValue", "totalValue");

        Aggregation aggregation = Aggregation.newAggregation(
                matchByType,
                projectToYMD,
                groupByDay,
                projectResults,
                Aggregation.sort(Sort.Direction.ASC, "year", "month", "day")
        );

        return mongoTemplate.aggregate(aggregation, "events", Map.class);
    }

    /**
     * Genera report di analisi completo
     */
    public Mono<Map<String, Object>> generateCompleteAnalyticsReport(int days) {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(days, ChronoUnit.DAYS);

        Map<String, Object> report = new HashMap<>();

        // 1. Conteggio eventi totali
        Mono<Long> totalEvents = eventRepository.count();

        // 2. Distribuzione per tipo
        Flux<Map<String, Object>> eventsByType = aggregateEventsByType(startTime, endTime);

        // 3. Trend temporali per ogni tipo
        // Questa è un'operazione costosa su dataset grandi, quindi limitiamo i tipi
        Flux<String> eventTypes = eventRepository.findAll()
                .map(Event::getType)
                .distinct()
                .take(5); // Limita a 5 tipi più comuni

        Flux<Map<String, Object>> trendsByType = eventTypes.flatMap(type ->
                analyzeEventTrends(type, days)
                        .collectList()
                        .map(trends -> {
                            Map<String, Object> typeTrend = new HashMap<>();
                            typeTrend.put("eventType", type);
                            typeTrend.put("dailyTrends", trends);
                            return typeTrend;
                        })
        );

        // Combina tutti i risultati in un unico report
        return totalEvents.flatMap(total -> {
            report.put("totalEvents", total);
            report.put("period", Map.of(
                    "start", startTime.toString(),
                    "end", endTime.toString(),
                    "durationDays", days
            ));

            return eventsByType.collectList()
                    .doOnNext(types -> report.put("eventsByType", types))
                    .then(trendsByType.collectList())
                    .doOnNext(trends -> report.put("trends", trends))
                    .thenReturn(report);
        });
    }
}