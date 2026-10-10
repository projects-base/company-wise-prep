# Model answers

Every question in the bank can have a model answer: `data/answers/<slug>.md`, where `<slug>` is
the question's `slug`. The app shows it on the question page under **Answer**, collapsed so you try
first. A question without the file simply has no answer yet.

Answers are written by Claude. They are study material, not sightings, so they need no source URL,
but they must be correct for **Java 21, Spring Boot 3/4 and PostgreSQL** unless the question says
otherwise. If a fact is version-specific, say which version. If you are not sure of something, leave
it out rather than guess.

## File format

Plain Markdown, no front matter, no `#` title (the page already shows the question). Start with
the short answer, then go deeper. Use `##` for sections. Fenced code blocks carry a language
(`java`, `sql`, `tsx`, `js`, `yaml`, `text`).

Every answer opens with:

```markdown
**Short answer:** two to four sentences you could say out loud in the first 30 seconds.
```

Then, by question type:

| Type | Sections after the short answer | Length |
|---|---|---|
| JAVA, SPRING, DOMAIN, SQL | `## Explanation` (how it works, the why) · `## Example` (small code or query) · `## Pitfalls and follow-ups` (what interviewers dig into next, with one-line answers) | 250–600 words |
| DSA | `## Approach` (brute force → the key insight → optimal) · `## Solution` (Java 21, clean and complete) · `## Complexity` (time and space, with the reason) · `## Edge cases` · `## Variations` (optional) | 200–500 words plus code |
| LLD | `## Requirements` (assumed scope) · `## Classes` (responsibilities, relationships) · `## Patterns used` (and why) · `## Code` (the core types and the one or two key methods, Java) · `## Extensions` (concurrency, new features) | 400–900 words |
| HLD | `## Requirements` (functional, non-functional) · `## Estimates` (rough numbers) · `## API` · `## Data model` · `## Architecture` (components; the picture is in `## Picture it`) · `## Deep dives` (2–3 hardest parts) · `## Trade-offs` · `## Follow-ups` | 600–1200 words |
| BEHAVIORAL | `## What they are checking` · `## Structure` (STAR or similar) · `## Template` (a fill-in answer with `[placeholders]`) · `## Mistakes to avoid` | 200–450 words |

## Picture it (diagrams)

HLD, LLD and medium/hard DSA answers carry a `## Picture it` section **right after the short
answer**, so the shape of the solution is seen before it is read. The app draws ` ```mermaid `
fences as diagrams (Answer, Academy and dossier pages). Keep each diagram small enough to take in at
a glance (about 15 nodes at most); two or three small diagrams beat one crowded one.

| Type | What goes in `## Picture it` |
|---|---|
| HLD | 1) The architecture as a `flowchart LR` with `subgraph`s (clients · edge · services · async · storage); stores as `[(name)]`, queues as `[[name]]`. 2) A `sequenceDiagram` of the one request path that matters most (e.g. "shorten, then redirect"), with `autonumber`. 3) Optional: the hardest deep dive as its own picture (a `stateDiagram-v2` for a payment/booking lifecycle, a flowchart for sharding or fan-out). Then `**How to read it:**` with 3–5 bullets that walk the numbered flow in plain words. The `text` diagram in `## Architecture` is replaced by a one-line pointer to the picture; the prose there stays. |
| LLD | 1) A `classDiagram` of the core types and their relationships (inheritance, composition, the interfaces the patterns hinge on), key methods only. 2) A `sequenceDiagram` (or `stateDiagram-v2`) of the main use case. Then `**How to read it:**` bullets. |
| DSA (medium, hard) | Show the idea working on a small example: a step-by-step **trace table** (one row per step: pointers/window/stack/queue/DP cell, the state, what happens) and/or a mermaid picture when the data has a shape (a tree or graph as `flowchart TD`, a recursion/backtracking tree, a DP dependency, a state machine). For DP, show the filled table. End with `**The picture in one sentence:**` naming the trick. |

Mermaid rules (so every diagram parses; `cd frontend && node scripts/check-diagrams.mjs` checks them all):
- Quote any label with punctuation: `A["Cache (Redis)"]`, `B -->|"200 OK"| C`. Line breaks with `<br/>`; no other HTML.
- Node ids are plain words (`api`, `db1`); never use `end`, `graph` or `subgraph` as an id.
- No colours, `style` or `classDef`: the app themes diagrams for light and dark mode.
- In `sequenceDiagram` messages avoid `;` and `#`; use `Note over A,B: text` for asides.
- In `classDiagram`, generics use `~`: `List~Order~`.

## Rules

- **Never invent Akhil's experience.** Behavioral templates use `[placeholders]` for his stories.
  They may suggest a kind of story that fits a ~5.5-year Java/Spring Boot backend engineer, but must
  not state it happened.
- If the question has follow-ups in its YAML (`follow_ups`), answer each one briefly.
- A DSA question with a runnable challenge (`data/code/<slug>/`) gets the same explanation; the
  solution may match `reference/Solution.java`. Say at the end: `Practise it in the app: Run / Submit
  on this page.`
- Link Academy lessons that go deeper with relative links, e.g.
  `[Q4 · Indexes](../academy/lessons/Q4.md)`. Link only lessons that exist and are relevant.
- External links only to official documentation you are certain of (docs.oracle.com,
  docs.spring.io, postgresql.org/docs, react.dev, developer.mozilla.org). No other URLs.
- Plain, direct English with short sentences. No filler ("Great question").
