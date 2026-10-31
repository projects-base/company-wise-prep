import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  ArrowRight, BookOpen, ChevronLeft, ChevronRight, MessageSquareQuote, Pencil, Plus, Sparkles, Trash2, TrendingDown,
  TrendingUp, Trophy,
} from 'lucide-react'
import { api, type ReadinessArea, type Story, type StoryDraft, type Win } from '../api'
import { useCampaign } from '../App'
import { Bar, fmtDate, Status, useLoad } from '../ui'

const TABS = [
  ['readiness', 'Readiness'],
  ['week', 'This week'],
  ['wins', 'Wins'],
  ['stories', 'Stories'],
] as const
type Tab = (typeof TABS)[number][0]

/** Evidence that you are getting ready: by area, by week, every win, and your own stories. */
export default function ProgressPage() {
  const { tab = 'readiness' } = useParams()
  const navigate = useNavigate()
  const current = (TABS.some(([t]) => t === tab) ? tab : 'readiness') as Tab
  return (
    <div className="container">
      <header className="page-head">
        <div>
          <div className="eyebrow">Progress</div>
          <h1 style={{ marginTop: 6 }}>How ready you are</h1>
          <p className="sub">Built only from what you have actually done: lessons, problems, judged answers and stories.</p>
        </div>
      </header>
      <div className="tabs" role="tablist">
        {TABS.map(([t, label]) => (
          <button key={t} role="tab" aria-selected={current === t} onClick={() => navigate(`/progress/${t}`)}>
            {label}
          </button>
        ))}
      </div>
      {current === 'readiness' && <ReadinessView />}
      {current === 'week' && <WeekView />}
      {current === 'wins' && <WinsView />}
      {current === 'stories' && <StoriesView />}
    </div>
  )
}

// ── Readiness ──────────────────────────────────────────────────────────────────────────────

const STATUS: Record<ReadinessArea['status'], { label: string; cls: string }> = {
  ready: { label: 'Ready', cls: 'ok' },
  'getting-there': { label: 'Getting there', cls: 'accent' },
  gap: { label: 'Gap', cls: 'warn' },
}

function ReadinessView() {
  const campaign = useCampaign()
  const { data, error } = useLoad(() => api.readiness(campaign?.id), [campaign?.id])
  if (!data) return <Status error={error} />
  return (
    <>
      <div className="card readiness-overall">
        <div className="readiness-ring" style={{ ['--p' as string]: data.overall }}>
          <span className="num">{data.overall}</span>
        </div>
        <div>
          <div className="eyebrow">Overall readiness</div>
          <p style={{ margin: '6px 0 0' }}>
            {data.overall >= 75
              ? 'Interview-ready across the board. Keep it warm with reviews and mocks.'
              : data.overall >= 45
                ? 'Solid progress. Close the gaps below one next step at a time.'
                : 'Early days. Every lesson and problem moves these numbers; follow the next step in each area.'}
          </p>
        </div>
      </div>
      <div className="readiness-grid">
        {data.areas.map((a) => (
          <AreaCard key={a.key} a={a} />
        ))}
      </div>
      <p className="hint" style={{ marginTop: 14 }}>
        Each area averages what applies: lessons done, planned practice done, your latest judged score per question, and
        (behavioural) stories written out of 5.
      </p>
    </>
  )
}

