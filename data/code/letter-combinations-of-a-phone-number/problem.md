On a classic phone keypad each digit from 2 to 9 stands for a few letters: 2 → abc, 3 → def, 4 → ghi, 5 → jkl, 6 → mno, 7 → pqrs, 8 → tuv, 9 → wxyz. Given a string `digits`, return every string you could type by picking one letter for each digit, keeping the digits' order. If `digits` is empty, return an empty list.

**Example 1**
Input: digits = "23"
Output: ["ad","ae","af","bd","be","bf","cd","ce","cf"]

**Example 2**
Input: digits = ""
Output: []

**Example 3**
Input: digits = "2"
Output: ["a","b","c"]

**Constraints**
- 0 ≤ digits.length ≤ 4
- every character of `digits` is between '2' and '9'

**Notes**: any order is accepted; the checker sorts your list before comparing.
