A country has `n` cities numbered `0` to `n-1`, joined by two-way roads. `edges[i] = [x, y, time]` is a road between cities `x` and `y` that takes `time` minutes to drive in either direction (there may be several roads between the same two cities). Every time you pass through a city `j` you pay its toll `passingFees[j]`, and this includes the city you start in and the city you finish in.

You start in city `0` at minute 0 and must reach city `n-1` within `maxTime` minutes (arriving at exactly `maxTime` is fine). Return the smallest total toll you can pay, or `-1` if city `n-1` cannot be reached in time.

**Example 1**
Input: maxTime = 30, edges = [[0,1,10],[1,2,10],[2,5,10],[0,3,1],[3,4,10],[4,5,15]], passingFees = [5,1,2,20,20,3]
Output: 11
Why: the route 0 → 1 → 2 → 5 takes 30 minutes and costs 5 + 1 + 2 + 3 = 11.

**Example 2**
Input: maxTime = 29, same roads and tolls
Output: 48
Why: the cheap route is now too slow; 0 → 3 → 4 → 5 takes 26 minutes and costs 5 + 20 + 20 + 3 = 48.

**Example 3**
Input: maxTime = 25, same roads and tolls
Output: -1
Why: no route reaches city 5 within 25 minutes.

**Constraints**
- 2 ≤ n ≤ 1000, n − 1 ≤ edges.length ≤ 1000
- 1 ≤ maxTime ≤ 1000, 1 ≤ time ≤ 1000, 1 ≤ passingFees[j] ≤ 1000
- the road network is connected and has no road from a city to itself

**Notes**: the cheapest route overall may be too slow and the fastest route may be expensive, so plain Dijkstra on either quantity alone is not enough. Revisiting a city costs its toll again.
