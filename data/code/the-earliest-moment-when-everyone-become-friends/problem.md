There are `n` people labelled `0..n-1`. Each entry `logs[i] = [timestamp, x, y]` says that `x` and `y` became friends at time `timestamp`. Friendship is symmetric, and people are *acquainted* if they are friends or are linked through a chain of friends. Return the earliest timestamp at which every person is acquainted with every other person, or `-1` if that never happens. The logs are **not** sorted by time.

**Example 1**
Input: logs = [[20190101,0,1],[20190104,3,4],[20190107,2,3],[20190211,1,5],[20190224,2,4],[20190301,0,3],[20190312,1,2],[20190322,4,5]], n = 6
Output: 20190301
Why: after the friendship at 20190301 the groups {0,1,5} and {2,3,4} merge into one.

**Example 2**
Input: logs = [[0,2,0],[1,0,1],[3,0,3],[4,1,2],[7,3,1]], n = 4
Output: 3

**Example 3**
Input: logs = [[5,0,1]], n = 3
Output: -1
Why: person 2 never makes a friend.

**Constraints**
- 2 ≤ n ≤ 10⁴
- 1 ≤ logs.length ≤ 2·10⁴
- 0 ≤ timestamp ≤ 10⁹, all timestamps are distinct
- 0 ≤ x, y < n, x ≠ y (a pair may appear more than once)
