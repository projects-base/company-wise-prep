**Short answer:** An address has only four parts, so a matching pattern can only differ from the query by turning some parts into `*`. That gives just 2⁴ = 16 candidate patterns per query. Put all patterns in a `HashSet<String>`, and for each query generate the 16 variants and look each one up. Each query costs O(16) lookups, independent of the number of patterns.

## Approach

- **Brute force:** compare every query with every pattern, part by part. O(P · Q), up to 25 million part comparisons at the limits. It works, but the interviewer will ask for better.
- **Key insight:** flip the direction. Instead of asking "which patterns match this query?", ask "which patterns *could* match it?" Each of the four positions is either the query's own number or `*`. A pattern matches exactly when it equals one of those 16 strings.
- **Alternative (trie):** a 4-level trie keyed by part, where each node has children by number plus one `*` child. A query walks both the exact child and the `*` child at each level: at most 16 paths. Same bound, more code; useful if parts became ranges or prefixes (CIDR).

## Solution

```java
import java.util.*;

class Solution {
    public boolean[] matchIps(String[] patterns, String[] queries) {
        Set<String> set = new HashSet<>(Arrays.asList(patterns));
        boolean[] out = new boolean[queries.length];
        for (int q = 0; q < queries.length; q++) {
            String[] parts = queries[q].split("[.]");
            // bit k of mask set => replace part k with '*'
            for (int mask = 0; mask < 16 && !out[q]; mask++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < 4; k++) {
                    if (k > 0) sb.append('.');
                    sb.append((mask >> k & 1) == 1 ? "*" : parts[k]);
                }
                out[q] = set.contains(sb.toString());
            }
        }
        return out;
    }
}
```

## Complexity

- **Time:** O(P · L + Q · 16 · L), where L ≤ 15 is the address length. Effectively O(P + Q).
- **Space:** O(P · L) for the set.

## Edge cases

- `*.*.*.*` matches everything (mask 15).
- Exact pattern with no wildcard (mask 0).
- `split(".")` would treat `.` as "any character" in a regex and return nothing useful; use `"[.]"` or `"\\."`.
- Formatting: the solution relies on numbers having no leading zeros, so `"010"` and `"10"` do not both appear. If input could be dirty, normalise each part with `Integer.parseInt` first.
- Duplicate patterns: harmless in a set.

## Follow-up: multiple pattern lists and pairs of IPs together

- **Several lists** (for example allow-list and deny-list, or one list per tenant): build one set per list, or one set of `listId + "|" + pattern`. A query checks its 16 variants against each list it cares about; cost grows with the number of lists, not their sizes.
- **Pairs (source, destination) rules** like `10.*.*.* -> 192.168.1.*`: a rule is a pair of patterns. Generate 16 variants of the source and 16 of the destination, and look up the 256 combined keys `src + "->" + dst` in a set of rules. Still constant per query.
- **Integer form:** pack an address into an `int` and a pattern into `(value, mask)`; a match is `(ip & mask) == value`. Group patterns by their mask (at most 16 masks) and keep a `HashSet<Integer>` per mask: 16 integer lookups, no string building.
- For real CIDR ranges (`/0` to `/32`), the same trick gives at most 33 lookups, or use a binary trie on bits (longest-prefix match).

Practise it in the app: Run / Submit on this page.
