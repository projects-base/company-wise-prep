import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { api, type PlanDay, type PlanItem } from '../api'
import { useCampaign } from '../App'
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
          Ordered by how often and how recently each question was reported at{' '}
          {Object.keys(c.companies).map(companyLabel).join(', ')}. Dates are targets, not deadlines — Today always serves the next unfinished
          item.
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
                    {d.kind === 'REVIEW' ? (
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
  for (let i = 0; i < days.length; i += 7) {
    const chunk = days.slice(i, i + 7)
    out.push({
      label: `Week ${i / 7 + 1}`,
      range: `${fmtDate(chunk[0].date, { day: 'numeric', month: 'short' })} – ${fmtDate(chunk[chunk.length - 1].date, { day: 'numeric', month: 'short' })}`,
      days: chunk,
      current: chunk.some((d) => d.date === today),
    })
  }
  if (!out.some((w) => w.current) && out.length) out[0].current = true
  return out
}
