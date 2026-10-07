Roman numerals use seven symbols: `I`=1, `V`=5, `X`=10, `L`=50, `C`=100, `D`=500, `M`=1000. Symbols are normally written from largest to smallest and their values added up. The exception is subtraction: when a smaller symbol sits directly before a larger one, it is subtracted instead (`IV`=4, `IX`=9, `XL`=40, `XC`=90, `CD`=400, `CM`=900). Given a valid Roman numeral `s`, return the integer it represents.

**Example 1**
Input: s = "III"
Output: 3

**Example 2**
Input: s = "LVIII"
Output: 58
Why: L = 50, V = 5, III = 3.

**Example 3**
Input: s = "MCMXCIV"
Output: 1994
Why: M = 1000, CM = 900, XC = 90, IV = 4.

**Constraints**
- 1 ≤ s.length ≤ 15
- s contains only the characters I, V, X, L, C, D, M
- s is a valid Roman numeral in the range [1, 3999]
