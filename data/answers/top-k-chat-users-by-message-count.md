**Short answer:** Count messages per user with a `HashMap`. Then keep the k best users in a min-heap of size k whose head is the *worst* kept user (fewest messages, then the larger name). Push each user and pop when the heap exceeds k. Finally sort the k survivors best-first. That is O(m + u log k) for m messages and u distinct users.

## Picture it

Counts after the first pass: `ann 3, bob 2, cat 1, dan 2`; `k = 2` (answer `["ann","bob"]`). The `HashMap` key order is arbitrary; say it yields cat, bob, dan, ann. The heap is listed worst-first (its head on the left).

| step | add | heap after add (worst first) | size > k? | poll | heap kept |
|---|---|---|---|---|---|
| 1 | cat(1) | cat(1) | no | | cat |
| 2 | bob(2) | cat(1), bob(2) | no | | cat, bob |
| 3 | dan(2) | cat(1), dan(2), bob(2) | yes | cat | dan, bob |
| 4 | ann(3) | dan(2), bob(2), ann(3) | yes | dan (ties with bob, but "dan" > "bob" so it is worse) | bob, ann |
| end | | sort with `better` | | | **ann, bob** |

**The picture in one sentence:** a size-k heap whose head is the weakest kept user lets every newcomer evict the current worst, and one `better` comparator handles the count-then-name tie-break everywhere.

## Approach

- **Simple.** Count, then sort all users by (count descending, name ascending) and take k: O(m + u log u). Accepted, and the right first answer to state.
- **Key insight (size-k heap).** You only need the k best, so never hold more than k candidates. A min-heap ordered by "worst first" lets you evict the weakest candidate in O(log k).
- **Tie-break consistency.** Define one comparator `better` (more messages, then smaller name) and use `better.reversed()` for the heap. Writing two comparators by hand is where tie-break bugs creep in.
- **Alternative:** bucket sort by count (counts are at most m) gives O(m + u) before tie sorting inside buckets.

## Solution

```java
import java.util.*;

class Solution {
    public List<String> topKUsers(String[][] messages, int k) {
        Map<String, Integer> count = new HashMap<>();
        for (String[] m : messages) count.merge(m[0], 1, Integer::sum);
        // "better" = more messages, then smaller name. Keep the k best in a min-heap of the worst.
        Comparator<String> better = (a, b) -> {
            int ca = count.get(a), cb = count.get(b);
            return ca != cb ? Integer.compare(cb, ca) : a.compareTo(b);
        };
        PriorityQueue<String> heap = new PriorityQueue<>(better.reversed());
        for (String u : count.keySet()) {
            heap.add(u);
            if (heap.size() > k) heap.poll();
        }
        List<String> out = new ArrayList<>(heap);
        out.sort(better);
        return out;
    }
}
```

## Complexity

- **Time:** O(m) to count, O(u log k) for the heap, O(k log k) for the final sort.
- **Space:** O(u) for the counts, O(k) for the heap.

## Edge cases

- Fewer than k users: return all of them, sorted.
- All users tied: alphabetical order decides.
- `k = 1`.
- Heap iteration order is not sorted; the final `sort` is required.

## Follow-ups

- **Rank by total word count instead of message count.** Only the weight changes: `count.merge(m[0], wordCount(m[1]), Integer::sum)` with `wordCount` = number of tokens after `text.trim().split("\\s+")` (0 for blank text). Clarify what a "word" is (punctuation, emojis) and use `long` if totals can exceed 2³¹. The heap logic is unchanged.
- **Truly unbounded stream, query at any time:** keep the counts map plus a `TreeSet` ordered by (count, name); on each message remove the user, bump the count, re-insert (O(log u)), and read the top k from the set's end. For huge user counts, approximate with Count-Min Sketch plus a heap of heavy hitters, or Space-Saving.
- **Distributed:** count per shard by user hash, take the top k per shard, merge; correct because each user lives on exactly one shard.

Practise it in the app: Run / Submit on this page.
