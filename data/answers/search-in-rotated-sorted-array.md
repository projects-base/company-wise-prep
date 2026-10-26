**Short answer:** Use one modified binary search. At any `mid`, at least one half, `[lo, mid]` or `[mid, hi]`, is sorted; you can tell which by comparing `nums[lo]` with `nums[mid]`. If the target lies inside the sorted half's range, search there; otherwise search the other half. That is O(log n) time and O(1) space.

## Picture it

`nums = [4,5,6,7,0,1,2]` (indices 0–6). Two sorted runs: `4 5 6 7 | 0 1 2`.

Target 0:

| Step | lo | hi | mid | nums[mid] | Sorted half | Target inside it? | Action |
|---|---|---|---|---|---|---|---|
| 1 | 0 | 6 | 3 | 7 | left [4..7] (4 ≤ 7) | 4 ≤ 0 < 7? no | lo = 4 |
| 2 | 4 | 6 | 5 | 1 | left [0..1] (0 ≤ 1) | 0 ≤ 0 < 1? yes | hi = 4 |
| 3 | 4 | 4 | 4 | 0 | – | – | found, return 4 |

Target 3 (missing):

| Step | lo | hi | mid | nums[mid] | Sorted half | Target inside it? | Action |
|---|---|---|---|---|---|---|---|
| 1 | 0 | 6 | 3 | 7 | left [4..7] | 4 ≤ 3 < 7? no | lo = 4 |
| 2 | 4 | 6 | 5 | 1 | left [0..1] | 0 ≤ 3 < 1? no | lo = 6 |
| 3 | 6 | 6 | 6 | 2 | left [2..2] | 2 ≤ 3 < 2? no | lo = 7 > hi, return -1 |

**The picture in one sentence:** every cut leaves one side sorted, and a range check on that side tells you in O(1) which half to throw away.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Search in a rotated sorted array: binary search on the sorted half","code":["int search(int[] a, int t) {","  int lo = 0, hi = a.length - 1;","  while (lo <= hi) {","    int mid = (lo + hi) >>> 1;","    if (a[mid] == t) return mid;","    if (a[lo] <= a[mid]) {                 // left half is sorted","      if (a[lo] <= t && t < a[mid]) hi = mid - 1;","      else lo = mid + 1;","    } else {                               // right half is sorted","      if (a[mid] < t && t <= a[hi]) lo = mid + 1;","      else hi = mid - 1;","    }","  }","  return -1;","}"],"frames":[
{"note":"Find 0. One half around mid is always sorted; check whether the target lies inside that half.","line":2,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":0,"hi":6},"range":[0,6]},"vars":{"t":0}},
{"note":"mid = 3, a[mid] = 7 ≠ 0.","line":4,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":0,"mid":3,"hi":6},"range":[0,6],"marks":{"3":"cur"}},"vars":{"lo":0,"mid":3,"hi":6}},
{"note":"Left half [4..7] is sorted but 0 is not in it: drop the left half.","line":8,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":4,"hi":6},"range":[4,6]},"vars":{"lo":4,"hi":6}},
{"note":"mid = 5, a[mid] = 1 ≠ 0.","line":4,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":4,"mid":5,"hi":6},"range":[4,6],"marks":{"5":"cur"}},"vars":{"lo":4,"mid":5,"hi":6}},
{"note":"Left half [0..1] is sorted and holds 0: drop the right half.","line":7,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":4,"hi":4},"range":[4,4]},"vars":{"lo":4,"hi":4}},
{"note":"a[4] = 0. Found in O(log n).","line":5,"array":{"label":"a (rotated)","values":[4,5,6,7,0,1,2],"pointers":{"lo":4,"mid":4,"hi":4},"range":[4,4],"marks":{"4":"hit"}},"vars":{"lo":4,"mid":4,"hi":4}}
]}
```

## Approach

- **Brute force:** linear scan. O(n). Correct, but it misses the point.
- **Two passes:** binary search for the rotation point (the index of the minimum), then a normal binary search in the correct side. O(log n), but two searches and more code.
- **Key insight:** rotation splits the array into two sorted runs. Cutting at any `mid` leaves one side fully sorted. For a sorted side, checking "is the target between its ends?" is O(1). That decides which side to keep.
- **Optimal:** a single loop:
  - If `nums[lo] <= nums[mid]`, the left half is sorted. Go left if `nums[lo] <= target < nums[mid]`, else go right.
  - Otherwise the right half is sorted. Go right if `nums[mid] < target <= nums[hi]`, else go left.

## Solution

```java
class Solution {
    public int search(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (nums[mid] == target) return mid;
            if (nums[lo] <= nums[mid]) {
                // left half [lo, mid] is sorted
                if (nums[lo] <= target && target < nums[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {
                // right half [mid, hi] is sorted
                if (nums[mid] < target && target <= nums[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }
}
```

## Complexity

- **Time:** O(log n). Each iteration discards half the range.
- **Space:** O(1).

## Edge cases

- One element (`[1]`, target 0): one comparison, return -1.
- Not rotated at all: the left half is always sorted and it becomes a plain binary search.
- Two elements, like `[3, 1]`: here `lo == mid`, which is why the check is `<=` and not `<`.
- Target at the rotation point, or at index 0 or n - 1.
- Target absent: the range empties, return -1. Hidden tests on Codility usually cover these.

## Variations

- **With duplicates (LeetCode 81):** when `nums[lo] == nums[mid] == nums[hi]` you cannot tell which side is sorted, so shrink with `lo++, hi--`. Worst case becomes O(n), for example `[1,1,1,1,0,1,1]`.
- **Find the minimum (LeetCode 153):** compare `nums[mid]` with `nums[hi]`. If `nums[mid] > nums[hi]` the minimum is to the right (`lo = mid + 1`), else `hi = mid`.
- **Number of rotations:** the index of the minimum.

Further reading: [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
