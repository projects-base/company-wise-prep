You roll `k` dice, each with faces numbered `1` to `n`. An outcome is the ordered list of values the dice show (die 1, die 2, …, die k), so there are `n^k` outcomes in total. Count the outcomes in which **one die shows a value strictly higher than every other die** — that is, the largest value appears on exactly one die.

For `k = 2` this is the classic question "in how many outcomes does one die beat the other?" (every outcome except a tie). Because the count grows very quickly, return it **modulo 1,000,000,007**.

**Example 1**
Input: n = 6, k = 2
Output: 30
Why: 36 outcomes, minus the 6 ties.

**Example 2**
Input: n = 3, k = 3
Output: 15
Why: for a unique maximum of 2 there are 3 positions for the 2 and the other two dice show 1 (3 outcomes); for a unique maximum of 3 there are 3 positions and 2·2 choices for the rest (12 outcomes).

**Example 3**
Input: n = 1, k = 4
Output: 0
Why: every die shows 1, so the maximum is always shared.

**Constraints**
- 1 ≤ n ≤ 10⁶
- 2 ≤ k ≤ 10⁹

**Notes**: enumerating outcomes is hopeless; think about which value is the unique maximum. The hidden tests include n = 10⁶ with k = 10⁹, so aim for about O(n log k).
