package com.analyzer.event_analyzer.controller;

import com.analyzer.event_analyzer.model.Event;
import com.analyzer.event_analyzer.model.EventAnalytics;
import com.analyzer.event_analyzer.service.AnalyticsService;
import com.analyzer.event_analyzer.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Event> createEvent(@RequestBody Event event) {
        return eventService.saveEvent(event);
    }

    @GetMapping("/events/recent")
    public Flux<Event> getRecentEvents(@RequestParam String type,
                                       @RequestParam(defaultValue = "10") int limit) {
        return eventService.getRecentEventsByType(type, limit);
    }

    @GetMapping("/events/anomalies")
    public Flux<Event> getAnomalies(@RequestParam double threshold) {
        return eventService.findAnomalousEvents(threshold);
    }

    @GetMapping("/analytics/{eventType}")
    public Flux<EventAnalytics> getAnalytics(@PathVariable String eventType,
                                             @RequestParam(defaultValue = "24") int hours) {
        return analyticsService.getAnalyticsForPeriod(eventType, hours);
    }

    @PutMapping("/events/{id}/status")
    public Mono<Event> updateEventStatus(@PathVariable String id,
                                         @RequestParam Event.EventStatus status) {
        return eventService.updateEventStatus(id, status);
    }
}
