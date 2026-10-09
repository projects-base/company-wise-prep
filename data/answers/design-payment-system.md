**Short answer:** Model money movement as a double-entry ledger: each transfer writes a debit and a credit entry that sum to zero, in one database transaction, with an idempotency key so retries never move money twice. For concurrent debit/credit on two wallets, lock both account rows in a fixed order (by account id) to avoid deadlocks, check the balance, then write entries and update cached balances. External rails (UPI, cards, banks) are slow and unreliable, so payments are a state machine driven by async callbacks, with reconciliation against the bank's settlement files as the final safety net.

## Requirements

Functional:
- Pay a person (wallet-to-wallet or via UPI/bank), pay a merchant, add money, withdraw.
- Payment status, history, refunds.

Non-functional:
- Correctness first: never lose or duplicate money; balances never negative (unless allowed).
- Exactly-once effect under retries, timeouts and crashes.
- Auditable: every change is traceable and immutable.
- Highly available; latency of a few hundred ms is fine for the user.

## Estimates

- Assume 100M transactions/day ≈ 1.2k TPS average, ~10k TPS peak.
- Each transaction writes ~2 ledger entries + 1 payment row ≈ 1 KB → 100 GB/day; years of retention for audit.
- A single Postgres primary handles a few thousand write TPS; peak needs sharding by account id.

## API

```text
POST /v1/payments  Idempotency-Key: <uuid>
     {fromAccount, toAccount|vpa, amount: {value: 10000, currency: "INR"}, note}
     -> 201 {paymentId, status: PENDING|SUCCEEDED|FAILED}
GET  /v1/payments/{id}
POST /v1/payments/{id}/refunds  Idempotency-Key: ...
POST /v1/webhooks/psp   (signed callbacks from the bank/PSP)
```

Money is an integer in minor units (paise), never `double`.

## Data model

```sql
CREATE TABLE account (id bigint PRIMARY KEY, owner_id bigint, currency char(3),
                      balance bigint NOT NULL, version bigint, status text);
CREATE TABLE payment (id uuid PRIMARY KEY, idempotency_key text, payer_id bigint,
                      amount bigint, currency char(3), status text, external_ref text,
                      created_at timestamptz, updated_at timestamptz,
                      UNIQUE (payer_id, idempotency_key));
CREATE TABLE ledger_entry (id bigserial PRIMARY KEY, payment_id uuid, account_id bigint,
                      amount bigint,            -- negative = debit, positive = credit
                      created_at timestamptz);  -- append-only, never updated
```

Invariant: for each `payment_id`, `SUM(amount) = 0`. `account.balance` is a cached sum of its entries, updated in the same transaction.

## Architecture

```text
App ─> API GW (auth, rate limit) ─> Payment Service ─> Risk/Fraud check (sync, < 50 ms)
                                        │
                     ┌──────────────────┼────────────────────────┐
                     v                  v                        v
              Ledger Service      PSP/UPI Adapter          Outbox -> Kafka
           (Postgres, sharded    (timeouts, retries,       (notifications,
            by account id)        status polling)           analytics, fraud features)
                     ^                  │
                     └── Webhook handler┘
                     Reconciliation job (daily, bank settlement files vs ledger)
```

## Deep dives

**1. Concurrent debit and credit on both users.** Internal wallet transfer in one transaction:

```sql
BEGIN;
SELECT id, balance FROM account WHERE id IN (:a, :b) ORDER BY id FOR UPDATE;
-- in code: if payer balance < amount -> ROLLBACK, return INSUFFICIENT_FUNDS
INSERT INTO ledger_entry(payment_id, account_id, amount) VALUES (:p, :payer, -:amt), (:p, :payee, :amt);
UPDATE account SET balance = balance - :amt WHERE id = :payer;
UPDATE account SET balance = balance + :amt WHERE id = :payee;
UPDATE payment SET status = 'SUCCEEDED' WHERE id = :p;
COMMIT;
```

Locking in id order means A→B and B→A at the same time cannot deadlock. Alternative: optimistic locking with a `version` column and retry, better when contention is low; pessimistic is simpler for hot accounts. In Java, `synchronized` on objects only works inside one JVM; with many instances the database row lock is the real guard. A hot merchant account receiving thousands of credits/s can be split into sub-accounts (balance = sum) to spread the lock.

**2. Sharding across two users.** If payer and payee live on different shards, one local transaction is impossible. Use a saga: debit payer (entry to a "transfer in flight" account on shard 1), then credit payee on shard 2, each step idempotent by `payment_id`; on failure, a compensating credit back to the payer. Avoid 2PC across shards for availability reasons.

**3. Idempotency and external rails.** Insert the `payment` row with the unique `(payer_id, idempotency_key)` first; a retry finds the existing row and returns its status. Calls to the PSP pass our `payment_id` as their reference so the bank dedupes too. On a timeout the status is UNKNOWN, not FAILED: poll the PSP or wait for the webhook, and never blindly retry a debit. Reconciliation compares our ledger with the settlement file daily and raises breaks for manual review.

## Trade-offs

- **Strong consistency over availability** for the ledger: reject a payment rather than risk a double spend.
- **Cached balance vs computed:** caching makes reads fast but must be updated in the same transaction; recompute from entries to audit.
- **Pessimistic vs optimistic locking:** pessimistic gives predictable behaviour under contention; optimistic gives higher throughput when conflicts are rare.
- **Sync fraud check** adds latency but blocks bad payments before money moves; heavier models run async.

## Follow-ups

- *Class design?* `Money` (value object, long minor units + currency), `Account`, `Payment` (state machine), `LedgerEntry` (immutable), `PaymentService` with `@Transactional` transfer, `PspGateway` interface per rail.
- *Fraud?* Velocity rules (count/amount per hour), device and location signals, ML score; hold for review above a threshold.
- *Refund?* A new payment with reversed entries linked to the original; never delete or edit entries.

See [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md) and [Q8 · Scaling databases: replicas, partitioning, sharding, caching, CQRS](../academy/lessons/Q8.md).
