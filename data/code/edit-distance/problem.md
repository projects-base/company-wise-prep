Given two strings `word1` and `word2`, return the minimum number of single-character edits needed to turn `word1` into `word2`. An edit is one of: insert a character, delete a character, or replace a character with another.

**Example 1**
Input: word1 = "horse", word2 = "ros"
Output: 3
Why: replace 'h' with 'r' (rorse), delete 'r' (rose), delete 'e' (ros).

**Example 2**
Input: word1 = "intention", word2 = "execution"
Output: 5

**Constraints**
- 0 ≤ word1.length, word2.length ≤ 500
- both strings consist of lowercase English letters

**Notes**: the hidden tests include two 500-character strings, so plain recursion without memoisation will not finish. Aim for O(m · n).
