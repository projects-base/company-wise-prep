There are `n` routers on a 2D plane; `routers[i] = [xi, yi]` is the position of router `i`. Every router has the same broadcast radius `radius`. At time 0 only router `source` is switched on. Each second, every router that is already on sends a ping that switches on **every** router whose Euclidean distance from it is at most `radius` (a distance of exactly `radius` counts). Return the number of seconds until all routers are on, or `-1` if some router can never be switched on.

**Example 1**
Input: routers = [[0,0],[3,0],[6,0],[3,3]], radius = 3, source = 0
Output: 2
Why: at t=1 router 1 (distance 3 from router 0) turns on; at t=2 router 1 switches on router 2 (distance 3) and router 3 (distance 3).

**Example 2**
Input: routers = [[0,0],[5,0]], radius = 4, source = 0
Output: -1
Why: the two routers are 5 apart, farther than the radius.

**Example 3**
Input: routers = [[2,2]], radius = 1, source = 0
Output: 0
Why: the only router is already on.

**Constraints**
- 1 ≤ n ≤ 2000
- −10⁶ ≤ xi, yi ≤ 10⁶ (several routers may share a position)
- 0 ≤ radius ≤ 3·10⁶
- 0 ≤ source < n

**Notes**: an O(n²) breadth-first search is fine; beware that squared distances overflow `int`.
