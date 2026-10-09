**Short answer:** `persist` (JPA) makes a new entity managed and returns nothing; `save` (old Hibernate API) did the same but returned the generated id. `update` (Hibernate) reattached the detached object itself, while `merge` (JPA) copies the detached object's state onto a managed instance and returns that managed copy. In modern code use the JPA methods: `persist` and `merge`, or Spring Data's `save`, which calls one or the other. Hibernate's `save`/`update`/`saveOrUpdate` have been deprecated since Hibernate 6.0 in favour of the JPA methods.

## Explanation

**Entity states:**

- **Transient:** `new Order()`. Not known to any persistence context, no row.
- **Managed (persistent):** attached to an open `EntityManager`/`Session`. Changes are tracked and flushed automatically (dirty checking).
- **Detached:** was managed, but the context closed (transaction ended, entity sent out of the service). Changes are no longer tracked.
- **Removed:** scheduled for delete on flush.

| Method | API | Input | Returns | Notes |
|---|---|---|---|---|
| `persist(e)` | JPA | Transient | `void` | `e` itself becomes managed. Throws if `e` is detached. The insert may wait until flush. |
| `save(e)` | Hibernate (legacy) | Transient | Generated id | Like persist, but returned the id. |
| `merge(e)` | JPA | Detached or transient | Managed copy | Loads (or creates) a managed instance and copies state onto it. **`e` stays detached.** |
| `update(e)` | Hibernate (legacy) | Detached | `void` | Reattached `e` itself. Failed if another instance with the same id was already in the session. |

**Spring Data `repository.save(e)`:** if the entity is new (null id, or a null/zero version), it calls `persist`; otherwise `merge`. It returns the managed instance, so always use the return value.

## Example

```java
@Transactional
public Order create(OrderRequest req) {
    Order o = new Order(req.customerId());   // transient
    em.persist(o);                           // managed; id set at flush (or immediately with IDENTITY)
    o.setStatus(NEW);                        // tracked, no extra call needed
    return o;
}

@Transactional
public Order rename(Order detached) {        // came from the client / an earlier transaction
    Order managed = em.merge(detached);      // SELECT, then copy fields
    managed.setNote("edited");               // tracked
    detached.setNote("ignored");             // NOT tracked
    return managed;
}
```

## Pitfalls and follow-ups

- **"I called `merge` but my later changes were lost"**: you kept modifying the argument, not the returned object.
- **Inside a transaction you don't need to call `save` on a loaded entity.** Dirty checking flushes changes at commit.
- **N+1 queries (follow-up):** loading N orders, then touching `order.getItems()` lazily fires one query per order. Fixes: `JOIN FETCH` in JPQL, `@EntityGraph(attributePaths = "items")` on the repository method, or `@BatchSize`/`hibernate.default_batch_fetch_size` to load collections in batches. Detect it with SQL logging or Hibernate statistics.
- **Lazy vs eager (follow-up):** `@OneToMany` and `@ManyToMany` default to LAZY; `@ManyToOne` and `@OneToOne` default to EAGER. Best practice: make everything LAZY and fetch what each use case needs with a fetch join or entity graph. EAGER can't be turned off per query and often causes extra queries.
- **`LazyInitializationException`:** touching a lazy association after the session closed. Fix by fetching it inside the transaction or returning a DTO, not by enabling Open Session in View.
- **`persist` on a detached entity** throws (`EntityExistsException` or a "detached entity passed to persist" error).
- **`merge` costs a SELECT** if the entity isn't already in the context.

Deeper: [D6 · Data: JPA, Hibernate and @Transactional](../academy/lessons/D6.md), [L7 · JPA entity and transaction lifecycle](../academy/lessons/L7.md), [Q7 · N+1, pagination, batching, pooling](../academy/lessons/Q7.md).
