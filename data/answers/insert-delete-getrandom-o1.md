**Short answer:** Keep the values in an `ArrayList` (so a random index gives a uniform pick) and a `HashMap` from value to its index (so lookups are O(1)). Insert appends and records the index. Remove swaps the value with the last element, updates that element's index, and pops the end. Removing from the end of an array list is O(1), which is the whole trick.

## Picture it

Insert 10, 20, 30, 40, then remove 20, remove 30, insert 20.

| Operation | values (list) | index (map) | Note |
|---|---|---|---|
| insert 10, 20, 30, 40 | [10, 20, 30, 40] | 10:0, 20:1, 30:2, 40:3 | append, record position |
| remove 20 | [10, **40**, 30] | 10:0, 40:1, 30:2 | hole at 1; tail 40 moves into it |
| remove 30 | [10, 40] | 10:0, 40:1 | 30 is the tail itself, just pop |
| insert 20 | [10, 40, 20] | 10:0, 40:1, 20:2 | append at the end |
| getRandom | [10, 40, 20] | — | `nextInt(3)` picks each with probability 1/3 |

```text
remove 20:   [10, 20, 30, 40]  ->  [10, 40, 30]
                  ^ hole   ^ tail moves into the hole, list shrinks by one
```

**The picture in one sentence:** fill the hole with the last element so the list never has gaps, and keep a map so you always know where the hole is.

## Approach

- **Brute force:** a `HashSet` handles insert and remove in O(1), but `getRandom` needs to iterate to a random position: O(n). A plain list gives O(1) random but O(n) remove.
- **Key insight:** combine them. The list must stay dense (no holes) for uniform sampling. To delete from the middle without shifting, move the last element into the hole and shrink by one. The map tells you where the hole is.
- **Optimal:** list + map, with swap-with-last delete.

## Solution

```java
import java.util.*;

class RandomizedSet {
    private final List<Integer> values = new ArrayList<>();
    private final Map<Integer, Integer> index = new HashMap<>();
    private final Random random = new Random();

    public boolean insert(int val) {
        if (index.containsKey(val)) return false;
        index.put(val, values.size());
        values.add(val);
        return true;
    }

    public boolean remove(int val) {
        Integer i = index.remove(val);
        if (i == null) return false;
        int last = values.remove(values.size() - 1);  // O(1): removes the tail
        if (last != val) {                            // fill the hole with the tail
            values.set(i, last);
            index.put(last, i);
        }
        return true;
    }

    public int getRandom() {
        return values.get(random.nextInt(values.size()));
    }
}
```

## Complexity

- **Time:** O(1) average for all three. Hash operations are O(1) on average; `ArrayList.add` is amortised O(1); removing the last element is O(1).
- **Space:** O(n): each value stored once in the list and once in the map.

## Edge cases

- Removing the last element itself: `last == val`, so skip the swap (otherwise you would re-insert it into the map).
- Insert of a duplicate and remove of a missing value return `false`.
- `values.remove(int)` vs `remove(Object)`: pass an `int` index here. With `List<Integer>`, `remove(val)` on a boxed value would remove by value in O(n). Easy bug to make.
- Negative and extreme values work because they are only map keys.

## Follow-up: make it thread-safe, and what it costs

- **Simplest:** mark all three methods `synchronized` (or use one `ReentrantLock`). The list and map must change together, so they need one lock; two separate concurrent collections would not keep them consistent.
- **Cost:** every call is serialised, so throughput does not scale with cores, and `getRandom` (a read) blocks behind writes. A `ReentrantReadWriteLock` lets concurrent `getRandom` calls share the read lock, but `Random` itself must then be thread-safe: use `ThreadLocalRandom.current()`.
- **Beyond that:** striping by hash does not work well because the dense list is one shared structure. For heavy write load, accept a weaker contract (for example, sharded sets and pick a shard weighted by size).

See [B2 · Locks](../academy/lessons/B2.md) and [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md).

## Variations

- **Duplicates allowed (LeetCode 381):** map value → set of indexes; same swap-with-last idea.
- **Weighted random:** prefix sums plus binary search, O(log n) per pick.

Practise it in the app: Run / Submit on this page.
