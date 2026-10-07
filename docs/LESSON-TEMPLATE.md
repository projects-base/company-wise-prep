# How an Academy lesson is written

One file per module: `data/academy/lessons/<MODULE-ID>.md`. The app shows the title, level,
minutes and prerequisites from `curriculum.yaml`, so **the file has no H1** — it starts at `##`.

## The smooth-learning-curve rules (non-negotiable)

1. **Assume only what the prerequisites taught.** A module with no prerequisites assumes a
   beginner who can write a basic Java class and nothing more. Never use a term a prerequisite
   didn't introduce without defining it right there.
2. **Define every new term at first use**, in one plain sentence, before using it.
3. **Concrete before abstract.** Show a tiny example (a few lines, a real situation), *then* state
   the general rule.
4. **One new idea per section.** Short sections. If a section needs two new ideas, split it.
5. **Build on the last lesson.** Open with a 2–3 line recap of what the reader now knows and why
   this lesson is the natural next step.
6. **At most one analogy per concept**, and say where the analogy breaks.
7. **Say why before how.** Every mechanism starts with the problem it solves.
8. **Accurate over impressive.** No invented numbers, dates or company claims. If unsure, leave
   it out or say "roughly". Java code targets Java 21 and must compile.

## Sections, in this order

```markdown
## Where we are
2–3 lines: what you already know (from the prerequisites) and what this lesson adds.

## The problem
The situation that makes this topic necessary. Concrete.

## <Concept sections — one idea each>
Explanation → tiny example → the general rule. Diagrams as ```text blocks (ASCII), since the
app renders Markdown, not Mermaid.

## Lab (~10 min)
A small, runnable Java 21 exercise (or a design exercise for design topics): what to build,
starter code, what you should observe. For "break it" labs, say how to break it and what failure
to watch for.

## Gotchas
3–6 bullets. Each one: the trap, and how to avoid it. These become review cards.

## In interviews
The ways this is asked. Link bank questions that exist as [Title](#/q/slug) — only slugs listed
in the bank index. Then 1–2 typical follow-ups and what a strong answer contains.

## Explain it in 2 minutes
One prompt to answer out loud, plus the 4–6 points a good answer hits.

## Check yourself
3–5 questions. Then a `### Answers` subsection with short answers.

## Next
One line pointing at the module(s) this unlocks and why.
```

Length: roughly 1,200–2,200 words — about 25 minutes including the lab.
