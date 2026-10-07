Given a string `s` of lowercase letters and an integer `k`, find the longest substring (contiguous) in which **every** character that appears occurs at least `k` times within that substring. Return its length, or `0` if no such substring exists.

**Example 1**
Input: s = "aaabb", k = 3
Output: 3
Why: "aaa" — 'a' appears 3 times. Any substring containing 'b' has only two b's.

**Example 2**
Input: s = "ababbc", k = 2
Output: 5
Why: "ababb" has 'a' twice and 'b' three times.

**Constraints**
- 1 ≤ s.length ≤ 10⁵
- s contains only lowercase English letters
- 1 ≤ k ≤ 10⁵

**Notes**: the hidden tests include strings of 100,000 characters, so checking every substring (O(n²)) is too slow. Look for an O(26·n) idea.
