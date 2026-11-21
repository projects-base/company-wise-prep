import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { BookOpen, Check, RotateCcw } from 'lucide-react'
import { api, type Mistake } from '../api'
import type { LessonLinkState } from './ModulePage'
import { Status, useLoad } from '../ui'

const LEVEL_LABEL: Record<string, string> = { easy: 'Check', medium: 'Apply', hard: 'Interview trap' }
const day = (instant: string) => new Date(instant).toLocaleDateString(undefined, { day: 'numeric', month: 'short' })

/** The mistakes notebook: every ```quiz answered wrongly in a lesson, with why, until a retry fixes it. */
export default function MistakesPage() {
  const { data, error } = useLoad(() => Promise.all([api.mistakes(), api.academy()]), [])
  const [track, setTrack] = useState<string | null>(null)
  const [status, setStatus] = useState<'open' | 'fixed' | 'all'>('open')
  const [trapsOnly, setTrapsOnly] = useState(false)

  const trackTitle = useMemo(() => new Map((data?.[1].tracks ?? []).map((t) => [t.id, t.title])), [data])
  if (!data) return <div className="container"><Status error={error} /></div>
  const [mistakes] = data

  const open = mistakes.filter((m) => !m.fixed)
  const fixed = mistakes.filter((m) => m.fixed)
  const twice = mistakes.filter((m) => m.wrongCount > 1)
  const byStatus = mistakes.filter((m) => status === 'all' || (status === 'open' ? !m.fixed : m.fixed))
  const trackCounts = new Map<string, number>()
  byStatus.forEach((m) => m.trackId && trackCounts.set(m.trackId, (trackCounts.get(m.trackId) ?? 0) + 1))
  const shown = byStatus.filter((m) => (!track || m.trackId === track) && (!trapsOnly || m.level === 'hard'))

  return (
    <div className="container narrow">
      <header className="page-head">
        <div>
          <h1>Mistakes notebook</h1>
          <p className="sub">Every quiz you get wrong lands here automatically, with why. Retry it until it sticks.</p>
        </div>
      </header>

      <div className="row mistakes-stats">
        <span className="pill warn">{open.length} open</span>
        <span className="pill ok">{fixed.length} fixed on retry</span>
        <span className="pill">{twice.length} wrong twice or more</span>
      </div>

      <div className="row mistakes-filters" role="group" aria-label="Filter mistakes">
        {(['open', 'fixed', 'all'] as const).map((s) => (
          <button key={s} className={`chip ${status === s ? 'on' : ''}`} aria-pressed={status === s} onClick={() => setStatus(s)}>
            {s === 'open' ? 'Open' : s === 'fixed' ? 'Fixed' : 'All'}
          </button>
        ))}
        <span className="mistakes-sep" aria-hidden />
        <button className={`chip ${track === null ? 'on' : ''}`} aria-pressed={track === null} onClick={() => setTrack(null)}>
          All tracks
        </button>
        {[...trackCounts.entries()].map(([id, n]) => (
          <button key={id} className={`chip ${track === id ? 'on' : ''}`} aria-pressed={track === id}
            onClick={() => setTrack(id)}>
            {trackTitle.get(id) ?? id} <span className="num">{n}</span>
          </button>
        ))}
        <button className={`chip ${trapsOnly ? 'on' : ''}`} aria-pressed={trapsOnly} onClick={() => setTrapsOnly(!trapsOnly)}>
          Interview traps only
        </button>
      </div>

      {mistakes.length === 0 ? (
        <div className="callout">
          <span>No mistakes yet. Answer the quick-check questions in a lesson; any you get wrong are saved here.</span>
        </div>
      ) : shown.length === 0 ? (
        <p className="muted">Nothing matches these filters.</p>
      ) : (
        <ul className="mistakes-list">
          {shown.map((m) => (
            <MistakeCard key={m.id} m={m} />
          ))}
        </ul>
      )}
    </div>
  )
}

function MistakeCard({ m }: { m: Mistake }) {
  const navigate = useNavigate()
  const retry = () => navigate(`/academy/${m.moduleId}`, { state: { quiz: m.question } satisfies LessonLinkState })
  return (
    <li className={`card pad mistake-card ${m.fixed ? 'fixed' : ''}`}>
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <p className="mistake-q">{m.question}</p>
        {m.level && <span className={`quiz-level ${m.level}`}>{LEVEL_LABEL[m.level]}</span>}
      </div>
      {!m.fixed && (
        <dl className="mistake-answers">
          <div>
            <dt>Your answer</dt>
            <dd className="bad-text">{m.chosen}</dd>
          </div>
          <div>
            <dt>Correct</dt>
            <dd className="ok-text">{m.correct}</dd>
          </div>
        </dl>
      )}
      {!m.fixed && m.why && (
        <p className="mistake-why">
          <strong>Why:</strong> {m.why}
        </p>
      )}
      <div className="row mistake-foot">
        <span className="muted">
          {m.moduleCode} Â·{' '}
          {m.fixed && m.fixedAt
            ? `fixed on retry ${day(m.fixedAt)}`
            : `wrong ${day(m.lastWrongAt)}${m.wrongCount > 1 ? ` (${m.wrongCount} times)` : ''}`}
        </span>
        <span className="spacer" />
        {m.fixed ? (
          <span className="pill ok">
            <Check size={12} strokeWidth={3} /> Fixed
          </span>
        ) : (
          <>
            <Link to={`/academy/${m.moduleId}`} className="btn sm ghost">
              <BookOpen size={14} /> Open lesson
            </Link>
            <button className="btn sm primary" onClick={retry}>
              <RotateCcw size={14} /> Retry now
            </button>
          </>
        )}
      </div>
    </li>
  )
}
