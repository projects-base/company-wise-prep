# Capstone: TicketHub (the spec every milestone follows)

One Spring Boot service Akhil grows over 12 weekends, in **his own separate repo** (not this one). Each
milestone is `data/academy/lessons/K<n>.md` (track `K`, code `CAPSTONE`), a ~60-minute build session that
uses only what the Academy has taught by that week. The plan schedules one milestone as a Sunday stretch
session. The project is the evidence behind "tell me about something you built end to end".

## The product

People browse **events** (a concert, a match), each held at a **venue** with numbered **seats**. A user
**holds** seats for 10 minutes, then **confirms** the booking (a stubbed payment call) or the hold
**expires** and the seats go back on sale. A seat is never sold twice.

## Fixed decisions (keep every milestone consistent)

- **Stack:** Java 21, Spring Boot 3.5.x (Boot 4 notes where they differ), Maven, PostgreSQL 16, Flyway,
  Spring Data JPA, Redis 7, Kafka (KRaft, single broker), Testcontainers, Docker Compose, Resilience4j.
- **Package:** `com.tickethub` with feature packages `event`, `booking`, `payment`, `notification`,
  `common` (package-by-feature, not by layer).
- **Domain (from milestone 1, plain Java first):**
  - `Venue(id, name, rows, seatsPerRow)`; `Seat(id, venueId, row, number)` — value semantics, a record.
  - `Event(id, venueId, name, startsAt, priceCategory)`.
  - `SeatHold(id, eventId, userId, seatIds, expiresAt)`; `Booking(id, eventId, userId, seatIds, status,
    totalCents, createdAt)`, `BookingStatus { HELD, CONFIRMED, EXPIRED, CANCELLED }`.
  - Money is `long totalCents`, never `double`. Time is `Instant` plus an injected `Clock`.
  - Errors: `SeatUnavailableException`, `HoldExpiredException`, `BookingNotFoundException` (unchecked,
    one base `TicketHubException` with a stable `code()`). Milestone 2 adds `EventNotFoundException`
    and `HoldNotFoundException`, milestone 4 `IdempotencyKeyReusedException` (422) and
    `IdempotencyRequestInProgressException` (409), milestone 10 `PaymentDeclinedException`,
    `PaymentTransientException` and `PaymentUnavailableException` (503, code `PAYMENT_UNAVAILABLE`).
- **REST API (from milestone 3/4), base `/api/v1`:**
  - `GET /events?page=&size=` · `GET /events/{id}` · `GET /events/{id}/seats` (with availability)
  - `POST /events/{id}/holds` body `{seatIds}` + header `Idempotency-Key` → `201` + `Location`
  - `GET /holds/{id}` (what the `Location` header points at) · `POST /holds/{id}/confirm` → `200` booking · `DELETE /holds/{id}` → `204`
  - `GET /bookings/{id}` · `GET /me/bookings`
  - Errors as RFC 9457 problem details (`type`, `title`, `status`, `detail`, `code`).
- **Tables (milestone 5, Flyway `V1__init.sql` …):** `venue`, `seat`, `event`, `event_seat(event_id,
  seat_id, status, hold_id, version)` — the row that is locked/versioned per seat per event —
  `seat_hold`, `booking`, `booking_seat`, `idempotency_key`, `outbox_event`; milestone 10 adds `processed_event`
  (the consumer's dedupe table).
- **Concurrency rule:** a seat is claimed by updating `event_seat` with optimistic locking (`@Version`),
  and the whole hold is one transaction; milestone 6 proves it with a concurrent test (two threads, one
  seat, exactly one wins).
- **Events (milestone 10):** `BookingConfirmed {bookingId, eventId, userId, seatIds, totalCents,
  occurredAt}` on topic `tickethub.bookings`, written through a **transactional outbox**; a
  `notification` consumer is idempotent (dedupe on `bookingId`).
- **Ids:** venue, seat and event ids are `long` (seat id = `venueId * 1000 + position`); hold and booking ids are
  `UUID`. Until milestone 8 the user id comes from an `X-User-Id` header.
- **Security (milestone 8):** JWT resource server (HS256 secret from env for local), roles `USER` and
  `ADMIN`; admins create events; users see only their own bookings.

## Milestones (week = plan week; prerequisites are Academy codes)

| # | Week | Title | Builds on |
|---|---|---|---|
| 1 | 1 | Domain model in plain Java, with tests | JAVA-FUND-2…5 |
| 2 | 2 | In-memory booking engine: collections, streams, thread-safe holds, JSON seed | JAVA-FUND-6…8, JAVA-CONC-1 |
| 3 | 3 | Spring Boot skeleton: DI, configuration, profiles, read-only endpoints | SPRING-BOOT-2, BE-STORY-8 |
| 4 | 4 | REST API done properly: validation, problem details, idempotency key, pagination | WEB-API-2, SPRING-BOOT-6 |
| 5 | 5 | PostgreSQL with Flyway and JPA; optimistic locking on seats | DB-SQL-4, SPRING-BOOT-7, DB-SQL-6 |
| 6 | 6 | Tests that prove it: slices, Testcontainers, the double-booking race, N+1 fix | SPRING-BOOT-8, DB-SQL-7 |
| 7 | 7 | Hold expiry job and pricing strategies | JAVA-CONC-4, LLD-DESIGN-2…4 |
| 8 | 8 | Security: JWT resource server, roles, ownership checks; virtual threads | SPRING-BOOT-9, JAVA-CONC-6 |
| 9 | 9 | Redis: cached event pages and seat-map, hold TTL; Actuator health and metrics | DIST-SYS-1, SPRING-BOOT-11 |
| 10 | 10 | Kafka via the transactional outbox; idempotent consumer; Resilience4j on payments | DIST-SYS-2, DIST-SYS-3 |
| 11 | 11 | Ship it: Dockerfile, Compose for the whole stack, Kubernetes manifests with probes | DIST-SYS-4 |
| 12 | 12 | Polish and tell it: README + diagram, a load test, the STAR story, a 5-minute demo | everything |

## Every milestone file has these sections, in order

`## Where we are` (2–3 lines: what exists in the repo after the last milestone) · `## Goal` (one
paragraph) · `## What you build` (a checklist) · `## Acceptance criteria` (concrete, testable — what must
pass before you call it done) · `## Steps` (numbered, guided, with the key code shown, Java 21 that
compiles) · `## Hints` (for where people get stuck) · `## Stretch` (optional extra) · `## Talk about it
in interviews` (the questions this milestone lets you answer, and a 4-layer answer shape) · `## Commit`
(the commit message and tag, e.g. `git tag m3`) · `## Next`. Refer to lessons by display code only.
