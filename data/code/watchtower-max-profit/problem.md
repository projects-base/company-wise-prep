A watchtower will be built at the origin `(0, 0)`. You choose its height `h`, any real number `h ≥ 0`. Building it costs `costPerHeight · h`. The tower covers every house whose straight-line (Euclidean) distance from the origin is **at most** `h`, and each covered house pays `payPerHouse`. Given the house positions `houses[i] = [xi, yi]`, return the largest achievable profit `payPerHouse · (houses covered) − costPerHeight · h`.

A height of 0 is allowed (it covers only houses standing exactly at the origin), so the answer is never negative.

**Example 1**
Input: houses = [[3,4],[0,1],[6,8],[-1,0]], costPerHeight = 1, payPerHouse = 3
Output: 5.00000
Why: the distances are 5, 1, 10 and 1. Height 1 covers two houses for 6 − 1 = 5; height 5 gives 9 − 5 = 4; height 10 gives 12 − 10 = 2.

**Example 2**
Input: houses = [[10,0]], costPerHeight = 5, payPerHouse = 20
Output: 0.00000
Why: reaching the house costs 50 but earns only 20, so build a tower of height 0.

**Example 3**
Input: houses = [[1,1],[2,2]], costPerHeight = 2, payPerHouse = 5
Output: 4.34315
Why: height 2√2 covers both houses: 10 − 2·2.82843 = 4.34315.

**Constraints**
- 1 ≤ houses.length ≤ 10⁵
- −10⁴ ≤ xi, yi ≤ 10⁴ (several houses may share a position)
- 1 ≤ costPerHeight, payPerHouse ≤ 10⁴

**Notes**: the judge prints the answer with 5 decimals. Only a few heights are worth considering; an O(n log n) solution is expected.
