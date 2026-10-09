**Short answer:** Backtracking. Map each digit to its letters. For position i, try each letter of `digits[i]`: append it to a shared `StringBuilder`, recurse to i + 1, then remove it. When i reaches the end, add the built string. Empty input returns an empty list, not `[""]`.

## Approach

- This is a Cartesian product of the digits' letter sets, so the output size is the product of 3s and 4s, up to 4⁴ = 256. No algorithm can beat the output size; the goal is clean generation.
- **Backtracking:** depth = number of digits, branching 3 or 4. One `StringBuilder` is reused, with append / delete around each recursive call.
- **Iterative (BFS) alternative:** start with `[""]`, and for each digit replace every partial string with its extensions by that digit's letters.

## Solution

```java
import java.util.ArrayList;
import java.util.List;

class Solution {
    private static final String[] KEYS =
        {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

    public List<String> letterCombinations(String digits) {
        List<String> out = new ArrayList<>();
        if (digits.isEmpty()) return out;
        build(digits, 0, new StringBuilder(), out);
        return out;
    }

    private void build(String digits, int i, StringBuilder cur, List<String> out) {
        if (i == digits.length()) {
            out.add(cur.toString());
            return;
        }
        for (char c : KEYS[digits.charAt(i) - '0'].toCharArray()) {
            cur.append(c);
            build(digits, i + 1, cur, out);
            cur.deleteCharAt(cur.length() - 1);   // undo
        }
    }
}
```

## Complexity

- **Time:** O(4ⁿ · n): up to 4ⁿ results, each copied in O(n).
- **Space:** O(n) for recursion and the builder, plus the output.

## Edge cases

- Empty string: return `[]`. Without the check the recursion would add one empty string.
- Digits 7 and 9 have four letters, the rest three.
- Digits 0 and 1 map to nothing; they are excluded here, but if allowed, a digit with no letters would make the whole result empty (or you skip it; ask).
- Single digit: just its letters.

## Variations

- **T9 word lookup:** given a dictionary, return only real words. Better: map each dictionary word to its digit string once (`HashMap<String, List<String>>`), then a lookup is O(1).
- **Count only:** multiply the letter counts.
- **Letter Case Permutation**, **Generate Parentheses**, **Subsets**: the same backtracking template.

Practise it in the app: Run / Submit on this page.
