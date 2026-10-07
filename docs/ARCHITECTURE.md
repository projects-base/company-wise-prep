# Architecture

## 1. The decision everything follows from

**A question exists once; a company is where it was seen.**

```
Question (canonical)  1 ──── *  Sighting  * ──── 1  Company
  title, type, difficulty        company, role, level, round,
  tags, leetcode slug            seen_on, source, confidence
```

Preparing for several companies sums sighting weights, so shared questions surface first and a
new target company re-ranks the plan without duplicating anything.

## 2. Domain modules

| Module | Entities | Notes |
|---|---|---|
| company | `Company`, `InterviewLoop`, `Round`, `Dossier` (versioned) | Dossier = how to interview at X |
| bank | `Question`, `Sighting`, `Solution`, `Tag` | Types: DSA, LLD, HLD, BEHAVIORAL, DOMAIN, JAVA, SPRING |
| campaign | `Campaign` (targets, role, date, daily minutes), `Plan` → `Week` → `Day` → `Task` | Multi-company |
| practice | `Attempt`, `ReviewCard` (FSRS) | Mistakes and Gotchas come back on schedule |
| research | `ResearchJob`, `DraftQuestion`, `ReviewQueue` | All Claude output lands here first |
| academy | `Track`, `Module` (+ prerequisite edges), `Lesson`, `Lab`, `Lifecycle`, `Gotcha`, `Era`, `DecisionCard`, `ChallengeCard`, `ScaleStage`, `Lab` (build/break/case/debate/drill), `CaseStudy`, `Incident` | Content is Markdown in the repo |
| identity | `User` | Google sign-in + email allowlist |

Carried over from DPT: progress is one JSON doc per user with a `revision` (stale write → 409);
replace-imports `flush()` between delete and insert (Hibernate orders inserts before deletes).

## 3. Research pipeline (M3)

```
1 RESEARCH  Opus 5.5 + web_search_20260209 + web_fetch_20260209, effort=high
            → free-text dossier with citations
2 EXTRACT   Opus 5.5 + structured outputs (output_config.format, JSON schema)
            → InterviewLoop + DraftQuestion[] with source URLs
3 DEDUPE    pg_trgm candidates from the bank → Claude judges "same question?"
4 REVIEW    drafts stay `claimed` until accepted in the review queue
5 PLAN      deterministic scheduler (docs/COMPANY-PREP.md §4); Claude writes only the narrative
```

- Research and extraction are separate calls: citations and `output_config.format` cannot be
  combined (400).
- `claude-opus-5-5`: thinking is always on, effort defaults to `medium` — set `high` for research.
  Enable refusal `fallbacks: "default"`; check `stop_reason` before reading content.
- Server-tool errors arrive as HTTP 200 with an error block — branch on it.
- Prompt-cache the system prompt and schemas; use the Batch API (50% cost) for scheduled refreshes.
- Jobs run on virtual threads; progress streams over SSE. Every call is logged with `usage`
  so cost is measured, not guessed.
- Until M3 exists, the `/prep-company` Claude Code skill does steps 1–5 and writes `data/` files
  in the same format; the app imports them.

In-app Claude modes (M5): mock interviewer per company and round type, hint ladder
(nudge → approach → solution), post-mortem against the company's bar.

## 4. System shape

```
React + Vite (served by Spring at /, same origin)
        │ REST + SSE
Spring Boot 4 / Java 21 — modular monolith (Spring Modulith)
  company · bank · campaign · practice · research · academy · identity
                     └─ ClaudeGateway: the only class that calls Anthropic
        │                                   │
Postgres (Neon) + pg_trgm             Anthropic API (official Java SDK)
```

- Modular monolith: one user, one developer. Modulith enforces boundaries so extraction later
  is a deliberate exercise, not an accident.
- Deploy: one container (Fly/Render) + Neon.

## 5. Build order

| Milestone | Scope | Done when |
|---|---|---|
| **M1 Bank** ✅ | Schema, importer for `data/`, browse/filter | The internet-researched bank in `data/` browsable with company filters |
| **M2 Campaign** (partly ✅) | Scheduler, Today view and +3/+10/+30 reviews built; campaign editing in-app and FSRS still to do | Daily use replaces DPT /prep |
| **M3 Research** | ClaudeGateway, pipeline, review queue, dossier view | "Add company" works in-app |
| **M4 Academy** ✅ | Track DAG, lesson renderer, Lifecycle viewer, Gotcha cards; first module D1 | One module complete end to end |
| **M5 Mock** | Mock interviewer, hints, post-mortem | — |

Capstone loop: Academy labs build PrepOS's own features (D9 starter → ClaudeGateway, G4 MCP server
over the bank, B7 rate limiter on Claude calls, A5 leak hunt on this app).
