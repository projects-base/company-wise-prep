# CompanyWisePrep

Company-wise interview preparation with a proper plan, and a structured backend Academy
from scratch to architect.

- **Prep:** one global question bank; companies attach to questions as *sightings*, so preparing
  for several companies at once prioritises the questions they share.
- **Research:** `/prep-company <name>` in Claude Code researches a company's interview loop and
  questions and writes them into `data/` (later done in-app through the Claude API).
- **Academy:** Java/JVM, concurrency, time & memory analysis, Spring internals, SOLID → patterns
  → LLD, HLD, AI engineering — threaded together by *The Story of Backend* (CGI → Servlets →
  Struts → EJB → Spring → Boot → microservices → cloud native → AI-native).

| Doc | What |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | App design and build order |
| [docs/CURRICULUM.md](docs/CURRICULUM.md) | The Academy curriculum |
| [docs/COMPANY-PREP.md](docs/COMPANY-PREP.md) | Data format and the company-prep process |
| [docs/STORY-LABS.md](docs/STORY-LABS.md) | Labs, challenges, industry cases and incidents per era |

## Run it

```powershell
.\run.ps1          # builds the UI the first time, then serves http://localhost:8090
.\run.ps1 -Build   # after changing frontend/
```

Needs Java 21 and Node. **Database:** copy `.env.example` to `.env` and fill in `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD` to use Neon Postgres; without `.env` it uses a local H2 file in `.local/`.
Content is imported from `data/` at start-up only when `data/` has changed (`POST /api/admin/reload` forces it). Tests: `.\mvnw.cmd test`.

UI development with hot reload: start the server, then `cd frontend; npm run dev` (port 5180,
proxies `/api` to 8090).

Status: M1 (bank) and the plan/progress half of M2 are built. Listens on localhost only — there is
no sign-in yet, so don't expose it.
