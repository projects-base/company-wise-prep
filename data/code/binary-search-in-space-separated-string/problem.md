A string `s` holds non-negative integers written in decimal, separated by single spaces and sorted in non-decreasing numeric order, for example `"3 17 17 205 9000000000000000000000"`. The numbers can be far too large for any built-in numeric type (hundreds of digits). Given another number `target` in the same decimal form, return `true` if it appears in `s` and `false` otherwise.

The point of the exercise is to search the string **in place**: binary search over character positions, and when you land in the middle of a number, find that number's boundaries and compare it with `target` as a digit string. Do not split the string into an array or parse every number.

**Example 1**
Input: s = "1 5 12 40 999", target = "12"
Output: true

**Example 2**
Input: s = "1 5 12 40 999", target = "41"
Output: false

**Example 3**
Input: s = "7 123456789012345678901234567890 123456789012345678901234567891", target = "123456789012345678901234567891"
Output: true

**Constraints**
- 0 ≤ s.length ≤ 2 × 10⁵ (an empty `s` contains no numbers)
- numbers in `s` are separated by exactly one space; there are no leading or trailing spaces
- every number (in `s` and `target`) has 1 to 1000 digits and no leading zeros, except the number `0` itself
- the numbers in `s` are sorted in non-decreasing order and may repeat

**Notes**: compare two numbers without parsing them: the one with more digits is larger; with equal length, compare digit by digit (plain string comparison). Aim for O(log n) comparisons — each costing at most the length of one number.
