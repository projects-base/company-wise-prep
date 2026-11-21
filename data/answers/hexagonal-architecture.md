**Short answer:** Hexagonal architecture, also called ports and adapters, puts the business logic in the centre and makes it independent of frameworks, databases and messaging. The core defines **ports**, interfaces describing what it offers (driving or inbound ports, such as `PlaceOrder`) and what it needs (driven or outbound ports, such as `OrderRepository`). **Adapters** on the outside implement or call those ports: a REST controller, a Kafka listener, a JPA repository. Dependencies always point inwards, so you can test the core without Spring and swap infrastructure without touching business rules.

## Explanation

- **The problem it solves.** In a classic layered app, the service layer imports JPA entities, Spring annotations and Kafka templates, so business rules get tangled with infrastructure. Changing the database or testing a rule then means dragging the framework along.
- **Inbound (driving) side.** Something outside starts an action: an HTTP request, a message, a scheduled job. An inbound adapter (a controller, a listener) translates it and calls an inbound port, a use case interface implemented by the core.
- **Outbound (driven) side.** The core needs something from outside: to save an order, publish an event, call a payment provider. It declares an outbound port interface in its own terms; an adapter implements it with JPA, Kafka or an HTTP client.
- **The dependency rule.** Adapters depend on the core, never the reverse. This is the Dependency Inversion Principle applied at the architecture level: the core owns the interfaces.
- **"Hexagon".** The shape just means "many sides, no top or bottom": the UI and the database are both just adapters on the edge.

```text
 REST controller ─┐                         ┌─ JPA adapter      → PostgreSQL
 Kafka listener  ─┼─► [inbound port] CORE [outbound port] ─┼─ Kafka publisher  → topic
 Scheduler       ─┘     PlaceOrder   domain  OrderRepository└─ Payment client   → provider
```

## Example

```java
// core: plain Java, no Spring or JPA imports
public interface PlaceOrder { OrderId place(PlaceOrderCommand cmd); }          // inbound port
public interface OrderRepository { void save(Order order); }                   // outbound port
public interface PaymentGateway { PaymentResult charge(CustomerId c, Money m); } // outbound port

public class PlaceOrderService implements PlaceOrder {
    private final OrderRepository orders;
    private final PaymentGateway payments;
    public PlaceOrderService(OrderRepository orders, PaymentGateway payments) {
        this.orders = orders;
        this.payments = payments;
    }
    public OrderId place(PlaceOrderCommand cmd) {
        Order order = Order.create(cmd.customer(), cmd.lines());   // business rules live in the domain
        payments.charge(cmd.customer(), order.total());
        orders.save(order);
        return order.id();
    }
}

// adapters: Spring lives here
@RestController
class OrderController {
    private final PlaceOrder placeOrder;
    OrderController(PlaceOrder placeOrder) { this.placeOrder = placeOrder; }
    @PostMapping("/orders") OrderId place(@RequestBody PlaceOrderRequest r) { return placeOrder.place(r.toCommand()); }
}

@Component
class JpaOrderRepository implements OrderRepository {
    private final OrderJpaRepository jpa;                        // Spring Data interface over OrderEntity
    JpaOrderRepository(OrderJpaRepository jpa) { this.jpa = jpa; }
    public void save(Order order) { jpa.save(OrderEntity.from(order)); }
}
```

A unit test builds `new PlaceOrderService(inMemoryRepo, fakeGateway)` with no Spring context and no database.

## Pitfalls and follow-ups

- **Versus layered architecture:** layers stack top to bottom and the service usually depends on persistence. Hexagonal flips the persistence dependency, so the core depends on nothing outside itself.
- **Where do JPA entities go?** Strictly, in the persistence adapter, with a mapping to domain objects. Pragmatic teams sometimes annotate domain classes with JPA to avoid the mapping; say it is a trade-off, not a rule.
- **Testing:** core tests are fast unit tests with fakes; adapters get focused integration tests (`@DataJpaTest`, `@WebMvcTest`, Testcontainers).
- **When it is not worth it:** simple CRUD services. Interfaces with one implementation and mappers that only copy fields add cost without benefit. Use it where the business rules are rich or the infrastructure is likely to change.
- **Related names:** Clean Architecture and Onion Architecture share the same core idea (dependencies point inwards). It pairs naturally with DDD: aggregates live in the core, and each bounded context can be its own hexagon.
- **Package layout in Spring Boot:** `order/domain`, `order/application` (ports and use cases), `order/adapter/in/web`, `order/adapter/out/persistence`. Tools such as ArchUnit can enforce the dependency rule in a test.

Further reading: [LLD-DESIGN-7 · DDD and hexagonal architecture](../academy/lessons/E7.md), [LLD-DESIGN-1 · SOLID](../academy/lessons/E1.md).
