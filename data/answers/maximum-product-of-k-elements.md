**Short answer:** Sort. If k is odd, take the largest value first; but if even the largest is ≤ 0, the product cannot be positive, so return the product of the k largest (the values closest to zero). Now k is even: repeatedly compare the product of the two smallest remaining values with the product of the two largest remaining, take the bigger pair, and move that pointer. Pairs make negatives cancel. O(n log n), or O(n log k) with heaps.

## Approach

- **Brute force:** try every k-subset: C(n, k), hopeless.
- **Why "take the k largest" fails:** two big negatives multiply to a big positive (`[1, 10, −5, 1, −10]`, k = 3 → 10 × −5 × −10 = 500).
- **Key insight:** after sorting, the best set only uses elements from the two ends. Think in pairs: a pair from the left end (two most negative) has a positive product, a pair from the right end (two largest) too. Greedily take whichever pair is larger.
- **Odd k:** one element must be taken alone. If the largest is positive, take it first; it keeps the result positive and the rest is the even case. If the largest is ≤ 0, everything is ≤ 0 and k is odd, so the result is ≤ 0: maximise it by taking the values with the smallest absolute value, which are the k largest.

## Solution

```java
import java.util.Arrays;

class Solution {
    public long maxProduct(int[] nums, int k) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length, l = 0, r = n - 1;
        long prod = 1;
        if (k % 2 == 1) {
            if (a[r] <= 0) {                     // all <= 0 and k odd: closest to zero
                for (int i = 0; i < k; i++) prod *= a[n - 1 - i];
                return prod;
            }
            prod = a[r--];
            k--;
        }
        while (k > 0) {                          // k even: take the better pair
            long left = (long) a[l] * a[l + 1];
            long right = (long) a[r] * a[r - 1];
            if (left > right) { prod *= left; l += 2; }
            else { prod *= right; r -= 2; }
            k -= 2;
        }
        return prod;
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort, then O(k).
- **Space:** O(n) for the copy; O(1) extra if you may sort the input in place.

## Edge cases

- All negative, k odd (`[−4, −3, −2, −1]`, k = 3): −6, from the three values closest to zero.
- All negative, k even: the most negative pairs give the largest positive product.
- Zeros: they take part in the pair comparison like any value (`[0, −1, 2, −3]`, k = 2 → 3, not 0).
- k = n: the product of everything.
- Overflow: |value| ≤ 10 and k ≤ 18 keep it under 10¹⁸, inside `long`. Without such bounds, use `BigInteger` or `Math.multiplyExact` to detect overflow.

## Follow-up: no extra space after sorting

Sort the input in place (if the caller allows it) and run the same two-pointer loop: O(1) extra variables. `Arrays.sort(int[])` is a dual-pivot quicksort, so it uses only O(log n) stack. If the input must not change, say that the clone is the price.

## Follow-up: without sorting

Only the k largest and the k smallest values can ever be picked, because the greedy takes at most k from each end. Keep a min-heap of size k for the k largest and a max-heap of size k for the k smallest in one pass: O(n log k). If n ≤ 2k just use all values; otherwise the two groups are different elements. Put the up-to-2k candidates in an array, sort it (at most 36 values here), and run the same greedy. Total O(n log k).

Because values are in [−10, 10], a counting sort over 21 buckets is also O(n) and needs only 21 counters, which is worth pointing out.

Practise it in the app: Run / Submit on this page.
