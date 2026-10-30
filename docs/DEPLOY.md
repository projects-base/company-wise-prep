# Deploying: Render (API) and Netlify (UI)

```
Browser ──► Netlify (static React UI)
   │
   └──► Render free: Spring Boot API (Docker, 512 MB) ──► Neon Postgres
        password login · CORS allows only the Netlify site · code runner OFF
```

**Coding (Run / Submit / Playground) is local-only.** Each test needs its own JVM, which a 512 MB
free instance can't afford, so the hosted app sets `CODE_RUNNER_ENABLED=false`. Problems, notes,
the editor and your saved code still show there; the Run buttons are replaced by a note. Use
`.\run.ps1` on your PC for coding — both point at the same Neon database, so progress is shared.
Locally nothing else changes: localhost only, no login.

## What protects the hosted app

The API runs submitted Java code, so it must never be reachable without a login:

- `AUTH_ENABLED=true` (set in the Dockerfile and render.yaml): every `/api` call needs a token
  from `POST /api/auth/login`. The app **refuses to start** if `APP_PASSWORD` is shorter than 12
  characters or `APP_TOKEN_SECRET` is shorter than 32.
- Five wrong passwords lock logins for 10 minutes.
- `CORS_ORIGINS` allows browser calls only from your Netlify site.
- Submitted code runs in a child JVM with the server's environment variables removed, so it
  cannot read `DB_PASSWORD` or the other secrets.

## 1. Neon

Reset the `neondb_owner` password if it was ever shared. Copy the **pooled** connection host.

## 2a. Google Cloud Run (API, current)

Cloud Run builds the same Dockerfile, sleeps when idle and wakes on the first request (10-20 s),
so a single user stays inside its monthly free tier, which does not expire after the $300 trial.
The service runs in `asia-southeast1` (Singapore), next to Neon, with 1 GiB, so the code runner is
on. It sleeps whenever the UI isn't open and in use (see "Staying awake"), which is what keeps it free.

One-time setup (you):

1. [console.cloud.google.com](https://console.cloud.google.com): create a project (e.g.
   `company-wise-prep`) and link a billing account (needs a card; the free tier still applies).
   Billing → **Budgets & alerts** → a monthly budget of ₹100 with email alerts.
2. Install the CLI (`winget install Google.CloudSDK`), open a new terminal, then:
   `gcloud auth login` and `gcloud config set project <project-id>`.
3. Enable the services once:
   `gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com`

Deploy (`.gcloudignore` keeps `.env` and the UI out of the upload):

```powershell
.\deploy-gcp.ps1
```

The first deploy asks to create an Artifact Registry repository: answer yes. Then add the secrets
once, in the console (Cloud Run → the service → **Edit & deploy new revision → Variables &
secrets**) or with `gcloud run services update company-wise-prep-api --region asia-southeast1
--update-env-vars ...`. Later deploys keep them.

| Variable | Value |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | same as on Render (Neon pooled URL) |
| `APP_PASSWORD` | your login password |
| `APP_TOKEN_SECRET` | **copy Render's value** (Render → Environment). Keys saved on the Settings page are encrypted with it; a new value means re-entering them and logging in again |
| `CORS_ORIGINS` | the Netlify URL |

Check `<url>/api/auth/status`, then point Netlify's `VITE_API_BASE` at the Cloud Run URL and
redeploy the UI. Once it works, suspend the Render service.

### Both hosts at once (Cloud Run main, Render backup)

Both services use the **same** Neon database, `APP_PASSWORD` and `APP_TOKEN_SECRET`, so data,
logins and saved keys are identical on either. Netlify's `VITE_API_BASE` lists both, main first:
`https://<cloud-run-url>,https://<render-url>`; the UI switches to Render only when Cloud Run is
unreachable or returns 502/503/504, and remembers the working host for the browser session. Add the
Netlify URL to `CORS_ORIGINS` on both. Only the host the UI is using gets keep-alive pings, so Render
sleeps and stays inside its free hours; the first request after a failover waits for it to wake (~1
minute). When both start together, the startup import takes a database row lock, so one imports and
the other waits and skips it.

