**Short answer:** A servlet is a Java class that a servlet container such as Tomcat calls to handle HTTP requests. The container calls `init()` once, `service()` for every request on a worker thread, and `destroy()` once at shutdown. Spring MVC registers a single servlet, `DispatcherServlet`, as a front controller that routes every request to the right `@Controller` method.

## Watch it

Press **Play**, or step with **→**.

```anim
{"title":"A request through Spring MVC","stages":["Tomcat worker thread","Filter chain","DispatcherServlet","HandlerMapping","Interceptor preHandle","HandlerAdapter: bind arguments","@RestController method","HttpMessageConverter → JSON","Interceptor afterCompletion","Response sent"],"frames":[
{"stage":0,"note":"Tomcat accepts the connection and hands the request to a worker thread from its pool (default max 200).","vars":{"request":"GET /bets/42","thread":"http-nio-8090-exec-3"}},
{"stage":1,"note":"Servlet filters run first, for every request: Spring Security (JWT check), CORS, logging with a correlation id.","vars":{"request":"GET /bets/42","user":"from JWT"}},
{"stage":2,"note":"The single front-controller servlet receives it. Spring Boot registered it at \"/\".","vars":{"request":"GET /bets/42"}},
{"stage":3,"note":"Which method handles GET /bets/{id}? RequestMappingHandlerMapping finds BetController.get(Long id).","vars":{"handler":"BetController.get"}},
{"stage":4,"note":"HandlerInterceptors run around the chosen handler; preHandle can stop the request (e.g. a feature flag).","vars":{"handler":"BetController.get"}},
{"stage":5,"note":"Argument resolvers bind @PathVariable id = 42 (and a @RequestBody via Jackson for POSTs); @Valid runs here.","vars":{"id":42}},
{"stage":6,"note":"Your code runs, usually calling a service bean (through its proxy if it is @Transactional).","vars":{"id":42,"returns":"BetDto"}},
{"stage":7,"note":"The return value is written as JSON by MappingJackson2HttpMessageConverter. Exceptions go to @ControllerAdvice instead.","vars":{"status":200,"body":"{\"id\":42,...}"}},
{"stage":8,"note":"afterCompletion runs even on errors: a good place for timing and cleanup.","vars":{"status":200}},
{"stage":9,"note":"The response goes back and the thread returns to the pool. A slow downstream call holds this thread the whole time: that is why timeouts matter.","vars":{"status":200,"thread":"back in pool"}}
]}
```

## Explanation

**Lifecycle.** The container loads the class, creates **one instance**, calls `init(ServletConfig)`, then for each request takes a thread from its pool and calls `service()`, which dispatches to `doGet`, `doPost` and so on. On shutdown it calls `destroy()`. Because one instance serves many threads at once, a servlet must not keep request state in fields.

**Filters** wrap servlets: `doFilter(req, res, chain)` runs before and after the servlet, for every request (authentication, CORS, logging, compression). Spring Security is a chain of filters.

**Spring MVC on top.** Spring Boot starts embedded Tomcat and registers `DispatcherServlet` at `/`. Per request:

1. Filters run.
2. `DispatcherServlet` asks the `HandlerMapping`s which handler matches (`@GetMapping("/bets/{id}")`).
3. `HandlerInterceptor.preHandle` runs.
4. A `HandlerAdapter` resolves arguments (`@PathVariable`, `@RequestBody` via `HttpMessageConverter`s) and invokes the method.
5. The return value is written as JSON by a message converter, or a `ViewResolver` renders a view.
6. `postHandle` and `afterCompletion` run; exceptions go to `@ControllerAdvice` handlers.

## Example

```java
// A raw servlet (rare in Boot apps, but this is what DispatcherServlet is)
@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    private int hits;                                        // shared by all threads: a race

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/plain");
        res.getWriter().write("UP");
    }
}

// The Spring MVC equivalent: DispatcherServlet routes here
@RestController
class HealthController {
    @GetMapping("/health") String health() { return "UP"; }
}
```

## Pitfalls and follow-ups

- **Filter vs HandlerInterceptor?** A filter is servlet-level, runs for every request before `DispatcherServlet`, and knows nothing about controllers. An interceptor is Spring MVC, runs around the chosen handler and can see which method it is. Use filters for cross-cutting HTTP concerns (security, CORS), interceptors for handler-aware logic.
- **Is a servlet thread-safe?** Only if it has no mutable fields. The same applies to `@Controller` beans, which are singletons: keep request data in local variables.
- **How many threads does Tomcat use?** Default max 200 (`server.tomcat.threads.max`). When all are busy, requests queue up to `accept-count`, then connections are refused. Slow downstream calls exhaust the pool, so set timeouts. With Java 21, `spring.threads.virtual.enabled=true` runs requests on virtual threads.
- **What does `load-on-startup` do?** Initialises the servlet at container start instead of on the first request.
- **Servlet vs reactive (WebFlux)?** Servlet is thread-per-request and blocking; WebFlux runs on a few event-loop threads with non-blocking I/O, and does not use `DispatcherServlet`.

Deeper: [S2 · Servlets & JSP: a thread per request](../academy/lessons/S2.md), [L6 · HTTP request lifecycle in Spring MVC](../academy/lessons/L6.md), [D5 · Spring MVC: the request pipeline](../academy/lessons/D5.md).
