**Short answer:** The front end fetches and decodes `mov rax, [rbx+8]` into a load micro-op, which is renamed and waits in the scheduler until `rbx` is ready. The address generation unit computes the virtual address; the TLB translates it to a physical address while the L1 data cache is indexed in parallel. On an L1 miss the request goes through L2, then L3 (which also handles coherence with other cores), then the memory controller, which opens the right DRAM row and returns a 64-byte line. The line fills the caches, the value is forwarded to `rax`, and dependent instructions wake up. Out of order execution keeps doing other work meanwhile.

## Explanation

Step by step on a typical x86-64 core:

1. **Fetch and decode.** The instruction is fetched from the L1 instruction cache (via the iTLB), decoded into micro-ops (or taken from the micro-op cache). For a plain load it is one load micro-op.
2. **Rename and schedule.** Architectural `rax` gets a fresh physical register. The micro-op enters the reservation station and a slot in the *load buffer*.
3. **Address generation.** When `rbx` is available, the AGU computes `rbx + 8`. The segment base is zero in 64-bit mode for normal data.
4. **Store-to-load forwarding check.** The store buffer is searched for an older store to the same address that has not yet reached the cache. If one matches, its data is forwarded directly.
5. **Translation.** The L1 dTLB maps virtual page -> physical frame. A miss goes to the L2 TLB (STLB). A miss there starts a **page walk** through the 4- or 5-level page tables (CR3 -> PML4 -> PDPT -> PD -> PT), whose entries are themselves cached in L1/L2/L3 and in paging-structure caches. A non-present page raises a **page fault** and the OS takes over.
6. **L1D lookup.** Indexed by page-offset bits in parallel with translation, tag compared with the physical address. Hit: about 4-5 cycles total.
7. **Miss path.** A *line fill buffer* entry tracks the miss. L2 (about 12-15 cycles), then L3 / the last-level cache (about 40-70 cycles). The LLC or a snoop filter also checks whether another core has the line Modified and fetches it from there if so.
8. **Memory controller.** Queues the request, maps the physical address to channel, rank, bank, row, column; activates the row (RAS), reads the column (CAS) and bursts out 64 bytes. Total is around 80-100+ ns.
9. **Fill and complete.** The line is installed in the caches with a MESI state, the requested 8 bytes go to the physical register, waiting micro-ops become ready, and the load retires in program order from the reorder buffer.

Hardware prefetchers watch the miss pattern and may already have fetched the line, which is why sequential access is much faster than random.

## Example

```text
mov rax, [rbx+8]
 decode -> load uop -> rename -> RS -> AGU (VA = rbx+8)
   -> store buffer?  -> dTLB/STLB (PA)   || L1D set index
   -> L1D hit? 4-5 cyc
   -> L2 ~14 cyc -> L3 ~50 cyc (+coherence) -> IMC -> DRAM ~100 ns
   -> fill L3/L2/L1, write rax, wake dependents, retire
```

## Pitfalls and follow-ups

- **What if the address is uncacheable (MMIO)?** Memory type comes from the page tables and MTRRs; UC accesses bypass caches and are not speculated.
- **Memory ordering on x86?** Loads are not reordered with older loads as seen by other cores (TSO), although the core may execute them early speculatively and re-check (memory-ordering machine clear).
- **Cache line split?** A load crossing a 64-byte boundary needs two cache accesses; crossing a page needs two translations.
- **Why can one miss be hidden?** Out of order execution plus several fill buffers allow multiple outstanding misses (memory-level parallelism).
- **Numbers vary by CPU** - quote ranges.
- **Java comparison:** a field read in Java is the same `mov` after JIT; pointer-heavy object graphs cause the chain of dependent misses that this path makes expensive.
