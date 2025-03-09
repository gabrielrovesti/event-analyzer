package com.analyzer.event_analyzer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String type;
    private String source;
    private String userId;
    private Instant timestamp;
    private Map<String, Object> payload;
    private String correlationId;
    private EventStatus status;

    public enum EventStatus {
        RECEIVED, PROCESSING, PROCESSED, FAILED
    }
}