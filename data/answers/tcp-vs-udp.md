**Short answer:** TCP is connection-oriented and gives a reliable, ordered byte stream with flow control and congestion control, at the cost of a handshake, retransmission delays and head-of-line blocking. UDP sends independent datagrams with no connection, no ordering, no retransmission and no congestion control: lower and more predictable latency, message boundaries preserved, and it supports multicast. Use TCP when every byte must arrive in order (HTTP/1.1 and HTTP/2, databases, order entry); use UDP when freshness beats completeness or when the application adds its own reliability (market data, DNS, video, QUIC).

## Explanation

| | TCP | UDP |
|---|---|---|
| Connection | 3-way handshake, teardown | none |
| Reliability | ACKs, retransmission | none |
| Ordering | in-order byte stream | each datagram independent |
| Boundaries | none (stream; app must frame messages) | preserved |
| Flow / congestion control | yes | no (app's job) |
| Header | 20-60 bytes | 8 bytes |
| One-to-many | no | broadcast and multicast |

**Head-of-line blocking:** one lost TCP segment holds back all later data until it is retransmitted, even if the application could have used it. For a price feed, a stale price delivered late is worse than useless.

**Throughput vs bandwidth vs latency**
- **Bandwidth:** the link's maximum capacity (bits per second).
- **Throughput:** what you actually achieve, limited by protocol overhead, loss, congestion and window size (TCP throughput is at most window / RTT).
- **Latency:** time for one bit/message to get from A to B (propagation + transmission + queuing + processing). A satellite link can have high bandwidth and high latency.

**Process vs thread** (also asked here): a process has its own address space; threads share their process's memory and have their own stacks and registers. **Virtual memory:** each process sees its own address space; the MMU translates virtual pages to physical frames through page tables, giving isolation and letting the OS page to disk.

## Example

```cpp
// UDP: one datagram in, one out, no connection
int s = socket(AF_INET, SOCK_DGRAM, 0);
sendto(s, buf, len, 0, (sockaddr*)&dst, sizeof dst);

// TCP: connect, then a byte stream - frame messages yourself (length prefix)
int t = socket(AF_INET, SOCK_STREAM, 0);
connect(t, (sockaddr*)&srv, sizeof srv);
int one = 1;
setsockopt(t, IPPROTO_TCP, TCP_NODELAY, &one, sizeof one);  // disable Nagle
send(t, frame, frameLen, 0);   // may be split or merged on the receiving side
```

## Pitfalls and follow-ups

- **Walk through the TCP three-way handshake.** Client sends `SYN` with its initial sequence number `x`. Server replies `SYN-ACK` with its own ISN `y` and `ack = x+1`. Client sends `ACK` with `ack = y+1`; data can follow (even on this ACK). Both sides now agree on sequence numbers and options (MSS, window scaling, SACK). Random ISNs prevent old segments and spoofing from being accepted. Teardown is `FIN`/`ACK` in each direction; the side that closes first sits in `TIME_WAIT` for 2 x MSL.
- **Why are market-data feeds UDP multicast?** One packet from the exchange reaches every subscriber at once; the network switches replicate it, so all participants get it at the same time and the sender's cost does not grow with N. There is no handshake, no retransmission stall (no head-of-line blocking), and no congestion control slowing a burst. Loss is handled by the app: sequence numbers, A/B redundant feeds on separate networks, and a separate retransmission or snapshot service.
- **Is UDP faster?** The protocol overhead is smaller, but the main win is avoiding stalls from retransmission and ordering, not raw speed.
- **Can you get reliability over UDP?** Yes: QUIC (HTTP/3) adds reliability, encryption and per-stream ordering on top of UDP.
- **TCP is a stream:** one `send` may arrive as two `recv`s or merged with the next; always frame messages.
- **Java comparison:** `Socket`/`SocketChannel` for TCP, `DatagramSocket`/`DatagramChannel` and `MulticastSocket` for UDP.

Networking basics: [S0 · Foundations](../academy/lessons/S0.md).
