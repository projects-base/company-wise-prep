**Short answer:** Dutch national flag partitioning with three pointers. Everything before `low` is 0, between `low` and `mid` is 1, after `high` is 2, and `mid..high` is unknown. Look at `nums[mid]`: a 0 swaps to `low` (advance both), a 1 just advances `mid`, a 2 swaps to `high` (shrink `high`, do not advance `mid`). One pass, O(1) space.

## Approach

- **Library sort.** `Arrays.sort` is O(n log n) and forbidden here.
- **Counting sort (two passes).** Count 0s, 1s and 2s, then overwrite the array. O(n) and O(1) space, perfectly acceptable to mention first, but it is two passes.
- **Key insight (one pass).** Keep four regions and shrink the unknown one by one element per step:

```text
[ 0 0 0 | 1 1 1 | ? ? ? ? | 2 2 2 ]
 0     low      mid     high     n-1
```

- When `nums[mid] == 0`: swap with `nums[low]`. The value coming back from `low` is a 1 (or `low == mid`), so `mid` can advance too.
- When `nums[mid] == 2`: swap with `nums[high]` and decrement `high`. The value coming back is unknown, so `mid` stays and checks it next.

## Solution

```java
class Solution {
    public void sortColors(int[] nums) {
        int low = 0, mid = 0, high = nums.length - 1;
        while (mid <= high) {
            if (nums[mid] == 0) swap(nums, low++, mid++);
            else if (nums[mid] == 1) mid++;
            else swap(nums, mid, high--);
        }
    }

    private void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
```

## Complexity

- **Time:** O(n). Every step either advances `mid` or decreases `high`, so there are at most n steps.
- **Space:** O(1).

## Edge cases

- All one colour: no useful swaps, still one pass.
- Already sorted or reverse sorted (`[2,2,1,0,0]`).
- Single element.
- The loop condition must be `mid <= high`, not `<`: the element at `high` is still unknown.

## Variations

- **Three-way partition in quicksort:** the same algorithm around a pivot value (< pivot, == pivot, > pivot). It makes quicksort fast on arrays with many duplicates.
- **k colours:** counting sort in O(n + k), or recursive partitioning ("rainbow sort") in O(n log k).
- **Stability:** this algorithm is not stable; for objects with a colour key where order matters, use counting with a second output array.

Practise it in the app: Run / Submit on this page.
