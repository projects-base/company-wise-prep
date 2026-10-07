# Company prep — data format and process

This is the contract between the `/prep-company` skill (today) and the app's research pipeline
(milestone M3). Both write exactly these files; the app's importer reads them.

```
data/
  companies/<company-slug>/
    company.yaml        # identity, interview loop per role, values, sources
    dossier.md          # the strategy: how to interview at this company
  questions/<type>/<question-slug>.yaml   # one file per question, sightings inside
  campaigns/<id>/campaign.yaml             # targets, dates, minutes; the app builds the plan
```

Slugs are lowercase-kebab: `tower-research`, `lru-cache`, `design-rate-limiter`.

---

## 1. `company.yaml`

```yaml
slug: tower-research
name: Tower Research Capital
aliases: [Tower]
researched_on: 2026-10-07          # last time the research ran
domain_notes: Low-latency trading; C++ heavy, Java for some infra.
values:                            # what they say they look for — used for behavioural prep
  - name: Ownership
    source: https://...
roles:
  - role: SDE-2                    # free text, as the company names it
    level: L4                      # normalised level if known
    loop:
      - round: Online assessment
        type: DSA                  # DSA | LLD | HLD | BEHAVIORAL | DOMAIN | JAVA | SPRING | HIRING_MANAGER
        duration_min: 90
        assesses: Speed and correctness on 2–3 medium/hard problems
        confidence: claimed
        source: https://...
        seen_on: 2026-06           # month the report was written
    bar: >-
      What distinguishes a hire at this level, in two or three sentences.
official:                          # from the company's own careers portal — the primary source
  hiring_process:                  # steps as the company describes them
    - step: Recruiter screen
      detail: ...
      source: https://...
  interview_tips: [...]            # the company's own advice, paraphrased, each with a source
  job_postings:                    # 5–10 current postings for the target role and location
    - title: Software Engineer II
      team: Azure Storage
      location: Hyderabad
      url: https://...
      accessed: 2026-10-07
      required: [...]
      preferred: [...]
      skills: [java, distributed-systems, kubernetes]   # normalised keywords
  skill_frequency:                 # keyword → number of postings that ask for it
    distributed-systems: 7
sources:                           # everything consulted, even if nothing was taken from it
  - url: https://...
    title: ...
    accessed: 2026-10-07
    kind: official | interview-report | article | forum
```

## 2. `dossier.md`

Fixed headings, so dossiers compare across companies:

1. **TL;DR** — five bullets: what the loop is, where people fail, what to over-prepare.
2. **The loop** — round by round, what each assesses, time, format.
3. **The bar** — what "hire" looks like at the target level.
4. **Question patterns** — which topics recur, with links to `data/questions/...` files.
5. **Behavioural** — company values → which of Akhil's stories to map to each.
6. **Company-specific angle** — e.g. latency for Tower, scale for Google, design docs for Microsoft.
6b. **What the job postings ask for** — top skills by frequency across current postings, and the
    prep they imply beyond DSA. Posting bullets are requirements, never interview questions.
7. **Strategy** — what to do in the last 4 weeks, last week, the day before.
8. **Sources** — dated.

## 3. `questions/<type>/<slug>.yaml`

```yaml
slug: lru-cache
title: Design an LRU cache
type: LLD                    # DSA | LLD | HLD | BEHAVIORAL | DOMAIN | JAVA | SPRING
difficulty: medium           # easy | medium | hard
tags: [hashmap, doubly-linked-list, design]
leetcode: lru-cache          # LeetCode slug if one exists — strongest dedupe key
prompt: |
  Implement get/put in O(1) with a fixed capacity and least-recently-used eviction.
follow_ups:
  - Make it thread-safe.
  - Add TTL per entry.
academy: [B7, E-LLD]         # curriculum module ids (docs/CURRICULUM.md)
sightings:
  - company: google
    role: SDE-2
    round: Onsite coding
    seen_on: 2026-08
    confidence: claimed      # claimed (Claude/web) | verified (Akhil only)
    source: https://...
```

**Dedupe rule.** Before creating a question, search `data/questions/**` for the same LeetCode
slug, the same slug, or a title with the same core nouns ("LRU", "rate limiter"). If it exists,
append a sighting. A variant with a meaningfully different core (LRU vs LFU) is a new question
that lists the other in `follow_ups` or `tags`.

## 4. Planning rules

Given target companies **C**, interview date **D**, daily minutes **m** (default 25):

```
priority(q) = Σ over c in C of  weight(c) × recency(sighting) × confidence(sighting)
              recency:    seen within 12 months = 1.0, within 24 = 0.6, older = 0.3
              confidence: verified = 1.0, claimed = 0.7
              weight(c):  1.0 unless Akhil says one company matters more
```

- Questions seen at **more than one** target company rise naturally — that is the point.
- Days available = D − today − 3 buffer days (final 3 days are review only).
- Each day fits in **m** minutes: one new item + spaced reviews. Hard DSA ≈ 40 min, so it
  splits across two days (attempt, then review solution).
- Round types are interleaved by the loop's weight (a loop with 3 DSA rounds and 1 HLD gets ~3:1).
- Weak Academy prerequisites (e.g. no B4 before a thread-pool question) are scheduled first.

## 5. `campaigns/<id>/campaign.yaml`

```yaml
id: microsoft-2027
name: Microsoft SDE II / Senior SDE, January 2027
role: SDE II (62)
companies: {microsoft: 1.0}     # target companies and their weights
start: 2026-10-07
interview: 2027-01-04
minutes: {weekday: 25, weekend: 40}
review_days: 3
notes: Free text shown on the plan.
```

The app's `Planner` turns this into the day-by-day plan using §4 (weekdays: DSA with every 4th
slot a concept/behavioural item; weekends: LLD/HLD; the last `review_days` review-only).
**Today** serves the next unfinished items in plan order, so missed days never pile up.
