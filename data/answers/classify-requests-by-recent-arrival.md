**Short answer:** Keep a `HashMap<String, Integer>` from URL to the index where it last arrived. At index `i`, print `error` if the same URL arrived at `prev` and `i - prev < 5`, else `resolved`; then store `i`, for O(n) time.

**Assumptions (the reported wording is short, so say these first):**
- "Arrived less than 5 units ago" means the **same URL** arrived earlier and `i - prev < 5`.
- **Every** arrival counts, including one that was answered `error`. So each retry restarts the 5-unit wait.
- A gap of exactly 5 is `resolved`.

## Approach

- **Brute force:** for each request, look back at the previous 4 entries and compare strings. Fine for 5, but O(n · w) for a window of `w`.
- **Key insight:** only the most recent arrival of a URL matters. If that one is 5 or more units old, every older one is too.
- **Optimal:** one map from URL to last index, one pass, any window size.

Trace for `[a, b, a, c, d, e, f, a]`:

| i | URL | last index before | gap | output |
|---|---|---|---|---|
| 0 | a | none | none | resolved |
| 1 | b | none | none | resolved |
| 2 | a | 0 | 2 | error |
| 3 | c | none | none | resolved |
| 4 | d | none | none | resolved |
| 5 | e | none | none | resolved |
| 6 | f | none | none | resolved |
| 7 | a | 2 | 5 | resolved |

## Solution

```java
import java.util.*;

class Solution {
    private static final int WINDOW = 5;

    public List<String> classify(String[] requests) {
        Map<String, Integer> lastArrival = new HashMap<>();
        List<String> out = new ArrayList<>(requests.length);
        for (int i = 0; i < requests.length; i++) {
            Integer prev = lastArrival.put(requests[i], i);   // old index, or null
            out.add(prev != null && i - prev < WINDOW ? "error" : "resolved");
        }
        return out;
    }
}
```

`Map.put` returns the previous value, so one call both reads the old index and stores the new one.

## Complexity

- **Time:** O(n) expected map operations, plus hashing the URL characters.
- **Space:** O(u) for `u` distinct URLs.

## Edge cases

- Empty array: empty output.
- The same URL at every index: `resolved` once, then `error` forever under this rule.
- Gap of exactly 5: `resolved`. Gap of 4: `error`.
- `prev` can be index 0, so test `prev != null`, not `prev > 0`.
- URLs that differ only in case or a trailing slash are different keys unless you normalise them.

## Variations

- **Only a resolved request starts the wait** (the usual rate limiter). Then an `error` must not update the map:

```java
import java.util.*;

class ResolvedOnly {
    List<String> classify(String[] requests) {
        Map<String, Integer> lastResolved = new HashMap<>();
        List<String> out = new ArrayList<>(requests.length);
        for (int i = 0; i < requests.length; i++) {
            Integer prev = lastResolved.get(requests[i]);
            boolean blocked = prev != null && i - prev < 5;
            if (!blocked) lastResolved.put(requests[i], i);
            out.add(blocked ? "error" : "resolved");
        }
        return out;
    }
}
```

- **The two rules differ** on six `a` requests in a row: index 5 is `error` under the first rule and `resolved` under this one. Ask which one the sample output matches.
- **Endless stream:** evict a URL once its last index is 5 or more behind, using a queue of (index, URL). Memory is then bounded by the window.

Related: [Logger Rate Limiter](logger-rate-limiter.md), [Contains Duplicate II](contains-duplicate-ii.md), [T1 · Hashing, two pointers, sliding window and prefix sums](../academy/lessons/T1.md).
