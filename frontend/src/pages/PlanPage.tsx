import { Fragment, useState } from 'react'
import { Link } from 'react-router-dom'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { ChevronRight, ScrollText } from 'lucide-react'
import { api, type PlanDay, type PlanItem, type Session } from '../api'
import { useCampaign } from '../App'
import { celebrate, recordActivity, TICKS_CHANGED } from '../rewards'
import { Bar, CheckDot, companyLabel, Difficulty, fmtDate, localToday, Meta, Status, TypeTag, useLoad } from '../ui'

export default function PlanPage() {
  const campaign = useCampaign()
  const { data, error } = useLoad(() => (campaign ? api.plan(campaign.id) : Promise.resolve(null)), [campaign?.id])
  const [showBeyond, setShowBeyond] = useState(false)

  if (!campaign) return <div className="container"><p className="muted">No campaign yet.</p></div>
  if (!data) return <div className="container"><Status error={error} /></div>

  const done = new Set(data.done)
  const today = localToday()
  const c = data.plan.campaign
  const weeks = groupByWeek(data.plan.days)
  const scheduled = data.plan.days.flatMap((d) => d.items)
  const doneCount = scheduled.filter((i) => done.has(i.slug)).length

  return (
    <div className="container narrow">
      <header className="page-head">
        <div>
          <div className="eyebrow">Plan</div>
          <h1 style={{ marginTop: 6 }}>{c.name}</h1>
          <p className="sub">
            {fmtDate(c.start)} → {fmtDate(c.interview)} · {c.weekdayMinutes} min on weekdays, {c.weekendMinutes} on
            weekends · last {c.reviewDays} days review only
          </p>
        </div>
      </header>

      {c.guide && <Guide text={c.guide} />}

      <div className="card pad" style={{ marginBottom: 8 }}>
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <span style={{ fontWeight: 600 }}>
            {doneCount} of {scheduled.length} questions done
          </span>
          <span className="muted" style={{ fontSize: '0.86rem' }}>
            {data.plan.beyond.length} more reported questions beyond the plan
          </span>
        </div>
        <div style={{ marginTop: 12 }}>
          <Bar value={doneCount} max={scheduled.length} />
        </div>
        <p className="hint">
          {c.guide ? (
            'Every day is fixed by the syllabus in the guide above. Dates are targets: Today always serves the next unfinished session.'
          ) : (
            <>
              Ordered by how often and how recently each question was reported at{' '}
              {Object.keys(c.companies).map(companyLabel).join(', ')}. Dates are targets, not deadlines — Today always serves the next
              unfinished item.
            </>
          )}
        </p>
      </div>

      {weeks.map(({ label, range, days, current }) => {
        const items = days.flatMap((d) => d.items)
        const wDone = items.filter((i) => done.has(i.slug)).length
        return (
          <details key={label} className="card week" open={current}>
            <summary>
              <ChevronRight size={16} className="chev" />
              <span style={{ fontWeight: 600 }}>{label}</span>
              <span className="muted" style={{ fontSize: '0.86rem' }}>
                {range}
              </span>
              <span className="spacer" />
              {current && <span className="pill accent">This week</span>}
              <span className="muted num" style={{ fontSize: '0.84rem' }}>
                {wDone}/{items.length}
              </span>
              <Bar value={wDone} max={items.length} />
            </summary>
            <div className="week-body">
              {days.map((d) => (
                <div key={d.date} className={`day ${d.date === today ? 'today' : ''}`}>
                  <div className="day-date">
                    {d.date === today ? 'Today' : fmtDate(d.date)}
                    {d.weekend && d.kind === 'STUDY' && <div className="muted" style={{ fontSize: '0.75rem' }}>design</div>}
                  </div>
                  <div>
                    {(d.sessions ?? []).map((s, n) => (
                      <SessionRow key={n} session={s} date={d.date} open={d.date === today} />
                    ))}
                    {(d.sessions ?? []).length > 0 ? (
                      d.items.map((i) => <Row key={i.slug} item={i} done={done.has(i.slug)} />)
                    ) : d.kind === 'REVIEW' ? (
                      <div className="list-row muted">Reviews and a timed mock</div>
                    ) : d.items.length === 0 ? (
                      <div className="list-row muted">—</div>
                    ) : (
                      d.items.map((i) => <Row key={i.slug} item={i} done={done.has(i.slug)} />)
                    )}
                  </div>
                </div>
              ))}
            </div>
          </details>
        )
      })}

      <section className="section">
        <div className="section-head">
          <h2>Beyond the plan</h2>
          <button className="btn sm" onClick={() => setShowBeyond((s) => !s)}>
            {showBeyond ? 'Hide' : `Show ${data.plan.beyond.length}`}
          </button>
        </div>
        <p className="muted" style={{ fontSize: '0.9rem' }}>
          Reported questions that didn't fit before the interview, highest priority first.
        </p>
        {showBeyond && (
          <div className="card" style={{ marginTop: 12 }}>
            <div className="list" style={{ padding: '0 16px' }}>
              {data.plan.beyond.map((i) => (
                <Row key={i.slug} item={i} done={done.has(i.slug)} />
              ))}
            </div>
          </div>
        )}
      </section>
    </div>
  )
}

const md = { table: ({ children }: { children?: React.ReactNode }) => <div className="table-wrap"><table>{children}</table></div> }

