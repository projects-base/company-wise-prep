`prices[i]` is the price of a stock on day `i`. You may buy one share on some day and sell it on a **later** day (at most one buy and one sell). Return the largest profit you can make, or `0` if no trade makes money.

**Example 1**
Input: prices = [7,1,5,3,6,4]
Output: 5
Why: buy on day 1 at price 1, sell on day 4 at price 6.

**Example 2**
Input: prices = [7,6,4,3,1]
Output: 0
Why: prices only fall, so it is best not to trade.

**Constraints**
- 1 ≤ prices.length ≤ 10⁵
- 0 ≤ prices[i] ≤ 10⁴

**Notes**: you cannot sell before you buy. The hidden tests include 50,000 days, so trying every buy/sell pair is too slow — aim for one pass.
