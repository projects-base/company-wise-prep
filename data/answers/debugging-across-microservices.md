**Short answer:** Give every request a trace context at the edge and propagate it on every hop - HTTP headers, Kafka message headers, async tasks - and put the trace id in every log line. Use the W3C `traceparent` standard (one trace id, a new span id per hop) instead of inventing several correlation ids; if a legacy system has its own id, keep it as a separate field or span attribute and log both. On Kubernetes, ship logs centrally, search by trace id, then use `kubectl logs/describe/exec` and the trace view to find the failing pod. Cloud authentication is OAuth 2.0 / OpenID Connect: services get short-lived tokens from an identity provider and validate them, ideally with managed identities instead of stored secrets.

## Explanation

**Correlation and tracing**
- A *trace id* identifies the whole request; each service creates a *span* (with its own span id and the parent's id). That gives a tree you can view in Jaeger, Zipkin, Tempo or Azure Application Insights.
- **Multiple ids problem:** an API gateway id, a client-supplied `X-Request-Id`, a business id (order id) and the trace id. Rule: the trace id is the join key for observability; keep business ids as span attributes / MDC fields so you can search by either; never overwrite an incoming trace id.
- **Async boundaries** are where context gets lost: thread pools, `@Async`, `CompletableFuture`, Kafka. Use context-propagating executors and put `traceparent` in message headers.
- **Fan-out / batch:** a consumer processing one batch of many messages has many parent traces; use span *links* rather than picking one parent.

**Spring Boot 3+** uses Micrometer Tracing (with a Brave or OpenTelemetry bridge). It propagates `traceparent` through `RestClient`, `WebClient`, and Kafka templates created as beans, and puts `traceId`/`spanId` into the logging MDC.

**Debugging on pods**
1. Find the trace for a failing request (from the error response's trace id, or search logs).
2. See which span errored or was slow.
3. `kubectl get pods`, `kubectl describe pod` (restarts, OOMKilled, failing probes), `kubectl logs <pod> --previous` for the crashed container, `kubectl exec` for live checks, `kubectl port-forward` to reach Actuator endpoints.
4. Check metrics around the time (latency, error rate, pool saturation) and recent deployments.

**Cloud authentication**
- *User to app*: OIDC login with the identity provider (e.g. Microsoft Entra ID), authorization code flow with PKCE; the app receives an ID token and an access token (JWT).
- *Service to service*: client credentials flow, or a **managed identity** / workload identity where the platform gives the pod a token with no secret in config.
- *Validation*: each API checks the JWT signature against the provider's public keys (JWKS), plus `iss`, `aud`, expiry and scopes/roles.

## Example

```yaml
# application.yaml (logging.pattern.correlation needs Spring Boot 3.2+)
management:
  tracing:
    sampling:
      probability: 0.1
logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId:-},%X{spanId:-}] "
```

```java
// keep a business id searchable alongside the trace
try (var ignored = MDC.putCloseable("orderId", order.id())) {
    log.info("Reserving stock");
    inventoryClient.reserve(order);   // traceparent header added automatically
}
```

## Pitfalls and follow-ups

- **Logs show different ids per service?** Something on the path is not propagating (custom HTTP client, raw Kafka producer, new thread). Fix that hop.
- **Sampling:** 100% sampling is expensive; sample a fraction but always keep error traces if your backend supports tail sampling.
- **Clock skew** makes span timelines look odd across nodes; trust parent/child structure over timestamps.
- **Token expiry mid-request chain?** Use short-lived tokens with refresh; downstream calls can use on-behalf-of exchange to keep user identity.

Go deeper: [D8 · Production readiness](../academy/lessons/D8.md), [D7 · Spring Security](../academy/lessons/D7.md), [S8 · Microservices](../academy/lessons/S8.md).
