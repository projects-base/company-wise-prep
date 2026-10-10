**Short answer:** Split the items into two ordered sets: `top` holds the k largest, `rest` holds everything else, and keep a running `sum` of `top`. Insert into `top`, and if it grows past k, move its smallest to `rest`. On delete from `top`, promote the largest of `rest`. Updates are delete + insert. With `TreeSet`s ordered by (value, id), every operation is O(log n) and the query is O(1).

## Picture it

Example 1 with `k = 2`. Items are shown as `id:value`.

| step | operation | what moves | top (k largest) | rest | sum |
|---|---|---|---|---|---|
| 1 | upsert(1, 5) | add to top | `1:5` | - | 5 |
| 2 | upsert(2, 3) | add to top | `2:3, 1:5` | - | 8 |
| 3 | upsert(3, 8) | add to top (size 3 > k), push smallest `2:3` down | `1:5, 3:8` | `2:3` | 13 |
| 4 | topKSum() | - | `1:5, 3:8` | `2:3` | **13** |
| 5 | upsert(2, 10) | remove old `2:3` from rest, add `2:10` to top, push smallest `1:5` down | `3:8, 2:10` | `1:5` | 18 |
| 6 | topKSum() | - | `3:8, 2:10` | `1:5` | **18** |
| 7 | remove(3) | remove `3:8` from top, promote best of rest `1:5` | `1:5, 2:10` | - | 15 |
| 8 | topKSum() | - | `1:5, 2:10` | - | **15** |

**The picture in one sentence:** two ordered sets split at the k-th largest value, so each change needs at most one item to cross the line and the sum of `top` is updated in place.

## Approach

- **Brute force.** Sort the values on every query: O(n log n) per query.
- **One heap.** A min-heap of size k gives the k largest for inserts only. It cannot delete arbitrary items, and when a top item leaves you need the next-best item from outside the heap, which a single heap does not keep.
- **Key insight.** Keep two balanced BSTs with the invariant *every value in `top` ≥ every value in `rest`*, and `top.size() == min(k, n)`. Then the answer is the sum of `top`, maintained incrementally. Fixing the invariant after any change needs at most one move between the sets.
- **Ordering.** Values can repeat, so order by `(value, id)` to make every element distinct in the `TreeSet`. A `HashMap<id, value>` provides the value for the comparator.
- **Trap.** Never change an item's value while it sits in a `TreeSet`; the set would be corrupted. `upsert` therefore removes the old entry first.

## Solution

```java
import java.util.*;

class TopKSum {
    private final int k;
    private final Map<Integer, Integer> value = new HashMap<>();
    // Items ordered by (value, id). `top` holds the k largest, `rest` everything else.
    private final TreeSet<Integer> top, rest;
    private long sum;

    public TopKSum(int k) {
        this.k = k;
        Comparator<Integer> cmp = (a, b) -> {
            int va = value.get(a), vb = value.get(b);
            return va != vb ? Integer.compare(va, vb) : Integer.compare(a, b);
        };
        top = new TreeSet<>(cmp);
        rest = new TreeSet<>(cmp);
    }

    public void upsert(int id, int v) {
        remove(id);                    // value must not change while in a set
        value.put(id, v);
        top.add(id);
        sum += v;
        if (top.size() > k) {          // push the smallest of top down
            int low = top.pollFirst();
            sum -= value.get(low);
            rest.add(low);
        }
    }

    public void remove(int id) {
        if (!value.containsKey(id)) return;
        if (top.remove(id)) {
            sum -= value.get(id);
            if (!rest.isEmpty()) {     // promote the best of the rest
                int up = rest.pollLast();
                top.add(up);
                sum += value.get(up);
            }
        } else {
            rest.remove(id);
        }
        value.remove(id);
    }

    public long topKSum() {
        return sum;
    }
}
```

Why the invariant survives an insert: if the new value is smaller than some value in `rest`, `top` was already full of larger values, so the new item is the minimum of `top` and is immediately pushed down to `rest`.

## Complexity

- **upsert / remove:** O(log n), a constant number of `TreeSet` operations and hash lookups.
- **topKSum:** O(1).
- **Space:** O(n).

## Edge cases

- Fewer than k items: `rest` is empty and `top` holds everything.
- Removing a missing id: no-op.
- Upsert with the same value: remove and re-add, still correct.
- Negative values: sum can go negative; `long` avoids overflow (10⁵ × 10⁹).

## Variations

- **Median or any order statistic of a stream with deletes:** the same two-set split, with the size target being n/2.
- **Lazy deletion with two heaps:** `PriorityQueue` plus a "deleted" count map; amortised O(log n), but more fragile to write.
- **k changes at runtime:** move items one by one between sets until `top.size() == k`.
- **Values from a small range:** a Fenwick tree over compressed values gives the sum of the k largest by a binary-lifting search.

Practise it in the app: Run / Submit on this page.
