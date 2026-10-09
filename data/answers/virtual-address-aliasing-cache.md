**Short answer:** Mapping several virtual pages to the same physical page is normal and allowed: shared memory between processes, `mmap` of the same file twice, copy-on-write before a write, and the "double-mapped ring buffer" trick all do it. These aliases are called *synonyms*. On x86 and most modern CPUs, you will **not** read stale data: the caches are physically tagged (L2/L3 also physically indexed, and L1 is virtually indexed only with page-offset bits, which are identical for every alias), so both virtual addresses land on the same cache line. Stale data is a real risk only on caches indexed or tagged with virtual address bits beyond the page offset (VIVT, or VIPT larger than page size x ways), where the OS must flush or use page colouring.

## Explanation

**How the L1 avoids the problem.** A typical L1D is *virtually indexed, physically tagged* (VIPT). With 4 KB pages, the low 12 bits of virtual and physical addresses are equal. If the set index and line offset fit in those 12 bits (64 sets x 64-byte lines), any two aliases select the same set, and the tag compare uses the physical address. So there is exactly one copy of the line, and a write via VA1 is seen by a read via VA2. That constraint is why L1 size is roughly `4 KB x associativity` (e.g. 48 KB = 12 ways).

**When aliasing breaks things**
- **VIVT caches** (some older ARM and MIPS designs): the same physical line can live in two places under two virtual tags; a write through one alias leaves the other stale. The OS must flush on mapping changes, or restrict aliases to the same "colour" (same index bits).
- **VIPT caches bigger than page size x ways:** some index bits come from the virtual page number, so two aliases can map to different sets. Hardware may detect and handle it, or the OS uses page colouring.
- **Instruction cache:** writing code through one mapping and executing through another needs explicit cache maintenance on ARM (x86 keeps I-cache coherent with stores).

**Other layers to mention**
- **Store buffer:** on x86, a load that follows a store to the same physical address through a different virtual address still gets the right value; the memory-disambiguation logic detects the conflict (possibly with a pipeline clear, which costs performance, not correctness). Loads whose addresses differ by a multiple of 4 KB can falsely appear to conflict (4K aliasing), a known performance effect.
- **TLB:** each alias has its own TLB entry; both translate to the same frame.
- **Compiler:** C++ compilers assume two pointers of compatible type may alias unless they can prove otherwise, so they reload after a store. Using `__restrict` on aliased pointers would be a lie and can produce stale register values.
- **Across cores:** coherence (MESI) works on physical lines, so aliases on different cores are kept coherent too.

## Example

The double-mapped ring buffer: map the same physical buffer twice, back to back, so a read or write that runs past the end continues at the start without wrap-around logic.

```cpp
int fd = memfd_create("ring", 0);
ftruncate(fd, N);                                        // N = multiple of page size
auto* base = (char*)mmap(nullptr, 2 * N, PROT_NONE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
mmap(base,     N, PROT_READ | PROT_WRITE, MAP_SHARED | MAP_FIXED, fd, 0);
mmap(base + N, N, PROT_READ | PROT_WRITE, MAP_SHARED | MAP_FIXED, fd, 0);

base[5] = 'x';
assert(base[N + 5] == 'x');   // same physical byte, same cache line on x86
```

## Pitfalls and follow-ups

- **Homonyms** are the opposite problem: the same virtual address meaning different physical pages in different processes. Physically tagged caches are immune; TLBs use ASIDs/PCIDs or flush on context switch.
- **Can DMA make cache data stale?** On x86 DMA is cache-coherent; on some embedded systems drivers must flush or invalidate.
- **Why do OSes still care about aliases on x86?** Performance (page colouring for cache spreading), and the TLB cost of extra mappings.
- **Java comparison:** two `MappedByteBuffer`s over the same file region see each other's writes for the same hardware reasons; cross-thread visibility still needs proper memory ordering.
