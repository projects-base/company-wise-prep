**Short answer:** Store the tracked set as sorted, disjoint, half-open intervals in a `TreeMap<start, end>`. `addRange` merges with every interval it overlaps or touches. `removeRange` trims the interval that straddles `left` and deletes or trims the ones starting inside `[left, right)`. `queryRange` is one `floorEntry` lookup: the interval starting at or before `left` must reach `right`. Queries are O(log n); updates are O(log n) amortised, since each interval is created once and removed once.

## Picture it

Example 2. The TreeMap holds start → end of disjoint, non-touching intervals:

| Step | Call | What happens | map after | Returns |
|---|---|---|---|---|
| 1 | addRange(1,5) | nothing before or inside; put | {1:5} | — |
| 2 | addRange(5,9) | floor(5) = [1,5) touches 5 → left = 1; absorb [1,5) → right = 9 | {1:9} | — |
| 3 | queryRange(2,8) | floor(2) = [1,9), 9 ≥ 8 | {1:9} | true |
| 4 | removeRange(3,4) | lower(3) = [1,9) straddles 3 → cut to [1,3); 9 > 4 → re-add tail [4,9) | {1:3, 4:9} | — |
| 5 | queryRange(1,3) | floor(1) = [1,3), 3 ≥ 3 | {1:3, 4:9} | true |

```text
after step 2:   1 ===================== 9
after step 4:   1 ===== 3     4 ======= 9
```

**The picture in one sentence:** keep the covered set as sorted, disjoint, non-touching intervals, so a query is one `floorEntry` lookup and add/remove only touch the intervals at the edges.

## Approach

- **Brute force.** A boolean per number. Coordinates go up to 10⁹, so that is impossible.
- **List of intervals.** A sorted `ArrayList` works but inserts and deletes shift elements, O(n) per operation.
- **Key insight.** Keep the invariant: intervals are disjoint and do not touch (`[1,5)` and `[5,9)` are stored as `[1,9)`). Then any point `x` is covered by at most one interval, the one found by `floorEntry(x)`. Query becomes a single lookup.
- **add:** if the interval just before `left` reaches `left`, extend from its start. Then absorb every interval whose start is `≤ right`, extending `right` to the furthest end.
- **remove:** if the interval starting before `left` extends past `left`, cut it to end at `left`, and if it also extends past `right`, re-add the tail `[right, end)`. Then delete every interval starting in `[left, right)`, re-adding a tail if one sticks out past `right`.

## Solution

```java
import java.util.*;

class RangeModule {
    // disjoint, non-touching intervals: start -> end (half-open)
    private final TreeMap<Integer, Integer> map = new TreeMap<>();

    public void addRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.floorEntry(left);
        if (e != null && e.getValue() >= left) {          // overlaps or touches
            left = e.getKey();
            right = Math.max(right, e.getValue());
        }
        Map.Entry<Integer, Integer> nx = map.ceilingEntry(left);
        while (nx != null && nx.getKey() <= right) {      // absorb intervals starting inside
            right = Math.max(right, nx.getValue());
            map.remove(nx.getKey());
            nx = map.ceilingEntry(left);
        }
        map.put(left, right);
    }

    public boolean queryRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.floorEntry(left);
        return e != null && e.getValue() >= right;
    }

    public void removeRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.lowerEntry(left);
        if (e != null && e.getValue() > left) {           // straddles left
            int end = e.getValue();
            map.put(e.getKey(), left);
            if (end > right) map.put(right, end);
        }
        Map.Entry<Integer, Integer> nx = map.ceilingEntry(left);
        while (nx != null && nx.getKey() < right) {
            map.remove(nx.getKey());
            if (nx.getValue() > right) map.put(right, nx.getValue());
            nx = map.ceilingEntry(left);
        }
    }
}
```

## Complexity

- **Query:** O(log n), where n is the number of stored intervals.
- **Add / remove:** O((1 + d) log n), where d is the number of intervals deleted. Each interval is inserted once, so over all operations the deletions are bounded by the insertions: O(log n) amortised.
- **Space:** O(n) intervals, at most one per `addRange` call plus one split per `removeRange`.

## Edge cases

- Touching ranges merge (`[1,5)` + `[5,9)` → `[1,9)`) because of `>= left` and `<= right`.
- Removing from the middle of an interval splits it in two.
- Removing a range that covers nothing: no change.
- Query exactly matching an interval's ends: `end >= right` is inclusive on the right, which is correct for half-open intervals.

## Follow-ups

- **Point query became a range query.** A point `x` is `queryRange(x, x + 1)`. Because intervals are merged and non-touching, a range is covered only if a *single* stored interval contains it, so the floor lookup still works. Without the merge invariant you would need to walk several adjacent intervals.
- **Count of covered length or very many operations:** a dynamic (sparse) segment tree over `[0, 10⁹)` with lazy "set covered / set uncovered" tags answers coverage sums too.

Practise it in the app: Run / Submit on this page.
