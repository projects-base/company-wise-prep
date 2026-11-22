# CompanyWisePrep

Company-wise interview preparation with a proper plan, and a structured backend Academy from scratch to
architect, with notes, flashcards and AI feedback built in. Single user (Akhil).

- **Prep:** one global question bank; companies attach to questions as *sightings*, so preparing for several
  companies at once prioritises the questions they share.
- **Research:** `/prep-company <name>` in Claude Code researches a company's interview loop and questions and
  writes them into `data/`.
- **Academy:** Java/JVM, concurrency, time & memory analysis, Spring internals, SOLID → patterns → LLD, HLD,
  AI engineering, threaded together by *The Story of Backend* (CGI → Servlets → Struts → EJB → Spring → Boot →
  microservices → cloud native → AI-native). Lessons have quizzes, diagrams, animations and runnable code.
- **Notes and review:** highlights, lesson summaries, an automatic mistakes notebook, flashcards with spaced
  repetition, "explain it in your words" with AI feedback, and linked concepts (see [Using it](#using-it)).

## Quick start

**Needs:** Java 21 and Node 20+ (check with `java -version` and `node -v`). Windows PowerShell commands below.

```powershell
cd CompanyWisePrep
.\run.ps1            # first run builds the UI, then serves everything on http://localhost:8090
```

Open **http://localhost:8090**. Stop with `Ctrl+C`.

| When | Command |
|---|---|
| You changed anything in `frontend/` and want it served on 8090 | `.\run.ps1 -Build` |
| UI work with hot reload | start the server as above, then in a second terminal: `cd frontend; npm install; npm run dev` and open **http://localhost:5180** (it proxies `/api` to 8090) |
| Run the backend tests | `.\mvnw.cmd test` |
| Check the frontend | `cd frontend; npx tsc -b; npm run build; node scripts/check-diagrams.mjs` |
| Force a content re-import from `data/` | `POST http://localhost:8090/api/admin/reload` (normally automatic when `data/` changes) |

### Database

- **Default:** a local H2 file in `.local/`. Nothing to set up.
- **Neon Postgres (shared with the hosted app):** copy `.env.example` to `.env` and fill in `DB_URL`,
  `DB_USERNAME` and `DB_PASSWORD`. `run.ps1` loads `.env` and prints which database it uses. `.env` is
  git-ignored. Never commit it.
- Tables are created and updated automatically (`ddl-auto: update`); there are no migrations to run.

### AI feedback (optional)

"Judge my answer" on questions and "Explain it" on lessons use Claude, Gemini or both. Paste a key on the
**Settings** page (or set `ANTHROPIC_API_KEY` / `GEMINI_API_KEY` in `.env`) and pick the reviewer there. Without a
key you still get the offline key-term check.

### Troubleshooting

- **Port 8090 already in use:** another copy is running (for example from IntelliJ). Stop that one, or start
  this one with `$env:PORT=8091; .\run.ps1`.
- **The UI looks old after a change:** run `.\run.ps1 -Build`, or use the dev server on 5180.
- **The app was stopped by itself:** your machine ran low on memory. Close heavy apps and start it again.
- The server listens on `127.0.0.1` only. Hosted use needs `APP_PASSWORD` (see [docs/DEPLOY.md](docs/DEPLOY.md)).

## Using it

A good daily loop (about 25 minutes):

1. **Today** shows what the plan has for you today.
2. **Academy** → open the lesson. Read one part at a time and answer the quizzes as they come.
3. **Take notes while reading:**
   - Select text → **Highlight** or **Add note**. **Make flashcard** turns it into a review card.
   - **My notes** (button on the lesson) opens the side panel: your highlights, your 4-line **summary**
     (idea, how it works inside, the trap, interview line; saved as you type) and this lesson's mistakes.
   - Type `[[Concept]]` in any note to link it, for example `[[HashMap]]`.
4. **Explain it** at the end of the lesson: type or speak your explanation, press **Check my explanation** and get
   marks for what / how / where / pitfalls plus an improved version. **Save to notes** keeps it and makes a card.
   With no API key the offline check still marks the layers, but an improved version (and so **Save to notes**)
   appears only for lessons that have a short-answer paragraph.
5. **Review** (side menu, the badge shows how many are due): flip each card with `Space`, grade it `1`–`4`
   (Again / Hard / Good / Easy); each button shows when the card comes back. New cards: 10 minutes, 1, 4 or 9
   days; after that the gap grows with each good answer (up to 180 days). Cards you marked Again come back
   after 10 minutes; press **Check again**. **All cards** lets you edit, pause (suspend) or delete cards.
6. **Mistakes:** every wrong quiz answer is saved here with why, and also becomes a review card. **Retry**
   jumps to that quiz in the lesson; a right answer marks it fixed.
7. **Playground:** run code, then **Save to notes** to keep the code and its output with the lesson (tick
   "also make a review card" if you want).
8. **My notes:** search everything you wrote, and **build a cheat sheet** (Markdown) for a lesson, a track, a
   week or everything, the night before an interview.
9. **Concepts:** every `[[concept]]` with where it shows up (your notes, lessons, mistakes), related concepts,
   and **gaps**: glossary terms from lessons you studied that you have not linked yet.

Your notes, cards and mistakes live only in the database, never in `data/` or git (the repo is public).

## Repo map

| Path | What |
|---|---|
| `src/main/java/com/companywiseprep/` | Spring Boot 4 modular monolith: `bank`, `academy`, `campaign` (plans), `notes`, `review`, `code` (runner), `web` (controllers), ... |
| `frontend/` | React + Vite UI; built into `src/main/resources/static` by `run.ps1 -Build` |
| `data/questions`, `data/answers`, `data/companies` | The question bank, model answers and company research |
| `data/academy/curriculum.yaml`, `data/academy/lessons/` | Academy tracks and lessons (Markdown) |
| `data/campaigns/` | Study plans (generated by `tools/backend-plan/generate.py`) |
| `.claude/skills/` | Claude Code skills: `prep-company`, `academy-lesson` |

| Doc | What |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | App design, modules and build order |
| [docs/CURRICULUM.md](docs/CURRICULUM.md) | The Academy curriculum |
| [docs/LESSON-TEMPLATE.md](docs/LESSON-TEMPLATE.md) | How a lesson is written (quizzes, diagrams, animations) |
| [docs/COMPANY-PREP.md](docs/COMPANY-PREP.md) | Data format and the company-prep process |
| [docs/ANSWERS.md](docs/ANSWERS.md) | Model answer format |
| [docs/STORY-LABS.md](docs/STORY-LABS.md) | Labs, challenges, industry cases and incidents per era |
| [docs/DEPLOY.md](docs/DEPLOY.md) | Hosting: Google Cloud Run (main), Render (backup), Netlify (UI), Neon (DB) |
| [docs/superpowers/specs/](docs/superpowers/specs/) | Feature designs (lesson notes Phase 1 and 2, ...) |
