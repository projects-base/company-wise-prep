A valid IPv4 address is four whole numbers separated by dots, where each number is between 0 and 255 and has no leading zero (so "0" is fine but "01" and "00" are not). Given a string `s` of digits, return every valid IPv4 address you can make by inserting exactly three dots into `s`. You may not reorder, add or remove any digits.

**Example 1**
Input: s = "25525511135"
Output: ["255.255.11.135","255.255.111.35"]

**Example 2**
Input: s = "0000"
Output: ["0.0.0.0"]

**Example 3**
Input: s = "101023"
Output: ["1.0.10.23","1.0.102.3","10.1.0.23","10.10.2.3","101.0.2.3"]

**Constraints**
- 1 ≤ s.length ≤ 20
- `s` contains only digits

**Notes**: any order is accepted; the checker sorts your list before comparing. Return an empty list when no address is possible.
