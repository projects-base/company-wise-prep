A message of letters was encoded by replacing each letter with its position in the alphabet: `A → "1"`, `B → "2"`, …, `Z → "26"`, and concatenating the results. Given the digit string `s`, return **how many different letter messages** could have produced it.

A group of digits can be decoded only if it is a number from `1` to `26` written without a leading zero — so `"06"` is not a valid group, and `"0"` on its own is not either. If `s` cannot be decoded at all, return `0`.

**Example 1**
Input: s = "12"
Output: 2
Why: "AB" (1 2) or "L" (12).

**Example 2**
Input: s = "226"
Output: 3
Why: "BZ" (2 26), "VF" (22 6) or "BBF" (2 2 6).

**Example 3**
Input: s = "06"
Output: 0
Why: "06" is not a valid group and "0" cannot start a group.

**Constraints**
- 1 ≤ s.length ≤ 100
- `s` contains only digits and may contain leading zeros
- the answer fits in a 32-bit signed integer

**Notes**: plain recursion re-solves the same suffixes exponentially often; the hidden tests include long strings.
