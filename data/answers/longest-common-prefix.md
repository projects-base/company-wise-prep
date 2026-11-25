**Short answer:** Take the first string as the candidate and compare column by column: at position `i`, every other string must have the same character. Stop at the first mismatch or when a string runs out, so the cost is at most O(S) for S total characters.

**Assumption:** the candidate wrote "lowest common prefix". I read that as the **longest** common prefix of an array of strings; if the interviewer draws a tree, they mean lowest common *ancestor*, so confirm.

## Approach

- **Brute force:** for each prefix of the first string, longest first, test `startsWith` on every string. O(n · m²) for length m.
- **Horizontal scan:** shrink the prefix against each next string. O(S), but it may compare two long strings fully before it meets a short one.
- **Vertical scan (chosen):** compare one column across all strings. It stops at the first bad column, so it never reads past the answer plus one column.

Trace for `["flower", "flow", "flight"]`:

| column | flower | flow | flight | same? |
|---|---|---|---|---|
| 0 | f | f | f | yes |
| 1 | l | l | l | yes |
| 2 | o | o | i | no, stop |

Answer: `"fl"`.

## Solution

```java
class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        String first = strs[0];
        for (int i = 0; i < first.length(); i++) {
            char c = first.charAt(i);
            for (int j = 1; j < strs.length; j++) {
                if (i == strs[j].length() || strs[j].charAt(i) != c) return first.substring(0, i);
            }
        }
        return first;                  // the first string is a prefix of all the others
    }
}
```

## Complexity

- **Time:** O(n · (L + 1)) comparisons, where L is the answer's length. The worst case is O(S), when all strings are equal.
- **Space:** O(1) beyond the returned substring.

## Edge cases

- Empty array: `""`. One string: that string.
- Any empty string: `""`.
- One string is a prefix of all the others: that string.
- No shared first character: `""`.
- Characters outside the basic plane (emoji): the cut can split a surrogate pair. Compare code points if that matters.

## Variations

- **Min and max only:** the common prefix of the smallest and largest strings is the answer for all. Finding them needs no sort.

```java
import java.util.*;

class MinMaxPrefix {
    String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) return "";
        String min = Collections.min(Arrays.asList(strs)), max = Collections.max(Arrays.asList(strs));
        int i = 0;
        while (i < min.length() && i < max.length() && min.charAt(i) == max.charAt(i)) i++;
        return min.substring(0, i);
    }
}
```

- **Divide and conquer:** combine the prefix of the left half with the prefix of the right half. Same O(S), and it parallelises.
- **Binary search on length:** O(S log m) with plain checks, so it only helps when prefix checks are cheap (hashes).
- **Many queries or a changing set:** build a trie. The answer is the path from the root while a node has one child and no word ends there.

Related: [T5 · Backtracking, tries and analysing recursion](../academy/lessons/T5.md).
