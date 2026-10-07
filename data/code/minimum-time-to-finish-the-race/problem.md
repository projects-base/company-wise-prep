A race has `numLaps` laps. You have unlimited copies of each tyre type in `tires`, where `tires[i] = [f, r]`. On a fresh tyre of type `i`, the 1st lap takes `f` seconds, the 2nd `f·r`, the 3rd `f·r²`, and so on: the tyre slows down geometrically. Between any two laps you may swap to a fresh tyre of any type, which costs `changeTime` seconds. You may start the race on any tyre type without paying a change. Return the minimum total time to complete all laps.

**Example 1**
Input: tires = [[2,3],[3,4]], changeTime = 5, numLaps = 4
Output: 21
Why: run 2 laps on a fresh [2,3] tyre (2 + 6), change (5), run 2 more laps on a fresh [2,3] tyre (2 + 6). Total 8 + 5 + 8 = 21.

**Example 2**
Input: tires = [[1,10],[2,2],[3,4]], changeTime = 6, numLaps = 5
Output: 25
Why: [2,2] for 2 laps (6), change (6), [1,10] for 1 lap (1), change (6), [2,2] for 2 laps (6). Total 25.

**Constraints**
- 1 ≤ tires.length ≤ 10⁵
- 1 ≤ f, changeTime ≤ 10⁵
- 2 ≤ r ≤ 10⁵
- 1 ≤ numLaps ≤ 1000

**Notes**: because r ≥ 2, a tyre is never worth keeping for more than about 20 laps. Lap times overflow `int` quickly, so compute them in `long`. The answer fits in an `int`.
