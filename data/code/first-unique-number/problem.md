A gallery receives paintings, each identified by an integer. Visitors always want to see the **oldest painting whose number is still unique** — a number that has arrived exactly once so far. Once a number arrives a second time, neither copy is unique any more, even if it later stops arriving. Implement the class `FirstUnique`:

- `FirstUnique(int[] nums)` starts the gallery with the paintings `nums`, in arrival order.
- `int showFirstUnique()` returns the earliest-arrived number that has arrived exactly once, or `-1` if there is none. It does not remove anything.
- `void add(int value)` records the arrival of `value`.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor, whose single argument is the starting array). The output is the list of return values, with `null` for the constructor and for `add`.

**Example 1**
Input:
["FirstUnique","showFirstUnique","add","showFirstUnique","add","showFirstUnique","add","showFirstUnique"]
[[[2,3,5]],[],[5],[],[2],[],[3],[]]
Output: [null,2,null,2,null,3,null,-1]
Why: after 2 arrives again, 3 is the oldest unique number; after 3 arrives again, nothing is unique (5 is already duplicated).

**Example 2**
Input:
["FirstUnique","showFirstUnique","add","add","showFirstUnique"]
[[[7,7]],[],[8],[7],[]]
Output: [null,-1,null,null,8]

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- 1 ≤ nums[i], value ≤ 10⁸
- at most 5 · 10⁴ calls to `showFirstUnique` and `add`

**Notes**: write your code in the class `FirstUnique` (keep that name). Both operations should be O(1) amortised. Interview follow-ups (not tested): what if millions of callers add and read concurrently, and how do you avoid serialising all readers behind one lock?
