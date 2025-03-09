package com.analyzer.event_analyzer.service;

import com.analyzer.event_analyzer.model.Event;
import com.analyzer.event_analyzer.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Mono<Event> saveEvent(Event event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        if (event.getStatus() == null) {
            event.setStatus(Event.EventStatus.RECEIVED);
        }
        return eventRepository.save(event);
    }

    public Flux<Event> getRecentEventsByType(String type, int limit) {
        Instant hourAgo = Instant.now().minusSeconds(3600);
        return eventRepository.findByTypeAndTimestampBetween(type, hourAgo, Instant.now())
                .take(limit);
    }

    public Flux<Event> findAnomalousEvents(double threshold) {
        return eventRepository.findByPayloadValueGreaterThan(threshold);
    }

    public Mono<Event> updateEventStatus(String eventId, Event.EventStatus newStatus) {
        return eventRepository.findById(eventId)
                .flatMap(event -> {
                    event.setStatus(newStatus);
                    return eventRepository.save(event);
                });
    }
}

