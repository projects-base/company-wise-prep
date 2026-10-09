**Short answer:** Caches are small SRAM memories close to the core that hold recently used 64-byte lines. L1 is per core, split into instruction and data caches of about 32-64 KB each, and answers in about 4-5 cycles. L2 is per core, roughly 256 KB to 2 MB, about 12-15 cycles. L3 is shared by all cores, several MB to tens of MB, about 40-70 cycles. RAM is around 100 ns (hundreds of cycles). L1 is fast *because* it is small and physically next to the load units; making it bigger would make every access slower.

## Explanation

**Why L1 is fast:**
- Short wires and few entries to search. Access time grows with capacity (longer word lines and bit lines, more tag comparisons).
- It is *virtually indexed, physically tagged* (VIPT): the set index comes from the page-offset bits, so the cache lookup runs in parallel with the TLB lookup.
- Built from fast, power-hungry SRAM cells with many ports so it can serve several loads and a store per cycle.

**Why not make L1 bigger:**
1. **Latency.** Every load pays L1 latency. Going from 4 to 6 cycles slows nearly all code, which costs more than the extra hit rate gains.
2. **The VIPT limit.** With 4 KB pages there are 12 offset bits. 64-byte lines use 6, leaving 6 bits = 64 sets. So capacity = 64 sets x 64 B x ways = 4 KB x associativity. A 48 KB L1D needs 12-way associativity; growing further means more ways, which means more tag comparators and more power on every access.
3. **Area and power.** L1 sits inside the core and is replicated per core.

The hierarchy is the compromise: a tiny fast level, then larger slower ones, exploiting temporal and spatial locality.

**Threads and caches.** Two hardware threads on the same core (SMT / Hyper-Threading) share that core's L1 and L2. Threads on different cores have private L1/L2 kept coherent by a protocol such as MESI: writing a line invalidates other cores' copies. This produces **false sharing**: two threads writing different variables on the same 64-byte line keep stealing the line from each other.

## Example

```cpp
// False sharing: both counters on one cache line
struct Counters { std::atomic<long> a, b; };

// Fix: put each on its own line
struct alignas(64) Padded { std::atomic<long> v; };
Padded counters[2];
// C++17 also offers std::hardware_destructive_interference_size
```

Traversal order matters too: iterating a 2D array row by row uses every byte of each fetched line; column by column on a large matrix misses on nearly every access.

## Pitfalls and follow-ups

- **Exact numbers vary by CPU.** Give ranges and say "on a recent x86 server core" rather than one precise figure.
- **Inclusive vs exclusive L3?** Some designs keep a copy of all L1/L2 lines in L3 (inclusive), others do not; it affects effective capacity and snoop filtering.
- **How do you measure misses?** `perf stat -e cache-misses,L1-dcache-load-misses ./app`.
- **What is a cache line and why 64 bytes?** The unit of transfer; large enough to amortise the DRAM burst, small enough to limit false sharing and wasted bandwidth.
- **Java comparison:** the same effects apply to the JVM; `@Contended` (with `-XX:-RestrictContended`) pads fields, and `LongAdder` spreads counts across cells to avoid contention.
