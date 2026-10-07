## Where we are

In S7, Spring Boot made one application cheap to start: auto-configuration, starters and an
embedded server in one runnable jar. But a company is never just one application. This lesson is
about the era (roughly the 2000s) when large companies tried to connect *many* applications to each
other, and what that taught us before microservices arrived.

## The problem

Picture ShopKart ten years in. It is no longer one app. It is:

- the **web shop** (Java),
- an **ERP** bought from a vendor that holds stock and invoices,
- a **CRM** (customer relationship system) bought from another vendor,
- a **warehouse system** written in-house in another language,
- a **payments provider** outside the company.

When an order is placed, all five need to hear about it. The first instinct is to connect each
system directly to each other system it needs:

```text
   Shop ─────── ERP
    │  ╲       ╱ │
    │    ╲   ╱   │
    │     ╳      │
    │   ╱   ╲    │
  CRM ─────── Warehouse
```

With 5 systems there can be up to 10 point-to-point links (n·(n−1)/2). With 20 systems, up to 190.
Each link has its own data format, its own protocol, its own error handling, and usually its own
developer who has since left. Teams call this **spaghetti integration**. Changing one system's
data format can break several others, and nobody has a full map.

That is the pain this era set out to fix.

## The idea: services with contracts (SOA)

**SOA (service-oriented architecture)** means: each system exposes its business functions as
*services*, each with a published *contract* (what you can call, with what data), and other systems
use only that contract, never the system's internals.

A tiny example. Instead of the shop reading the ERP's database tables directly, the ERP publishes:

```text
Service: InventoryService
  operation reserveStock(sku, quantity) -> reservationId
  operation releaseStock(reservationId)
```

The shop now depends on two operations, not on forty tables. The ERP can change its tables freely.

The general rule: **depend on a contract, not on someone else's internals.** That rule survived
every era after this one. Microservices (S8) keep it; they change *where the smarts live*.

## How SOA was usually built: SOAP and WSDL

In practice, most SOA programmes of the 2000s used **web services** built on XML:

- **SOAP**: an XML message format for calling an operation over the network (usually over HTTP).
- **WSDL** (Web Services Description Language): an XML file that describes a service's operations
  and data types. Tools could generate Java client code from it.
- **WS-\* standards**: a family of extra specifications layered on top (security, reliable
  messaging, transactions and more).

A SOAP call is a fairly large XML envelope for a small request. That verbosity and the size of the
WS-\* family are part of why later teams moved to plain JSON over HTTP. In Java, this era gave us
JAX-WS for SOAP services.

## The invention: the enterprise service bus (ESB)

Contracts alone do not solve the spaghetti. Something still has to connect the systems. The
**enterprise service bus (ESB)** is a central piece of middleware that every system plugs into once.
The bus then does the work of moving messages between them:

```text
   Shop     ERP     CRM     Warehouse     Payments
     │       │       │          │             │
  ═══╧═══════╧═══════╧══════════╧═════════════╧═══  ESB
     routing · transformation · protocol bridging · (business rules?)
```

Now each system has one connection (to the bus), not one per partner. A typical ESB offered:

- **Routing**: "an OrderPlaced message goes to ERP, CRM and Warehouse."
- **Transformation**: turn the shop's order XML into the shape the ERP expects.
- **Protocol bridging**: receive over HTTP, deliver over a message queue or FTP file drop.
- **Orchestration**: run a multi-step business process across services.

Products of the period included IBM's WebSphere ESB and Message Broker, Oracle Service Bus,
TIBCO, and the open-source Mule ESB and Apache ServiceMix. Apache Camel (an integration library,
not a whole bus) implements the same patterns and is still widely used.

Analogy: the ESB is a telephone switchboard. Everybody calls the operator, and the operator connects
and translates. **Where it breaks:** a switchboard operator does not decide *business policy*. An ESB
was often allowed to, and that is where the trouble started.

## The new pain: the smart pipe becomes the bottleneck

Here is how it went wrong in many companies. The bus was owned by one central **integration team**.
Every change that crossed two systems needed a change in the bus. Slowly, business logic moved
into the bus, because it was the one place that saw everything:

```text
ESB flow "PlaceOrder":
  if customer.tier == GOLD and order.total > 500: route to FastTrackWarehouse
  if country == "DE": add VAT field, convert date format
  if ERP is down: write to retry table, email ops
```

Now:

- To add a field to an order, the shop team files a ticket with the integration team and waits.
- The rules for "what is a gold customer" live in the bus, not in the CRM that owns customers.
- The bus is a **single point of failure** (one part whose failure stops everything): when it is
  down, every integration is down.
- Testing a flow means standing up the bus, which only the integration team knows how to do.

This is the same shape of pain you saw in S4 (EJB): a heavy central container that does useful
things but slows every team down. The general lesson: **central governance helps consistency, but
a central component that every change must pass through becomes a central bottleneck.**

## The reaction: smart endpoints, dumb pipes

In March 2014, James Lewis and Martin Fowler published the article *Microservices* on
martinfowler.com. One of the characteristics it describes is **"smart endpoints and dumb pipes"**:
put the business logic inside the services that own it, and keep the thing between them simple
(plain HTTP, or a message broker that just delivers messages). It contrasts this directly with
ESB-style integration.

In 2009, analyst Anne Thomas Manes wrote a widely shared blog post titled *SOA is Dead; Long Live
Services*: her point was that the *SOA programme* label had failed, while the idea of services
was still right. Read both originals; they are short.

What survived from SOA:

- services with explicit contracts,
- reuse of business capabilities through APIs,
- messaging between systems.

What was dropped:

- business logic in the middleware,
- a central team owning every integration,
- heavyweight XML standards as the default.

## Lab (~10 min)

