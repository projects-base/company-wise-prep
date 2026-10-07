Implement `ftoa`: convert a floating-point number `x` to a string showing exactly `precision` digits after the decimal point — without using any library number-to-string or formatting routine (`String.valueOf(double)`, `Double.toString`, `String.format`, `BigDecimal`, `Integer.toString`, string concatenation of a number, etc.). Build the digits yourself with arithmetic.

The output must follow these rules exactly:
- **Rounding**: round `x` to `precision` decimal places, with exact halves rounded **away from zero** (`2.5 → "3"`, `-2.5 → "-3"`, `0.125` at precision 2 → `"0.13"`).
- **Digits**: exactly `precision` digits after the point, padded with trailing zeros (`1.5` at precision 3 → `"1.500"`). When `precision` is 0 there is no decimal point at all.
- **Integer part**: no leading zeros, but always at least one digit (`"0.25"`, not `".25"`). Rounding may carry into it (`9.996` at precision 2 → `"10.00"`).
- **Sign**: a leading `-` for negative values, **except** when the rounded result is zero, which is always written without a sign (`-0.004` at precision 2 → `"0.00"`).
- No `+` sign, no exponent notation, no spaces.

**Example 1**
Input: x = 3.14159, precision = 2
Output: "3.14"

**Example 2**
Input: x = -2.5, precision = 0
Output: "-3"
Why: halves round away from zero.

**Example 3**
Input: x = 0.0625, precision = 3
Output: "0.063"
Why: 0.0625 is exactly halfway between 0.062 and 0.063, so it rounds away from zero.

**Constraints**
- −10⁷ < x < 10⁷
- 0 ≤ precision ≤ 6

**Notes**: a `double` cannot store most decimals exactly — `2.675` is really stored as 2.67499999999999982…, so "correct" rounding of it is ambiguous. The tests avoid such knife-edge values: every input is either an exact half that a `double` stores exactly (like `0.5`, `0.125`, `0.0625`), or clearly away from a rounding boundary. Within the stated limits, `|x| · 10^precision` stays below 10¹³, so ordinary `double` and `long` arithmetic is precise enough.
