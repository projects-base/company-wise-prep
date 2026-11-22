import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Flame, Pause, Pencil, Play, RotateCcw, Trash } from 'lucide-react'
import { api, type Card, type CardSource, type Grade, type ReviewStats } from '../api'
import { BACK_MAX, FRONT_MAX } from '../LessonNotes'
import { conceptHref } from '../LinkedText'
import { fmtDate, MdLink, Status, useLoad } from '../ui'

/**
 * The daily review: due flashcards one at a time (Space shows the answer, 1–4 grades it), the streak, cards known
 * well and what is due this week; and every card, to edit, suspend or delete.
 */

/** Fired after a grade or a card change, so the side menu's due badge updates. */
export const REVIEW_CHANGED = 'review:changed'

const GRADES: { grade: Grade; label: string; key: string }[] = [
  { grade: 'AGAIN', label: 'Again', key: '1' },
  { grade: 'HARD', label: 'Hard', key: '2' },
  { grade: 'GOOD', label: 'Good', key: '3' },
  { grade: 'EASY', label: 'Easy', key: '4' },
]

const SOURCE_LABEL: Record<CardSource, string> = {
  HIGHLIGHT: 'From your highlight',
  MISTAKE: 'From a quiz mistake',
  SNIPPET: 'From the Playground',
  EXPLANATION: 'From your explanation',
}

/** About half a minute a card, the usual pace for short answers. */
const SECONDS_PER_CARD = 30

/** "10 min", "1 day", "4 days", "3 mo". */
export function interval(minutes: number): string {
  if (minutes < 60) return `${minutes} min`
  if (minutes < 24 * 60) return `${Math.round(minutes / 60)} h`
  const days = Math.round(minutes / (24 * 60))
  if (days < 60) return `${days} day${days === 1 ? '' : 's'}`
  return `${Math.round(days / 30)} mo`
}

