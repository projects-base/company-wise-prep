**Short answer:** `POST` asks the server to process the body, usually to *create* a new resource in a collection: the server chooses the id, and sending it twice creates two resources. `PUT` *replaces* the resource at a URI the client already knows with the body sent, creating it if allowed. `PUT` is idempotent (sending it once or ten times leaves the same state); `POST` is not. That matters because clients, proxies and retry libraries can safely retry an idempotent request after a timeout.

## Explanation

| | POST | PUT |
|---|---|---|
| Target URI | A collection or a processing endpoint: `POST /orders` | The resource itself: `PUT /orders/42` |
| Who picks the id | The server | The client (it is in the URI) |
| Meaning | "Process this", typically create | "Make the resource at this URI equal to this body" |
| Idempotent | No | Yes |
| Typical success | `201 Created` with a `Location` header | `200 OK` or `204 No Content`; `201 Created` if it created it |
| Partial update | No (that is `PATCH`) | No, it is a full replacement |

**Idempotent** (RFC 9110) means the *effect on the server* of N identical requests is the same as one. It does not mean the response is identical: a second `DELETE` may return `404`. `GET`, `HEAD`, `PUT` and `DELETE` are idempotent; `POST` and `PATCH` are not guaranteed to be.

**Why it matters.** Networks fail after the server has processed a request but before the client sees the response. With `PUT`, the client just retries. With `POST`, a blind retry can create a duplicate order or charge a card twice.

**`PUT` replaces the whole resource.** If you `PUT` a body without the `email` field, the stored resource should end up without an email. For changing a few fields use `PATCH` (JSON Merge Patch or JSON Patch).

## Example

```java
@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderService service;
    OrderController(OrderService service) { this.service = service; }

    @PostMapping                                           // create: server assigns the id
    ResponseEntity<Order> create(@RequestBody NewOrder body,
                                 @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        Order created = service.create(body, key);         // same key -> return the first result
        return ResponseEntity.created(URI.create("/orders/" + created.id())).body(created);
    }

    @PutMapping("/{id}")                                   // full replace at a known URI
    ResponseEntity<Order> replace(@PathVariable long id, @RequestBody OrderBody body) {
        return ResponseEntity.ok(service.replace(id, body));
    }
}
```

## Pitfalls and follow-ups

- **Which is idempotent and why does it matter?** `PUT`. It lets clients, gateways and libraries retry safely after timeouts and network errors. `POST` needs extra work to be retry-safe.
- **How do you make `POST` safe to retry?** An `Idempotency-Key` header: the server stores the key with the result (often with a unique constraint in the database) and returns the stored result for repeats. Payment APIs work this way.
- **Can `PUT` create?** Yes, if the client is allowed to choose the id, e.g. `PUT /users/akhil/settings`.
- **Is `PATCH` idempotent?** Not necessarily: `{"op": "add", "path": "/items/-"}` appends each time. A merge patch that sets fields usually is.
- **Safe vs idempotent:** safe methods (`GET`, `HEAD`, `OPTIONS`) do not change state; every safe method is idempotent, but `PUT` and `DELETE` are idempotent without being safe.
- **Concurrent `PUT`s:** last write wins unless you use optimistic locking with `ETag` and `If-Match` (return `412 Precondition Failed` on mismatch) or a JPA `@Version` field.

Go deeper: [D5 · Spring MVC: the request pipeline](../academy/lessons/D5.md), [L6 · HTTP request lifecycle in Spring MVC](../academy/lessons/L6.md). Reference: [HTTP request methods (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods).
