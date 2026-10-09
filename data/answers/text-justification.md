**Short answer:** It is a careful simulation. Greedily take as many words as fit with single spaces. For a normal line, distribute `maxWidth - letters` spaces over `gaps = words - 1` gaps: each gets `spaces / gaps`, and the first `spaces % gaps` gaps get one more. A single-word line and the last line are left-justified and padded on the right. O(total characters) time.

## Approach

There is no clever trick; the interview tests whether you split the problem cleanly and get every rule right.

1. **Pick the line.** From word `i`, extend `j` while `len + 1 + words[j].length() ≤ maxWidth`, where `len` counts single spaces between words.
2. **Classify the line.** Last line (`j == n`) or one word (`gaps == 0`): join with single spaces, pad right.
3. **Full justification.** `letters = len - gaps`, `spaces = maxWidth - letters`, `each = spaces / gaps`, `extra = spaces % gaps`. The leftmost `extra` gaps get `each + 1` spaces.

Keeping step 3 behind the `gaps == 0` check also avoids a division by zero.

## Solution

```java
import java.util.*;

class Solution {
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> out = new ArrayList<>();
        int i = 0, n = words.length;
        while (i < n) {
            // Take words i..j-1 onto this line.
            int j = i + 1, len = words[i].length();
            while (j < n && len + 1 + words[j].length() <= maxWidth) {
                len += 1 + words[j].length();
                j++;
            }
            StringBuilder sb = new StringBuilder();
            int gaps = j - i - 1;
            if (j == n || gaps == 0) {               // last line or single word: left-justify
                for (int k = i; k < j; k++) {
                    if (k > i) sb.append(' ');
                    sb.append(words[k]);
                }
                sb.append(" ".repeat(maxWidth - sb.length()));
            } else {
                int letters = len - gaps;            // characters of the words alone
                int spaces = maxWidth - letters;
                int each = spaces / gaps, extra = spaces % gaps;
                for (int k = i; k < j; k++) {
                    sb.append(words[k]);
                    if (k < j - 1) sb.append(" ".repeat(each + (k - i < extra ? 1 : 0)));
                }
            }
            out.add(sb.toString());
            i = j;
        }
        return out;
    }
}
```

`String.repeat` is available since Java 11.

## Complexity

- **Time:** O(L) where L is the total output size (lines × maxWidth); each word is placed once.
- **Space:** O(L) for the output; O(maxWidth) working buffer per line.

## Edge cases

- A word exactly `maxWidth` long: alone on its line, no padding.
- Single word overall: it is the last line, left-justified.
- A middle line with one word ("acknowledgment"): left-justified, not centred.
- Uneven spaces: left gaps get the extra ones ("example  of text").
- Last line with several words: single spaces only, even if more would fit.

## Variations

- **Minimise raggedness (word wrap DP):** choose line breaks to minimise the sum of squared trailing spaces; O(n²) DP, which is how TeX-style layout differs from greedy.
- **Centre alignment:** split the padding between left and right.
- **Words longer than `maxWidth`:** clarify; usually hyphenate or put the word on its own overflowing line.

See [H2 · Interview technique: think aloud, test, recover](../academy/lessons/H2.md): this problem rewards stating the rules first and testing each one.

Practise it in the app: Run / Submit on this page.
