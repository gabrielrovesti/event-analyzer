package com.analyzer.event_analyzer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;

import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.data.domain.Sort;

@Configuration
public class MongoIndexConfig {

    private final ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public MongoIndexConfig(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        // Ottieni ReactiveIndexOperations invece di IndexOperations
        ReactiveIndexOperations eventIndexOps = mongoTemplate.indexOps("events");
        ReactiveIndexOperations analyticsIndexOps = mongoTemplate.indexOps("event_analytics");

        // Creazione indici per eventi
        createTimestampIndex(eventIndexOps);
        createTypeTimestampIndex(eventIndexOps);
        createSourceStatusIndex(eventIndexOps);
        createPayloadValueIndex(eventIndexOps);

        // Creazione indici per analytics
        createEventTypePeriodIndex(analyticsIndexOps);
    }

    private void createTimestampIndex(ReactiveIndexOperations indexOps) {
        Index index = new Index().on("timestamp", Sort.Direction.DESC);
        indexOps.ensureIndex(index).subscribe();
    }

    private void createTypeTimestampIndex(ReactiveIndexOperations indexOps) {
        Document compoundIdx = new Document();
        compoundIdx.put("type", 1);
        compoundIdx.put("timestamp", -1);
        IndexDefinition indexDefinition = new CompoundIndexDefinition(compoundIdx);
        indexOps.ensureIndex(indexDefinition).subscribe();
    }

    private void createSourceStatusIndex(ReactiveIndexOperations indexOps) {
        Document compoundIdx = new Document();
        compoundIdx.put("source", 1);
        compoundIdx.put("status", 1);
        IndexDefinition indexDefinition = new CompoundIndexDefinition(compoundIdx);
        indexOps.ensureIndex(indexDefinition).subscribe();
    }

    private void createPayloadValueIndex(ReactiveIndexOperations indexOps) {
        Index index = new Index().on("payload.value", Sort.Direction.ASC);
        indexOps.ensureIndex(index).subscribe();
    }

    private void createEventTypePeriodIndex(ReactiveIndexOperations indexOps) {
        Document compoundIdx = new Document();
        compoundIdx.put("eventType", 1);
        compoundIdx.put("periodStart", -1);
        IndexDefinition indexDefinition = new CompoundIndexDefinition(compoundIdx);
        indexOps.ensureIndex(indexDefinition).subscribe();
    }
}