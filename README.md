# Event Analyzer

Sistema reattivo di analisi eventi in tempo reale basato su Spring WebFlux, MongoDB e Redis.

## Panoramica

Event Analyzer è un'applicazione Spring Boot reattiva progettata per la raccolta, l'elaborazione e l'analisi in tempo reale di eventi provenienti da diverse fonti. Utilizza MongoDB come storage principale e Redis per il caching, garantendo alta performance e scalabilità.

## Caratteristiche principali

- **Architettura reattiva** - Utilizzo di WebFlux e Reactive MongoDB/Redis per un'elaborazione non bloccante
- **Storage efficiente** - MongoDB per la persistenza dei dati con indici ottimizzati
- **Caching avanzato** - Redis per migliorare le performance delle query frequenti
- **Analisi in tempo reale** - Aggregazioni e metriche calcolate periodicamente
- **API RESTful** - Endpoints per l'inserimento e la consultazione degli eventi
- **Sicurezza** - Autenticazione e autorizzazione integrate

## Tecnologie utilizzate

- **Spring Boot 3.2.x**
- **Spring WebFlux**
- **Spring Data MongoDB Reactive**
- **Spring Data Redis Reactive**
- **Spring Security**
- **MongoDB**
- **Redis**
- **TestContainers** (per i test di integrazione)
- **Java 17**

## Requisiti

- JDK 17 o superiore
- MongoDB 6.0+
- Redis (opzionale, ma consigliato)
- Maven 3.6+

## Installazione

### Prerequisiti

```bash
# Installazione di MongoDB (Windows)
# Avvio del servizio MongoDB
mongod --dbpath="C:\data\db"

# Installazione di Redis (Windows)
# Avvio del servizio Redis
redis-server
```

### Configurazione

1. Clona il repository
```bash
git clone https://github.com/tuoutente/event-analyzer.git
cd event-analyzer
```

2. Configura le proprietà dell'applicazione in `src/main/resources/application.yml`

3. Compila il progetto
```bash
mvn clean package
```

4. Esegui l'applicazione
```bash
java -jar target/event-analyzer-0.0.1-SNAPSHOT.jar
```

## Struttura del progetto

```
src/main/java/com/analyzer/event_analyzer/
├── EventAnalyzerApplication.java
├── config/
│   ├── MongoConfig.java
│   ├── MongoIndexConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── EventController.java
│   └── AuthController.java
├── model/
│   ├── Event.java
│   └── EventAnalytics.java
├── repository/
│   ├── EventRepository.java
│   └── EventAnalyticsRepository.java
├── security/
│   └── JwtService.java
└── service/
    ├── EventService.java
    ├── AnalyticsService.java
    ├── EventAggregationService.java
    └── CacheService.java
```

## API

### Endpoint principali

- `POST /api/events` - Crea un nuovo evento
- `GET /api/events/recent?type={type}&limit={limit}` - Ottieni eventi recenti per tipo
- `GET /api/events/anomalies?threshold={threshold}` - Ottieni eventi anomali
- `GET /api/analytics/{eventType}?hours={hours}` - Ottieni analisi per tipo di evento
- `POST /api/auth/login` - Autenticazione (se si utilizza JWT)

### Esempio di creazione evento

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dXNlcjpwYXNzd29yZA==" \
  -d '{
    "type": "user_login",
    "source": "web_app",
    "userId": "user123",
    "payload": {
      "browser": "Chrome",
      "ip": "192.168.1.100",
      "device": "desktop"
    }
  }'
```

## Casi d'uso

- **Monitoraggio applicazioni**: Tracciamento di eventi come login, logout, azioni utente
- **Analisi IoT**: Elaborazione dati provenienti da sensori e dispositivi
- **Business Intelligence**: Raccolta metriche per analisi di vendite o performance
- **Sicurezza**: Identificazione di eventi anomali o potenziali minacce

## Licenza

MIT License

## Contribuire

Le pull request sono benvenute. Per modifiche importanti, apri prima un issue per discutere cosa vorresti cambiare.

---

Progetto sviluppato per il corso di Sistemi Distribuiti, Università XXX.