/** The plan's rules and map: folded after the first visit so the days stay in view. */
function Guide({ text }: { text: string }) {
  const key = 'plan.guide.seen'
  const [open] = useState(() => {
    try {
      const seen = localStorage.getItem(key) === '1'
      localStorage.setItem(key, '1')
      return !seen
    } catch {
      return true
    }
  })
  return (
    <details className="card pad fold guide" open={open} style={{ marginBottom: 12 }}>
      <summary>
        <ScrollText size={16} /> The rules and the map <ChevronRight size={16} className="chev" />
      </summary>
      <div className="prose guide-body">
        <Markdown remarkPlugins={[remarkGfm]} components={md}>
          {text}
        </Markdown>
      </div>
    </details>
  )
}

/** "Academy D2" in a step becomes a link to that lesson. */
function withLessonLinks(text: string) {
  return text.split(/(Academy [A-Z]\d+[A-Z]?)\b/).map((part, i) => {
    const m = part.match(/^Academy ([A-Z]\d+[A-Z]?)$/)
    return m ? (
      <Link key={i} to={`/academy/${m[1]}`}>
        {part}
      </Link>
    ) : (
      <Fragment key={i}>{part}</Fragment>
    )
  })
}

/** "a; b. C" → steps. A leading "30:" is that step's minutes. */
export function sessionSteps(detail: string): { min?: number; text: string }[] {
  return detail
    .split(/;\s+|(?<!\b(?:e\.g|i\.e|vs)\.)(?<=[a-z)\]"'0-9]\.)\s+(?=[A-Z0-9])/)
    .map((t) => t.trim().replace(/\.$/, ''))
    .filter(Boolean)
    .map((t) => {
      const m = t.match(/^(\d{1,3}):\s+(.*)$/)
      return m ? { min: Number(m[1]), text: m[2] } : { text: t }
    })
}

export const readTicks = (k: string): number[] => {
  try {
    return JSON.parse(localStorage.getItem(k) ?? '[]')
  } catch {
    return []
  }
}

/**
 * A fixed session: the title, then its detail as steps to tick off (remembered in this browser),
 * so a long sentence of instructions becomes a short checklist.
 */
export function SessionRow({ session, date, open: startOpen = true }: { session: Session; date?: string; open?: boolean }) {
  const steps = sessionSteps(session.detail ?? '')
  const key = `ticks.${date ?? ''}.${session.title}`
  const [ticks, setTicks] = useState<number[]>(() => readTicks(key))
  const [open, setOpen] = useState(startOpen)
  const flip = (i: number) => {
    const next = ticks.includes(i) ? ticks.filter((t) => t !== i) : [...ticks, i]
    setTicks(next)
    try {
      localStorage.setItem(key, JSON.stringify(next))
    } catch {
      /* private window: not remembered */
    }
    if (next.length > ticks.length) {
      const days = recordActivity()
      if (steps.length > 0 && next.length === steps.length) {
        celebrate('Session done!', days > 1 ? `${days}-day streak, keep it going` : 'First day of your streak')
      }
    }
    window.dispatchEvent(new Event(TICKS_CHANGED))
  }
  const done = steps.length > 0 && ticks.length >= steps.length

  return (
    <div className={`list-row session-row ${done ? 'done' : ''}`}>
      <div className="grow">
        <button className="session-head" onClick={() => setOpen(!open)} aria-expanded={open}>
          <ChevronRight size={15} className={`chev ${open ? 'open' : ''}`} />
          <span className="title">{session.title}</span>
          {steps.length > 1 && (
            <span className="muted num" style={{ fontSize: '0.8rem' }}>
              {Math.min(ticks.length, steps.length)}/{steps.length} steps
            </span>
          )}
        </button>
        {open && steps.length > 0 && (
          <ul className="session-steps">
            {steps.map((st, i) => (
              <li key={i}>
                <label className={ticks.includes(i) ? 'ticked' : ''}>
                  <input type="checkbox" checked={ticks.includes(i)} onChange={() => flip(i)} />
                  {st.min != null && <span className="pill">{st.min}m</span>}
                  <span>{withLessonLinks(st.text)}</span>
                </label>
              </li>
            ))}
          </ul>
        )}
      </div>
      {session.minutes > 0 && <span className="muted num" style={{ fontSize: '0.84rem' }}>{session.minutes}m</span>}
    </div>
  )
}

function Row({ item, done }: { item: PlanItem; done: boolean }) {
  return (
    <div className={`list-row ${done ? 'done' : ''}`}>
      <CheckDot on={done} />
      <div className="grow">
        <Link to={`/q/${item.slug}`} className="title">
          {item.title}
        </Link>
      </div>
      <Meta>
        <TypeTag type={item.type} />
        <Difficulty value={item.difficulty} />
        <span className="num">{item.minutes}m</span>
      </Meta>
    </div>
  )
}

function groupByWeek(days: PlanDay[]) {
  const today = localToday()
  const out: { label: string; range: string; days: PlanDay[]; current: boolean }[] = []
  // Calendar weeks, Monday to Sunday, so "week 2" means the same dates everywhere.
  const chunks: PlanDay[][] = []
  for (const d of days) {
    if (!chunks.length || new Date(`${d.date}T00:00:00`).getDay() === 1) chunks.push([])
    chunks[chunks.length - 1].push(d)
  }
  for (const [i, chunk] of chunks.entries()) {
    out.push({
      label: `Week ${i + 1}`,
      range: `${fmtDate(chunk[0].date, { day: 'numeric', month: 'short' })} – ${fmtDate(chunk[chunk.length - 1].date, { day: 'numeric', month: 'short' })}`,
      days: chunk,
      current: chunk.some((d) => d.date === today),
    })
  }
  if (!out.some((w) => w.current) && out.length) out[0].current = true
  return out
}
