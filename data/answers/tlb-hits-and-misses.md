**Short answer:** Convert each address to its virtual page number (`addr >> log2(pageSize)`, or `addr / pageSize`), then treat the TLB as a fixed-size cache of page numbers. If the page is in the TLB it is a hit (and, under LRU, becomes most recently used); otherwise it is a miss and the page is inserted, evicting the least recently used entry when full. An LRU map (hash map + doubly linked list) makes each access O(1). Confirm the replacement policy (LRU vs FIFO) and whether the TLB is fully associative or set-associative before coding.

## Explanation

**Inputs to clarify**
- Page size (e.g. 4096), TLB entries (e.g. 4), the address list (hex or decimal).
- Replacement policy: LRU is the usual default; FIFO is the other common choice. With FIFO, a hit does **not** change the order.
- Associativity: fully associative unless told otherwise. For an `S`-set TLB, set = `vpn % S`, and each set is its own small LRU of `entries / S` ways.
- Process ids / ASIDs: if addresses come with a pid, the key is `(pid, vpn)`; a context switch without ASIDs means flushing the TLB.

**Algorithm (fully associative LRU)**
1. `vpn = addr / pageSize`. The offset is irrelevant for hit/miss.
2. If `vpn` in map: hit; move its node to the front.
3. Else: miss; if size == capacity, remove the back node and its map entry; insert `vpn` at the front.

## Example

```cpp
#include <cstdint>
#include <list>
#include <unordered_map>
#include <vector>

struct TlbStats { int hits = 0, misses = 0; };

TlbStats simulate(const std::vector<uint64_t>& addrs, uint64_t pageSize, size_t entries) {
    std::list<uint64_t> lru;                                   // front = most recent
    std::unordered_map<uint64_t, std::list<uint64_t>::iterator> pos;
    TlbStats s;
    for (uint64_t a : addrs) {
        uint64_t vpn = a / pageSize;
        if (auto it = pos.find(vpn); it != pos.end()) {
            ++s.hits;
            lru.splice(lru.begin(), lru, it->second);          // move to front, O(1)
        } else {
            ++s.misses;
            if (lru.size() == entries) {
                pos.erase(lru.back());
                lru.pop_back();
            }
            lru.push_front(vpn);
            pos[vpn] = lru.begin();
        }
    }
    return s;
}
```

Trace: page size 4096, 2 entries, addresses `0x1000, 0x1FFF, 0x2000, 0x3000, 0x1004`.

```text
0x1000 vpn 1  miss  [1]
0x1FFF vpn 1  hit   [1]
0x2000 vpn 2  miss  [2,1]
0x3000 vpn 3  miss  [3,2]   evict 1
0x1004 vpn 1  miss  [1,3]   evict 2
hits 1, misses 4
```

Complexity: O(1) per access on average, O(n) total; space O(entries).

## Pitfalls and follow-ups

- **Using the full address as the key:** every access becomes a miss. Strip the offset.
- **FIFO variant:** keep a queue; on a hit do nothing. Easy to get wrong by reordering on hit.
- **Belady's anomaly:** with FIFO, more entries can sometimes give more misses; LRU never does.
- **Page faults vs TLB misses:** a TLB miss triggers a page walk; only if the page is not present in the page table is it a page fault. If asked, simulate both levels (TLB, then page table / physical frames).
- **Why do TLBs matter?** A miss costs a page walk of up to 4-5 memory reads (partly cached). Huge pages (2 MB) let one entry cover 512x more memory.
- **Java comparison:** in Java, `LinkedHashMap` with `accessOrder = true` and `removeEldestEntry` is an LRU in a few lines.

Related: [E6 · LLD case studies (LRU cache)](../academy/lessons/E6.md).