**Part 1: Build a mini bus and watch logic creep in (Java 21, no dependencies).**

```java
import java.util.*;
import java.util.function.UnaryOperator;

public class MiniBus {
    record Message(String type, Map<String, String> body) {}

    interface Endpoint { void receive(Message m); }

    private final Map<String, List<String>> routes = new HashMap<>();
    private final Map<String, Endpoint> endpoints = new HashMap<>();
    private final Map<String, UnaryOperator<Message>> transforms = new HashMap<>();

    void register(String name, Endpoint e) { endpoints.put(name, e); }
    void route(String type, String... targets) { routes.put(type, List.of(targets)); }
    void transform(String target, UnaryOperator<Message> t) { transforms.put(target, t); }

    void publish(Message m) {
        for (String target : routes.getOrDefault(m.type(), List.of())) {
            Message out = transforms.getOrDefault(target, UnaryOperator.identity()).apply(m);
            endpoints.get(target).receive(out);
        }
    }

    public static void main(String[] args) {
        MiniBus bus = new MiniBus();
        bus.register("erp", m -> System.out.println("ERP got " + m.body()));
        bus.register("crm", m -> System.out.println("CRM got " + m.body()));
        bus.route("OrderPlaced", "erp", "crm");

        // Transformation: the ERP wants different field names. Fine for a pipe.
        bus.transform("erp", m -> {
            Map<String, String> b = new HashMap<>(m.body());
            b.put("ARTICLE_NO", b.remove("sku"));
            return new Message(m.type(), b);
        });

        bus.publish(new Message("OrderPlaced", Map.of("sku", "B-42", "total", "650", "tier", "GOLD")));
    }
}
```

Run it with `java MiniBus.java`. You should see the ERP receive `ARTICLE_NO` and the CRM receive `sku`.

Now add a business rule *inside the bus*: if `tier` is `GOLD` and `total` is above 500, route
to a new `fastTrackWarehouse` endpoint as well. Notice where you had to write it: in the bus, not
in the shop or the CRM.

**Part 2: Debate (design, on paper).** Draw ShopKart's five systems around a central ESB that
does routing, transformation *and* business rules. Then list what happens when:

1. the CRM team renames "GOLD" to "PLATINUM";
2. the shop needs a new field on orders before a sale next week;
3. the ESB server needs an upgrade over a weekend.

For each, write who has to act and who waits. Then redraw it with "smart endpoints, dumb pipes":
the rule lives in the shop or the CRM, and the pipe only delivers. Write two sentences on what you
gained and what you lost (hint: one central place to see all flows).

## Gotchas

- **Confusing SOA with SOAP.** SOA is the idea (services with contracts); SOAP is one wire format
  used to build it. You can do service orientation with JSON over HTTP.
- **Business rules in the pipe.** Any rule in the integration layer has no clear owner and is hard
  to test. Keep rules in the service that owns the data.
- **A central team as a required hop.** If every cross-system change needs one team, that team sets
  your delivery speed. Give teams ownership of their own integrations.
- **Throwing away the good parts.** Contracts, versioned interfaces and messaging all came from this
  era and are still right. The lesson is "dumb pipes", not "no contracts".
- **Thinking a modern broker can't become an ESB.** Put routing rules, transformations and business
  logic into Kafka stream jobs or an API gateway, and you have rebuilt the smart pipe.

## In interviews

SOA itself is rarely asked by name now. It shows up as integration and communication questions:

- [Integrate a central HR system with many local HR systems](#/q/design-central-hr-system-integration)
  is exactly this era's problem: many systems, different formats, one source of truth.
- [Microservices communication: gRPC vs REST vs events](#/q/microservices-communication-grpc-vs-rest-eda)
  often opens with "why not just put a bus in the middle?"
- [Design a Configuration Service](#/q/design-configuration-service): a central service many
  systems depend on, where the bottleneck question comes up.

Typical follow-ups:

- *"What's the difference between an ESB and Kafka?"* Strong answer: Kafka is a log that stores and
  delivers events; it does not (by itself) transform or apply business rules. The difference is
  where the logic lives. You can still misuse either.
- *"How do you avoid point-to-point spaghetti without a central bus?"* Strong answer: published
  contracts, events for one-to-many notifications, an API gateway for edge concerns only, and clear
  ownership of each integration.

## Explain it in 2 minutes

**Prompt:** "What was SOA, why did ESBs fall out of favour, and what did microservices keep?"

A good answer hits:

1. The pain: point-to-point integration between many enterprise systems.
2. SOA: business functions as services with contracts; usually SOAP/WSDL in practice.
3. ESB: one central bus for routing, transformation and protocol bridging.
4. The new pain: business logic crept into the bus; a central team became a bottleneck and a single
   point of failure.
5. The reaction: "smart endpoints, dumb pipes" (Lewis and Fowler, 2014).
6. What survived: contracts, reuse through APIs, messaging.

## Check yourself

1. How many point-to-point links can 8 systems need, and how many connections with a bus?
2. Name three things an ESB typically did.
3. Why is putting a "gold customer" rule in the ESB a problem?
4. Is SOAP required for SOA?
5. Give one example of turning a modern tool into a "smart pipe".

### Answers

1. Up to 8·7/2 = 28 links; with a bus, 8 connections (one each).
2. Any three of: routing, transformation, protocol bridging, orchestration.
3. The rule belongs to the team that owns customers; in the bus it has no clear owner, is hard to
   test, and every change waits on the integration team.
4. No. SOAP is one implementation; SOA is the idea of services behind contracts.
5. For example, business rules in an API gateway's routing config, or order logic in a stream job
   that sits between services instead of inside one.

## Next

S8, Microservices: keep the contracts, drop the central bus, and give each team its own service.
That solves the bottleneck, then hands you the network as a new problem.
