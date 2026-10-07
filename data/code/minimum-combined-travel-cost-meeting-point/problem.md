A city has `n` places numbered `0` to `n − 1`, joined by two-way roads `edges[i] = [u, v, w]`, where travelling the road in either direction costs `w`. Some friends start at the places listed in `friends`. They want to meet at one place `x`, and each friend travels there by their cheapest route. Return the smallest possible **total** cost, summed over all friends, among all choices of `x`. Return `-1` if no place can be reached by every friend.

The original question had just two people, Alice and Bob. This version allows up to 10 friends.

**Example 1**
Input: n = 4, edges = [[0,1,2],[1,2,3],[2,3,1],[0,3,10]], friends = [0,3]
Output: 6
Why: the cheapest route from 0 to 3 is 0→1→2→3 with cost 6, and any place on that route (even 0 or 3 itself) totals 6. With only two people, the answer is just the shortest distance between them.

**Example 2**
Input: n = 5, edges = [[0,1,4],[0,2,4],[0,3,4],[1,2,1],[3,4,2]], friends = [1,2,4]
Output: 11
Why: meeting at place 1 costs 0 + 1 + 10 = 11 (the friend at 4 travels 4→3→0→1). Meeting at place 0 costs 4 + 4 + 6 = 14.

**Example 3**
Input: n = 3, edges = [[0,1,5]], friends = [0,2]
Output: -1
Why: place 2 is cut off from place 0.

**Constraints**
- 1 ≤ n ≤ 10⁴
- 0 ≤ edges.length ≤ 3·10⁴, 0 ≤ u, v < n, 1 ≤ w ≤ 10⁶ (roads may repeat and may form loops)
- 1 ≤ friends.length ≤ 10. Friends may share a starting place.

**Notes**: run Dijkstra once from each friend (O(k · m log n)) and add the distances at each place. Totals can exceed the `int` range, so use `long`.
