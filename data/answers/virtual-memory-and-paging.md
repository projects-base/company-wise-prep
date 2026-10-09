**Short answer:** Every process sees its own large, private virtual address space. The CPU's MMU translates each virtual address to a physical RAM address in fixed-size pages (usually 4 KB) using per-process page tables, with the TLB caching recent translations. A page does not have to be in RAM: if it is not present, the access raises a page fault and the OS loads it from disk or SSD (the file it maps, or swap), possibly evicting another page. This gives isolation between processes, lets memory be shared and copy-on-write, and lets programs use more memory than RAM - at the cost of very slow accesses when pages are not resident.

## Explanation

**Address split.** With 4 KB pages, a virtual address = virtual page number (VPN) + 12-bit offset. Translation replaces the VPN with a physical frame number (PFN); the offset is unchanged.

**Page tables.** On x86-64 a 4-level tree (PML4 -> PDPT -> PD -> PT, 9 bits of index each, plus the 12-bit offset = 48-bit addresses; 5-level paging gives 57 bits). Multi-level tables are sparse: unused regions need no entries. Each entry holds the PFN plus flags: present, writable, user/kernel, accessed, dirty, no-execute. CR3 points to the current process's top-level table; switching processes switches CR3.

**TLB.** Walking four levels per access would be too slow, so the TLB caches VPN -> PFN. A hit costs almost nothing; a miss costs a page walk (several memory reads, often cached). Huge pages (2 MB, 1 GB) cover more memory per entry.

**Page faults**
- *Minor*: the page is in RAM but not mapped yet (first touch of allocated memory, shared library already in page cache, COW).
- *Major*: data must be read from storage - from the file for a memory-mapped file or code, or from **swap** for anonymous memory that was paged out.
- *Invalid*: no valid mapping -> segmentation fault.

**RAM, SSD and disk.** Think of RAM as a cache of pages whose backing store is disk. Approximate costs: RAM access ~100 ns; NVMe SSD read ~tens to ~100 µs; spinning disk seek ~several ms. A major fault is therefore thousands to tens of thousands of times slower than a RAM access. When the working set exceeds RAM, the system **thrashes**: it spends its time paging. The OS picks victims with approximations of LRU (clock / active and inactive lists); dirty pages must be written back first.

**Physical vs virtual addressing.** User programs only ever see virtual addresses. Physical addresses are used by the MMU, caches (L2/L3 are physically indexed), DMA devices (through an IOMMU in modern systems) and the kernel's own mappings.

## Example

```text
VA 0x00007f3a_1234_5678 (4 KB pages)
  offset = 0x678
  VPN    = 0x7f3a12345 -> PML4[0x0fe] -> PDPT[0x0e8] -> PD[0x091] -> PT[0x145]
  PTE: present=1, PFN=0x1a2b3 -> PA = 0x1a2b3678
  TLB caches VPN 0x7f3a12345 -> PFN 0x1a2b3
```

```java
// The JVM sees the same thing: -Xmx reserves virtual space, RSS grows as pages are touched.
// -XX:+AlwaysPreTouch touches every heap page at startup to avoid page faults later.
```

## Pitfalls and follow-ups

- **Virtual memory is not "swap".** Virtual memory is the address translation; swap is one optional backing store.
- **Why is `malloc(1 GB)` instant?** Only virtual space is reserved; physical pages are allocated on first touch (overcommit on Linux).
- **Page size trade-off:** bigger pages mean fewer TLB misses and smaller tables, but more internal fragmentation and costlier COW.
- **Segmentation vs paging?** Segmentation uses variable-size regions (external fragmentation); x86-64 in long mode effectively uses flat segments plus paging.
- **Latency-sensitive services** lock memory (`mlockall`), pre-fault, disable swap and use huge pages to avoid faults in the hot path.

Go deeper: [A2 · JVM memory areas](../academy/lessons/A2.md) for how the JVM uses its address space.
