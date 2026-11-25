**Short answer:** I first define "correct", because search has three kinds: the right *set* of results (filters, permissions, nothing missing), the right *order* (relevance), and *fresh* data. Then I check each in its own way: a brute-force oracle and fixed examples for the set, judged queries and metrics for ranking, and lag checks between the database and the index.

After release I keep checking with canary queries, zero-result rate and click metrics, because a search can return 200 OK and still be wrong.

## Explanation

I assume a product or restaurant search backed by an index such as Elasticsearch, with the database as the source of truth.

**1. Say what correct means**
- *Precision:* of what we returned, how much is right? *Recall:* of what is right, how much did we return?
- Hard rules: never show closed, out-of-area or forbidden items. These are pass or fail.
- Soft quality: better matches first. This is a score, compared with a baseline.
- Agree examples with the product owner: typo, plural, accent, empty query, tie-breaking.

**2. Before release**

| Layer | Check | Catches |
|---|---|---|
| Unit | query builder, tokeniser, filters | wrong operator, missing filter |
| Oracle | engine vs brute-force scan on random data | edge cases nobody thought of |
| Integration | the real engine in Testcontainers | mapping and analyser mistakes |
| Golden set | fixed data, query → expected ids | regressions |
| Relevance | judged queries, precision@k, NDCG | worse ranking |
| Security | the same query as two users | leaking other people's data |

**3. Properties that always hold** (no expected answer needed)
- Adding a filter never adds results.
- Every returned item satisfies every filter.
- Pages joined together equal the whole list: no duplicates, no gaps.
- The same query on the same data gives the same order, so a unique tie-breaker (such as the id) is needed.

**4. Index vs source of truth**
- Count per tenant or city in both; compare checksums by id range and `updated_at`.
- Measure indexing lag: write a test item, time until it is searchable.

**5. In production**
- *Shadow:* send a copy of real queries to the new version and compare the top 10 with the old one. Review the biggest differences.
- *Canary and A/B:* click-through, conversion, reformulated queries.
- *Alerts:* zero-result rate, lag, p99, timed-out shards.
- *Synthetic queries:* every minute, "the known item must be in the top 3".

## Example

An oracle test: the simple version decides what the fast version must return.

```java
import java.util.*;
import java.util.function.BiFunction;

public final class SearchOracleCheck {
    record Dish(long id, String name, int pricePence) {}
    record Query(String text, int maxPrice) {}

    /** The oracle: slow, obvious, easy to review. */
    static Set<Long> bruteForce(List<Dish> all, Query q) {
        Set<Long> ids = new TreeSet<>();
        for (Dish d : all) {
            boolean nameMatches = d.name().toLowerCase(Locale.ROOT).contains(q.text().toLowerCase(Locale.ROOT));
            if (nameMatches && d.pricePence() <= q.maxPrice()) ids.add(d.id());
        }
        return ids;
    }

    /** Compares any search engine with the oracle on random data. */
    static void check(BiFunction<List<Dish>, Query, List<Long>> engine, long seed) {
        Random random = new Random(seed);                         // fixed seed: a failure can be replayed
        String[] names = {"Pad Thai", "Thai curry", "Pizza", "PIZZA bianca", ""};
        for (int run = 0; run < 1_000; run++) {
            List<Dish> data = new ArrayList<>();
            int rows = random.nextInt(20);                        // zero rows is a case too
            for (int id = 0; id < rows; id++) {
                data.add(new Dish(id, names[random.nextInt(names.length)], random.nextInt(2_000)));
            }
            Query q = new Query(random.nextBoolean() ? "thai" : "pizza", random.nextInt(2_000));
            List<Long> actual = engine.apply(data, q);
            if (new HashSet<>(actual).size() != actual.size()) throw new AssertionError("duplicates, seed " + seed);
            if (!new TreeSet<>(actual).equals(bruteForce(data, q))) {
                throw new AssertionError("seed " + seed + " run " + run + " " + q + " got " + actual);
            }
        }
    }

    /** Ranking quality: the share of the top k results that people judged relevant. */
    static double precisionAtK(List<Long> ranked, Set<Long> relevant, int k) {
        return ranked.stream().limit(k).filter(relevant::contains).count() / (double) k;
    }
}
```

An engine that forgets to ignore case fails this check at once; one that handles it passes all 1,000 runs. `precisionAtK([1,2,3,4,5], {1,3}, 5)` is 0.4.

## Pitfalls and follow-ups

- **Testing with mocks only?** A mocked engine returns what you told it to. Analyser and mapping bugs need the real engine.
- **Exact score assertions?** Brittle: scores move when data changes. Assert the set, the order of clear cases, or a metric threshold.
- **Flaky right after a write?** Elasticsearch is near-real-time (refresh is 1 s by default). In tests, index with `refresh=wait_for`.
- **Who decides relevance?** Human judgements for a few hundred frequent and difficult queries, plus click data, which is biased towards the top position.
- **What is NDCG?** Graded relevance, discounted by position, divided by the best possible order. 1.0 is perfect.
- **Partial failure?** A shard timeout can return 200 with part of the results. Check `timed_out` and `_shards.failed` in the response, and show or log it.
- **A user reports a wrong result?** Reproduce it, read the engine's explain output, compare index and database, then add it to the golden set.

Deeper: [D12 · Testing Spring Boot: JUnit 5, Mockito, test slices, Testcontainers](../academy/lessons/D12.md).
