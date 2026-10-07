You are given a list of meetings, where `intervals[i] = [start, end]` means meeting `i` occupies a room from time `start` up to (but not including) time `end`. Return the smallest number of meeting rooms needed so that every meeting can take place without two overlapping meetings sharing a room. A meeting that ends at time `t` frees its room for a meeting that starts at time `t`.

**Example 1**
Input: intervals = [[0,30],[5,10],[15,20]]
Output: 2
Why: [0,30] overlaps both others, but [5,10] and [15,20] can share the second room.

**Example 2**
Input: intervals = [[7,10],[2,4]]
Output: 1

**Example 3**
Input: intervals = [[1,5],[5,10],[10,15]]
Output: 1
Why: each meeting starts exactly when the previous one ends.

**Constraints**
- 1 ≤ intervals.length ≤ 10⁴
- 0 ≤ start < end ≤ 10⁶
- intervals are given in no particular order

**Notes**: the hidden tests include 10,000 meetings; aim for O(n log n).
