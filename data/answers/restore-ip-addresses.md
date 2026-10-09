**Short answer:** Backtrack over where to place the three dots. Each part is 1 to 3 digits, its value is at most 255, and it has no leading zero unless it is exactly "0". Prune when the remaining digits cannot fill the remaining parts (fewer than one per part or more than three per part). There are at most 3⁴ = 81 splits, so the work is constant for any input.

## Approach

- **Brute force.** Three nested loops over the dot positions, validate the four parts. That is fine too: at most 81 combinations because each part is 1 to 3 digits.
- **Backtracking (cleaner).** Build parts left to right. At position `at`, try lengths 1, 2, 3. Stop extending the moment a part is invalid: a leading zero makes all longer parts invalid, and once the value exceeds 255 longer parts are larger still, so `break` rather than `continue`.
- **Pruning.** With `left` digits and `need` parts to go, require `need ≤ left ≤ 3 × need`. This also rejects strings longer than 12 or shorter than 4 immediately.

## Solution

```java
import java.util.*;

class Solution {
    public List<String> restoreIpAddresses(String s) {
        List<String> out = new ArrayList<>();
        build(s, 0, new ArrayList<>(), out);
        return out;
    }

    private void build(String s, int at, List<String> parts, List<String> out) {
        if (parts.size() == 4) {
            if (at == s.length()) out.add(String.join(".", parts));
            return;
        }
        int left = s.length() - at, need = 4 - parts.size();
        if (left < need || left > 3 * need) return;
        for (int len = 1; len <= 3 && at + len <= s.length(); len++) {
            String part = s.substring(at, at + len);
            if (len > 1 && part.charAt(0) == '0') break; // "01", "00" are invalid
            if (Integer.parseInt(part) > 255) break;
            parts.add(part);
            build(s, at + len, parts, out);
            parts.remove(parts.size() - 1);
        }
    }
}
```

## Complexity

- **Time:** O(1) in terms of input: at most 81 leaf paths, each building a string of at most 15 characters. Formally O(3⁴ · |s|) with |s| ≤ 12 after pruning.
- **Space:** O(1) recursion depth (4) plus the output.

## Edge cases (the 5-6 tests to write yourself on HackerRank)

1. `"0000"` → `["0.0.0.0"]` (zero parts are allowed, leading zeros are not).
2. `"010010"` → `["0.10.0.10", "0.100.1.0"]` (leading zeros rejected, "0" accepted).
3. `"255255255255"` → `["255.255.255.255"]` (upper bound exactly 255).
4. `"256256256256"` → `[]` (every part over 255).
5. `"123"` (too short) and `"1234567890123"` (13 digits, too long) → `[]`.
6. `"101023"` → five addresses, a good check that all splits are found.

## Follow-ups

- **"Line-by-line alternatives: ternary vs if/else, do they still work?"** Yes, as long as the logic is identical. Replacing `if (cond) break;` with a ternary is not possible because `break` is a statement, not an expression; you would rewrite it as a loop condition. Replacing the `parts.size() == 4` check with a ternary that returns different values is fine. The things that do change behaviour: turning `break` into `continue` (still correct, just slower), dropping the leading-zero check, or using `>=` instead of `>` against 255. Say this out loud: equivalent control flow is fine, and you would rerun the edge-case list after each change.
- **IPv6 or a different number of parts:** parametrise the part count, the max length and the validator.
- **Integer.parseInt on 3 digits** cannot overflow, so no try/catch is needed.

See [H2 · Interview technique: think aloud, test, recover](../academy/lessons/H2.md) for writing your own test cases when the platform gives none.

Practise it in the app: Run / Submit on this page.
