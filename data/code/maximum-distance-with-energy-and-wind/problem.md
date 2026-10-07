You travel for `n` days. You start with energy `k`, which is also the most energy you can ever hold. On day `i` you do exactly one of two things:

- **Move**: this needs at least 1 energy and uses 1. Your distance changes by `a[i]`, which depends on that day's wind. A strong headwind can make `a[i]` zero or negative.
- **Rest**: your energy goes up by 1, but never above `k`.

Energy can never go below 0. Return the largest total distance you can have after the `n` days.

**Example 1**
Input: a = [5,1,4,2], k = 1
Output: 9
Why: move on day 0 (+5, energy 0), rest on day 1 (energy 1), move on day 2 (+4, energy 0), rest on day 3.

**Example 2**
Input: a = [3,3,3], k = 2
Output: 6
Why: you can move on only two of the three days, because one day must be spent resting.

**Example 3**
Input: a = [-2,7,-1], k = 1
Output: 7
Why: moving into a headwind only loses distance, so move only on day 1.

**Constraints**
- 1 ≤ n ≤ 10⁵
- 0 ≤ k ≤ 100
- −10⁴ ≤ a[i] ≤ 10⁴

**Notes**: with k = 0 you can never move, so the answer is 0. Trying every move/rest pattern is exponential. Track the best distance for each possible energy level instead.
