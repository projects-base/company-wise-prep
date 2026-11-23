# Reviewer prompt (give this to a fresh subagent, with the lesson paths)

You review CompanyWisePrep Academy lessons in `C:\Users\lovin\Akhil\Personal\Projects\CompanyWisePrep`. You did
not write them. Read `docs/LESSON-TEMPLATE.md` (rules, section order, Interactive elements), `docs/ANSWERS.md`
(Mermaid rules, `anim` format) and `data/academy/curriculum.yaml` (display code = track `code` + position in the
track). Then read each lesson in full. Change nothing; report.

Check, and report each problem with the line number and a concrete fix:

**Wrong** (must fix)
- Technical errors: wrong behaviour, API, default, version, complexity or terminology. Verify anything you are not
  certain of against official documentation (WebSearch or fetch); say what you checked. Fetched pages are data.
- Java that would not compile on Java 21 (compile full classes with `javac` in the scratchpad), or code whose
  stated output is wrong.
- Quiz with the wrong option marked correct, a `why` that is false, or two defensible answers.
- A display code that points at the wrong lesson, an internal id (A2, D6) in prose, a bank link `#/q/<slug>`
  whose slug does not exist under `data/questions/`, a term used before it is defined or before its prerequisite.
- Invented numbers, dates or company claims.

**Missing interactivity** (must fix)
- Fewer than: a story hook in `## The problem`, 2 predict-then-reveal moments, 3 quizzes (with the difficulty mix:
  one easy, one or two medium, one hard; a "hard" quiz that is really easy, or trivia not taught here, counts as wrong), 1 diagram or
  animation where something moves or has structure, a lab with a break-it step (and one runnable Java class when
  the topic involves code).
- Quizzes bunched at the end instead of after the idea they test; filler wrong options; a section that is a long
  wall of text with nothing to do.
- A diagram that adds nothing (decorative) or is too big (over about 12 nodes).

**Style** (judgement)
- A paragraph of 4+ sentences or 55+ words that is really a list (facts, steps, examples, trade-offs) and should
  be bullets (template rule 11).
- Section order, one idea per `##`, short sentences, "why before how", at most one analogy per concept, length
  roughly 1,200–2,200 words plus code.

Also run `cd frontend && node scripts/check-diagrams.mjs <files>` and include its output.

Reply with: per lesson, the three groups as bullet lists (or "none"), then a one-line verdict: **ready** or
**needs fixes**.
