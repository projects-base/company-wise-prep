**Short answer:** TCP limits how much unacknowledged data it has in flight with a congestion window (`cwnd`), separate from the receiver's flow-control window; it sends at most `min(cwnd, rwnd)`. In **slow start**, `cwnd` starts small and grows by one segment per ACK, so it doubles every round trip, until it reaches the slow-start threshold (`ssthresh`). Then **congestion avoidance** grows it by about one segment per round trip (additive increase). Three duplicate ACKs trigger **fast retransmit** of the missing segment and **fast recovery**: halve the window and continue without going back to slow start. A retransmission timeout is treated as severe congestion: `ssthresh` is halved and `cwnd` drops to one segment.

## Explanation

**The goal:** use the available bandwidth without overflowing router queues, and share fairly between flows. The classic signal of congestion is packet loss.

**Phases (Reno / NewReno, the textbook model)**

1. **Slow start.** Initial window today is usually 10 segments (RFC 6928). Each ACK adds 1 MSS, so `cwnd` doubles per RTT. Exponential, despite the name.
2. **Congestion avoidance** once `cwnd >= ssthresh`: add about `MSS * MSS / cwnd` per ACK, which is +1 MSS per RTT. This is the AIMD (additive increase, multiplicative decrease) sawtooth.
3. **Fast retransmit.** The receiver sends a duplicate ACK for each out-of-order segment. Three duplicates mean one segment was probably lost while later ones arrived. Retransmit it immediately instead of waiting for the timer.
4. **Fast recovery.** `ssthresh = cwnd / 2`, `cwnd = ssthresh` (+3 MSS for the segments known to have left the network), inflate by one MSS per further duplicate ACK, and when the new data is ACKed, deflate to `ssthresh` and continue in congestion avoidance. NewReno handles several losses in one window; SACK tells the sender exactly which blocks arrived.
5. **Timeout (RTO).** No ACKs at all means things are bad: `ssthresh = cwnd / 2`, `cwnd = 1` MSS (the "loss window"), slow start again. The RTO is computed from a smoothed RTT and its variance, with exponential back-off.

**Modern algorithms**
- **CUBIC** (Linux default): window growth is a cubic function of time since the last loss, so it ramps quickly back to the previous maximum and probes carefully around it. Better on high bandwidth-delay paths.
- **BBR** (from Google): models the bottleneck bandwidth and minimum RTT and paces sending to match, instead of reacting only to loss. Keeps queues shorter.
- **ECN:** routers mark packets instead of dropping them; the sender reacts as to a loss without the retransmission.

## Example

A trace in segments, per round trip, starting with `cwnd = 1` and `ssthresh = 16` for readability:

```text
RTT:   1  2  3  4   5   6   7 ... 13   14  15  16 ...
cwnd:  1  2  4  8  16  17  18 ... 24   12  13  14       (3 dup ACKs at 24: ssthresh=12, cwnd=12)

If an RTO fired at 24 instead:
cwnd: 24 -> 1  2  4  8  12  13  14 ...                  (ssthresh=12, slow start again)
```

## Pitfalls and follow-ups

- **Flow control vs congestion control?** Flow control protects the receiver (`rwnd` in the header); congestion control protects the network (`cwnd`, sender-side only).
- **Why is loss a bad signal on Wi-Fi or mobile?** Losses can be radio errors, not congestion, so loss-based TCP backs off needlessly; BBR helps.
- **Nagle and delayed ACKs:** different mechanisms (small-packet coalescing); together they can add up to tens of ms latency, which is why latency-sensitive apps set `TCP_NODELAY`.
- **Bufferbloat:** large router buffers delay the loss signal and inflate RTT.
- **Linux:** `sysctl net.ipv4.tcp_congestion_control` shows the algorithm; `ss -ti` shows `cwnd`, `ssthresh` and RTT per connection.

Networking basics: [S0 · Foundations](../academy/lessons/S0.md).
