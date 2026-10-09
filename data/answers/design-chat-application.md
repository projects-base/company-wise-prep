**Short answer:** Clients keep a WebSocket to a stateless gateway tier; a presence/session registry maps each user to the gateway holding their connection. A message is written once to a store partitioned by conversation_id, gets a per-conversation sequence number, and is then fanned out to online members via the gateways. Offline or reconnecting clients sync by asking for "everything after my last seq". For 10k+ member groups, fan out on read (members pull) or send only lightweight notifications, not the full message to each connection.

## Requirements

Functional:
- 1:1 chats, group chats up to 10k+ members, channels.
- Send, receive in real time, message history, edits, read receipts, presence.
- Multi-device: phone and desktop both stay in sync; offline sync on reconnect.

Non-functional:
- Low latency (under ~200 ms delivery for online users).
- No message loss; order consistent within a conversation.
- High availability; history stored durably for years.

## Estimates

- 300 M monthly users, 100 M daily, 50 messages/user/day = 5 B messages/day ≈ 60k/s average, ~200k/s peak.
- 200 bytes/message → ~1 TB/day, ~365 TB/year before replication.
- Concurrent connections: maybe 50 M; at ~100k connections per gateway node, that is ~500 gateway nodes.

## API

```text
WebSocket frames:
  send   { client_msg_id, conversation_id, body }
  ack    { client_msg_id, message_id, seq }
  push   { conversation_id, seq, message }
REST:
  GET /v1/conversations/{id}/messages?after_seq=..&limit=50
  GET /v1/sync?cursor=..        (all conversations changed since cursor)
  POST /v1/conversations/{id}/read { seq }
```

## Data model

```text
messages        PK (conversation_id, seq)   message_id, sender_id, body, created_at, edited_at
conversations   conversation_id, type, member_count, last_seq
members         (conversation_id, user_id), role, last_read_seq
user_inbox      (user_id, updated_at, conversation_id)   -- for "what changed" sync
```

A wide-column store (Cassandra / Cosmos DB style) fits `messages`: partition key conversation_id, clustering key seq. Very busy conversations are split by time bucket: partition key (conversation_id, month).

## Architecture

```text
client <--WS--> gateway nodes <---> session registry (user -> gateway, Redis)
                    |
                    v
             chat service --(1) assign seq, persist--> message store (partitioned by conversation)
                    |
                    +--(2) publish--> broker (topic partitioned by conversation_id)
                                           |
                                   fan-out workers --> gateways of online members
                                           |
                                   push notification svc (offline mobile)
```

## Deep dives

**WebSockets vs long polling.** WebSocket is one persistent, full-duplex TCP connection: low latency and low overhead per message. Long polling opens a new HTTP request after each response; it works through restrictive proxies but costs a request per message and adds latency. Use WebSocket as default with long polling (or SSE) as fallback. Gateways are stateful only for the connection; on node failure clients reconnect to another node and sync from their last seq.

**Ordering: sequence numbers vs timestamps.** Timestamps from many servers suffer clock skew, so two messages can sort wrongly or tie. A per-conversation monotonic `seq` gives a total order inside a conversation, makes gap detection trivial (client has 41, got 43 → fetch 42) and makes sync a simple range query. Generate it from the conversation's single writer: route all sends for a conversation to one partition owner (broker partition or a counter row with atomic increment). Global order across conversations is not needed. `client_msg_id` makes retries idempotent.

**10k+ member groups.** Fan-out on write to 10k inboxes per message is expensive and bursty. For large groups: write the message once, push a small "conversation X has new seq N" event only to members currently connected (found via the registry, grouped by gateway, so one RPC per gateway, not per user), and let clients pull the content. Read receipts become per-user `last_read_seq` instead of per-message records. Small chats can fan out on write.

**History partitioning.** Partition by conversation_id so one conversation's history is a contiguous range scan. Add a time bucket to cap partition size for long-lived busy channels. Older data can move to cheaper storage.

**Offline sync.** Each device stores a cursor per conversation (last seq). On reconnect: get list of conversations changed since cursor, then fetch messages after last seq for each.

## Trade-offs

- Wide-column store scales writes and range reads, but secondary queries (search) need a separate search index.
- Single writer per conversation gives clean order but limits one conversation's write rate; fine, since humans type slowly.
- Fan-out on read lowers write cost but makes reads heavier; hybrid by group size.

## Follow-ups

- **Edits and deletes?** New event with a new seq referencing the original message_id; clients apply it.
- **End-to-end encryption?** Server stores ciphertext; group key management gets complex for large groups.
- **Presence at scale?** Heartbeats with TTL in Redis; publish presence changes only to users who are viewing that contact.

Further reading: [F5 · Case studies: chat, news feed, collaborative editor](../academy/lessons/F5.md), [Q8 · Scaling databases](../academy/lessons/Q8.md).
