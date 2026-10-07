You are given a list of envelopes, where `envelopes[i] = [wi, hi]` is the width and height of envelope `i`. One envelope fits inside another only if **both** its width and its height are strictly smaller. Envelopes cannot be rotated. Return the largest number of envelopes you can nest one inside the other (a chain like a set of Russian dolls).

**Example 1**
Input: envelopes = [[5,4],[6,4],[6,7],[2,3]]
Output: 3
Why: [2,3] fits in [5,4], which fits in [6,7].

**Example 2**
Input: envelopes = [[1,1],[1,1],[1,1]]
Output: 1
Why: identical envelopes do not fit inside each other.

**Constraints**
- 1 ≤ envelopes.length ≤ 10⁵
- 1 ≤ wi, hi ≤ 10⁵

**Notes**: the hidden tests include around 20,000 envelopes, where an O(n²) DP is clearly slower — aim for O(n log n).
The interview version allowed boxes to be rotated; this runnable version keeps orientation fixed.
