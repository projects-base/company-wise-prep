There are `piles.length` piles of bananas; pile `i` holds `piles[i]` bananas, and a guard returns in `h` hours. Each hour you pick one pile and eat up to `k` bananas from it. If that pile has fewer than `k` left, you finish it and do nothing else that hour (you never start a second pile within the same hour). Return the smallest whole-number speed `k` that lets you finish every pile within `h` hours.

**Example 1**
Input: piles = [3,6,7,11], h = 8
Output: 4
Why: at speed 4 the piles take 1 + 2 + 2 + 3 = 8 hours; at speed 3 they would take 10.

**Example 2**
Input: piles = [30,11,23,4,20], h = 5
Output: 30
Why: with exactly one hour per pile, every pile must be finished in a single hour.

**Example 3**
Input: piles = [30,11,23,4,20], h = 6
Output: 23

**Constraints**
- 1 ≤ piles.length ≤ 10⁴
- piles.length ≤ h ≤ 10⁹
- 1 ≤ piles[i] ≤ 10⁹

**Notes**: the total number of hours can overflow an `int` while you search; the hidden tests include piles of 10⁹ bananas.
