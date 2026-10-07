Given strings `s` and `t`, return the shortest contiguous substring of `s` that contains every character of `t`, counting repeats (if `t` has two `'a'`s, the window needs at least two `'a'`s). Upper- and lower-case letters are different characters. If no such substring exists, return `""`. If several shortest windows exist, return the one that starts furthest to the left.

**Example 1**
Input: s = "ADOBECODEBANC", t = "ABC"
Output: "BANC"
Why: "BANC" is the shortest piece of s that holds an A, a B and a C.

**Example 2**
Input: s = "a", t = "a"
Output: "a"

**Example 3**
Input: s = "a", t = "aa"
Output: ""
Why: s has only one 'a', but t needs two.

**Constraints**
- 1 ≤ s.length, t.length ≤ 10⁵
- s and t consist of upper- and lower-case English letters

**Notes**: the hidden tests include a string of 100,000 characters, so checking every substring is far too slow. Aim for O(|s| + |t|).
