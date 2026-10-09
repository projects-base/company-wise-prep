**Short answer:** After `fork()`, parent and child share every physical page, marked read-only, and each physical frame keeps a reference count. A write to a shared page triggers a page fault; the kernel copies the page into a new frame for the writer, decrements the old frame's count, and makes both mappings writable again once a frame has only one owner. So the number of physical pages is: pages before forks, plus one per first write to a still-shared page, minus frames whose count drops to zero on exit. Model it with per-process page tables and a refcount per frame.

## Explanation

The exercise usually gives a starting process with `P` pages and a list of operations: `fork(parent)`, `write(pid, page)`, maybe `exit(pid)`. The rules to encode:

1. **fork** copies the parent's page table (virtual page -> frame id) to a new pid and increments the refcount of every frame. No new physical pages.
2. **write(pid, vpage)**: look up the frame. If its refcount is 1, the process already owns it: write in place, no new page. If refcount > 1, allocate a new frame, point this process's entry at it, decrement the old frame's count. Physical pages +1.
3. **exit(pid)**: decrement every frame in its table; frames reaching 0 are freed.
4. **read** never copies.

The answer at any time is the number of frames with refcount > 0.

## Example

```cpp
#include <unordered_map>
#include <vector>

class CowSim {
    std::unordered_map<int, std::vector<int>> table_; // pid -> frame per vpage
    std::vector<int> ref_;                             // frame id -> refcount
    int live_ = 0, nextPid_ = 1;

    int newFrame() { ref_.push_back(1); ++live_; return (int)ref_.size() - 1; }

public:
    explicit CowSim(int pages) {                       // pid 0 = init
        auto& t = table_[0];
        for (int i = 0; i < pages; ++i) t.push_back(newFrame());
    }
    int fork(int parent) {
        int child = nextPid_++;
        table_[child] = table_[parent];                // share all frames
        for (int f : table_[child]) ++ref_[f];
        return child;
    }
    void write(int pid, int vpage) {
        int& f = table_[pid][vpage];
        if (ref_[f] == 1) return;                      // sole owner
        --ref_[f];
        f = newFrame();                                // private copy
    }
    void exit(int pid) {
        for (int f : table_[pid]) if (--ref_[f] == 0) --live_;
        table_.erase(pid);
    }
    int physicalPages() const { return live_; }
};
```

Trace with 3 pages: start = 3. `c = fork(0)` -> 3. `write(c, 0)` -> 4. `write(0, 0)` -> frame 0 now has refcount 1 (only the parent), no copy -> 4. `write(c, 0)` again -> 4. `exit(c)` -> its private frame and its shares go; frames 1, 2 drop to 1 -> 3.

## Pitfalls and follow-ups

- **Second writer after the first copied:** the original frame now has one owner, so no copy. Many candidates over-count here.
- **Freed frames:** reuse ids from a free list in a real allocator; the count logic is the same.
- **Why COW?** `fork()` followed by `exec()` would otherwise copy the whole address space just to throw it away.
- **How does the kernel detect the write?** Shared pages are mapped read-only in both page tables; the write causes a protection fault, and the handler sees the COW mark.
- **Huge pages:** a write may copy 2 MB instead of 4 KB, which is why fork of a large Redis process can spike memory during `BGSAVE`.
- **Complexity:** fork and exit are O(pages), write is O(1).
