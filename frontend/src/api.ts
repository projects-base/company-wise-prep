// Types mirror the Java records in com.companywiseprep (field names are the record components).

export type QType = 'DSA' | 'LLD' | 'HLD' | 'BEHAVIORAL' | 'DOMAIN' | 'JAVA' | 'SPRING' | 'SQL'

export interface CampaignConfig {
  id: string
  name: string
  role: string | null
  companies: Record<string, number>
  start: string
  interview: string
  weekdayMinutes: number
  weekendMinutes: number
  reviewDays: number
  notes: string | null
  sessions: Session[]
  /** Markdown: the plan's rules and map, shown on the Plan page. */
  guide: string | null
  archived: boolean
}

/** A session fixed to a date in campaign.yaml: a quiz, a mock or a review block. */
export interface Session {
  date: string
  minutes: number
  title: string
  detail: string | null
  questions: string[]
  /** core = the plan is built on these; stretch = optional extra on a good day. */
  tier?: 'core' | 'stretch'
  /** The minimum for a bad day (usually the Academy lesson). */
  floor?: boolean
}

/** A judged theory answer (review.AnswerReview). Layer scores are 0-10; null = nothing to compare. */
export interface AnswerReview {
  mode: 'claude' | 'gemini' | 'offline'
  score: number
  verdict: string
  layers: { key: string; name: string; score: number | null; comment: string }[]
  covered: string[]
  missed: string[]
  mistakes: string[]
  improvedAnswer: string | null
  followUp: string | null
  note: string | null
  history: number[]
  /** Gemini's review when both reviewers are on. */
  second: AnswerReview | null
}

export interface ProviderSettings {
  keySet: boolean
  /** Last four characters only; the key itself never comes back from the server. */
  keyHint: string | null
  keySource: 'settings' | 'environment' | null
  model: string
  defaultModel: string
  /** Tried in order when the main model is busy or failing (Gemini only). */
  fallbacks: string[]
  defaultFallbacks: string[]
}

export type ModelCheck = { model: string; ok: boolean; message: string; millis: number }
export type ProviderTest = { ok: boolean; model: string; message: string; checks: ModelCheck[] | null }

export interface Settings {
  reviewer: 'auto' | 'claude' | 'gemini' | 'both' | 'offline'
  effective: string
  claude: ProviderSettings
  gemini: ProviderSettings
  encrypted: boolean
}

export interface PlanItem {
  slug: string
  title: string
  type: QType
  difficulty: string
  leetcode: string | null
  minutes: number
  priority: number
  companies: string[]
  target: string | null
}

export interface PlanDay {
  date: string
  kind: 'STUDY' | 'REVIEW'
  weekend: boolean
  budget: number
  items: PlanItem[]
  sessions: Session[]
}

export interface PlanResponse {
  plan: { campaign: CampaignConfig; days: PlanDay[]; beyond: PlanItem[] }
  done: string[]
}

export interface Review {
  slug: string
  title: string
  type: QType
  stage: number
  due: string
}

export interface Today {
  date: string
  phase: 'BEFORE_START' | 'STUDY' | 'REVIEW' | 'FINISHED'
  weekend: boolean
  budget: number
  items: PlanItem[]
  reviews: Review[]
  behind: number
  done: number
  total: number
  daysLeft: number
  campaign: CampaignConfig
  sessions: Session[]
}

export interface CompanySummary {
  slug: string
  name: string
  researchedOn: string | null
  questions: number
}

export interface CompanyView {
  slug: string
  name: string
  researchedOn: string | null
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data: Record<string, any>
  dossier: string
}

export interface QuestionSummary {
  slug: string
  title: string
  type: QType
  difficulty: string
  tags: string[]
  leetcode: string | null
  companies: string[]
  sightings: number
  lastSeen: string | null
  done: boolean
  starred: boolean
  runnable: boolean
}

export interface Sighting {
  company: string
  role: string | null
  round: string | null
  seenOn: string | null
  confidence: string
  source: string | null
}

export interface Progress {
  done: boolean
  doneOn: string | null
  reviewStage: number
  nextReviewOn: string | null
  starred: boolean
  notes: string | null
  code: string | null
}

