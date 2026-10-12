**Short answer:** Bulb `k` is toggled once on every pass `i` that divides `k`, so its final state depends on how many divisors it has. Divisors come in pairs (`d` and `k/d`), except when `k` is a perfect square, where `√k` pairs with itself. So only perfect squares have an odd number of divisors and stay on. For 100 bulbs that is 1, 4, 9, …, 100: **10 bulbs**. In general the answer is `⌊√n⌋`.

## Picture it

`n = 10`. Pass `i` toggles bulbs `i, 2i, 3i, …`. Read each column down: a bulb flips once per divisor.

| Bulb | Divisors (passes that toggle it) | Toggles | Final |
|---|---|---|---|
| 1 | 1 | 1 | on |
| 2 | 1, 2 | 2 | off |
| 3 | 1, 3 | 2 | off |
| 4 | 1, 2, **4** | 3 | on |
| 5 | 1, 5 | 2 | off |
| 6 | 1, 2, 3, 6 | 4 | off |
| 7 | 1, 7 | 2 | off |
| 8 | 1, 2, 4, 8 | 4 | off |
| 9 | 1, **3**, 9 | 3 | on |
| 10 | 1, 2, 5, 10 | 4 | off |

On: 1, 4, 9, so `⌊√10⌋ = 3`. For 12 the pairs are (1,12), (2,6), (3,4): even. For 16 they are (1,16), (2,8), (4,4): the 4 counts once, so odd.

**The picture in one sentence:** a bulb ends on only if it was toggled an odd number of times, and only perfect squares have an odd number of divisors.

## Approach

**Brute force (simulate).** Keep a `boolean[n + 1]`. For each pass `i` from 1 to `n`, flip every multiple of `i`. The inner loops add up to `n/1 + n/2 + … + n/n`, which is O(n log n). Fine for 100, too slow for `n = 10⁹`, and it misses what the puzzle is really testing.

**Key insight.** Ask "how many times is bulb `k` touched?" instead of "what does pass `i` do?". The answer is the number of divisors of `k`. All bulbs start off, so odd toggles means on. Divisors pair up as `d · (k/d) = k`; the only unpaired divisor is `√k`, which exists only for perfect squares.

**Optimal.** Count the perfect squares `≤ n`. That is `⌊√n⌋`, an O(1) calculation.

Say it out loud in this order: simulate a small case, notice 1, 4, 9 stay on, then explain why with divisor pairs. Interviewers in a puzzle round want the reasoning, not just "10".

## Solution

```java
class Solution {
    public int bulbSwitch(int n) {
        int r = (int) Math.sqrt(n);
        // Guard against floating-point rounding at the edges of large perfect squares.
        while ((long) r * r > n) r--;
        while ((long) (r + 1) * (r + 1) <= n) r++;
        return r;
    }

    // Simulation, useful to show the pattern before proving it.
    int simulate(int n) {
        boolean[] on = new boolean[n + 1];
        for (int pass = 1; pass <= n; pass++)
            for (int b = pass; b <= n; b += pass) on[b] = !on[b];
        int count = 0;
        for (int b = 1; b <= n; b++) if (on[b]) count++;
        return count;
    }
}
```

## Complexity

- **Time:** O(1) for the formula (one `sqrt` and at most a couple of correction steps). The simulation is O(n log n), from the harmonic sum.
- **Space:** O(1) for the formula; O(n) for the simulation array.

## Edge cases

- `n = 0`: no bulbs, answer 0.
- `n = 1`: bulb 1 is toggled once, answer 1.
- Large `n` near `Integer.MAX_VALUE`: `Math.sqrt` on a `double` is exact enough here, but the correction loops and `long` multiplication keep it safe.

## Variations

- **Which bulbs are on?** Print `i * i` for `i = 1 … ⌊√n⌋`.
- **Bulbs start on instead of off:** the on/off result flips, so `n − ⌊√n⌋` are on.
- **Only passes 1 to m run (m < n):** a bulb is toggled once per divisor `≤ m`; there is no closed form, so simulate or count divisors.
- **The "100 lockers" puzzle** is the same problem with doors instead of bulbs.

See [H2 · Interview technique: think aloud, test, recover](../academy/lessons/H2.md) for working a puzzle out loud from small cases.