/** Card text is Markdown (code fences from quizzes and snippets); [[Concept]] becomes a link to its page. */
function CardText({ text }: { text: string }) {
  // Only outside code: fenced blocks and inline code keep [[...]] as written.
  const md = text.split(/(```[\s\S]*?```|`[^`\n]*`)/).map((part, i) => (i % 2 === 1 ? part
    : part.replace(/\[\[([^[\]\n]{1,100})\]\]/g, (_, name: string) => `[${name.trim()}](#${conceptHref(name)})`))).join('')
  return (
    <div className="review-text">
      <Markdown remarkPlugins={[remarkGfm]} components={{ a: MdLink }}>{md}</Markdown>
    </div>
  )
}

const changed = () => window.dispatchEvent(new Event(REVIEW_CHANGED))

export default function ReviewPage() {
  const [tab, setTab] = useState<'today' | 'all'>('today')
  const stats = useLoad(() => api.reviewStats(), [])
  return (
    <div className="container narrow">
      <div className="tabs" role="tablist">
        <button role="tab" aria-selected={tab === 'today'} onClick={() => setTab('today')}>Today's review</button>
        <button role="tab" aria-selected={tab === 'all'} onClick={() => setTab('all')}>All cards</button>
      </div>
      <div className="review-page">
        {tab === 'today' ? <Session onGraded={stats.reload} /> : <AllCards onChange={stats.reload} />}
        <StatsPanel stats={stats.data} />
      </div>
    </div>
  )
}

function Session({ onGraded }: { onGraded: () => void }) {
  const [queue, setQueue] = useState<Card[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [index, setIndex] = useState(0)
  const [shown, setShown] = useState(false)
  const [busy, setBusy] = useState(false)
  // Two quick keypresses must send one grade; state updates too late to stop the second.
  const grading = useRef(false)
  const [again, setAgain] = useState(0)

  const load = useCallback(() => {
    setError(null)
    api.reviewQueue().then((q) => {
      setQueue(q)
      setIndex(0)
      setShown(false)
      setAgain(0)
    }, (e: Error) => setError(e.message))
  }, [])
  useEffect(load, [load])

  const card = queue && index < queue.length ? queue[index] : null
  const grade = useCallback(async (g: Grade) => {
    if (!card || grading.current) return
    grading.current = true
    setBusy(true)
    setError(null)
    try {
      await api.gradeCard(card.id, g)
      if (g === 'AGAIN') setAgain((n) => n + 1)
      setIndex((i) => i + 1)
      setShown(false)
      onGraded()
      changed()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      grading.current = false
      setBusy(false)
    }
  }, [card, onGraded])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement
      if (!card || t.closest('input, textarea, select, [contenteditable="true"]') || e.ctrlKey || e.metaKey || e.altKey) return
      if (e.key === ' ' && !shown) {
        e.preventDefault()
        setShown(true)
      } else if (shown) {
        const g = GRADES.find((x) => x.key === e.key)
        if (g) {
          e.preventDefault()
          grade(g.grade)
        }
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [card, shown, grade])

  if (!queue) return <Status error={error} />
  if (!card) {
    return (
      <section className="card pad review-done">
        <h1>{queue.length ? 'Done for now' : 'Nothing due'}</h1>
        <p className="muted">
          {queue.length ? `You reviewed ${queue.length} card${queue.length === 1 ? '' : 's'}. ` : ''}
          {again > 0 ? `${again} come${again === 1 ? 's' : ''} back in 10 minutes. ` : ''}
          Make cards from highlights in any lesson ("Make flashcard"); quiz mistakes become cards by themselves.
        </p>
        <button className="btn" onClick={load}>
          <RotateCcw size={14} /> Check again
        </button>
      </section>
    )
  }

  const left = queue.length - index
  return (
    <section>
      <div className="review-head">
        <h1>Today's review</h1>
        <span className="muted num">
          Card {index + 1} of {queue.length} · about {Math.max(1, Math.round((left * SECONDS_PER_CARD) / 60))} min left
        </span>
      </div>
      <div className="bar review-progress" role="progressbar" aria-label="Review progress" aria-valuenow={index}
        aria-valuemin={0} aria-valuemax={queue.length}>
        <span style={{ width: `${(index / queue.length) * 100}%` }} />
      </div>
      <article className="card raised review-card">
        <div className="review-source">
          <span className="pill accent">{SOURCE_LABEL[card.sourceKind]}</span>
          {card.moduleId && card.moduleCode && (
            <Link to={`/academy/${card.moduleId}`} className="muted num">{card.moduleCode}</Link>
          )}
        </div>
        <div className="review-front"><CardText text={card.front} /></div>
        {shown ? (
          <>
            <hr className="review-rule" />
            <div className="review-back">
              <div className="eyebrow">Answer</div>
              {card.back ? <CardText text={card.back} /> : <p className="muted">No answer written on this card.</p>}
            </div>
            <div className="review-grades" role="group" aria-label="How well did you recall it?">
              {GRADES.map((g) => (
                <button key={g.grade} className={`btn review-grade ${g.grade.toLowerCase()}`} disabled={busy}
                  onClick={() => grade(g.grade)} title={`Key ${g.key}`}>
                  {g.label}
                  <span className="num">{g.grade === 'AGAIN' ? 'in ' : ''}{interval(card.next[g.grade])}</span>
                </button>
              ))}
            </div>
          </>
        ) : (
          <button className="btn primary review-show" onClick={() => setShown(true)}>
            Show answer <kbd>Space</kbd>
          </button>
        )}
        {error && <p className="error">{error}</p>}
      </article>
      <p className="muted review-keys">Space shows the answer · 1 Again · 2 Hard · 3 Good · 4 Easy</p>
    </section>
  )
}

function StatsPanel({ stats }: { stats: ReviewStats | null }) {
  if (!stats) return <aside className="review-stats" />
  const max = Math.max(1, ...stats.week.map((d) => d.count))
  return (
    <aside className="review-stats" aria-label="Review stats">
      <div className="card stat">
        <div className="stat-label"><Flame size={14} /> Streak</div>
        <div className="stat-value">{stats.streak}<span className="stat-unit"> day{stats.streak === 1 ? '' : 's'}</span></div>
        {!stats.reviewedToday && stats.streak > 0 && <div className="muted review-hint">Review today to keep it.</div>}
      </div>
      <div className="card stat">
        <div className="stat-label">Cards you know well</div>
        <div className="stat-value">{stats.knownWell}<span className="stat-unit"> of {stats.active}</span></div>
        <div className="muted review-hint">Interval of 3 weeks or more</div>
      </div>
      <div className="card stat">
        <div className="stat-label">Due next 7 days</div>
        <div className="review-week" role="img"
          aria-label={stats.week.map((d) => `${fmtDate(d.date, { weekday: 'short' })} ${d.count}`).join(', ')}>
          {stats.week.map((d) => (
            <div key={d.date} className="review-day"
              title={`${fmtDate(d.date, { weekday: 'long', day: 'numeric', month: 'short' })}: ${d.count}`}>
              <span className="num">{d.count}</span>
              <div className="review-day-bar"><span style={{ height: `${(d.count / max) * 100}%` }} /></div>
              <span className="muted">{fmtDate(d.date, { weekday: 'narrow' })}</span>
            </div>
          ))}
        </div>
      </div>
    </aside>
  )
}

function AllCards({ onChange }: { onChange: () => void }) {
  const { data, error, reload } = useLoad(() => api.cards(), [])
  if (!data) return <Status error={error} />
  if (!data.length) return <p className="muted">No cards yet.</p>
  const refresh = () => {
    reload()
    onChange()
    changed()
  }
  return (
    <ul className="review-list">
      {data.map((c) => (
        <CardRow key={c.id} c={c} onChange={refresh} />
      ))}
    </ul>
  )
}

function CardRow({ c, onChange }: { c: Card; onChange: () => void }) {
  const [editing, setEditing] = useState(false)
  const [front, setFront] = useState(c.front)
  const [back, setBack] = useState(c.back ?? '')
  const [error, setError] = useState<string | null>(null)
  const run = async (work: () => Promise<unknown>) => {
    setError(null)
    try {
      await work()
      setEditing(false)
      onChange()
    } catch (e) {
      setError((e as Error).message)
    }
  }
  return (
    <li className={`card pad review-row ${c.suspended ? 'suspended' : ''}`}>
      <div className="review-source">
        <span className="pill">{SOURCE_LABEL[c.sourceKind]}</span>
        {c.moduleCode && <Link to={`/academy/${c.moduleId}`} className="muted num">{c.moduleCode}</Link>}
        <span className="muted">
          {c.suspended ? 'Suspended' : `Due ${new Date(c.due).toLocaleDateString(undefined, { day: 'numeric', month: 'short' })}`}
        </span>
      </div>
      {editing ? (
        <form className="card-editor" onSubmit={(e) => {
          e.preventDefault()
          run(() => api.editCard(c.id, { front: front.trim(), back: back.trim() || null, suspended: c.suspended }))
        }}>
          <label className="card-editor-label" htmlFor={`f-${c.id}`}>Front</label>
          <textarea id={`f-${c.id}`} rows={2} maxLength={FRONT_MAX} value={front} onChange={(e) => setFront(e.target.value)} />
          <label className="card-editor-label" htmlFor={`b-${c.id}`}>Back</label>
          <textarea id={`b-${c.id}`} rows={4} maxLength={BACK_MAX} value={back} onChange={(e) => setBack(e.target.value)} />
          <div className="row">
            <button type="submit" className="btn sm primary" disabled={!front.trim()}>Save</button>
            <button type="button" className="btn sm ghost" onClick={() => {
              setFront(c.front)
              setBack(c.back ?? '')
              setEditing(false)
            }}>Cancel</button>
          </div>
        </form>
      ) : (
        <CardText text={c.front} />
      )}
      <div className="notes-item-meta">
        {!editing && (
          <button className="btn sm ghost" onClick={() => setEditing(true)}>
            <Pencil size={13} /> Edit
          </button>
        )}
        <button className="btn sm ghost"
          onClick={() => run(() => api.editCard(c.id, { front: c.front, back: c.back, suspended: !c.suspended }))}>
          {c.suspended ? <Play size={13} /> : <Pause size={13} />} {c.suspended ? 'Resume' : 'Suspend'}
        </button>
        <button className="btn sm ghost" aria-label="Delete this card" onClick={() => {
          if (window.confirm('Delete this card? Your past reviews still count for the streak.')) run(() => api.deleteCard(c.id))
        }}>
          <Trash size={13} />
        </button>
      </div>
      {error && <p className="error">{error}</p>}
    </li>
  )
}
