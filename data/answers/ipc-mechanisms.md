**Short answer:** Pipes, Unix domain sockets, TCP sockets and message queues all copy data through the kernel and usually wake the receiver with a syscall, so each message costs microseconds. Shared memory maps the same physical pages into both processes; after setup, data moves with ordinary loads and stores and no kernel involvement, so a lock-free ring buffer in shared memory with busy polling gets latency down to roughly the cost of moving a cache line between cores (well under a microsecond). That is why trading systems use shared-memory queues between processes on the same host, and sockets or multicast between hosts.

## Explanation

| Mechanism | Scope | Data path | Notes |
|---|---|---|---|
| Anonymous pipe | related processes | kernel buffer, 2 copies | byte stream, one direction |
| Named pipe (FIFO) | same host | same | has a filesystem name |
| Unix domain socket | same host | kernel, 2 copies | bidirectional, stream or datagram, can pass file descriptors |
| TCP / UDP loopback | same host or network | full network stack | slower than UDS on one host; only option across hosts |
| POSIX / System V message queue | same host | kernel copy | message boundaries, priorities |
| Shared memory (`shm_open` + `mmap`) | same host | no copy after setup | you must build synchronisation yourself |
| Signals | same host | none | notification only, tiny payload |
| Memory-mapped file | same host | page cache | shared memory that can persist |

**Why kernel-based IPC is slower:** each send and receive is a syscall (mode switch, possible cache and TLB effects), data is copied user -> kernel -> user, and a blocked receiver must be woken by the scheduler, which can cost several microseconds on its own.

**Why shared memory is fastest:** the only cost is cache coherence: the consumer's core pulls the line the producer wrote. With both processes pinned to cores on the same socket and spinning, a hand-off is typically on the order of 100 ns. It costs CPU (spinning cores) and complexity (memory ordering, crash recovery, layout must be identical on both sides - no pointers, only offsets).

**Synchronisation options for shared memory:** spin on atomics (lowest latency), process-shared mutex/condition variable (`PTHREAD_PROCESS_SHARED`), semaphores, or `futex`, or `eventfd` to wake a sleeping consumer.

## Example

Single-producer single-consumer ring in shared memory:

```cpp
struct alignas(64) Ring {
    alignas(64) std::atomic<uint64_t> head{0};   // written by producer
    alignas(64) std::atomic<uint64_t> tail{0};   // written by consumer
    Msg slots[1024];                             // power of two
};

int fd = shm_open("/md_ring", O_CREAT | O_RDWR, 0600);
ftruncate(fd, sizeof(Ring));
auto* r = static_cast<Ring*>(mmap(nullptr, sizeof(Ring),
              PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0));
// Producer creates it with placement new: new (r) Ring{};

bool push(Ring* r, const Msg& m) {
    auto h = r->head.load(std::memory_order_relaxed);
    if (h - r->tail.load(std::memory_order_acquire) == 1024) return false; // full
    r->slots[h & 1023] = m;
    r->head.store(h + 1, std::memory_order_release);   // publish
    return true;
}
```

The `release` store on `head` makes the slot contents visible before the new head; the consumer reads `head` with `acquire`.

## Pitfalls and follow-ups

- **`std::atomic` in shared memory** works across processes only if it is lock-free (`is_always_lock_free`).
- **Crash of one side** can leave a mutex locked; robust mutexes (`PTHREAD_MUTEX_ROBUST`) or lock-free designs avoid that.
- **Pipes vs Unix sockets?** Sockets are bidirectional, support datagrams and FD passing; pipes are simpler.
- **Measuring IPC latency:** ping-pong between two pinned processes, take the round trip with `rdtsc` or `clock_gettime`, report percentiles, not just the mean.
- **Java comparison:** Java reaches shared memory through `FileChannel.map` (memory-mapped files) or the Foreign Function & Memory API (final in Java 22); libraries such as Chronicle Queue and Aeron IPC use this.
