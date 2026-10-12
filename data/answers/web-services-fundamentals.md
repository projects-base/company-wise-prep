**Short answer:** A web service is an API that one program calls over HTTP. The two classic styles are **SOAP**, a protocol with XML envelopes and a formal WSDL contract, common in older enterprise and banking integrations, and **REST**, an architectural style where resources have URIs and are manipulated with standard HTTP methods, usually with JSON. Today most new services are REST (or gRPC internally). Designing one well means clear resources, correct methods and status codes, versioning, validation, security and documentation; consuming one well means timeouts, retries and handling failures.

## Explanation

**REST vs SOAP:**

| | REST | SOAP |
|---|---|---|
| What it is | Architectural style | Protocol (W3C standard) |
| Payload | Usually JSON, any format | XML envelope only |
| Contract | OpenAPI (optional) | WSDL (required in practice) |
| Transport | HTTP | Usually HTTP, can be others (JMS, SMTP) |
| Operations | HTTP methods on resources | Named operations, all usually via `POST` |
| Extras | Relies on HTTP: caching, status codes | WS-Security, WS-ReliableMessaging, WS-AtomicTransaction |
| Java | Spring MVC / WebFlux, JAX-RS | JAX-WS, Spring Web Services, Apache CXF |

**REST constraints** (Fielding): client-server, stateless (each request carries what it needs, e.g. a JWT), cacheable, uniform interface (resources, representations, HTTP methods), layered system. Most "REST" APIs are really "HTTP + JSON" and do not use hypermedia (HATEOAS); say so if asked.

**Designing a REST API, the checklist:**
- Nouns for resources, plural: `/customers/42/orders`. Methods give the verb.
- Correct status codes: `200`, `201` + `Location`, `204`, `400` validation, `401` vs `403`, `404`, `409` conflict, `422`, `429` rate limited, `5xx` server faults.
- A consistent error body (Spring's `ProblemDetail`, RFC 9457).
- Pagination (`?page=&size=` or cursor-based), filtering and sorting.
- Versioning: in the path (`/v1/`) or a header; never break existing clients.
- Idempotency for retries; validation with Bean Validation (`@Valid`).
- Security: HTTPS, OAuth2/JWT, input validation, rate limiting.
- Documentation: OpenAPI with springdoc.

**Consuming a service:** set connect and read timeouts, retry only idempotent calls with backoff, add a circuit breaker (Resilience4j), and map remote errors to your own exceptions.

## Example

Building and consuming a REST endpoint in Spring Boot 3.2+:

```java
@RestController
@RequestMapping("/v1/customers")
class CustomerController {
    private final CustomerService service;
    CustomerController(CustomerService service) { this.service = service; }

    @GetMapping("/{id}")
    CustomerDto get(@PathVariable long id) {
        return service.find(id).orElseThrow(() ->
            new ErrorResponseException(HttpStatus.NOT_FOUND));   // rendered as ProblemDetail
    }

    @PostMapping
    ResponseEntity<CustomerDto> create(@Valid @RequestBody NewCustomer body) {
        CustomerDto c = service.create(body);
        return ResponseEntity.created(URI.create("/v1/customers/" + c.id())).body(c);
    }
}

// Consuming another service with RestClient (Spring Framework 6.1+)
RestClient client = RestClient.builder().baseUrl("https://payments.internal").build();
PaymentDto p = client.get().uri("/v1/payments/{id}", paymentId)
        .retrieve()
        .body(PaymentDto.class);
```

## Pitfalls and follow-ups

- **When would you still pick SOAP?** Integrating with a partner or legacy system that only offers WSDL, or when WS-Security message-level signing is required.
- **REST vs gRPC?** gRPC uses Protobuf over HTTP/2: smaller, faster, strongly typed, streaming; good between internal services, less convenient for browsers.
- **Stateless but users log in?** The state lives in a token (JWT) or a shared session store, not in one server's memory, so any instance can serve the request.
- **`RestTemplate` vs `WebClient` vs `RestClient`?** `RestTemplate` is in maintenance mode; `RestClient` is the modern synchronous client; `WebClient` is reactive.
- **How do you test it?** `@WebMvcTest` with MockMvc for the controller, WireMock for outbound calls, contract tests (Spring Cloud Contract or Pact) between teams.
- **PUT vs POST, idempotency:** see the separate question on POST vs PUT.

Go deeper: [S7B · SOA & the enterprise service bus](../academy/lessons/S7B.md), [D5 · Spring MVC: the request pipeline](../academy/lessons/D5.md), [L6 · HTTP request lifecycle in Spring MVC](../academy/lessons/L6.md). Reference: [Spring Framework REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html).
