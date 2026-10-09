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
| HLD | `## Requirements` (functional, non-functional) · `## Estimates` (rough numbers) · `## API` · `## Data model` · `## Architecture` (components, a `text` diagram) · `## Deep dives` (2–3 hardest parts) · `## Trade-offs` · `## Follow-ups` | 600–1200 words |
| BEHAVIORAL | `## What they are checking` · `## Structure` (STAR or similar) · `## Template` (a fill-in answer with `[placeholders]`) · `## Mistakes to avoid` | 200–450 words |

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
