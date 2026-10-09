**Short answer:** `RDTSC` reads the CPU's timestamp counter, but it is not a serializing instruction: an out-of-order core may execute it before earlier instructions have finished or after later ones have started, so the timestamp does not mark a precise point in your code. For short measured regions that error can be as large as the thing you measure. A "serialized" read forces ordering: `RDTSCP` waits for all earlier instructions to execute before reading the counter, and a fence (`LFENCE`) or `CPUID` stops later instructions from starting early. The standard pattern is a fenced `RDTSC` at the start and `RDTSCP` followed by a fence at the end.

## Explanation

**Why ordering matters.** Modern x86 cores execute out of order across a window of hundreds of micro-ops. With plain `RDTSC`:
- the start read may slide *later*, into the measured code, or the code may start before it;
- the end read may execute *before* the measured code finishes.

Both make small intervals look shorter, longer or noisier than reality.

**The tools**
- `CPUID` is fully serializing (drains the pipeline) but slow and variable (it can cost a hundred cycles or more and may trap in a VM).
- `LFENCE` on Intel does not let later instructions begin executing until earlier ones have completed locally. `LFENCE; RDTSC` gives a cheap ordered start. On AMD this property depends on CPU/OS configuration (it is enabled on modern kernels as a Spectre mitigation).
- `RDTSCP` waits until all previous instructions have executed (and previous loads are globally visible) before reading, and also returns `IA32_TSC_AUX` (typically the CPU id) in `ECX`. It does **not** stop *later* instructions from starting before it, so follow it with `LFENCE` (or `CPUID`).

**The recommended measurement pattern** (from Intel's benchmarking guidance): serialize, read at the start; run the code; `RDTSCP` at the end, then serialize again so nothing after leaks in.

**Other things a good answer mentions**
- **Invariant TSC** (CPUID flag): on modern CPUs the TSC ticks at a constant nominal rate regardless of turbo or power states. So it measures wall time in "reference cycles", not core clock cycles. Convert using the calibrated TSC frequency.
- **Cross-core comparisons:** TSCs on different cores/sockets are usually synchronised on modern systems, but compare on one core if you can; `RDTSCP`'s `ECX` tells you if you migrated.
- **Overhead:** subtract the cost of an empty measurement, take many samples, and report a distribution (min, median, p99), not one number.
- **Why trading uses it:** `rdtsc` costs tens of cycles versus a `clock_gettime` call, so it is used for tick-to-trade latency stamps along the hot path.

## Example

```cpp
#include <x86intrin.h>
#include <cstdint>

inline uint64_t tscStart() {
    _mm_lfence();                 // earlier work done before we read
    uint64_t t = __rdtsc();
    _mm_lfence();                 // measured code cannot start before the read
    return t;
}

inline uint64_t tscEnd() {
    unsigned aux;
    uint64_t t = __rdtscp(&aux);  // waits for earlier (measured) instructions
    _mm_lfence();                 // later instructions cannot move above the read
    return t;
}

uint64_t t0 = tscStart();
onPacket(pkt);
uint64_t cycles = tscEnd() - t0;  // reference cycles; divide by TSC GHz for ns
```

## Pitfalls and follow-ups

- **Is `RDTSCP` alone enough at the start?** Not ideal: code after it can begin early. Use it at the end.
- **Fences on the hot path in production?** Each costs some cycles and reduces overlap; for coarse event stamps a plain `rdtsc` is often acceptable, and the fenced version is for micro-benchmarks.
- **Compiler reordering:** CPU fences do not stop the *compiler* from moving or deleting pure computations; use the measured code's result (or an optimisation barrier) and check the generated assembly.
- **VMs:** the TSC may be virtualised or offset; check `constant_tsc` and `nonstop_tsc` in `/proc/cpuinfo`.
- **Java comparison:** `System.nanoTime()` is the portable equivalent; on Linux it typically goes through `clock_gettime(CLOCK_MONOTONIC)`, which itself may read the TSC via the vDSO.
