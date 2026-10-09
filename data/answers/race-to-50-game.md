**Short answer:** Go first and say 6. After that, whatever number `k` your opponent adds, you add `11 - k`. The running total then goes 6, 17, 28, 39, 50 on your turns, and you hit 50. It works because whatever the opponent picks (1 to 10), you can always top the round up to exactly 11, and 50 = 4 x 11 + 6.

## Explanation

Work backwards from the goal. Call a total a **winning total** if the player who just made it will win with correct play.

- 50 is winning: you made it, you won.
- From 40 to 49, the player to move can reach 50 directly. So leaving the total anywhere from 40 to 49 loses.
- 39 is winning: from 39 the opponent can only reach 40-49 (adding 1-10), and you then finish.
- By the same argument 28, 17 and 6 are winning: each is 11 below the next.

So the winning totals are those with `total ≡ 50 (mod 11)`, i.e. `total mod 11 == 6`. The key number 11 is `max + min` = 10 + 1: one round (opponent's move plus your reply) can always be forced to add exactly 11, because the opponent's pick `k` is in 1..10 and so `11 - k` is also in 1..10.

**Who wins?** The first player, by opening with 6. If 50 had been a multiple of 11 (say the target were 44), the first player would have no winning opening and the second player would win by always completing to 11.

**If you are second, or the opponent opens with something else,** play to the next winning total if you can (`(6 - total) mod 11`, as long as it is between 1 and 10). If the total is already on a winning number for them, play small and hope they slip.

## Example

```text
You: 6   -> 6
Opp: 3   -> 9     You: 8 -> 17
Opp: 10  -> 27    You: 1 -> 28
Opp: 5   -> 33    You: 6 -> 39
Opp: 7   -> 46    You: 4 -> 50  win
```

General rule as code, in case they ask you to write the bot:

```java
static int bestMove(int total, int target, int maxPick) {
    int m = maxPick + 1;
    int move = Math.floorMod(target - total, m);   // distance to next winning total
    return (move >= 1 && move <= maxPick) ? move : 1;  // 0 means we are in a losing spot
}
// bestMove(0, 50, 10) == 6
```

## Pitfalls and follow-ups

- **General version:** target `T`, picks from 1 to `M`. Winning totals are `T mod (M+1)` plus multiples of `M+1`. First player wins unless `T` is a multiple of `M+1`.
- **"Whoever reaches or passes 50 wins"** is the same game here, because you can only pass 50 from 40 or above, where 50 itself is reachable.
- **"Whoever says 50 loses" (misère):** aim for 49 instead, so winning totals are `49 mod 11 = 5`; open with 5.
- **What is this testing?** Backward induction and spotting the invariant. Say the backwards reasoning out loud; that is what earns the credit, more than the number.
