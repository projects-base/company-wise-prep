# Lesson notes, Phase 2 — design

Phase 1 (highlights, summaries, mistakes notebook, Playground save, search, cheat-sheet export) shipped on
21 Nov 2026 (`2026-11-21-lesson-notes-design.md`). Phase 2 covers the other four ideas from the approved mockup
(`lesson-notes-mockup.html`, screens 1, 5, 6 and 7): **4 flashcards, 5 spaced repetition, 6 explain it with AI
feedback, 7 linked concepts.** Akhil asked for it on 22 Nov 2026. Akhil is the only user, and the notes stay in
the database, never in git (the repo is public).

## Why
Phase 1 captures what Akhil reads. Phase 2 makes him recall and explain it, because that is what an interview
tests. It also shows how ideas connect, so he can spot the topics he has not touched yet.

## Goals
1. **Flashcards (idea 4).**
   - **Sources:**
     - **Make flashcard:** a highlight in the lesson toolbar or notes panel. It opens a small editor: the front
       is a question he writes (pre-filled with a suggestion), the back is the quote plus his note.
     - **Automatic:** every quiz mistake. Front: the question, plus code if any. Back: the correct option and
       why.
     - **Opt-in:** a Playground snippet, through an "also make a review card" checkbox.
     - **Explain it:** the improved explanation.
   - **Chip:** a highlight with a card shows a "Flashcard on" chip.
   - **Editing:** a card can be edited, suspended or deleted. Deleting its source does not delete the card.
2. **Spaced repetition (idea 5).**
   - **Review page:** a daily queue of due cards. He shows the answer, then picks Again / Hard / Good / Easy;
     each button shows the next interval.
   - **Schedule:**
     - **New card:** Again 10 min, Hard 1 day, Good 4 days, Easy 9 days, as in the mockup.
     - **Later reviews:** SM-2 style. Ease starts at 2.5 with a floor of 1.3. Again resets the card (10 min) and
       lowers ease by 0.2. Hard multiplies the interval by 1.2 and lowers ease by 0.15. Good multiplies it by
       ease. Easy multiplies it by ease × 1.3 and raises ease by 0.15. The maximum interval is 180 days.
   - **Stats:** the streak (consecutive days with at least one review), "cards you know well" (interval
     ≥ 21 days) out of all active cards, and due counts for the next 7 days.
   - **Nav badges:** a due-count badge on the Review link in the side menu.
   - **Fixed mistakes:** when a quiz mistake is fixed in the lesson, its card stays. Recall still counts.
3. **Explain it in your words (idea 6).**
   - **Where:** at the end of every lesson, an "Explain it" box. The prompt is "Explain in your words: <lesson
     title>". He can type the explanation or speak it, using the recorder the app already has.
   - **Review:** "Check my explanation" uses **the existing reviewer stack** (Claude, Gemini or both from Settings,
     through `ClaudeGateway` / `GeminiGateway`). The reference is the lesson's Markdown, with quiz, anim and
     mermaid blocks stripped and the length capped. Without an API key it falls back to the offline term check.
   - **Result:** the 4-layer marks (what / how / where / pitfalls), the score, the misconceptions and an improved
     explanation.
   - **Storage:** each attempt is stored, so a lesson can show its score trend. "Save to notes" stores the
     improved version as a note and makes a flashcard from it.
4. **Linked concepts (idea 7).**
   - **Linking:** typing `[[Concept]]` in any note text (highlight note, snippet note, summary field,
     explanation) links that item to the concept. Matching ignores case and extra spaces.
   - **Concepts page (/concepts):** every concept with its link count, strongest first.
   - **Concept detail:**
     - **Where it shows up:** the user's own items that link the concept, the lessons whose text mentions the
       term (word-boundary match), and the mistakes whose question mentions it.
     - **Related concepts:** concepts that appear together in the same items.
   - **Gaps:** terms from the Reference glossary (lesson R1, first column of its tables; the Lesson column says
     where each is taught) that have no personal link, limited to lessons he has already studied. If the app
     tracks no lesson completion, "studied" means lessons with any note.
   - **Display:** `[[x]]` in notes shows as a link to the concept page.

## Non-goals
Obsidian vault or PDF export, sharing, multiple users, an AI-generated card front, image cards, and importing
from Anki.

## Constraints
- **Modules:** extend the `notes` module, or add a `review` package inside it. Controllers go in `web`. JPA uses
  `ddl-auto: update`, so new tables only; existing Phase 1 tables keep their columns.
- **Reuse:** the reviewer prompt and parsing in `review/AnswerReviewService` instead of copying them. Extract a
  shared piece if needed, and do not change the behaviour for questions.
- **Schedule rules:** the scheduling rule is a pure function with its own unit tests. It takes the time from the
  injected `Clock`.
- **Idempotency:** a quiz mistake makes at most one card. A repeated wrong answer does not duplicate it.
- **Limits:** input limits match Phase 1 (front 1,000, back 4,000, explanation 6,000 characters), with
  validation.
- **Frontend:** React + Vite with the existing styles and tokens, and the visuals of mockup screens 5–7. Both
  the lesson's step mode and full mode work, and keyboard shortcuts on Review (Space shows the answer, 1–4 grade).

## Approach (chosen)
**Tables:**
- `review_card`: source kind and id, module id, front, back, due, interval, ease, reps, lapses, suspended,
  timestamps.
- `review_log`: one row per grade, for the streak and stats.
- `explanation_attempt`.

Concepts are not a table. They are parsed from note text when asked, because the data is small and parsing
keeps them always in step.

Alternatives considered:
- An FSRS scheduler: better retention modelling, but needs review history the app does not have yet. Rejected
  for now; the log table keeps that door open.
- A concept table kept in sync on every save: more moving parts for a single user. Rejected.

## Left to the builder
Exact names, component structure, how the Review page lays out its stats, and the suggested card front.