Updating: run `.\deploy-gcp.ps1` after pushing backend changes (or connect the repo under Cloud
Run → **Set up continuous deployment** to deploy on every push to `main`).

## 2. Render (API, previous)

1. Dashboard → **New → Blueprint** → choose the GitHub repo. Render reads `render.yaml`
   (Docker, **free** plan, Singapore region, health check `/api/auth/status`). The free
   instance sleeps after ~15 minutes idle; the first request after that takes ~1 minute.
2. Fill the secrets it asks for:

   | Variable | Value |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://<neon-host>-pooler.<region>.aws.neon.tech/neondb?sslmode=require&channelBinding=require` (no user/password in it) |
   | `DB_USERNAME` | `neondb_owner` |
   | `DB_PASSWORD` | the Neon password |
   | `APP_PASSWORD` | the password you'll type to log in (12+ characters) |
   | `CORS_ORIGINS` | your Netlify URL — put a placeholder now, fix it in step 4 |

   `APP_TOKEN_SECRET` is generated by Render. Changing it later logs out every device.
3. Deploy. The first start creates the tables and imports `data/` into Neon (a minute or two);
   later starts skip the import unless `data/` changed. Note the URL, e.g.
   `https://company-wise-prep-api.onrender.com`. Check `<url>/api/auth/status` returns
   `{"authRequired":true,"authenticated":false}`.

## 3. Netlify (UI)

1. **Add new site → Import an existing project** → the same repo. `netlify.toml` sets the base
   (`frontend`), build command and publish folder.
2. Site configuration → **Environment variables** → `VITE_API_BASE` = the Render URL from step 2
   (no trailing slash). Trigger a deploy. Note the site URL, e.g. `https://company-wise-prep.netlify.app`.

## 4. Connect them

Back in Render, set `CORS_ORIGINS` to the exact Netlify URL (scheme included, no trailing slash)
and save — Render restarts the service. Open the Netlify URL and sign in.

## Updating

One repo, two deploys. Render builds only the backend (the Dockerfile copies `pom.xml`, `src/`,
`data/`; `.dockerignore` drops `frontend/`) and Netlify builds only `frontend/` (`base` in
netlify.toml). Push to `main`: each one rebuilds only when its own files changed — Render's
`buildFilter` watches `src/`, `data/`, `pom.xml`, `Dockerfile`; Netlify's `ignore` watches
`frontend/`. New
companies from `/prep-company` land in `data/`, so the next Render start imports them.

## Staying awake

Both hosts stop the API after ~15 minutes without requests, and the next request waits for a cold
start (Cloud Run 20-30 s, Render about a minute). The **UI** keeps it awake while you study
(`frontend/src/keepAlive.ts`): every 10 minutes it calls `/api/auth/status` (no login, no
database, so Neon can still suspend) on the host it is using, but only while the tab is visible and
you have clicked, typed or scrolled in the last 30 minutes. Coming back after a break pings at once,
so the API is waking while you read. Close the tab or walk away and the API sleeps, so a tab left
open overnight costs nothing. Locally (same-origin UI) it does nothing.

## Memory

Free plan (512 MB): app JVM `-Xmx300m` (Dockerfile `JAVA_OPTS`), code runner off. The data
import saves code challenges one at a time so it fits; after the first import, starts skip it
unless `data/` changed.

To enable coding on the hosted app later: move to a 2 GB plan, set `CODE_RUNNER_ENABLED=true`,
`JUDGE_HEAP=384m`, `JUDGE_PARALLEL=2`, and raise `JAVA_OPTS` to `-Xmx640m`.

## Cost

Render free · Netlify free · Neon free → $0 (Render Standard ≈ $25/month if you enable coding).
