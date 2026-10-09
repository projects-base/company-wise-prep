**Short answer:** `Object` is the superclass of every class. For exceptions, `Throwable` is the root, with two branches: `Error` (serious JVM problems like `OutOfMemoryError`, not meant to be caught) and `Exception`. Checked exceptions are `Exception` subclasses other than `RuntimeException`; the compiler forces you to catch or declare them (`IOException`, `SQLException`). Unchecked exceptions are `RuntimeException` and its subclasses (`NullPointerException`, `IllegalArgumentException`) plus `Error`; the compiler doesn't force handling, because they usually signal bugs.

## Explanation

```text
Object
 └─ Throwable
     ├─ Error                      (unchecked)  OutOfMemoryError, StackOverflowError
     └─ Exception                  (checked)    IOException, SQLException, InterruptedException
         └─ RuntimeException       (unchecked)  NullPointerException, IllegalArgumentException,
                                                IllegalStateException, ArithmeticException,
                                                IndexOutOfBoundsException, ClassCastException
```

- **Checked:** recoverable conditions outside the program's control (file missing, network down). Checked at compile time: `catch` it or add `throws`.
- **Unchecked:** programming errors or invalid state. Not checked by the compiler.
- **NullPointerException:** thrown when you use a `null` reference as if it pointed to an object: call a method, read a field, unbox a null `Integer`, take `length` of a null array. Since Java 14, helpful NPE messages name the null part, e.g. `Cannot invoke "String.length()" because "user.name" is null`.

**Modern practice:** Spring and most libraries prefer unchecked exceptions (Spring wraps `SQLException` into `DataAccessException`). Checked exceptions also don't mix well with lambdas, since `java.util.function` interfaces can't throw them.

## Example

```java
// Custom unchecked domain exception
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) { super("Order " + id + " not found"); }
}

// Global REST error handling
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    }
}

// try-with-resources: closes in reverse order, even on exception
try (var in = Files.newBufferedReader(path);
     var out = Files.newBufferedWriter(target)) {
    in.transferTo(out);
} catch (IOException e) {
    throw new UncheckedIOException("Copy failed", e);   // wrap, keep the cause
}
```

## Pitfalls and follow-ups

- **Custom exceptions and `@ControllerAdvice` (follow-up):** extend `RuntimeException` for domain errors, map them in one `@RestControllerAdvice` to HTTP statuses. Spring 6 has `ProblemDetail` (RFC 9457 / 7807 format) built in. Controllers stay free of try/catch.
- **try-with-resources (follow-up):** works with any `AutoCloseable`. If both the body and `close()` throw, the body's exception wins and the close exception is attached as a suppressed exception (`getSuppressed()`).
- **finally vs return (follow-up):** `finally` runs even after a `return` in `try`. If `finally` itself returns, it overrides the `try`'s return value and silently swallows any exception. Never return from `finally`. `finally` doesn't run only if the JVM exits (`System.exit`) or the thread dies.
- **Catch order:** more specific first, otherwise the compiler reports an unreachable catch.
- **`throw` vs `throws`:** `throw` raises an exception; `throws` declares it in the method signature.
- **Don't swallow:** `catch (Exception e) {}` hides bugs. Log with context or rethrow wrapped with the cause.
- **Rollback link:** `@Transactional` rolls back on unchecked exceptions by default, not on checked ones.
- **Overriding rule:** an overriding method can't throw broader checked exceptions than the method it overrides.
