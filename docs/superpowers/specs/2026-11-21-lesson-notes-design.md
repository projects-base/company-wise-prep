# Lesson notes, Phase 1 — design

Akhil studies the Academy alone and wants to take notes while learning Zero to Pro. The app had notes on questions
only. Mockup reviewed and approved on 21 Nov 2026 (`lesson-notes-mockup.html`, 7 screens, 9 ideas). This is Phase 1
(ideas 1, 2, 3, 8, 9); Phase 2 (flashcards, spaced repetition, explain-it with AI feedback, linked concepts) follows in
early December and builds on these tables.

## Goals
1. **Highlights and margin notes** — select text in a lesson, save it as a highlight (the quoted text, the lesson,
   the section heading), optionally with a note. Highlights re-appear marked in the lesson text and are listed in a
   notes panel beside the lesson.
2. **Lesson summary** — per lesson, four short fields: idea, how it works inside, the trap, interview line. Autosaved.
3. **Mistakes notebook, automatic** — a wrong answer on any ```quiz block is saved (lesson, question, chosen option,
   correct option, why). A later correct answer on the same quiz marks it fixed. A page lists open and fixed
   mistakes with Retry (opens the lesson at that quiz) and filters by track.
4. **Save from the Playground** — a "Save to notes" button saves the selected code (or the whole file) and the last
   output, with an optional note, to the lesson the Playground was opened from (or to a general notes bucket).
5. **Search** — one search over notes, highlights, summaries, Playground snippets and mistakes.
6. **Cheat-sheet export** — Markdown for a scope: one lesson, a track, a week of the plan, or everything; built only
   from the learner's own notes, highlights, summaries and mistakes, ordered by the plan.

## Non-goals (Phase 2 or never)
Flashcards, spaced repetition, AI feedback, `[[concept]]` links, Obsidian vault and PDF export, sharing, multi-user.

## Constraints
- Single user; data lives in the app database (H2 locally, Neon hosted) — never in lesson files or git.
- Spring Boot 4 modular monolith: a new `notes` module; controllers in `web`; JPA entities with `ddl-auto: update`.
- Highlights anchor by the quoted text plus the section heading (lessons are Markdown that changes rarely). If the
  text no longer appears, the highlight stays in the panel and simply is not marked in the lesson.
- Quiz identity: lesson id + a stable hash of the quiz question text.
- Frontend: React + Vite, existing styles and tokens; works in the lesson's one-part-at-a-time mode and full mode.
- Input limits: text fields bounded (e.g. note 4,000 chars, quote 1,000, code 20,000) and validated.

## Approach (chosen)
One `notes` module with three tables — `lesson_note` (highlights, Playground snippets and the summary as typed
rows), `quiz_attempt_mistake` (mistakes), and the summary as a `kind = SUMMARY` row with JSON fields — exposed through a
small REST API, plus a "My notes" page and "Mistakes" page in the UI. Alternatives considered: storing notes in the
browser (lost across devices, rejected); extending the question `Progress` entity (wrong shape, rejected).

## Left to the builder
Exact entity and endpoint names, component structure, and how selection is captured in the rendered Markdown.
