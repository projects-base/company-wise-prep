A car-rental desk keeps a list of bookings, where `intervals[i] = [start, end]` covers every time from `start` to `end` inclusive. Merge all bookings that overlap — including ones that merely touch, such as `[1,4]` and `[4,5]` — and return the consolidated list of non-overlapping intervals that covers exactly the same times.

**Example 1**
Input: intervals = [[1,3],[2,6],[8,10],[15,18]]
Output: [[1,6],[8,10],[15,18]]
Why: [1,3] and [2,6] overlap, so they become [1,6]; the others stand alone.

**Example 2**
Input: intervals = [[1,4],[4,5]]
Output: [[1,5]]
Why: the bookings touch at time 4.

**Example 3**
Input: intervals = [[5,7],[1,2]]
Output: [[1,2],[5,7]]

**Constraints**
- 1 ≤ intervals.length ≤ 10⁴
- 0 ≤ start ≤ end ≤ 10⁵
- the input is in no particular order

**Notes**: the merged intervals may be returned in any order — the judge sorts them by start before comparing. The hidden tests include 10,000 bookings, so aim for O(n log n).