export interface QuestionView {
  slug: string
  title: string
  type: QType
  difficulty: string
  tags: string[]
  leetcode: string | null
  prompt: string | null
  answer: string | null
  followUps: string[]
  academy: string[]
  sightings: Sighting[]
  progress: Progress
  runnable: boolean
}

export interface ModuleSummary {
  id: string
  /** What the UI shows: track code + position, e.g. JAVA-JVM-3 (the id stays the stable key). */
  code: string
  trackId: string
  title: string
  level: number
  minutes: number
  prerequisites: string[]
  written: boolean
  done: boolean
  ready: boolean
}

export interface AcademyOverview {
  paths: { id: string; title: string; description: string; modules: string[]; done: number; next: string | null }[]
  tracks: { id: string; code: string; title: string; description: string; modules: ModuleSummary[] }[]
  done: number
  total: number
  written: number
}

export interface Ref {
  id: string
  code: string
  title: string
  done: boolean
}

export interface ModuleView {
  module: ModuleSummary
  trackTitle: string
  lesson: string
  prerequisites: Ref[]
  unlocks: Ref[]
  previous: Ref | null
  next: Ref | null
  pathPosition: number
  pathLength: number
}

export type Verdict = 'ACCEPTED' | 'WRONG_ANSWER' | 'RUNTIME_ERROR' | 'TIME_LIMIT' | 'COMPILE_ERROR'

export interface TestCase {
  name: string
  input: string
  expected: string
  hidden: boolean
}

export interface Challenge {
  slug: string
  method: string | null
  problem: string
  starter: string
  savedCode: string | null
  examples: TestCase[]
  hiddenCount: number
}

export interface CompileError {
  file: string
  line: number
  column: number
  message: string
}

export interface TestResult {
  name: string
  hidden: boolean
  verdict: Verdict
  input: string | null
  expected: string | null
  actual: string | null
  stderr: string | null
  millis: number
}

export interface JudgeResult {
  verdict: Verdict
  passed: number
  total: number
  maxMillis: number
  compileErrors: CompileError[]
  results: TestResult[]
}

export interface PlaygroundResult {
  verdict: Verdict
  stdout: string
  stderr: string
  millis: number
  truncated: boolean
  compileErrors: CompileError[]
}

export type Action = 'DONE' | 'UNDO' | 'REMEMBERED' | 'FORGOT' | 'STAR' | 'UNSTAR' | 'NOTES'

/**
 * Where the API lives. Empty = same origin (local: Spring serves the UI; dev: Vite proxies /api).
 * On Netlify, VITE_API_BASE lists the API hosts, comma-separated, main first (Cloud Run, then
 * Render). Both share one database and token secret, so either can serve any request.
 */
const BASES = ((import.meta.env.VITE_API_BASE as string | undefined) ?? '')
  .split(',')
  .map((b) => b.trim().replace(/\/$/, ''))
  .filter(Boolean)
if (BASES.length === 0) BASES.push('')
/** True on Netlify (the API is on another host); false when Spring or Vite serves the UI. */
export const hosted = BASES[0] !== ''
let lastCall = 0
/** When the last request reached an API host (ms since epoch); keepAlive skips a ping after one. */
export const lastApiCall = () => lastCall
const ACTIVE_KEY = 'cwp.api'
const TOKEN_KEY = 'cwp.token'

/** The hosted app's login token. Locally there is none and the server doesn't ask for one. */
export const session = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set(token: string) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch {
      /* private mode: the login lasts for this tab only */
    }
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      /* nothing stored */
    }
  },
}

export const LOGGED_OUT_EVENT = 'cwp:logged-out'

function activeHost(): number {
  try {
    const n = Number(sessionStorage.getItem(ACTIVE_KEY))
    return Number.isInteger(n) && n >= 0 && n < BASES.length ? n : 0
  } catch {
    return 0
  }
}

/**
 * Tries the host that worked last, then the others. Only a hard failure moves on: unreachable,
 * or 502/503/504 from the platform. A slow answer (a host waking from sleep) is still an answer.
 */
