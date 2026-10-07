There are `n` cities numbered `0` to `n-1` and a list of one-way flights, where `flights[i] = [from, to, price]`. Find the cheapest total price to travel from city `src` to city `dst` using a route with **at most `k` stops** — that is, at most `k` intermediate cities, or equivalently at most `k + 1` flights. Return `-1` if no such route exists.

**Example 1**
Input: n = 4, flights = [[0,1,100],[1,2,100],[2,0,100],[1,3,600],[2,3,200]], src = 0, dst = 3, k = 1
Output: 700
Why: 0 → 1 → 3 costs 700 with one stop. The cheaper 0 → 1 → 2 → 3 (400) needs two stops.

**Example 2**
Input: n = 3, flights = [[0,1,100],[1,2,100],[0,2,500]], src = 0, dst = 2, k = 1
Output: 200

**Example 3**
Input: n = 3, flights = [[0,1,100],[1,2,100],[0,2,500]], src = 0, dst = 2, k = 0
Output: 500

**Constraints**
- 1 ≤ n ≤ 100
- 0 ≤ flights.length ≤ n · (n − 1) / 2
- 0 ≤ from, to < n, from ≠ to, and there is at most one flight from a given city to another
- 1 ≤ price ≤ 10⁴
- 0 ≤ src, dst, k < n and src ≠ dst

**Notes**: the cheapest route overall may use too many stops, so plain Dijkstra on price alone is not enough. Bellman–Ford limited to k + 1 rounds, or BFS by number of flights, both work.
