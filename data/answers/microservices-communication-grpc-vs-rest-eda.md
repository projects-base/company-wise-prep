**Short answer:** A monolith is one deployable; microservices split the system into independently deployable services that own their data, which buys independent scaling and releases at the cost of network calls and distributed failures. Services on different VMs talk over the network either synchronously (REST over HTTP/JSON, or gRPC over HTTP/2 with Protobuf) or asynchronously through a broker like Kafka. I pick REST for public and simple APIs, gRPC for high-volume internal calls with strict contracts, and events when the caller doesn't need an immediate answer.

## Explanation

**Monolith vs microservices**

| | Monolith | Microservices |
|---|---|---|
| Deploy | One unit | Each service separately |
| Scaling | Whole app | Per service |
| Data | One database, ACID transactions | Database per service, eventual consistency |
| Failure | In-process calls | Network: timeouts, retries, partial failure |
| Ops cost | Low | High: discovery, tracing, CI/CD per service |

A well-structured modular monolith is often the right start; split when team size or scaling needs justify it.

**How services on different VMs find and call each other:** a service discovery registry (Eureka, Consul) or Kubernetes DNS turns a name like `order-service` into instance addresses; a load balancer (client-side or a proxy) picks one; the call goes over HTTP/TCP with TLS. Add timeouts, retries and circuit breakers on every remote call.

**REST vs gRPC**

| | REST | gRPC |
|---|---|---|
| Transport | HTTP/1.1 or 2 | HTTP/2 |
| Payload | JSON text | Protobuf binary, smaller and faster to parse |
| Contract | OpenAPI (optional) | `.proto` file is required; client and server stubs are generated |
| Streaming | Not built in (SSE/WebSocket aside) | Unary, server, client and bidirectional streaming |
| Browser | Native | Needs gRPC-Web or a proxy |
| Debugging | curl, readable | Needs tools like grpcurl |

**Why gRPC over REST internally:** lower latency and payload size, multiplexed calls over one connection, strong typed contracts that catch breaking changes at compile time, and streaming. Why not: harder for browsers and third parties, less human-readable.

**Event-driven architecture:** a service publishes an event (a fact that already happened, e.g. `OrderPlaced`) to a broker. Interested services subscribe and react on their own time.

- Benefits: loose coupling (the producer doesn't know consumers), the producer stays up even if consumers are down, easy to add new consumers, buffers traffic spikes, natural audit log.
- Trade-offs: eventual consistency, harder debugging (needs correlation ids and tracing), duplicate delivery (consumers must be idempotent), ordering only within a partition, schema evolution must be managed.

## Example

```text
Sync:   checkout ──REST/gRPC──▶ pricing  (needs the answer now)
Async:  orders ──OrderPlaced──▶ Kafka ──▶ email, invoicing, analytics
```

```java
// Publish an event after the order is saved (better: via an outbox table)
kafkaTemplate.send("orders.placed", order.id().toString(), new OrderPlaced(order.id(), order.total()));
```

## Pitfalls and follow-ups

- **Command vs event:** a command asks ("ReserveStock") and has one handler; an event states a fact ("OrderPlaced") and can have many.
- **Chained sync calls multiply latency and failure.** Five services at 99.9% each give about 99.5% overall. Prefer async where you can.
- **Dual-write problem:** saving to the DB and publishing to Kafka are two systems. Use the transactional outbox.
- **Idempotent consumers:** store processed event ids, or use upserts, since at-least-once delivery means repeats.
- **Can gRPC go through an API gateway?** Yes if the gateway supports HTTP/2 and gRPC; many teams keep REST at the edge and gRPC inside.

Deeper: [S8 · Microservices: why, how, and what it costs](../academy/lessons/S8.md), [S9 · Cloud native & event-driven systems](../academy/lessons/S9.md), [S10 · The pendulum: modular monoliths](../academy/lessons/S10.md).