function AreaCard({ a }: { a: ReadinessArea }) {
  const st = STATUS[a.status]
  return (
    <div className="card area-card">
      <div className="row" style={{ justifyContent: 'space-between' }}>
        <h3>{a.name}</h3>
        <span className={`pill ${st.cls}`}>{st.label}</span>
      </div>
      <div className="area-score">
        <span className="num">{a.score}</span>
        <span className="muted">/ 100</span>
        {a.trend !== null && (
          <span className={`area-trend ${a.trend >= 0 ? 'up' : 'down'}`} title="Judged score: last 14 days vs the 14 before">
            {a.trend >= 0 ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
            {a.trend > 0 ? '+' : ''}
            {a.trend}
          </span>
        )}
      </div>
      <Bar value={a.score} max={100} />
      <dl className="area-facts">
        <dt>Lessons</dt>
        <dd className="num">
          {a.lessons.done}/{a.lessons.total}
        </dd>
        <dt>Practice</dt>
        <dd className="num">
          {a.practice.done}/{a.practice.total}
        </dd>
        <dt>Judged</dt>
        <dd className="num">{a.judged.count ? `${a.judged.average}/10 · ${a.judged.count}` : '—'}</dd>
        {a.stories !== null && (
          <>
            <dt>Stories</dt>
            <dd className="num">{a.stories}/5</dd>
          </>
        )}
      </dl>
      {a.next ? (
        <Link to={a.next.link} className="area-next">
          <span className="eyebrow">Next step</span>
          <span>{a.next.label}</span>
          <ArrowRight size={15} />
        </Link>
      ) : (
        <span className="area-next done">Everything here is done.</span>
      )}
    </div>
  )
}

// ── The week ───────────────────────────────────────────────────────────────────────────────

function isoDay(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function WeekView() {
  const campaign = useCampaign()
  const [date, setDate] = useState<string | undefined>(undefined)
  const { data, error } = useLoad(() => api.week(campaign?.id, date), [campaign?.id, date])
  if (!data) return <Status error={error} />
  const shift = (days: number) => {
    const d = new Date(data.from + 'T00:00:00')
    d.setDate(d.getDate() + days)
    setDate(isoDay(d))
  }
  const lessonPct = data.lessons.planned ? data.lessons.done / data.lessons.planned : 0
  return (
    <>
      <div className="row week-head">
        <button className="icon-btn" onClick={() => shift(-7)} aria-label="Previous week">
          <ChevronLeft size={16} />
        </button>
        <h2>
          {fmtDate(data.from, { day: 'numeric', month: 'short' })} – {fmtDate(data.to, { day: 'numeric', month: 'short' })}
        </h2>
        <button className="icon-btn" onClick={() => shift(7)} aria-label="Next week">
          <ChevronRight size={16} />
        </button>
      </div>
      <div className="stats week-stats">
        <Tile value={`${data.lessons.done}/${data.lessons.planned}`} label="Lessons done / planned" />
        <Tile value={`${data.practice.done}/${data.practice.planned}`} label="Problems done / planned core" />
        <Tile value={data.answers.count ? `${data.answers.average}` : '—'} label={`Avg judged score · ${data.answers.count} answers`} />
        <Tile value={`${data.activeDays}/7`} label="Days you studied" />
      </div>
      <p className="week-note">
        {data.activeDays === 0
          ? 'Nothing logged yet this week. One floor session (just the lesson) starts the streak.'
          : lessonPct >= 1
            ? 'Every planned lesson done. That is exactly how the plan stays on time.'
            : `${data.activeDays} active ${data.activeDays === 1 ? 'day' : 'days'}. Sunday's catch-up slot takes whatever core session is left.`}
      </p>
      <div className="grid-2">
        <section className="card week-list">
          <h3>
            <Sparkles size={15} /> Most improved answers
          </h3>
          {data.improved.length ? (
            data.improved.map((i) => (
              <Link key={i.slug} to={`/q/${i.slug}`} className="week-row">
                <span>{i.title}</span>
                <span className="num up">
                  {i.from} → {i.to}
                </span>
              </Link>
            ))
          ) : (
            <p className="muted">Answer a question again with Judge my answer to see it climb.</p>
          )}
        </section>
        <section className="card week-list">
          <h3>
            <TrendingDown size={15} /> Redo these
          </h3>
          {data.weakest.length ? (
            data.weakest.map((w) => (
              <Link key={w.slug} to={`/q/${w.slug}`} className="week-row">
                <span>{w.title}</span>
                <span className="num down">{w.score}/10</span>
              </Link>
            ))
          ) : (
            <p className="muted">No judged answer below 7. Nice.</p>
          )}
        </section>
      </div>
    </>
  )
}

function Tile({ value, label }: { value: string; label: string }) {
  return (
    <div className="card stat">
      <div className="value">{value}</div>
      <div className="label">{label}</div>
    </div>
  )
}

// ── Wins ───────────────────────────────────────────────────────────────────────────────────

const WIN_ICON: Record<Win['kind'], typeof Trophy> = {
  LESSON: BookOpen,
  QUESTION: Trophy,
  ANSWER: Sparkles,
  STORY: MessageSquareQuote,
}

function WinsView() {
  const { data, error } = useLoad(() => api.wins(), [])
  if (!data) return <Status error={error} />
  if (!data.length) {
    return <p className="muted">Your wins land here: every lesson finished, problem solved, answer scored 8+ and story written.</p>
  }
  const byDay = new Map<string, Win[]>()
  data.forEach((w) => byDay.set(w.date, [...(byDay.get(w.date) ?? []), w]))
  const count = (k: Win['kind']) => data.filter((w) => w.kind === k).length
  return (
    <>
      <div className="stats week-stats">
        <Tile value={String(count('LESSON'))} label="Lessons finished" />
        <Tile value={String(count('QUESTION'))} label="Problems and questions done" />
        <Tile value={String(count('ANSWER'))} label="Answers scored 8+" />
        <Tile value={String(count('STORY'))} label="Stories written" />
      </div>
      <div className="wins">
        {[...byDay.entries()].map(([day, wins]) => (
          <section key={day} className="wins-day">
            <div className="eyebrow">{fmtDate(day)}</div>
            {wins.map((w, i) => {
              const Icon = WIN_ICON[w.kind]
              return (
                <Link key={i} to={w.link} className={`win win-${w.kind.toLowerCase()}`}>
                  <Icon size={15} />
                  <span>{w.title}</span>
                </Link>
              )
            })}
          </section>
        ))}
      </div>
    </>
  )
}

// ── Stories ────────────────────────────────────────────────────────────────────────────────

const EMPTY: StoryDraft = { title: '', situation: '', task: '', action: '', result: '', learned: '', questions: [] }
const FIELDS: [keyof StoryDraft, string, string][] = [
  ['situation', 'Situation', 'Where, when, what was at stake (1–2 lines)'],
  ['task', 'Task', 'What you owned'],
  ['action', 'Action', 'What you did, in "I" not "we": the decisions and why'],
  ['result', 'Result', 'Numbers if you have them: time saved, errors cut, users helped'],
  ['learned', 'What you learned', 'What you would do differently next time'],
]

function StoriesView() {
  const { data, error, reload } = useLoad(() => Promise.all([api.stories(), api.questions()]), [])
  const [editing, setEditing] = useState<Story | 'new' | null>(null)
  if (!data) return <Status error={error} />
  const [stories, questions] = data
  const behavioural = questions.filter((q) => q.type === 'BEHAVIORAL').sort((a, b) => a.title.localeCompare(b.title))
  const titleOf = new Map(behavioural.map((q) => [q.slug, q.title]))

  if (editing) {
    return (
      <StoryForm
        initial={editing === 'new' ? EMPTY : editing}
        questions={behavioural}
        onCancel={() => setEditing(null)}
        onSave={async (d) => {
          await api.saveStory(d, editing === 'new' ? undefined : editing.id)
          setEditing(null)
          reload()
        }}
      />
    )
  }
  return (
    <>
      <div className="row" style={{ justifyContent: 'space-between', marginBottom: 14, flexWrap: 'wrap', gap: 10 }}>
        <p className="muted" style={{ margin: 0 }}>
          Your own STAR stories, linked to the behavioural questions they answer. Saved in your database only, never in the
          repo. Aim for 5 strong ones: one story can answer several questions.
        </p>
        <button className="btn primary" onClick={() => setEditing('new')}>
          <Plus size={15} /> New story
        </button>
      </div>
      {stories.length === 0 && (
        <div className="callout">
          <MessageSquareQuote size={18} />
          <span>Start with your hardest project, a conflict, and a failure: those three cover most behavioural rounds.</span>
        </div>
      )}
      <div className="stories">
        {stories.map((s) => (
          <StoryCard key={s.id} s={s} titleOf={titleOf} onEdit={() => setEditing(s)} onDeleted={reload} />
        ))}
      </div>
    </>
  )
}

function StoryCard({ s, titleOf, onEdit, onDeleted }: {
  s: Story
  titleOf: Map<string, string>
  onEdit: () => void
  onDeleted: () => void
}) {
  const [confirm, setConfirm] = useState(false)
  return (
    <article className="card story">
      <div className="row" style={{ justifyContent: 'space-between', gap: 8 }}>
        <h3>{s.title}</h3>
        <div className="row" style={{ gap: 6 }}>
          <button className="icon-btn" onClick={onEdit} aria-label="Edit story">
            <Pencil size={14} />
          </button>
          {confirm ? (
            <button className="btn sm danger" onClick={() => api.deleteStory(s.id).then(onDeleted)}>
              Delete?
            </button>
          ) : (
            <button className="icon-btn" onClick={() => setConfirm(true)} aria-label="Delete story">
              <Trash2 size={14} />
            </button>
          )}
        </div>
      </div>
      <dl className="star">
        {FIELDS.filter(([k]) => s[k]).map(([k, label]) => (
          <div key={k}>
            <dt>{label}</dt>
            <dd>{s[k] as string}</dd>
          </div>
        ))}
      </dl>
      {s.questions.length > 0 && (
        <div className="story-qs">
          {s.questions.map((q) => (
            <Link key={q} to={`/q/${q}`} className="pill">
              {titleOf.get(q) ?? q}
            </Link>
          ))}
        </div>
      )}
    </article>
  )
}

function StoryForm({ initial, questions, onSave, onCancel }: {
  initial: StoryDraft
  questions: { slug: string; title: string }[]
  onSave: (d: StoryDraft) => Promise<void>
  onCancel: () => void
}) {
  const [d, setD] = useState<StoryDraft>({ ...EMPTY, ...initial })
  const [saving, setSaving] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const set = (k: keyof StoryDraft, v: string) => setD({ ...d, [k]: v })
  const toggleQ = (slug: string) =>
    setD({ ...d, questions: d.questions.includes(slug) ? d.questions.filter((q) => q !== slug) : [...d.questions, slug] })
  return (
    <form
      className="card story-form"
      onSubmit={async (e) => {
        e.preventDefault()
        setSaving(true)
        setErr(null)
        try {
          await onSave(d)
        } catch (x) {
          setErr((x as Error).message)
          setSaving(false)
        }
      }}
    >
      <label>
        <span>Title</span>
        <input value={d.title} onChange={(e) => set('title', e.target.value)} placeholder="Cut the nightly sync from 6 h to 40 min" required maxLength={200} />
      </label>
      {FIELDS.map(([k, label, hint]) => (
        <label key={k}>
          <span>{label}</span>
          <textarea value={d[k] as string} onChange={(e) => set(k, e.target.value)} placeholder={hint} rows={k === 'action' ? 5 : 3} maxLength={4000} />
        </label>
      ))}
      <fieldset>
        <legend>Answers these questions</legend>
        <div className="story-qs pick">
          {questions.map((q) => (
            <button type="button" key={q.slug} className={`pill ${d.questions.includes(q.slug) ? 'accent' : ''}`} aria-pressed={d.questions.includes(q.slug)} onClick={() => toggleQ(q.slug)}>
              {q.title}
            </button>
          ))}
        </div>
      </fieldset>
      {err && <p className="error">Couldn't save ({err}).</p>}
      <div className="row" style={{ gap: 10 }}>
        <button className="btn primary" disabled={saving || !d.title.trim()}>
          {saving ? 'Saving…' : 'Save story'}
        </button>
        <button type="button" className="btn" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  )
}
