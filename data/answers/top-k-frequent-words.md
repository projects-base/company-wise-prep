**Short answer:** Count each error code with a `HashMap`. Keep a min-heap of size k whose head is the worst kept code: lowest count, and on a tie the *larger* string. Push every code, pop when the heap exceeds k, then pop everything and reverse to get best-first order. O(n + u log k) time.

## Approach

- **Sort.** Count, then sort distinct codes by (count descending, string ascending), take the first k: O(n + u log u). Correct and easy; state it first.
- **Key insight (size-k heap).** Only k results are needed, so keep at most k candidates. The tricky part is the tie-break inside a min-heap: the heap evicts the *worst*, and among equal counts the worst is the lexicographically *larger* code. That is why the comparator compares counts ascending but strings descending (`b.compareTo(a)`).
- **Output order.** Popping a min-heap yields worst first, so insert each popped code at the front of the result.
- **String order.** `String.compareTo` compares UTF-16 code units, so digits < uppercase < lowercase, and "E10" < "E2". If the business wants numeric order of codes, that is a different comparator; ask.

## Solution

```java
import java.util.*;

class Solution {
    public List<String> topKFrequent(String[] words, int k) {
        Map<String, Integer> count = new HashMap<>();
        for (String w : words) count.merge(w, 1, Integer::sum);
        // Min-heap of size k whose root is the "worst" kept code: lowest count, then largest string.
        PriorityQueue<String> heap = new PriorityQueue<>((a, b) -> {
            int ca = count.get(a), cb = count.get(b);
            return ca != cb ? Integer.compare(ca, cb) : b.compareTo(a);
        });
        for (String w : count.keySet()) {
            heap.add(w);
            if (heap.size() > k) heap.poll();
        }
        LinkedList<String> out = new LinkedList<>();
        while (!heap.isEmpty()) out.addFirst(heap.poll());
        return out;
    }
}
```

Parsing real log lines first (for example extracting the code with a regex such as `\bE\d{3}\b`) is a separate step; keep it in its own method so the counting logic stays testable.

## Complexity

- **Time:** O(n · L) to count (L = code length, for hashing), O(u log k) for the heap, O(k log k) to drain it.
- **Space:** O(u) for the map, O(k) for the heap.

## Edge cases

- Ties at the boundary of the top k: the smaller string wins.
- `k` equals the number of distinct codes.
- Mixed case ("e500" vs "E500"): different codes unless you normalise; clarify.
- One code only.

## Variations

- **Bucket sort:** buckets by count, sort each bucket alphabetically, read from the top: O(n + u log u) worst case, but fast when buckets are small.
- **Top K Frequent Elements:** the integer version without the string tie-break.
- **Sliding time window (top errors in the last 5 minutes):** add counts on arrival and subtract on expiry, with a `TreeSet` ordered by (count, code) for O(log u) updates.

Practise it in the app: Run / Submit on this page.
