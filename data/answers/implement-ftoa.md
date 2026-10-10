**Short answer:** Work on the magnitude and remember the sign. Scale by `10^precision`, add 0.5 and floor to get a whole number of "units" (this rounds halves away from zero). Split the units into integer part (`units / scale`) and fraction part (`units % scale`), then emit digits by repeated `% 10` and `/ 10`. Print `-` only if the rounded value is non-zero.

## Picture it

Round once in integer space, then only split and print:

| x | precision | scale | \|x\| · scale + 0.5 | units (floor) | intPart | fracPart | Output |
|---|---|---|---|---|---|---|---|
| 3.14159 | 2 | 100 | 314.659 | 314 | 3 | 14 | "3.14" |
| -2.5 | 0 | 1 | 3.0 | 3 | 3 | 0 | "-3" (no `.`) |
| 0.0625 | 3 | 1000 | 63.0 | 63 | 0 | 63 | "0.063" |
| 9.996 | 2 | 100 | ≈ 1000.1 | 1000 | 10 | 0 | "10.00" (carry for free) |
| -0.004 | 2 | 100 | 0.9 | 0 | 0 | 0 | "0.00" (no sign, units = 0) |

The fixed-width fraction loop for `0.0625`, `fracPart = 63`, filled right to left:

| i | fracPart % 10 | frac so far | fracPart after /10 |
|---|---|---|---|
| 2 | 3 | `_ _ 3` | 6 |
| 1 | 6 | `_ 6 3` | 0 |
| 0 | 0 | `0 6 3` | 0 |

**The picture in one sentence:** turn the whole number into one rounded integer of `10^-p` units first, so carries and leading fraction zeros come out of exact integer maths.

## Approach

- **Brute force:** peel digits off the float directly: `int` part first, then multiply the fraction by 10 repeatedly. This breaks on rounding: you only know whether to round up after you have printed the digits, and a carry can ripple back into the integer part (`9.996` → `10.00`).
- **Key insight:** round *first*, in integer space. `units = floor(|x| * 10^p + 0.5)` is the whole answer as one integer. After that everything is exact `long` arithmetic: no further floating-point error, and the carry is handled for free.
- **Optimal:** build the integer part's digits right to left into a buffer (at least one digit, so `0` prints as `"0"`). Build exactly `p` fraction digits right to left, so leading zeros in the fraction (`0.063`) are kept. Sign last: skip it when `units == 0`, which turns `-0.004` into `"0.00"`.

## Solution

```java
class Solution {
    public String ftoa(double x, int precision) {
        boolean negative = x < 0;
        double a = Math.abs(x);
        long scale = 1;
        for (int i = 0; i < precision; i++) scale *= 10;
        // Round half away from zero on the magnitude, in units of 10^-precision.
        long units = (long) Math.floor(a * scale + 0.5);
        long intPart = units / scale, fracPart = units % scale;

        StringBuilder sb = new StringBuilder();
        if (negative && units != 0) sb.append('-');
        appendDigits(sb, intPart);
        if (precision > 0) {
            sb.append('.');
            char[] frac = new char[precision];
            for (int i = precision - 1; i >= 0; i--) {   // fixed width keeps leading zeros
                frac[i] = (char) ('0' + fracPart % 10);
                fracPart /= 10;
            }
            sb.append(frac);
        }
        return sb.toString();
    }

    private void appendDigits(StringBuilder sb, long v) {
        if (v == 0) { sb.append('0'); return; }
        char[] buf = new char[20];
        int i = buf.length;
        while (v > 0) {
            buf[--i] = (char) ('0' + v % 10);
            v /= 10;
        }
        sb.append(buf, i, buf.length - i);
    }
}
```

## Complexity

- **Time:** O(d + p), where d is the number of integer digits and p the precision. Each digit is produced once.
- **Space:** O(d + p) for the output; the buffers are fixed size.

## Edge cases

- `precision = 0`: no decimal point at all.
- Rounding carries into the integer part: `9.996` at 2 → `"10.00"`.
- Negative zero after rounding: `-0.004` at 2 → `"0.00"`, and `-0.0` itself.
- Fraction with leading zeros: `0.0625` at 3 → `"0.063"`, not `"0.63"`.
- Values below 1: integer part must still print `"0"`.
- Overflow: `|x| * 10^p` must fit in a `long`; the constraints keep it below 10¹³.

## Variations

- **Precision of the double itself:** `2.675` is stored as 2.67499999…, so it rounds to `2.67`. In an interview, say this out loud. If exact decimal rounding matters (money), you would use `BigDecimal` with `RoundingMode.HALF_UP`, or store values as integer cents in the first place.
- **Banker's rounding (half to even):** compute `floor(a * scale)`, look at the remainder, and round up only if it is above 0.5, or exactly 0.5 with an odd last digit.
- **`atof` (the reverse):** parse sign, integer digits, then fraction digits with a running divisor.

Practise it in the app: Run / Submit on this page.
