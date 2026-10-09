**Short answer:** The `i`-th element becomes the new maximum exactly when it is the largest of the first `i` elements, which happens with probability `1/i`. By linearity of expectation the expected number of updates is `1 + 1/2 + 1/3 + ... + 1/n = H_n`, the `n`-th harmonic number, which is about `ln n + 0.577`. If the first assignment (`max = a[0]`) does not count as an update, subtract one: `H_n - 1`.

## Explanation

Define an indicator `X_i = 1` if position `i` (1-based) updates the running maximum, else 0. The number of updates is `X = X_1 + ... + X_n`.

`X_i = 1` exactly when `a[i]` is the largest among `a[1..i]`. In a uniformly random permutation, the first `i` elements are a uniformly random set in a uniformly random order, so each of them is equally likely to be the largest: `P(X_i = 1) = 1/i`.

Linearity of expectation works even though the `X_i` are not obviously independent (they are in fact independent here, but you do not need that):

```text
E[X] = sum_{i=1..n} E[X_i] = sum_{i=1..n} 1/i = H_n
```

Values: `n = 1 -> 1`, `n = 2 -> 1.5`, `n = 3 -> 1.83`, `n = 10 -> 2.93`, `n = 1000 -> about 7.49`.

Sanity check for `n = 3`. All 6 permutations of {1,2,3}, counting the first element as an update:

```text
123 -> 3    132 -> 2    213 -> 2
231 -> 2    312 -> 1    321 -> 1      total 11, mean 11/6 = 1.83 = 1 + 1/2 + 1/3
```

## Example

A quick simulation to confirm in an interview, if asked to code it:

```cpp
#include <algorithm>
#include <numeric>
#include <random>
#include <vector>

double simulate(int n, int trials) {
    std::mt19937 rng(42);
    std::vector<int> a(n);
    std::iota(a.begin(), a.end(), 0);
    long long updates = 0;
    for (int t = 0; t < trials; ++t) {
        std::shuffle(a.begin(), a.end(), rng);
        int mx = -1;
        for (int x : a) if (x > mx) { mx = x; ++updates; }
    }
    return double(updates) / trials;   // ~ H_n
}
```

## Pitfalls and follow-ups

- **Why does it matter for engineers?** It is the expected number of writes in a "track the max" loop and the analysis behind the *hiring problem* (CLRS). It grows only logarithmically, so updating a shared "best price" variable on a new maximum is rare on random input.
- **Variance?** Because the indicators are independent, `Var = sum (1/i)(1 - 1/i) = H_n - H_n^(2)`, where `H_n^(2) = sum 1/i^2`.
- **Expected number of "records" from the right?** Same, `H_n`, by symmetry.
- **Probability of exactly one update** (the max is first): `1/n`.
- **Common mistake:** trying to compute the distribution directly (that gives Stirling numbers of the first kind). Indicators plus linearity is the intended trick.
