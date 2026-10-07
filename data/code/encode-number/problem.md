A secret function turns a non-negative integer into a string of `0`s and `1`s. You are shown its first few values:

| num | encode(num) |
|-----|-------------|
| 0 | `""` (empty) |
| 1 | `"0"` |
| 2 | `"1"` |
| 3 | `"00"` |
| 4 | `"01"` |
| 5 | `"10"` |
| 6 | `"11"` |
| 7 | `"000"` |

The pattern continues the same way: all strings of length 1, then all of length 2, then all of length 3, and so on, each length in increasing binary order. Work out the rule and implement `encode(num)`.

**Example 1**
Input: num = 23
Output: "1000"

**Example 2**
Input: num = 107
Output: "101100"

**Example 3**
Input: num = 0
Output: ""

**Constraints**
- 0 ≤ num ≤ 10⁹

**Notes**: listing every string up to `num` is far too slow for large inputs. Look at the binary form of `num + 1`.
