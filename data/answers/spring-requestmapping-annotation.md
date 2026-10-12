**Short answer:** `@RequestMapping` maps HTTP requests to controller classes and methods in Spring MVC. It matches on **path**, **HTTP method**, **params**, **headers**, **consumes** (request `Content-Type`) and **produces** (`Accept`). Put it on the class for a shared prefix and on methods for the rest. In practice you use its shortcuts, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping` and `@DeleteMapping`, which are `@RequestMapping` with the method fixed. At startup Spring registers every mapping in `RequestMappingHandlerMapping`; at request time the `DispatcherServlet` picks the best match.

## Explanation

**Attributes**

| Attribute | Matches on | Example |
|---|---|---|
| `path` / `value` | URL path, with `{variables}` and patterns | `"/bets/{id}"` |
| `method` | HTTP verb | `RequestMethod.GET` |
| `params` | Query/form parameter present, absent or equal | `"type=live"`, `"!debug"` |
| `headers` | Request header | `"X-Api-Version=2"` |
| `consumes` | Request `Content-Type` | `MediaType.APPLICATION_JSON_VALUE` |
| `produces` | Response type, matched against `Accept` | `"application/json"` |

A mapping without `method` matches **every** HTTP method, which is why the shortcut annotations are preferred.

**Class level + method level.** Class-level values are combined with method-level ones: paths are concatenated, and method-level `consumes`/`produces` override the class's.

**How a request finds its method**
1. `DispatcherServlet` receives the request.
2. `RequestMappingHandlerMapping` (which indexed all `@RequestMapping` methods at startup) finds the candidates and chooses the **most specific** match: fewer variables and wildcards win, then more specific conditions.
3. `RequestMappingHandlerAdapter` resolves the method's arguments (`@PathVariable`, `@RequestParam`, `@RequestBody`, `@RequestHeader`, validated with `@Valid`), invokes the method, and writes the return value with an `HttpMessageConverter` (Jackson for JSON) because `@RestController` implies `@ResponseBody`.

**Errors you get for free:** no path match → `404`; path matches but the method doesn't → `405 Method Not Allowed`; `consumes` mismatch → `415`; `produces` mismatch → `406`.

**Path matching in Spring Boot 3:** `PathPatternParser` is the default. **Trailing-slash matching is off** since Spring Framework 6: `/bets/` no longer matches `/bets`. Suffix pattern matching (`/bets.json`) is gone too.

**API versioning (Spring Framework 7 / Boot 4):** `@RequestMapping` and its shortcuts gain a `version` attribute (for example `@GetMapping(path = "/bets", version = "2")`), with the version resolved from a header, parameter or path segment that you configure.

## Example

```java
@RestController
@RequestMapping(path = "/api/bets", produces = MediaType.APPLICATION_JSON_VALUE)
class BetController {

    private final BetService service;
    BetController(BetService service) { this.service = service; }

    @GetMapping("/{id}")                                       // GET /api/bets/42
    BetDto get(@PathVariable long id) {
        return service.find(id);
    }

    @GetMapping                                                // GET /api/bets?status=OPEN&page=0
    List<BetDto> list(@RequestParam(defaultValue = "OPEN") String status,
                      @RequestParam(defaultValue = "0") int page) {
        return service.list(status, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) // POST with a JSON body
    ResponseEntity<BetDto> place(@Valid @RequestBody PlaceBetRequest req) {
        BetDto bet = service.place(req);
        return ResponseEntity.created(URI.create("/api/bets/" + bet.id())).body(bet);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@PathVariable long id) {
        service.cancel(id);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, headers = "X-Api-Version=2")
    BetDetailsDto getV2(@PathVariable long id) {               // the long form, header-based selection
        return service.details(id);
    }
}
```

## Pitfalls and follow-ups

- **`@RequestMapping` vs `@GetMapping`:** same mechanism; `@GetMapping` is a composed annotation meta-annotated with `@RequestMapping(method = GET)`. Shortcuts are method-level only; use `@RequestMapping` on the class.
- **`@Controller` vs `@RestController`:** `@RestController` = `@Controller` + `@ResponseBody`, so return values are serialized to the body instead of resolved as view names.
- **Ambiguous mappings** (two methods with the same conditions) fail at **startup** with `IllegalStateException`, which is good.
- **`@PathVariable` vs `@RequestParam`:** identifies the resource (`/bets/42`) vs filters or options (`?status=OPEN`).
- **Parameter names:** binding by name needs the `-parameters` compiler flag; Spring Boot's build plugins set it. Without it, name them explicitly: `@PathVariable("id")`.
- **Where do exceptions go?** `@ExceptionHandler` in a `@RestControllerAdvice`; Spring Boot 3 can return RFC 9457 `ProblemDetail` bodies.
- **Reactive stack:** the same annotations work in Spring WebFlux; only the return types change (`Mono`, `Flux`).

Related: [D5 · Spring MVC: the request pipeline](../academy/lessons/D5.md), [L6 · HTTP request lifecycle in Spring MVC](../academy/lessons/L6.md), [D11 · Spring Boot annotations, grouped by job](../academy/lessons/D11.md).
