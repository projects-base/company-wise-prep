You are given two non-negative integers `num1` and `num2` written as decimal strings. They can be far too long for any built-in numeric type. Return their sum, also as a decimal string, without converting the whole inputs to numbers and without `BigInteger` or `BigDecimal`.

**Example 1**
Input: num1 = "11", num2 = "123"
Output: "134"

**Example 2**
Input: num1 = "456", num2 = "77"
Output: "533"

**Example 3**
Input: num1 = "0", num2 = "0"
Output: "0"

**Constraints**
- 1 ≤ num1.length, num2.length ≤ 10⁴
- both strings contain only digits and have no leading zeros, except the number "0" itself

**Notes**: the result must not have leading zeros. The hidden tests include 10,000-digit numbers.