async function request(path: string, init?: RequestInit): Promise<Response> {
  const headers = new Headers(init?.headers)
  const token = session.get()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const start = activeHost()
  let failure: unknown = null
  for (let k = 0; k < BASES.length; k++) {
    const i = (start + k) % BASES.length
    let res: Response
    try {
      res = await fetch(BASES[i] + path, { ...init, headers })
    } catch (e) {
      failure = e
      continue
    }
    if ([502, 503, 504].includes(res.status) && k < BASES.length - 1) continue
    lastCall = Date.now()
    if (i !== start) {
      try {
        sessionStorage.setItem(ACTIVE_KEY, String(i))
      } catch {
        /* not remembered */
      }
    }
    if (res.status === 401 && !path.startsWith('/api/auth/')) {
      session.clear()
      window.dispatchEvent(new Event(LOGGED_OUT_EVENT))
    }
    return res
  }
  throw failure instanceof Error ? failure : new Error('No API host could be reached')
}

/** LeetCode company-tag frequency, read live from GitHub (frequency.FrequencyService). */
export type FreqWindow = 'thirty-days' | 'three-months' | 'six-months' | 'older' | 'all'
export type FreqRow = {
  title: string
  leetcode: string
  difficulty: string
  frequency: number
  acceptance: number
  topics: string[]
  link: string
  /** The bank question for this problem, if there is one. */
  question: string | null
  done: boolean
}
export type FreqList = { folder: string; window: FreqWindow; source: string; fetchedAt: string; rows: FreqRow[]; inBank: number; done: number }
export type HotTag = { company: string; frequency: number }
export type Hot = { companies: string[]; tags: Record<string, HotTag[]> }

/** Help while studying (lifeline.LifelineService): a hint ladder plus your own doubts. */
export type LifelineKind = 'NUDGE' | 'IDEA' | 'EXPLAIN' | 'ASK'
export type LifelineRequest = {
  context: 'question' | 'lesson' | 'general'
  ref: string
  kind: LifelineKind
  doubt?: string
  history?: { q: string; a: string }[]
}
export type LifelineHelp = { mode: 'claude' | 'gemini' | 'offline'; text: string; note: string | null }

async function call<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await request(path, init)
  if (!res.ok) throw new Error(`${init?.method ?? 'GET'} ${path} failed: ${res.status}`)
  return res.json() as Promise<T>
}

