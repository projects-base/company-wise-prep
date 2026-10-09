**Short answer:** Rephrase it: find the longest window that contains at most k zeros (those are the zeros you flip). Slide a window: extend `right`, count zeros, and while there are more than k, move `left` forward. Track the largest window. O(n), O(1) space.

## Approach

- **Brute force:** for every start, extend until you pass k zeros. O(n²).
- **Key insight:** "flip at most k zeros" is the same as "a window with at most k zeros". That rule is monotonic: if a window is valid, every smaller window inside it is valid too. Monotonic validity means a two-pointer sliding window works.
- **Prefix sums + binary search** also works in O(n log n): for each right end, binary search the earliest left with `zeros(left..right) ≤ k`. The window is simpler.

## Solution

```java
class Solution {
    public int longestOnes(int[] nums, int k) {
        int zeros = 0, best = 0, left = 0;
        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) zeros++;
            while (zeros > k) {                      // too many flips needed
                if (nums[left++] == 0) zeros--;
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n): `left` and `right` each move forward at most n times.
- **Space:** O(1).

## Edge cases

- `k = 0`: longest run of existing ones.
- `k ≥` number of zeros: the whole array.
- All zeros: answer is `min(k, n)`.
- All ones: n.

## Variations

- **Max Consecutive Ones II (k = 1):** same code with k = 1.
- **Streaming input, k = 1, cannot store the array:** remember only the index of the last zero; when a new zero arrives, `left = lastZero + 1`.
- **Streaming with general k:** keep a queue of the last k zero positions.
- **Longest Repeating Character Replacement:** the same window, where the "zeros" are characters that differ from the window's most frequent letter.

Practise it in the app: Run / Submit on this page.
