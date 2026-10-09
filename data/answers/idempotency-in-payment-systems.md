**Short answer:** The client generates an idempotency key per payment intent and sends it with every attempt. The server stores the key with a hash of the request and the final response, under a unique constraint, in the same transaction as the payment record. A retry with the same key returns the stored result instead of charging again. The same key is passed down to the payment provider, and ledger entries carry unique references, so every layer deduplicates, not just the API.

## Requirements

**Functional**
- `pay(from, to, amount)` charges exactly once per user intent, even if the request is sent many times.
- Retries return the same outcome as the first attempt (success, decline, or "still processing").

**Non-functional**
- Correct under concurrent duplicates (double click, two app instances, client retry while the first is still running).
- Correct when our process crashes mid-flight or the provider call times out.
- Keys expire after a retention window (for example 24 hours to a few days).

## Estimates

- 1k payments/s at peak ⇒ 86M keys/day. Each key row ~300 bytes with stored response ⇒ ~26 GB/day; with 24–72 h retention, under 100 GB. Postgres table partitioned by day (drop old partitions), or a KV store with TTL.

## API

```text
POST /v1/payments
Idempotency-Key: 6f1c...-uuid
{ "fromAccount": "A", "toAccount": "B", "amount": 50000, "currency": "INR" }

201 { paymentId, status: SUCCEEDED }         first call
201 { paymentId, status: SUCCEEDED }         replay, same body returned
409 { error: "request in progress" }         duplicate while the first is still running
422 { error: "key reused with a different request" }
```

## Data model

```sql
CREATE TABLE idempotency_key (
  key           TEXT        NOT NULL,
  client_id     TEXT        NOT NULL,
  request_hash  TEXT        NOT NULL,
  status        TEXT        NOT NULL,   -- IN_PROGRESS, COMPLETED
  response_code INT,
  response_body JSONB,
  payment_id    UUID,
  locked_until  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (client_id, key)
);

payment(id PK, client_id, idempotency_key, amount, status, psp_ref, UNIQUE (client_id, idempotency_key))
ledger_entry(id PK, payment_id, account_id, amount_signed, entry_type, UNIQUE (payment_id, account_id, entry_type))
```

Scope the key by client (or user) so two clients cannot collide.

## Architecture

```text
client (retries with the same key)
   |
   v
[ API gateway ] --> [ Payment API ]
                        | 1. claim key (INSERT ... ON CONFLICT DO NOTHING)
                        | 2. create payment + ledger in one DB transaction
                        | 3. call PSP with the same key
                        | 4. store final response on the key row
                        v
                  [ Postgres ]        [ PSP ] (dedupes by its own idempotency key)
                        |
                  [ Reconciler ]: finds IN_PROGRESS keys / PENDING payments older than N s,
                                  asks PSP for the real outcome, completes them
```

## Deep dives

**1. Claiming the key atomically.** The first step must be atomic, or two concurrent duplicates both proceed.

```sql
INSERT INTO idempotency_key (client_id, key, request_hash, status, locked_until)
VALUES (:client, :key, :hash, 'IN_PROGRESS', now() + interval '30 seconds')
ON CONFLICT (client_id, key) DO NOTHING
RETURNING key;
```

If a row comes back, this request owns the key. If not, read the existing row: hash differs → 422; `COMPLETED` → return the stored response; `IN_PROGRESS` and lock still valid → 409 (client retries later); `IN_PROGRESS` and lock expired → the previous owner probably crashed, so take over with a conditional update on `locked_until` and *resume* rather than restart.

**2. Lifecycle with recovery points.** Treat the payment as a state machine: `CREATED → PSP_REQUESTED → SUCCEEDED | FAILED`. Persist each state before the side effect it leads to. If the process crashes after `PSP_REQUESTED`, the resumer does not know if money moved, so it asks the PSP (by the same idempotency key or our reference) instead of charging again. Only after the outcome is known is the response saved on the key row and the key marked `COMPLETED`.

**3. Internal transfers (wallet to wallet).** Debit and credit go in one local transaction, with the ledger's unique constraint as a final guard. Lock both account rows in a fixed order (lower account ID first) to avoid deadlocks, and check balance inside the transaction:

```sql
SELECT id FROM account WHERE id IN (:from, :to) ORDER BY id FOR UPDATE;          -- fixed lock order
UPDATE account SET balance = balance - :amt WHERE id = :from AND balance >= :amt;  -- 0 rows => insufficient funds
UPDATE account SET balance = balance + :amt WHERE id = :to;
```

**4. Downstream events.** Publish "payment succeeded" through a transactional outbox. Consumers keep a processed-event table keyed by event ID, so redelivery does not send two receipts or two settlements.

## Trade-offs

- **Postgres vs Redis for keys:** Postgres lets the key and the payment commit atomically, which is the whole point. Redis `SET NX` is faster, but a crash between Redis and the DB leaves them disagreeing; use it only as a front-line duplicate filter.
- **Store full response vs status only:** storing the response gives true replay (same body, same IDs); status-only is smaller but clients see different shapes on retry.
- **Key retention:** longer retention covers late retries but costs storage; a client must not retry after the window ends, and should know that.
- **Client-generated vs server-generated keys:** client-generated is needed for the first call (the server cannot dedupe what it has not seen yet). A server "create payment intent" step that returns an ID is a clean alternative.

## Follow-ups

- *Is the idempotency key the same as a payment ID?* Close: the key identifies the *intent*; a new intent (user pays again on purpose) gets a new key.
- *Should a declined payment be replayed?* Yes, return the stored decline. A new attempt after fixing the card is a new intent with a new key.
- *Exactly-once?* No such thing across networks. At-least-once delivery plus idempotent processing at every hop gives the effect of once.
- *Do GET requests need keys?* No; GET, PUT and DELETE are idempotent by HTTP semantics. POST is not, which is why keys exist.

Related lessons: [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md), [F2 · Distributed theory: CAP, consistency, consensus](../academy/lessons/F2.md), [S9 · Cloud native & event-driven systems](../academy/lessons/S9.md).
