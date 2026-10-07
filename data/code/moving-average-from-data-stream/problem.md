Design a class that receives a stream of integers and reports the average of the most recent values in a sliding window of fixed size.

- `MovingAverage(int size)` creates the object with window size `size`.
- `double next(int val)` adds `val` to the stream and returns the average of the last `size` values. If fewer than `size` values have arrived so far, it averages all of them.

**Input format**: the first line lists the operations, the second line their arguments. The output is the list of results (`null` for the constructor), with averages printed to 5 decimal places.

**Example 1**
Input:
["MovingAverage","next","next","next","next"]
[[3],[1],[10],[3],[5]]
Output: [null,1.00000,5.50000,4.66667,6.00000]
Why: 1/1, (1+10)/2, (1+10+3)/3, then the window drops the 1: (10+3+5)/3.

**Example 2**
Input:
["MovingAverage","next","next","next"]
[[1],[4],[-2],[7]]
Output: [null,4.00000,-2.00000,7.00000]
Why: a window of size 1 always returns the latest value.

**Constraints**
- 1 ≤ size ≤ 1000
- −10⁵ ≤ val ≤ 10⁵
- at most 10⁴ calls to `next`

**Notes**: each call to `next` should be O(1): keep a running sum instead of re-adding the whole window.
