**Short answer:** An **API gateway** is the single entry point: it routes requests, and handles auth, rate limiting and CORS in one place. **Service discovery** (Eureka) lets services register themselves and find each other by name instead of hard-coded hosts. A **circuit breaker** (Resilience4j) stops calling a failing dependency and fails fast with a fallback, so one slow service doesn't take down the rest. A **saga** keeps data consistent across services without a distributed transaction: a chain of local transactions, each with a compensating action if a later step fails.

## Explanation

**API gateway (Spring Cloud Gateway):** clients call one host. The gateway matches a route (`/orders/**` → `order-service`), validates the JWT, applies rate limits, adds a correlation id, then forwards. With discovery, the target URI is `lb://order-service` and a client-side load balancer picks an instance.

**Service discovery (Eureka):** each service instance registers with the Eureka server at startup and sends heartbeats. Callers ask the registry for `order-service` instances (clients cache the list) and load-balance across them. On Kubernetes you usually don't need Eureka, because Kubernetes Services and DNS do the same job.

**Circuit breaker (Resilience4j):**

- **CLOSED:** calls pass; failures and slow calls are recorded in a sliding window.
- **OPEN:** once the failure rate crosses a threshold (e.g. 50%), calls fail immediately (`CallNotPermittedException`) and the fallback runs. It stays open for a wait duration.
- **HALF_OPEN:** lets a few trial calls through. If they succeed, back to CLOSED; if not, OPEN again.

Combine it with a **timeout** (never wait forever), **retry with exponential backoff and jitter** (only for idempotent calls and transient errors), and a **bulkhead** (cap concurrent calls to one dependency).

**Saga:** example: place order → reserve stock → charge payment. If payment fails, run compensations: release stock, cancel order.

- **Choreography:** each service listens for events and emits the next one (`OrderCreated` → inventory reserves → `StockReserved` → payment charges). No central brain; simple for 2-3 steps, but the flow is hard to see and debug.
- **Orchestration:** one orchestrator tells each service what to do and tracks the state. Easier to reason about, monitor and change; the orchestrator is extra code to own.

## Example

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: orders
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
resilience4j:
  circuitbreaker:
    instances:
      inventory:
        sliding-window-size: 20
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

```java
@CircuitBreaker(name = "inventory", fallbackMethod = "stockUnknown")
@Retry(name = "inventory")
public StockDto checkStock(String sku) {
    return inventoryClient.getStock(sku);     // RestClient / OpenFeign, with a timeout set
}
private StockDto stockUnknown(String sku, Throwable t) {
    return StockDto.unknown(sku);             // degrade, don't cascade
}
```

(The gateway route keys above follow the classic `spring.cloud.gateway.routes` layout; newer Spring Cloud releases moved server properties under `spring.cloud.gateway.server.webflux`, so check the version you use.)

## Pitfalls and follow-ups

- **How did you use them?** Describe a real flow from your project: [which gateway, which downstream call got a circuit breaker, what the fallback returned, what the saga steps were]. Don't claim patterns you haven't used; say "we used X, and I know Y would fit when…".
- **Retries can make an outage worse.** Cap attempts, add backoff, and retry only idempotent operations (or send an idempotency key).
- **Saga gives eventual consistency, not isolation.** Other requests can see intermediate states; use status fields like `PENDING`.
- **Reliable events:** write the business row and an outbox row in one local transaction, then publish from the outbox. Avoids "DB committed but the event was lost".
- **Service-to-service auth behind the gateway:** the gateway validates the user's JWT and passes it on (token relay), and each service still validates it as an OAuth2 resource server (zero trust). For calls not on behalf of a user, use the OAuth2 client-credentials flow; mTLS (often via a service mesh) authenticates the services themselves.

Deeper: [S8 · Microservices: why, how, and what it costs](../academy/lessons/S8.md), [S9 · Cloud native & event-driven systems](../academy/lessons/S9.md).
