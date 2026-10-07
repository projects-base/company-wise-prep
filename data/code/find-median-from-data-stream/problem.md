Numbers arrive one at a time and at any moment you may be asked for the median of everything seen so far. The median of an odd count of numbers is the middle one after sorting; for an even count it is the average of the two middle ones. Implement class `MedianFinder`:

- `MedianFinder()` creates an empty structure.
- `void addNum(int num)` records `num`.
- `double findMedian()` returns the median of all numbers recorded so far. It is only called after at least one `addNum`.

**Input format**: two lines — the list of operation names, then the list of argument lists. The checker prints the list of return values, `null` for the constructor and `addNum`, and medians with 5 decimals.

**Example 1**
Input:
["MedianFinder","addNum","addNum","findMedian","addNum","findMedian"]
[[],[1],[2],[],[3],[]]
Output: [null,null,null,1.50000,null,2.00000]
Why: the median of {1, 2} is 1.5; of {1, 2, 3} it is 2.

**Example 2**
Input:
["MedianFinder","addNum","addNum","findMedian","addNum","findMedian","addNum","findMedian"]
[[],[-1],[-2],[],[-3],[],[-4],[]]
Output: [null,null,null,-1.50000,null,-2.00000,null,-2.50000]

**Constraints**
- −10⁵ ≤ num ≤ 10⁵
- at most 5 · 10⁴ calls in total

**Notes**: keeping a sorted list costs O(n) per insert; two heaps (a max-heap for the lower half, a min-heap for the upper half) give O(log n) inserts and O(1) medians. The hidden tests include a stream with a median query after every insert.
