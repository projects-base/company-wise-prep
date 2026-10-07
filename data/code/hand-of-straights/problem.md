You hold a hand of cards, `hand[i]` being the number on card `i`. Decide whether you can split **all** the cards into groups where every group has exactly `groupSize` cards with **consecutive** numbers (like 3, 4, 5). Each card must be used in exactly one group. Return `true` if it is possible.

(The interview version fixes `groupSize = 5`; here it is a parameter.)

**Example 1**
Input: hand = [1,2,3,6,2,3,4,7,8], groupSize = 3
Output: true
Why: [1,2,3], [2,3,4], [6,7,8].

**Example 2**
Input: hand = [1,2,3,4,5], groupSize = 4
Output: false
Why: 5 cards cannot be split into groups of 4.

**Example 3**
Input: hand = [5,1,2,3,4,6,7,8,9,10], groupSize = 5
Output: true
Why: [1..5] and [6..10].

**Constraints**
- 1 ≤ hand.length ≤ 10⁴
- 0 ≤ hand[i] ≤ 10⁹
- 1 ≤ groupSize ≤ hand.length

**Notes**: the smallest remaining card must start a group — there is nothing smaller to put before it.
