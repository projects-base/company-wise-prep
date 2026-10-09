**Short answer:** Scan left to right. Add each symbol's value, except when it is smaller than the symbol right after it: then subtract it. That one rule covers all six subtractive pairs (IV, IX, XL, XC, CD, CM). O(n) time, O(1) space.

## Approach

- **Explicit pairs.** Check the two-character pairs first ("CM", "CD", …) and fall back to single symbols. Works, but needs a table of 13 entries and careful lookahead.
- **Key insight.** In a valid numeral, symbols go from large to small. The only time a symbol is followed by a larger one is a subtractive pair, and in that case the smaller symbol counts negative. So compare each value with the next value: smaller means subtract, otherwise add.
- **Alternative.** Scan right to left, keeping the largest value seen so far; subtract a symbol if it is smaller than the previous (right-hand) one.

## Solution

```java
class Solution {
    public int romanToInt(String s) {
        int total = 0;
        for (int i = 0; i < s.length(); i++) {
            int v = value(s.charAt(i));
            if (i + 1 < s.length() && v < value(s.charAt(i + 1))) total -= v;
            else total += v;
        }
        return total;
    }

    private int value(char c) {
        return switch (c) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> throw new IllegalArgumentException("Not a Roman symbol: " + c);
        };
    }
}
```

Trace "MCMXCIV": M +1000, C < M −100, M +1000, X < C −10, C +100, I < V −1, V +5 = 1994.

## Complexity

- **Time:** O(n), with n ≤ 15.
- **Space:** O(1).

## Edge cases

- Single symbol ("I", "M").
- Repeated symbols ("III", "MMM").
- Subtractive pair at the end ("XIV") or at the start ("CMXC").
- Invalid input: the problem guarantees validity. In production code, throw on unknown characters (as above) rather than silently returning a number; full validation (no "IIII", no "IL") needs a regex or a canonical round-trip check.

## Variations

- **Integer to Roman (LC 12):** greedy over the 13 values `1000, 900, 500, 400, …, 4, 1`, subtracting the largest that fits.
- **Validate a Roman numeral:** convert to an integer, convert back, and compare with the input.

Practise it in the app: Run / Submit on this page.
