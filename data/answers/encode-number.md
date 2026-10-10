**Short answer:** Write `num + 1` in binary and drop its leading `1`. There are `2^k` strings of length `k`, so the strings of length `k` start at index `2^k − 1`. Adding 1 moves that start to `2^k`, a 1 followed by `k` zeros, and the bits after the leading 1 count through the length-`k` strings in binary order. For example, 23 + 1 = 24 = `11000`, so the answer is `"1000"`.

## Picture it

Line up `num`, `num + 1` in binary, and the encoding. The leading 1 of `num + 1` marks the block (the length), and the rest is the string:

| num | num + 1 | Binary of num + 1 | Drop leading 1 | Block (length k) starts at 2^k − 1 |
|---|---|---|---|---|
| 0 | 1 | `1` | `""` | k = 0, starts at 0 |
| 1 | 2 | `10` | `"0"` | k = 1, starts at 1 |
| 2 | 3 | `11` | `"1"` | k = 1 |
| 3 | 4 | `100` | `"00"` | k = 2, starts at 3 |
| 6 | 7 | `111` | `"11"` | k = 2 (last one) |
| 7 | 8 | `1000` | `"000"` | k = 3, starts at 7 |
| 23 | 24 | `11000` | `"1000"` | k = 4, starts at 15, offset 8 = `1000` |
| 107 | 108 | `1101100` | `"101100"` | k = 6, starts at 63, offset 44 = `101100` |

**The picture in one sentence:** adding 1 shifts each length-k block to start exactly at 2^k, so the leading bit encodes the length and the remaining bits are the padded offset.

## Approach

- **Brute force:** generate the strings in order (empty, then all of length 1, then all of length 2, and so on) until you reach index `num`. That is O(num) strings, far too slow for `num = 10⁹`.
- **Find the block:** the strings of length `k` start at index `1 + 2 + … + 2^(k−1) = 2^k − 1`. Find the `k` with `2^k − 1 ≤ num < 2^(k+1) − 1`. The offset inside the block is `num − (2^k − 1)`. Write it in binary, padded to `k` bits.
- **Key insight:** `offset = (num + 1) − 2^k`, and `2^k ≤ num + 1 < 2^(k+1)`. So `num + 1` has exactly `k + 1` bits: a leading 1 (the `2^k`) followed by the offset in `k` bits, already padded with zeros. Removing the leading 1 gives the answer directly.

Check against the table: 0 → `1` → `""`, 3 → `100` → `"00"`, 6 → `111` → `"11"`, 7 → `1000` → `"000"`.

## Solution

```java
import java.util.*;

class Solution {
    // There are 2^k strings of length k, and those of length k start at index 2^k - 1.
    // So encode(num) is the binary form of num + 1 with its leading 1 removed.
    public String encode(int num) {
        return Long.toBinaryString((long) num + 1).substring(1);
    }
}
```

The cast to `long` is a habit to guard against overflow. `num ≤ 10⁹` fits in an `int` even after adding 1, but if `num` could be `Integer.MAX_VALUE`, `num + 1` would wrap to a negative number and give 32 ones.

## Complexity

- **Time:** O(log num). The string has about 30 characters at most.
- **Space:** O(log num) for the result.

## Edge cases

- `num = 0`: `"1".substring(1)` is `""`.
- The last string of a block (e.g. `num = 6`, giving `"11"`) and the first of the next block (`num = 7`, giving `"000"`).
- Large `num`: no loop, so no performance problem.

## Variations

- **Decode:** put a `1` in front of the string, parse it as binary and subtract 1.
- **Bijective base-2 (digits 1 and 2):** the same "shift by one" idea is behind spreadsheet column names (A, B, …, Z, AA), which is bijective base-26.
- **Without library calls:** build the bits with `(num + 1) >> i & 1`, from the top bit down to bit 0, skipping the highest set bit.

Practise it in the app: Run / Submit on this page.
