**Short answer:** Kernel bypass means the application reads and writes packets directly from the network card's memory rings in user space, instead of going through the kernel's network stack with syscalls, interrupts and copies. A dedicated core busy-polls the NIC, so there is no interrupt, no context switch, no socket buffer copy and no scheduler wake-up. That cuts per-packet latency from several microseconds to around a microsecond or less, and makes it far more predictable (lower jitter), which matters when trading strategies compete on reacting first to market data.

## Explanation

**The normal kernel path for a received packet:** NIC DMA into a kernel ring -> interrupt (or NAPI polling) -> driver builds an `sk_buff` -> IP/UDP or TCP processing -> socket receive buffer -> the blocked thread is woken by the scheduler -> `recv()` syscall copies into user memory. Each step adds latency and, worse, variance (interrupt coalescing, softirq scheduling, cache pollution from other work).

**With kernel bypass:**
- The NIC's RX/TX descriptor rings and packet buffers are mapped into the process.
- The application polls the ring in a tight loop on an isolated, pinned core (`isolcpus`, IRQ affinity away from it).
- Packets are processed in place (zero copy). The app or a user-space library does the protocol work.

**Common technologies**
- **DPDK:** user-space poll-mode drivers; you get raw packets and bring your own stack. Popular for high throughput.
- **Solarflare/AMD Onload:** a user-space TCP/UDP stack that intercepts standard socket calls (via `LD_PRELOAD`), so existing socket code gets bypass with no rewrite. **ef_vi** is the lower-level raw API for the lowest latency.
- **RDMA / InfiniBand / RoCE (verbs):** the NIC writes directly into remote memory; common in HPC and some storage.
- **AF_XDP / XDP:** Linux's in-kernel fast path; not full bypass, but packets go to user space through a shared ring with far less overhead.

**Why trading uses it**
- Market data arrives as UDP multicast bursts; the first to see a price change and react wins.
- Tail latency (p99, p99.9) matters as much as the average; the kernel's variance is the main problem.
- Often combined with: hardware timestamping on the NIC, FPGA-based NICs for the very hottest paths, CPU pinning, huge pages, no allocations in the hot path.

**Costs**
- A full core per polling thread spinning at 100% even when idle.
- You lose kernel features: firewall rules, `tcpdump` visibility (vendors provide their own capture tools), standard tooling.
- Vendor-specific hardware and APIs; more complex operations.
- Security and isolation are your problem.

## Example

```cpp
// Shape of a DPDK receive loop (simplified)
struct rte_mbuf* bufs[32];
for (;;) {                                         // pinned, isolated core
    uint16_t n = rte_eth_rx_burst(port, queue, bufs, 32);
    for (uint16_t i = 0; i < n; ++i) {
        auto* data = rte_pktmbuf_mtod(bufs[i], const uint8_t*);
        onMarketData(data, rte_pktmbuf_data_len(bufs[i]));  // parse in place
        rte_pktmbuf_free(bufs[i]);
    }
}
```

## Pitfalls and follow-ups

- **Is busy polling alone kernel bypass?** No. `SO_BUSY_POLL` reduces wake-up latency but still uses the kernel stack and syscalls.
- **Kernel bypass and TCP?** You need a user-space TCP stack (Onload, or one you build on DPDK); order entry to exchanges is usually TCP.
- **What else adds jitter?** Page faults (pre-fault and lock memory), interrupts on the trading core, frequency scaling and C-states, cache misses, logging on the hot path.
- **Zero copy vs kernel bypass?** Related but different: `sendfile` is zero copy through the kernel; bypass removes the kernel from the data path.
- **Java comparison:** Java trading systems use the same ideas (for example running the JVM under Onload, or low-latency messaging libraries such as Aeron) plus allocation-free code to avoid GC pauses.

Networking basics: [S0 · Foundations](../academy/lessons/S0.md).
