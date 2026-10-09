**Short answer:** Sort the words once. All words with a given prefix sit in one contiguous block of the sorted list, starting at the binary-search lower bound of the prefix. So for each typed prefix, binary-search the start and take up to three words that still start with the prefix. A trie that keeps the three smallest words at each node is the other standard answer, and fits the interview's `addWord` / `getWords` class.

## Approach

- **Brute force:** for each prefix, scan all words, filter by `startsWith`, sort, take three. O(m · n · L) where m is the search word length.
- **Key insight:** in lexicographic order, words sharing a prefix are adjacent, and the first of them is the lower bound of the prefix itself. The three smallest matches are simply the next three words.
- **Optimal (sorted array):** sort once. For each prefix, find `lowerBound(prefix)`, then walk at most 3 words checking `startsWith`. Prefixes only grow, so the lower bound only moves right, and each search can start from the previous position.
- **Trie version:** each node stores a sorted list of at most 3 words passing through it. `addWord` walks the word and inserts it into each node's list, keeping only the three smallest. `getWords(prefix)` walks the prefix and returns that node's list in O(|prefix|).

## Solution

```java
import java.util.*;

class Solution {
    public List<List<String>> suggestedProducts(String[] products, String searchWord) {
        String[] sorted = products.clone();
        Arrays.sort(sorted);
        List<List<String>> out = new ArrayList<>();
        StringBuilder prefix = new StringBuilder();
        int from = 0;
        for (char c : searchWord.toCharArray()) {
            prefix.append(c);
            String p = prefix.toString();
            from = lowerBound(sorted, p, from);     // a longer prefix never moves the bound left
            List<String> step = new ArrayList<>();
            for (int i = from; i < sorted.length && step.size() < 3 && sorted[i].startsWith(p); i++) {
                step.add(sorted[i]);
            }
            out.add(step);
        }
        return out;
    }

    /** First index >= lo whose word is >= key. */
    private int lowerBound(String[] a, String key, int lo) {
        int hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].compareTo(key) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
```

## Complexity

- **Time:** sorting is O(n log n) comparisons, each up to O(L). Each of the m prefixes then costs O(log n · L) for the search plus O(3L) for the scan.
- **Space:** O(n) for the sorted copy plus the output. The trie version uses O(total characters) nodes.

## Edge cases

- No word matches: empty list for that step and every later step.
- Fewer than three matches.
- A word equal to the prefix counts as a match.
- A prefix longer than every word.

## Follow-ups

- **Pattern with `.` (any one letter):** use the trie and DFS. At a letter follow that one child; at `.` try children in `a` to `z` order. When the pattern is consumed, the node's stored top-3 list gives candidates. Because branches are explored in alphabetical order, results come out sorted, so stop as soon as you have three. The worst case still grows with the number of dots, but early stopping keeps typical queries fast.

Practise it in the app: Run / Submit on this page.
