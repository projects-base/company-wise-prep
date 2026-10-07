// Types mirror the Java records in com.companywiseprep (field names are the record components).

export type QType = 'DSA' | 'LLD' | 'HLD' | 'BEHAVIORAL' | 'DOMAIN' | 'JAVA' | 'SPRING'

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
  followUps: string[]
  academy: string[]
  sightings: Sighting[]
  progress: Progress
  runnable: boolean
}

export interface ModuleSummary {
  id: string
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
  tracks: { id: string; title: string; description: string; modules: ModuleSummary[] }[]
  done: number
  total: number
  written: number
}

export interface Ref {
  id: string
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

async function call<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, init)
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
    fetch(`/api/code/${slug}/draft`, {
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
  reload: () => call<{ companies: number; questions: number }>('/api/admin/reload', { method: 'POST' }),
}

export const leetcodeUrl = (slug: string) => `https://leetcode.com/problems/${slug}/`
