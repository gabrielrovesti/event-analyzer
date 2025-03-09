package com.analyzer.event_analyzer.repository;

import com.analyzer.event_analyzer.model.Event;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import java.time.Instant;

public interface EventRepository extends ReactiveMongoRepository<Event, String> {
    Flux<Event> findByTypeAndTimestampBetween(String type, Instant start, Instant end);

    @Query("{'payload.value': {$gt: ?0}}")
    Flux<Event> findByPayloadValueGreaterThan(double threshold);

    Flux<Event> findBySourceAndStatusOrderByTimestampDesc(String source, Event.EventStatus status);
}