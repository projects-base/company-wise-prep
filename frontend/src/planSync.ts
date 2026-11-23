import { useEffect, useSyncExternalStore } from 'react'
import { api, type CampaignConfig, type LessonStatus, type PlanTick, type Session } from './api'

/**
 * Plan session checkboxes, shared by Plan and Today: manual ticks kept on the server (so dev, local and hosted show the
 * same boxes) plus sessions that tick themselves, because the lesson they name is done or every question they list is.
 * One store, loaded once per page, so rows never fetch on their own.
 */

/** "Scratch · Step 8 of 136 · JAVA-FUND-4: …" → "Scratch · JAVA-FUND-4: …": the server's key (PlanTickService.key). */
export const tickKey = (title: string) => title.replace(/Step \d+ of \d+ · /g, '').trim()

// "Scratch · Step 12 of 110 · JAVA-JVM-2: …" — the API opens a lesson by its code as well as its id.
const LESSON = /^(?:Scratch · Step \d+ of \d+|Capstone) · ([A-Z]+(?:-[A-Z]+)*-\d+):/
export const sessionLesson = (title: string) => LESSON.exec(title)?.[1]

const MAX_STEP = 50

interface State {
  /** The campaign whose ticks are loaded; null until the first load lands (checkboxes stay read-only till then). */
  campaign: string | null
  ticks: Map<string, number[]>
  /** By id and by code: session titles name lessons by code. */
  lessons: Map<string, LessonStatus>
  doneQuestions: Set<string>
}

let state: State = { campaign: null, ticks: new Map(), lessons: new Map(), doneQuestions: new Set() }
const listeners = new Set<() => void>()
const emit = (next: Partial<State>) => {
  state = { ...state, ...next }
  listeners.forEach((l) => l())
}
const subscribe = (l: () => void) => {
  listeners.add(l)
  return () => listeners.delete(l)
}
const at = (date: string, key: string) => `${date}|${key}`

export const getPlanSync = () => state

export type SessionStatus = {
  ticks: number[]
  /** Why the session counts as done without a tick: its lesson is done, or all its questions are. */
  auto: 'lesson' | 'questions' | null
  /** Lesson parts ticked so far, while the lesson itself is not done. */
  parts: { done: number; total: number } | null
  loaded: boolean
}

export function sessionStatus(s: State, date: string, session: Session): SessionStatus {
  const lesson = s.lessons.get(sessionLesson(session.title) ?? '')
  const questionsDone = session.questions.length > 0 && session.questions.every((q) => s.doneQuestions.has(q))
  return {
    ticks: s.ticks.get(at(date, tickKey(session.title))) ?? [],
    auto: lesson?.done ? 'lesson' : questionsDone ? 'questions' : null,
    parts: lesson && !lesson.done && lesson.partsDone > 0 ? { done: lesson.partsDone, total: lesson.parts } : null,
    loaded: s.campaign !== null,
  }
}

let generation = 0

/** Loads ticks, lesson progress and done questions for a campaign, then moves this browser's old local ticks over once. */
export async function refreshPlanSync(campaign: CampaignConfig) {
  const mine = ++generation
  // Another campaign's ticks must not show (or be edited) while this one loads.
  if (state.campaign !== campaign.id) emit({ campaign: null, ticks: new Map() })
  const [ticks, lessons, questions] = await Promise.all([api.ticks(campaign.id), api.lessonStatus(), api.questions()])
  if (mine !== generation) return // a newer load (another campaign, or a focus refresh) wins
  const byKey = new Map(ticks.map((t) => [at(t.date, t.key), t.steps]))
  emit({
    campaign: campaign.id,
    ticks: byKey,
    lessons: new Map(lessons.flatMap((l) => [[l.id, l], [l.code, l]] as const)),
    doneQuestions: new Set(questions.filter((q) => q.done).map((q) => q.slug)),
  })
  await migrateLocalTicks(campaign, byKey)
}

/** Plan and Today call this once: loads on mount and when the window regains focus (a lesson finished in another tab). */
export function usePlanSync(campaign: CampaignConfig | null | undefined) {
  useEffect(() => {
    if (!campaign) return
    const load = () => refreshPlanSync(campaign).catch((e) => console.error('Plan ticks could not be loaded', e))
    load()
    window.addEventListener('focus', load)
    return () => window.removeEventListener('focus', load)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [campaign?.id])
  return usePlanState()
}

export const usePlanState = () => useSyncExternalStore(subscribe, getPlanSync)

/**
 * Sets a session's ticked steps: shown at once, then saved; a failed save puts the old ticks back, so the box never
 * claims a tick the server doesn't have.
 */
export async function saveTicks(date: string, title: string, steps: number[]) {
  const campaign = state.campaign
  if (!campaign) return
  const k = at(date, tickKey(title))
  const before = state.ticks.get(k) ?? []
  emit({ ticks: new Map(state.ticks).set(k, steps) })
  try {
    await api.setTicks(campaign, { date, key: title, steps })
  } catch (e) {
    if (state.campaign === campaign) emit({ ticks: new Map(state.ticks).set(k, before) })
    console.error('Tick not saved', e)
  }
}

const migratedFlag = (campaign: string) => `ticks.migrated.${campaign}`

/**
 * Ticks used to live only in this browser under "ticks.<date>.<title>". Each one that names a session of this campaign
 * is merged into the server's ticks; its local key is removed only after the server took it, and the campaign is
 * marked migrated so this runs once. Keys for other campaigns' sessions stay for those campaigns.
 */
async function migrateLocalTicks(campaign: CampaignConfig, server: Map<string, number[]>) {
  try {
    if (localStorage.getItem(migratedFlag(campaign.id)) === '1') return
  } catch {
    return // private window: nothing stored locally either
  }
  const sessions = new Set(campaign.sessions.map((s) => at(s.date, tickKey(s.title))))
  const merged = new Map<string, { tick: PlanTick; localKeys: string[] }>()
  for (const localKey of Object.keys(localStorage)) {
    const m = /^ticks\.(\d{4}-\d{2}-\d{2})\.(.+)$/.exec(localKey)
    if (!m) continue
    const key = tickKey(m[2])
    const k = at(m[1], key)
    if (!sessions.has(k)) continue
    const entry = merged.get(k) ?? { tick: { date: m[1], key, steps: server.get(k) ?? [] }, localKeys: [] }
    entry.tick.steps = [...new Set([...entry.tick.steps, ...readLocal(localKey)])].sort((a, b) => a - b)
    entry.localKeys.push(localKey)
    merged.set(k, entry)
  }
  let ok = true
  for (const { tick, localKeys } of merged.values()) {
    try {
      const saved = await api.setTicks(campaign.id, tick)
      if (state.campaign === campaign.id) emit({ ticks: new Map(state.ticks).set(at(saved.date, saved.key), saved.steps) })
      localKeys.forEach((k) => localStorage.removeItem(k))
    } catch (e) {
      ok = false
      console.error('Old local ticks not moved to the server; will retry next load', e)
    }
  }
  if (ok) localStorage.setItem(migratedFlag(campaign.id), '1')
}

function readLocal(key: string): number[] {
  try {
    const v: unknown = JSON.parse(localStorage.getItem(key) ?? '[]')
    return Array.isArray(v) ? v.filter((n): n is number => Number.isInteger(n) && n >= 0 && n <= MAX_STEP) : []
  } catch {
    return []
  }
}
