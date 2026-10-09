**Short answer:** Send every message once over UDP multicast with a per-stream sequence number. Receivers detect gaps from the sequence numbers and ask for only the missing ones with a NACK sent to a retransmission server over unicast. The sender keeps a bounded history buffer to serve retransmits; if a receiver has fallen too far behind, it recovers from a snapshot of the current state and then replays from the snapshot's sequence number. Heartbeats with the latest sequence number reveal losses at the tail when traffic is idle. Using NACKs instead of ACKs avoids ACK implosion, where N receivers acknowledging every packet would swamp the sender.

## Explanation

**Requirements to state first:** one sender (or few), N receivers on a LAN, in-order delivery per stream, no loss visible to the application, low latency on the normal path, and receivers may join late or restart.

**Message format**

```text
| stream_id | seq (u64) | send_ts | msg_count | payload... |
```

Batching several application messages per packet with `msg_count` keeps one sequence number per packet or per message (per message is simpler for recovery).

**Normal path:** sender increments `seq`, multicasts, stores the packet in a ring buffer of the last K packets (or last T seconds). Receivers track `expected`. If `seq == expected`, deliver and advance.

**Gap detection and NACK**
- `seq > expected`: buffer out-of-order packets, start a short timer (to tolerate reordering), then send `NACK(stream, from, to)` over unicast TCP/UDP to a **retransmission server** (not the primary sender, so the hot path is not disturbed).
- The server replies by unicast to that receiver, or re-multicasts if many receivers missed the same range.
- **NACK implosion** (a switch drops a packet for everyone, so everyone NACKs): add a small random delay before NACKing and suppress your NACK if you see someone else's request or the multicast repair first (the SRM-style approach), or aggregate NACKs at the server.

**Tail loss:** if the last packet before an idle period is lost, nobody sees a gap. The sender multicasts **heartbeats** carrying the latest `seq` every few milliseconds when idle.

**Late join, restart or too far behind:** if the missing range is older than the history buffer, the receiver requests a **snapshot** (full state, e.g. the current order book) tagged with the sequence number it reflects, applies it, discards buffered packets up to that number, and continues from there. Exchanges commonly publish a separate snapshot / recovery feed for this.

**Redundancy without retransmission (A/B feeds):** send the same stream on two multicast groups over separate network paths. Receivers take whichever copy arrives first (arbitration by sequence number). This hides most single drops with no added latency; retransmission covers the rest.

**Sender side:** when to free history? Without ACKs the sender cannot know everyone has it, so size the buffer by time (e.g. a few seconds) and rely on snapshots beyond that. If you need guaranteed delivery to each receiver, add periodic *cumulative* ACKs (a receiver reports its highest contiguous seq every 100 ms), which is cheap compared to per-packet ACKs.

## Example

```cpp
void Receiver::onPacket(const Packet& p) {
    if (p.seq < expected_) return;                       // duplicate
    if (p.seq == expected_) {
        deliver(p); ++expected_;
        while (pending_.count(expected_)) {              // drain buffered
            deliver(pending_[expected_]); pending_.erase(expected_++);
        }
        return;
    }
    pending_.emplace(p.seq, p);                          // gap
    if (!nackTimer_.armed()) nackTimer_.arm(jitter(200us), [this, upTo = p.seq - 1] {
        if (expected_ <= upTo) retransClient_.nack(expected_, upTo);
    });
}
```

## Pitfalls and follow-ups

- **ACK vs NACK?** ACKs give the sender certainty but scale as N x messages; NACKs scale with losses only.
- **Ordering guarantees?** Per stream only; across streams, timestamps or a single sequencer.
- **Flow control?** A slow receiver must not slow the sender; it falls back to snapshot recovery.
- **Existing protocols:** PGM (RFC 3208) and the NACK-oriented NORM (RFC 5740) do this; Aeron uses a similar NAK-based design.
- **Java comparison:** the same design in Java uses `MulticastSocket` or `DatagramChannel` joined to a group.

Related: [F1 · Building blocks](../academy/lessons/F1.md) for queue and broker alternatives on less latency-sensitive paths.
