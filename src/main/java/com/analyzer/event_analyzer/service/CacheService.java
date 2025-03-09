package com.analyzer.event_analyzer.service;

import com.analyzer.event_analyzer.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final ReactiveRedisTemplate<String, Event> redisTemplate;

    private static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(30);
    private static final String EVENT_KEY_PREFIX = "event:";
    private static final String RECENT_EVENTS_KEY_PREFIX = "recent:";

    /**
     * Salva un evento nella cache
     */
    public Mono<Boolean> cacheEvent(Event event) {
        String key = buildEventKey(event);
        return redisTemplate.opsForValue()
                .set(key, event, DEFAULT_CACHE_DURATION);
    }

    /**
     * Recupera un evento dalla cache per ID
     */
    public Mono<Event> getCachedEvent(String eventId) {
        String key = EVENT_KEY_PREFIX + eventId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Aggiunge un evento alla lista degli eventi recenti per tipo
     */
    public Mono<Long> addToRecentEvents(Event event) {
        String key = RECENT_EVENTS_KEY_PREFIX + event.getType();
        return redisTemplate.opsForList()
                .leftPush(key, event)
                .doOnNext(size -> {
                    // Mantiene la lista a una dimensione massima di 100 elementi
                    if (size > 100) {
                        redisTemplate.opsForList().trim(key, 0, 99).subscribe();
                    }

                    // Imposta scadenza per la lista
                    redisTemplate.expire(key, Duration.ofHours(6)).subscribe();
                });
    }

    /**
     * Recupera gli eventi recenti per tipo
     */
    public Flux<Event> getRecentEventsByType(String type, int limit) {
        String key = RECENT_EVENTS_KEY_PREFIX + type;
        return redisTemplate.opsForList()
                .range(key, 0, limit - 1);
    }

    /**
     * Elimina dati vecchi dalla cache (pulizia)
     */
    public Mono<Long> cleanupExpiredEvents() {
        // Pattern per trovare tutte le chiavi degli eventi
        String pattern = EVENT_KEY_PREFIX + "*";

        // Cerca chiavi che contengono eventi più vecchi di 1 giorno
        Instant cutoffTime = Instant.now().minus(1, ChronoUnit.DAYS);

        return redisTemplate.scan(ScanOptions.scanOptions().match(pattern).build())
                .buffer(100) // raggruppa le chiavi in batch di 100
                .flatMap(keys -> Flux.fromIterable(keys)
                        .flatMap(key -> redisTemplate.opsForValue().get(key)
                                .filter(event -> event.getTimestamp().isBefore(cutoffTime))
                                .flatMap(event -> redisTemplate.delete(key))))
                .reduce(0L, (count, deleted) -> count + deleted);
    }

    /**
     * Genera la chiave Redis per un evento
     */
    private String buildEventKey(Event event) {
        return EVENT_KEY_PREFIX + event.getId();
    }
}