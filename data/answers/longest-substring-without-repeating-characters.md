**Short answer:** Sliding window with a "last seen" index per character. Move `right` across the string. If the current character was last seen inside the window, jump `left` to just past that position. The window `[left, right]` then has no repeats, and its length is a candidate. One pass, O(n).

## Picture it

Example 3: `s = "pwwkew"`.

| right | char | last[char] before | Inside window (≥ left)? | left after | Window | best |
|---|---|---|---|---|---|---|
| 0 | p | −1 | no | 0 | p | 1 |
| 1 | w | −1 | no | 0 | pw | 2 |
| 2 | w | 1 | yes, jump to 2 | 2 | w | 2 |
| 3 | k | −1 | no | 2 | wk | 2 |
| 4 | e | −1 | no | 2 | wke | 3 |
| 5 | w | 2 | yes, jump to 3 | 3 | kew | 3 |

```text
p w w k e w
    ^ left jumps past the old w (index 1), straight to index 2
```

Why the `>= left` check matters: in `"abba"`, at the second `a` the stored `last[a] = 0` is already outside the window (left = 2), so `left` must not jump back.

**The picture in one sentence:** remember where each character was last seen, so on a repeat the left edge jumps past it in one step instead of crawling.

## Approach

- **Brute force:** every substring, checked with a set. O(n³), or O(n²) if you extend each start until the first repeat.
- **Sliding window with a set:** grow right; on a repeat, remove characters from the left one by one until the duplicate is gone. O(n), each character added and removed once.
- **Key insight:** you do not need to remove one by one. If you know where the duplicate was last seen, `left` can jump straight to `last[c] + 1`. The check `last[c] >= left` ignores old positions that are already outside the window.

## Solution

```java
import java.util.Arrays;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int[] last = new int[128];        // ASCII; last index of each character
        Arrays.fill(last, -1);
        int best = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (last[c] >= left) left = last[c] + 1;   // repeat inside the window
            last[c] = right;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n), one pass.
- **Space:** O(1): an array of 128 (the alphabet size, not n).

## Edge cases

- Empty string: the loop does not run, answer 0.
- All the same character: 1.
- All distinct: n.
- Old position outside the window (`"abba"`): at the last `a`, `last['a'] = 0 < left = 2`, so `left` must not move back. The `>= left` check (or `left = Math.max(left, last[c] + 1)`) prevents it. This is the most common bug.
- Spaces and symbols are characters too; the 128-slot array covers ASCII. For full Unicode use a `HashMap<Character, Integer>`.

## Variations

- **At most k distinct characters:** window with a count map, shrink while distinct > k.
- **At most two of each character:** shrink while the newest character's count is above 2.
- **Return the substring:** remember `left` when `best` improves.
- **Longest Repeating Character Replacement**, **Minimum Window Substring:** other windows with different validity rules.

Practise it in the app: Run / Submit on this page.
