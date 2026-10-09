**Short answer:** Do grade-school addition. Walk both strings from the last digit to the first, add the two digits and the carry, append `sum % 10`, keep `sum / 10` as the new carry. Keep going while either string has digits left or the carry is non-zero, then reverse the builder. O(max(m, n)) time.

## Approach

- **Brute force:** `Long.parseLong` or `BigInteger`. The first overflows past 19 digits, the second is not allowed.
- **Key insight:** the sum of two digits plus a carry is at most 9 + 9 + 1 = 19. So each column produces one output digit and a carry of 0 or 1. Only the current column matters.
- **Optimal:** two indices from the right, one loop. Append digits to a `StringBuilder` (appending is amortised O(1)), then reverse once at the end. Do not insert at index 0 each time — that is O(n) per insert and O(n²) in total.

## Solution

```java
class Solution {
    public String addStrings(String num1, String num2) {
        StringBuilder sb = new StringBuilder();
        int i = num1.length() - 1, j = num2.length() - 1, carry = 0;
        while (i >= 0 || j >= 0 || carry > 0) {
            int sum = carry;
            if (i >= 0) sum += num1.charAt(i--) - '0';
            if (j >= 0) sum += num2.charAt(j--) - '0';
            sb.append((char) ('0' + sum % 10));
            carry = sum / 10;
        }
        return sb.reverse().toString();
    }
}
```

The `carry > 0` in the loop condition handles the final carry, e.g. `"999" + "1"` → `"1000"`, with no special case after the loop.

## Complexity

- **Time:** O(max(m, n)). Each column is O(1), and the reverse is one more linear pass.
- **Space:** O(max(m, n)) for the result.

## Edge cases

- Different lengths: `"11" + "123"`. The shorter index goes negative and simply adds 0.
- Final carry: `"99" + "1"` → `"100"`.
- `"0" + "0"` → `"0"`. No leading zeros can appear, because inputs have none and we only add a digit while there is input or carry.

## Variations

- **Multiply strings:** use an `int[m + n]` array. Digit `i` times digit `j` goes to position `i + j + 1`, then push carries.
- **Subtract strings / add binary / add linked-list numbers:** the same column-by-column loop with a borrow or with base 2.

See [H1 · Java idioms for coding interviews](../academy/lessons/H1.md) for `StringBuilder` and char-digit arithmetic.

Practise it in the app: Run / Submit on this page.
