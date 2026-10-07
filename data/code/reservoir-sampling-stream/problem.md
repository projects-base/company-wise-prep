Numbers arrive one at a time on a stream whose length you do not know in advance; it may as well be infinite. At any moment someone may stop the stream and ask for **one element chosen uniformly at random from everything seen so far**: each of the `n` elements received must be returned with probability exactly `1/n`. You may use only **O(1) extra memory**, so you cannot store the stream.

Implement the class `StreamSampler`:

- `StreamSampler(Random rng)`: use `rng` for all of your randomness.
- `void add(int x)`: the next element arrives.
- `int sample()`: return a uniformly random element among all elements added so far. It is only called after at least one `add`, and it may be called many times, between any two `add` calls.

**How it is tested**: the result is random, so the harness runs many independent trials. Each trial creates a new sampler, feeds the stream, and calls `sample()` at the given checkpoints, meaning after the first `p` elements have arrived. For each checkpoint the harness checks that every returned value really is one of the first `p` elements, and runs a chi-square test of uniformity. The test is very lenient: a correct solution fails it with probability below one in a billion. Each checkpoint prints `"uniform"`, or `"valid"` when there are too few trials for a statistical test (then only membership is checked). Positions count, not values: if a value appears twice among the first `p` elements, it should come back twice as often.

Input lines: the stream (a list of values, or a number N meaning the stream 0, 1, …, N−1), the checkpoints, the number of trials, and a seed for `rng`.

**Example 1**
Input: stream = [10,20,30,40], checkpoints = [1,2,4], trials = 20000, seed = 1
Output: ["uniform","uniform","uniform"]

**Example 2**
Input: stream = [7,7,9], checkpoints = [3], trials = 30000, seed = 2
Output: ["uniform"]
Why: after 3 elements, 7 must come back about 2/3 of the time and 9 about 1/3.

**Constraints**
- up to 10⁸ calls to `add` in a single trial. That hidden test runs one trial over a stream of 100 million elements, and storing the stream runs out of memory.
- `sample()` is called only after at least one `add`

**Notes**: the classic answer is *reservoir sampling*. When the i-th element arrives (counting from 1), it replaces the current choice with probability 1/i.