export const api = {
  campaigns: () => call<CampaignConfig[]>('/api/campaigns'),
  today: (id: string) => call<Today>(`/api/campaigns/${id}/today`),
  plan: (id: string) => call<PlanResponse>(`/api/campaigns/${id}/plan`),
  companies: () => call<CompanySummary[]>('/api/companies'),
  company: (slug: string) => call<CompanyView>(`/api/companies/${slug}`),
  questions: () => call<QuestionSummary[]>('/api/questions'),
  question: (slug: string) => call<QuestionView>(`/api/questions/${slug}`),
  review: (slug: string, answer: string) =>
    call<AnswerReview>(`/api/questions/${slug}/review`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ answer }),
    }),
  reviewHistory: (slug: string) => call<number[]>(`/api/questions/${slug}/reviews`),
  transcribe: (wav: Blob) =>
    call<{ text: string | null; error: string | null }>('/api/transcribe', { method: 'POST', headers: { 'Content-Type': 'audio/wav' }, body: wav }),
  companyFrequency: (slug: string, window: FreqWindow, folder?: string) =>
    call<{ folder: string | null; list: FreqList | null }>(
      `/api/companies/${slug}/frequency?window=${window}${folder ? `&folder=${encodeURIComponent(folder)}` : ''}`,
    ),
  frequencyFolders: () => call<string[]>('/api/frequency/folders'),
  hot: () => call<Hot>('/api/frequency/hot'),
  lifeline: (req: LifelineRequest) =>
    call<LifelineHelp>('/api/lifeline', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(req) }),
  settings: () => call<Settings>('/api/settings'),
  saveSettings: (s: Partial<{ reviewer: string; claudeKey: string; claudeModel: string; geminiKey: string; geminiModel: string; geminiFallbacks: string }>) =>
    call<Settings>('/api/settings', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(s) }),
  testProvider: (p: 'claude' | 'gemini') =>
    call<ProviderTest>(`/api/settings/test/${p}`, { method: 'POST' }),
  progress: (slug: string, action: Action, notes?: string) =>
    call<Progress>(`/api/questions/${slug}/progress`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action, notes }),
    }),
  academy: () => call<AcademyOverview>('/api/academy'),
  module: (id: string) => call<ModuleView>(`/api/academy/modules/${id}`),
  moduleDone: (id: string, done: boolean) =>
    call<ModuleSummary>(`/api/academy/modules/${id}/progress`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ done }),
    }),
  challenge: (slug: string) => call<Challenge>(`/api/code/${slug}`),
  solution: (slug: string) => call<{ code: string }>(`/api/code/${slug}/solution`),
  runCode: (slug: string, code: string, mode: 'RUN' | 'SUBMIT', customInput?: string) =>
    call<JudgeResult>(`/api/code/${slug}/run`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, mode, customInput }),
    }),
  saveDraft: (slug: string, code: string) =>
    request(`/api/code/${slug}/draft`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    }),
  playground: (code: string, stdin: string) =>
    call<PlaygroundResult>('/api/playground/run', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, stdin }),
    }),
  authStatus: () =>
    call<{ authRequired: boolean; authenticated: boolean; codeRunner: boolean; reviewer?: string }>('/api/auth/status'),
  login: async (password: string): Promise<{ ok: true } | { ok: false; message: string }> => {
    const res = await request('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ password }),
    })
    if (res.ok) {
      const body = (await res.json()) as { token: string }
      if (body.token) session.set(body.token)
      return { ok: true }
    }
    if (res.status === 429) return { ok: false, message: 'Too many attempts. Wait 10 minutes and try again.' }
    return { ok: false, message: 'Wrong password.' }
  },
  reload: () => call<{ companies: number; questions: number }>('/api/admin/reload', { method: 'POST' }),
  readiness: (campaign?: string) => call<Readiness>(`/api/insights/readiness${campaign ? `?campaign=${campaign}` : ''}`),
  week: (campaign?: string, date?: string) =>
    call<WeekReport>(`/api/insights/week?${new URLSearchParams({ ...(campaign && { campaign }), ...(date && { date }) })}`),
  wins: () => call<Win[]>('/api/insights/wins'),
  stories: () => call<Story[]>('/api/stories'),
  saveStory: (s: StoryDraft, id?: number) =>
    call<Story>(id ? `/api/stories/${id}` : '/api/stories', {
      method: id ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(s),
    }),
  deleteStory: async (id: number) => {
    const res = await request(`/api/stories/${id}`, { method: 'DELETE' })
    if (!res.ok) throw new Error(`DELETE story failed: ${res.status}`)
  },
}

/** Progress page (insights.InsightsService): evidence computed from what you have done. */
export type ReadinessArea = {
  key: string
  name: string
  score: number
  status: 'ready' | 'getting-there' | 'gap'
  lessons: { done: number; total: number }
  practice: { done: number; total: number }
  judged: { average: number | null; count: number }
  stories: number | null
  trend: number | null
  next: { kind: 'lesson' | 'practice' | 'redo' | 'story'; label: string; link: string } | null
}
export type Readiness = { areas: ReadinessArea[]; overall: number }
export type WeekReport = {
  from: string
  to: string
  lessons: { done: number; planned: number }
  practice: { done: number; planned: number }
  answers: { average: number | null; count: number }
  improved: { slug: string; title: string; from: number; to: number }[]
  weakest: { slug: string; title: string; score: number }[]
  activeDays: number
}
export type Win = { date: string; kind: 'LESSON' | 'QUESTION' | 'ANSWER' | 'STORY'; title: string; link: string }
export type StoryDraft = {
  title: string
  situation: string
  task: string
  action: string
  result: string
  learned: string
  questions: string[]
}
export type Story = StoryDraft & { id: number; createdAt: string; updatedAt: string }

export const leetcodeUrl = (slug: string) => `https://leetcode.com/problems/${slug}/`
