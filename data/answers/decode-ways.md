**Short answer:** Let `ways(i)` be the number of decodings of the first i characters. The last group is either one digit (valid if it is not `'0'`) or two digits (valid if they form 10..26). So `ways(i) = ways(i−1)·[s[i−1] ≠ '0'] + ways(i−2)·[10 ≤ s[i−2..i−1] ≤ 26]`. Only the last two values are needed, so it is O(n) time and O(1) space — a Fibonacci with conditions.

## Picture it

`s = "11106"`. Each cell `ways(i)` reads at most two earlier cells: `ways(i−1)` through the single digit and `ways(i−2)` through the pair.

| i | Prefix | Single `s[i−1]` | Pair `s[i−2..i−1]` | `ways(i)` |
|---|---|---|---|---|
| 0 | "" | – | – | 1 (base) |
| 1 | "1" | '1' ok | – | 1 (base) |
| 2 | "11" | '1' ok: + ways(1) = 1 | "11" ok: + ways(0) = 1 | 2 |
| 3 | "111" | '1' ok: + ways(2) = 2 | "11" ok: + ways(1) = 1 | 3 |
| 4 | "1110" | '0' not allowed | "10" ok: + ways(2) = 2 | 2 |
| 5 | "11106" | '6' ok: + ways(4) = 2 | "06" < 10, not allowed | **2** |

```mermaid
flowchart RL
    w5["ways 5 = 2"] -->|"single 6"| w4["ways 4 = 2"]
    w4 -->|"pair 10"| w2["ways 2 = 2"]
    w2 -->|"single 1"| w1["ways 1 = 1"]
    w2 -->|"pair 11"| w0["ways 0 = 1"]
```

The two decodings are `1 1 10 6` (AAJF) and `11 10 6` (KJF). The `0` forces the `10` pair, which cuts off the extra choices from `ways(3)`.

**The picture in one sentence:** it is Fibonacci where each of the two steps back is allowed only if its digit group is a valid letter.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Decode ways: 1-D dynamic programming","code":["int numDecodings(String s) {","  int n = s.length(); int[] dp = new int[n + 1];   // dp[i] = ways to decode the first i digits","  dp[0] = 1; dp[1] = s.charAt(0) == '0' ? 0 : 1;","  for (int i = 2; i <= n; i++) {","    int one = s.charAt(i - 1) - '0', two = Integer.parseInt(s.substring(i - 2, i));","    if (one >= 1) dp[i] += dp[i - 1];","    if (two >= 10 && two <= 26) dp[i] += dp[i - 2];","  }","  return dp[n];","}"],"frames":[
{"note":"dp[i] = ways to decode the first i digits of \"12261\". dp[0] = 1 (empty), dp[1] = 1.","line":3,"array":{"label":"dp","values":[1,1,0,0,0,0],"marks":{"0":"done","1":"done"}},"vars":{"s":"12261"}},
{"note":"i = 2: '2' alone → dp[1] = 1; '12' as a pair → dp[0] = 1. dp[2] = 2.","line":7,"array":{"label":"dp","values":[1,1,2,0,0,0],"pointers":{"i":2},"marks":{"2":"cur","1":"hit","0":"hit"}},"vars":{"s":"12261","one":2,"two":12}},
{"note":"i = 3: '2' alone → dp[2] = 2; '22' as a pair → dp[1] = 1. dp[3] = 3.","line":7,"array":{"label":"dp","values":[1,1,2,3,0,0],"pointers":{"i":3},"marks":{"3":"cur","2":"hit","1":"hit"}},"vars":{"s":"12261","one":2,"two":22}},
{"note":"i = 4: '6' alone → dp[3] = 3; '26' as a pair → dp[2] = 2. dp[4] = 5.","line":7,"array":{"label":"dp","values":[1,1,2,3,5,0],"pointers":{"i":4},"marks":{"4":"cur","3":"hit","2":"hit"}},"vars":{"s":"12261","one":6,"two":26}},
{"note":"i = 5: '1' alone → dp[4] = 5. dp[5] = 5.","line":7,"array":{"label":"dp","values":[1,1,2,3,5,5],"pointers":{"i":5},"marks":{"5":"cur","4":"hit"}},"vars":{"s":"12261","one":1,"two":61}},
{"note":"\"12261\" decodes 5 ways. Only the last two dp values are needed, so O(1) space is possible.","line":9,"array":{"label":"dp","values":[1,1,2,3,5,5],"marks":{"5":"hit"}},"vars":{"answer":5}}
]}
```

## Approach

**Brute force.** Recursively try a one-digit group and a two-digit group at each position. The same suffixes get re-solved over and over, so the cost is exponential (Fibonacci-like) on strings such as "1111…".

**Key insight.** The number of ways to decode a prefix depends only on the number of ways for the two shorter prefixes and on the last one or two digits. That is a 1-D DP.

**Optimal.** Iterate i from 2 to n with base cases `ways(0) = 1` (the empty prefix has one decoding) and `ways(1) = 1` if `s[0] ≠ '0'` else 0.

- Single digit `s[i−1]`: allowed when it is 1..9 → add `ways(i−1)`.
- Pair `s[i−2..i−1]`: allowed when it is 10..26 (the `≥ 10` check rejects a leading zero like "06") → add `ways(i−2)`.

## Solution

```java
import java.util.*;

class Solution {
    // ways(i) = number of decodings of the first i characters; only the last two values are kept.
    public int numDecodings(String s) {
        int prev2 = 1, prev1 = s.charAt(0) == '0' ? 0 : 1; // ways(0), ways(1)
        for (int i = 2; i <= s.length(); i++) {
            int cur = 0;
            if (s.charAt(i - 1) != '0') cur += prev1;
            int two = (s.charAt(i - 2) - '0') * 10 + (s.charAt(i - 1) - '0');
            if (two >= 10 && two <= 26) cur += prev2;
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}
```

Trace "226": ways = 1, 1, 2 ("2 2", "22"), then for '6': single → 2, pair "26" → +1 = 3.

## Complexity

- **Time:** O(n) — one pass.
- **Space:** O(1) — two rolling values.

## Edge cases

- Leading zero ("06", "0") → 0. Once a count hits 0 with no way to recover, it stays 0.
- "10", "20": the '0' cannot stand alone, only the pair is valid → 1.
- "30", "100": a '0' preceded by 3..9 or by another '0' makes the whole string undecodable → 0.
- "27": the pair is above 26, only "2 7" → 1.

## Variations

- **Decode Ways II** (LeetCode 639): `*` matches 1..9; the same recurrence with multiplied counts and a modulus.
- **List all decodings:** backtracking; the output itself can be exponential.
- **Top-down with memo:** `f(i)` = ways for the suffix from i; equivalent, slightly easier to explain first.

See also [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
