**Short answer:** Modern CPUs are deeply pipelined, so at every branch they guess the outcome and keep executing speculatively. A wrong guess flushes the pipeline and costs roughly 15-20 cycles on current x86 cores. `__builtin_expect` (or C++20 `[[likely]]` / `[[unlikely]]`) does not change the hardware predictor; it tells the compiler which path is hot so it lays out code with the hot path as the straight-line fall-through and moves the cold path out of the way. The bigger wins usually come from removing unpredictable branches altogether.

## Explanation

- **The hardware predictor** learns from history (per-branch and global patterns). Loops and branches that almost always go one way are predicted nearly perfectly. Data-dependent branches on random data (around 50/50) are the expensive ones.
- **What `__builtin_expect(expr, value)` does:** it returns `expr` and records that `expr` is probably `value`. GCC and Clang use it for block layout: the likely path falls through, the unlikely one is placed later or in a cold section. That improves instruction cache use and avoids a taken jump on the hot path. Compilers generally do not emit x86 branch-hint prefixes for it, so the effect is through layout, not hints.
- **When it helps:** error handling and rare cases in a hot path, for example "is this a recoverable market-data gap?" checked on every packet.
- **When it hurts:** if your hint is wrong, you have made the common case slower. Measure; profile-guided optimisation (`-fprofile-generate` / `-fprofile-use`) gives the compiler real data and often beats hand hints.

## Example

```cpp
#define LIKELY(x)   __builtin_expect(!!(x), 1)
#define UNLIKELY(x) __builtin_expect(!!(x), 0)

void onPacket(const Packet& p) {
    if (UNLIKELY(p.seq != expectedSeq_)) {   // gap: rare
        handleGap(p);
        return;
    }
    apply(p);                                 // hot path falls through
    ++expectedSeq_;
}

// C++20 equivalent
if (p.seq != expectedSeq_) [[unlikely]] { handleGap(p); return; }
```

Removing a branch that the predictor cannot learn:

```cpp
// branchy: ~50% mispredicts on random data
for (int x : v) if (x >= 128) sum += x;

// branchless: the compiler can emit cmov or vectorise
for (int x : v) sum += (x >= 128) ? x : 0;
// or: sum += x & -(x >= 128);
```

Sorting the data first also makes the branchy version fast, because the outcome becomes a long run of false then a long run of true.

## Pitfalls and follow-ups

- **Does the hint change the branch predictor?** No, it changes code layout (and sometimes whether the compiler chooses cmov vs a branch).
- **Branchless is always faster?** No. For a well-predicted branch, a branch is cheap and a cmov adds a data dependency. Use it when the branch is unpredictable.
- **How do you measure mispredicts?** `perf stat -e branches,branch-misses ./app`.
- **Other tricks:** lookup tables instead of switch chains, moving checks out of the loop, making the hot loop handle the common case and batching rare cases.
- **Java comparison:** you have no `__builtin_expect`; the JIT uses runtime profiling (branch counts) to lay out hot paths, which is effectively built-in PGO.
