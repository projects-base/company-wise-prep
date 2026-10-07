You are planning an event over days `1` to `totalDays`. There are `people` participants, numbered `0` to `people − 1`. Each entry `busy[i] = [person, start, end]` says that `person` is unavailable on every day from `start` to `end` inclusive. A person may have many blocks, and their blocks may overlap or touch.

A day is **good** if at least `minFree` people are available on it. Return all good days as a list of maximal ranges `[first, last]` (inclusive), sorted by `first`. Two ranges in the answer never touch or overlap — consecutive good days belong to the same range. Return an empty list if there are no good days.

Setting `minFree = people` answers the classic question "on which days is **everyone** available?".

**Example 1**
Input: people = 3, totalDays = 10, busy = [[0,1,3],[1,2,4],[2,6,7],[0,9,9]], minFree = 3
Output: [[5,5],[8,8],[10,10]]
Why: these are the only days on which nobody is busy.

**Example 2**
Input: people = 3, totalDays = 10, busy = [[0,1,3],[1,2,4],[2,6,7],[0,9,9]], minFree = 2
Output: [[1,1],[4,10]]
Why: only on days 2 and 3 are two people busy at once.

**Example 3**
Input: people = 2, totalDays = 6, busy = [[0,1,4],[0,3,5]], minFree = 2
Output: [[6,6]]
Why: person 0's two blocks overlap; they still count as one busy person on days 3–4.

**Constraints**
- 1 ≤ people ≤ 10⁵
- 1 ≤ totalDays ≤ 10⁹
- 0 ≤ busy.length ≤ 10⁴
- 0 ≤ person < people, 1 ≤ start ≤ end ≤ totalDays
- 1 ≤ minFree ≤ people

**Notes**: `totalDays` can be up to a billion, so walking day by day is too slow — work with the block boundaries instead.
