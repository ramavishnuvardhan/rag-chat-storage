# RAG Backend Platform
A production-ready backend microservices for securely storing and managing RAG-based chatbot conversations, built with Java 21, Spring Boot, PostgreSQL, Redis, and Dockerized microservice architecture.

## Requirements

- JDK 21
- Docker 
- Maven 3.9+

## Quick Start (Local, everything via Docker)

The compose files live in ./docker. We use .env.* files to drive all settings.

### Build the project

```bash
  cd rag-chat-storege
  mvn clean install
```

### Running locally
1. Create .env files (examples)
- `docker build -t rag-chat:tag .` 
- `docker run -d -p 8080:8080 rag-chat:tag` 

2. Start all services:

**Option 1: Local**:
```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=local
  
```

> Note: Ensure Docker is running and ports mentioned in the docker-compose.*.yml are available.

## Verify services
- Chat Storage: http://localhost:8080/actuator/health

## Swagger / API Docs
- Through Service: http://localhost:8080/swagger-ui/index.html

## Tech Stack
- Java 21
- Spring Boot 3.5
- H2
- Redis
- Springdoc OpenAPI
- Docker

## Features
- Microservice architecture with Spring Boot
- Chat conversation management (CRUD for sessions/messages)
- API Gateway with routing and rate limiting
- Redis caching for performance
- H2 for reliable storage
- API Key authentication for security
- OpenTelemetry for distributed tracing
- Health checks with Spring Boot Actuator
- Pagination support for message retrieval
- Swagger/OpenAPI documentation
- Dockerized setup for easy deployment
- Environment-specific configurations
- Unit  tests
- Auditing and logging of operations
- Global exception handling

## Microservices
| project                | Description                                |
|-----------------------|--------------------------------------------|
| `rag-chat-storage` | Manages the RAG-base chatbot conversations |
|

## REST API Key Endpoints (rag-chat-storage)
- Base path (service): /api/**

### Sessions
- `POST /api/sessions/` → Create new chat session
- `GET /api/sessions/` → Get all chat sessions
- `GET /api/sessions/{sessionId}` → Get chat session by ID
- `PATCH /api/sessions/{sessionId}/rename` → Rename chat session
- `PATCH /api/sessions/{sessionId}/favorite?favorite=true` → Mark or unmark chat session as favorite
- `DELETE /api/sessions/{sessionId}` → Delete chat session by ID

- `POST /api/sessions/{sessionId}/messages` → Add new message (supports optional retrieved context)
- `GET /api/sessions/{sessionId}/messages?page={page}&size={size}` → Get messages for session (paginated)
- `DELETE /api/sessions/{sessionId}/messages` → Delete all messages in a session
- `DELETE /api/messages/{messageId}` → Delete a single message by ID

> Note: Authorize and provide your API key in header X-API-Key in Swagger UI to test endpoints.

## Security (API Key & Filters)
- All endpoints (except /actuator/** and Swagger docs) require header `X-API-Key: <value from environment API_KEY>`.
- Local default: `API_KEY=local-ragchat-api-key`; override per environment in `.env.*`.
- Servlet filter chain enforces security:
  - `BaseFilter` defines a single allowlist for public paths (health checks, Swagger).
  - `AuthenticationFilter` (order 1) validates the API key.
  - `RateLimitFilter` (order 2) applies service-level throttling.
  - `AuditFilter` (order 3) populates correlation/audit context.
- Spring Security is kept minimal — CSRF is disabled and the custom filters handle authentication and authorization logic.

## Configurations
  - Chat Storage profiles: `rag-chat-storage-{local,dev,prod}.yml`

## Caching (Redis - Spring Cache Abstraction)
- Caches 
  - sessionsList → cached list of sessions 
  - sessions → individual session details 
  - sessionMessages → message lists per (sessionId, page, size) tuple
- TTL configurable via CACHE_TTL (seconds).

**Note**:
- We use Spring Cache Abstraction with RedisCacheManager. This can be easily swapped out for another cache provider if needed.
- We rely on @CacheEvict on mutating operations (create/rename/delete) to keep caches consistent.

## Rate Limiting
This project implements two types of rate limiting using Redis:

1.  Custom Redis Rate Limiter
    - Location: rag-chat-storage
    - Local profile → In-memory (ConcurrentHashMap + sliding window)
    - Dev/Prod → Redis atomic operations (keyed by clientId/IP)
    - Controlled by:
      - RATE_LIMIT_REQUESTS → allowed requests per window 
      - RATE_LIMIT_TIME_WINDOW → window seconds

## Error Handling
- Centralized GlobalExceptionHandler returns clear JSON errors with appropriate HTTP status:
  - 400 → validation errors (@Valid, @NotBlank, etc.)
  - 404 → not found (session/message ids)
  - 429 → rate-limit exceeded 
  - 500 → unhandled errors (with correlation IDs in logs)

## Further Enhancements
| Area                          | Description                                                                   |
|-------------------------------| ----------------------------------------------------------------------------- |
| **Authentication & Roles**    | Add JWT-based authentication with role-based access (Admin, User, Service).   |
| **Event Integration**         | Introduce Kafka or RabbitMQ for asynchronous message events and auditing.     |
| **Enhanced Caching**          | Implement cache warming and distributed cache invalidation strategies.        |
| **Search Functionality**      | Implement Elasticsearch and full-text search of conversations.              |
| **Monitoring Dashboard**      | Add Prometheus + Grafana for real-time metrics visualization.                 |
| **Unit & Integration Tests**  | Expand test coverage for services, controllers, and caching logic.            |
## Smoke Test (curl)
- Create a session:
```bash
curl -X POST http://localhost:8080/api/sessions/ \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: local-ragchat-api-key' \
  -d '{"name":"Demo Session"}'
```
- Add a message (note the optional `context` field):
```bash
curl -X POST http://localhost:8080/ragchatstorage/api/sessions/{sessionId}/messages \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: local-ragchat-api-key' \
  -d '{"sender":"USER","content":"Hello","context":"Weather forecast snippet"}'
```
- List messages with pagination:
```bash
curl -H 'X-API-Key: local-ragchat-api-key' \
  "http://localhost:8081/ragchatstorage/api/sessions/{sessionId}/messages?page=0&size=20"
```

## Troubleshooting
- **Kibana 5601 not responding**: Verify Elasticsearch is healthy via `docker compose logs elasticsearch`. If you see `vm.max_map_count` warnings, set `sudo sysctl -w vm.max_map_count=262144` on the host and restart the stack so Elasticsearch can allocate memory.
- **Gateway container unhealthy**: Health checks now rely on Redis being marked `healthy` and on successfully fetching configuration from Config Server/Eureka. If the gateway keeps restarting, inspect `docker compose logs api-gateway-service` and confirm `redis` reports `healthy`.
- **Elasticsearch exits with code 137**: Docker killed the process due to memory pressure. The default heap has been capped via `ES_JAVA_OPTS`; if the issue persists, increase Docker Desktop’s memory allowance or lower the heap further (e.g., `ES_JAVA_OPTS=-Xms256m -Xmx256m` in your `.env.*`) and remove stale data with `docker compose down -v esdata` before restarting.
- **Logstash exits unexpectedly**: The image defaults to a 1 GB JVM heap. Set `LS_JAVA_OPTS` in your `.env.*` (already wired to 256 MB locally) to fit within Docker Desktop’s memory budget, or allocate more RAM to Docker.
