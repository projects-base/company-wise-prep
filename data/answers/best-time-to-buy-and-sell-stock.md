**Short answer:** One pass. Keep the lowest price seen so far. On each day, the best profit from selling today is `price − minSoFar`. Track the largest such value. O(n) time, O(1) space. If prices only fall, the answer stays 0.

## Approach

- **Brute force:** try every buy day `i` and every later sell day `j`. O(n²), which is too slow for 10⁵ days.
- **Key insight:** fix the sell day. The best buy day is simply the cheapest day *before* it. So we never need to look back — just carry the running minimum forward.
- **Another view:** this is Kadane's maximum subarray over the daily differences `prices[i] − prices[i−1]`. Buy-then-sell is a sum of consecutive daily changes.

## Solution

```java
class Solution {
    public int maxProfit(int[] prices) {
        int minSoFar = Integer.MAX_VALUE, best = 0;
        for (int p : prices) {
            minSoFar = Math.min(minSoFar, p);          // cheapest buy up to today
            best = Math.max(best, p - minSoFar);       // sell today
        }
        return best;
    }
}
```

Updating `minSoFar` before computing the profit is safe: on the day a new minimum appears, the profit is `p − p = 0`, which is the "buy and sell the same day" case and never beats `best`.

## Complexity

- **Time:** O(n), one pass.
- **Space:** O(1).

## Edge cases

- One day → 0.
- Strictly falling prices → 0 (do not return a negative number).
- All prices equal → 0.
- The minimum comes after the best pair, e.g. `[3, 8, 1, 2]` → 5. A new low later does not erase the earlier best.

## Follow-ups

- **Also print the buy and sell days:** keep `minDay` (the index of `minSoFar`) and, whenever `best` improves, save `buyDay = minDay` and `sellDay = i`. If `best` stays 0, report "no trade".

  ```java
  int minDay = 0, buy = -1, sell = -1, best = 0;
  for (int i = 1; i < prices.length; i++) {
      if (prices[i] < prices[minDay]) minDay = i;
      else if (prices[i] - prices[minDay] > best) {
          best = prices[i] - prices[minDay]; buy = minDay; sell = i;
      }
  }
  ```

- **At most two transactions (Stock III):** a four-state DP updated in one pass:

  ```java
  int buy1 = Integer.MIN_VALUE, sell1 = 0, buy2 = Integer.MIN_VALUE, sell2 = 0;
  for (int p : prices) {
      buy1  = Math.max(buy1, -p);          // best balance after the first buy
      sell1 = Math.max(sell1, buy1 + p);   // after the first sell
      buy2  = Math.max(buy2, sell1 - p);   // after the second buy
      sell2 = Math.max(sell2, buy2 + p);   // after the second sell
  }
  return sell2;
  ```

  O(n) time, O(1) space. Another way: prefix best profit `left[i]` plus suffix best profit `right[i+1]`, maximised over the split point. For **k transactions**, generalise to arrays `buy[k]` and `sell[k]`: O(n·k).

Practise it in the app: Run / Submit on this page.
