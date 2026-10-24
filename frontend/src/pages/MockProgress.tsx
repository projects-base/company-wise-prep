import { useState } from 'react'
import { Check, ChevronDown, CircleAlert, Minus, RotateCcw, Sparkles, Target, TrendingUp, X } from 'lucide-react'
import { fmtDate } from '../ui'

/* The `mocks:` block of company.yaml (docs/COMPANY-PREP.md §1); every field is optional. */
/* eslint-disable @typescript-eslint/no-explicit-any */

const LEVEL: Record<string, { label: string; cls: string; Icon: typeof Check }> = {
  strong: { label: 'Strong', cls: 'ok', Icon: Check },
  partial: { label: 'Partial', cls: 'warn', Icon: Minus },
  weak: { label: 'Weak', cls: 'bad', Icon: X },
}

const day = (iso: string) => fmtDate(iso, { day: 'numeric', month: 'short' })
const signed = (n: number) => `${n >= 0 ? '+' : ''}${n}`

/**
 * Built for reading under stress: a headline and three numbers first, one idea per card, and
 * details only on tap. The gaps are flip cards, so recalling the fix comes before reading it.
 */
export function MockProgress({ m }: { m: any }) {
  const sessions: any[] = m.sessions ?? []
  const mocks = sessions.filter((s) => s.kind === 'mock')
  const quizzes = sessions.filter((s) => s.kind !== 'mock')
  const first = mocks[0]
  const last = mocks[mocks.length - 1]
  const [notes, setNotes] = useState(false)

  return (
    <div className="stack" style={{ gap: 28 }}>
      {last && (
        <section className="stat-row">
          <Stat icon={<Sparkles size={16} />} label="Latest mock" value={`${last.score}`} unit="/10" />
          {first && first !== last && (
            <Stat icon={<TrendingUp size={16} />} label="Since mock 1" value={signed(last.score - first.score)} tone="ok" />
          )}
          {m.target != null && (
            <Stat icon={<Target size={16} />} label={`To target ${m.target}`} value={`${Math.max(0, m.target - last.score)}`} unit=" to go" />
          )}
        </section>
      )}
      {m.headline && <p className="progress-headline">{m.headline}</p>}

      <section className="card pad">
        <TrendChart mocks={mocks} target={m.target} />
        {quizzes.length > 0 && (
          <div className="row" style={{ gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
            <span className="muted" style={{ fontSize: '0.84rem' }}>Quizzes</span>
            {quizzes.map((q, i) => (
              <span key={i} className="pill ok" title={q.verdict}>
                <Check size={12} /> {q.title} {q.score}/{q.out_of}
              </span>
            ))}
          </div>
        )}
      </section>

      {(m.lacking ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>Your {m.lacking.length} gaps</h2>
            <span className="muted" style={{ fontSize: '0.86rem' }}>Say the fix out loud, then tap to check</span>
          </div>
          <div className="flip-grid">
            {m.lacking.map((g: any, i: number) => (
              <FlipCard key={i} n={i + 1} front={g.gap} back={g.fix} />
            ))}
          </div>
        </section>
      )}

      {(m.next ?? []).length > 0 && (
        <section className="card pad next-steps">
          <h3>From {last?.score} to {m.target}</h3>
          <ul>
            {[].concat(m.next).map((t: string, i: number) => (
              <li key={i}>{t}</li>
            ))}
          </ul>
        </section>
      )}

      {(m.doing_well ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>Doing well</h2>
          </div>
          <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
            {m.doing_well.map((t: string, i: number) => (
              <span key={i} className="chip-ok">
                <Check size={14} /> {t}
              </span>
            ))}
          </div>
        </section>
      )}

      {(m.skills ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>Skill by skill</h2>
            <button className="btn ghost sm" onClick={() => setNotes(!notes)}>
              {notes ? 'Hide' : 'Show'} what happened
            </button>
          </div>
          <div className="card" style={{ overflowX: 'auto' }}>
            <table className="skill-grid">
              <thead>
                <tr>
                  <th>Skill</th>
                  {mocks.map((s, i) => (
                    <th key={i}>
                      Mock {i + 1} <span className="muted">· {day(s.date)}</span>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {m.skills.map((k: any, i: number) => (
                  <tr key={i}>
                    <td>{k.name}</td>
                    {mocks.map((_, j) => {
                      const l = LEVEL[k.levels?.[j]]
                      return (
                        <td key={j} title={k.notes?.[j]}>
                          {l && (
                            <span className={`pill ${l.cls}`}>
                              <l.Icon size={13} /> {l.label}
                            </span>
                          )}
                          {notes && <div className="skill-note">{k.notes?.[j]}</div>}
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {m.encouragement && <Encouragement e={m.encouragement} />}

      {sessions.length > 0 && (
        <details className="card pad fold">
          <summary>
            Every session ({sessions.length}) <ChevronDown size={16} />
          </summary>
          <div className="stack" style={{ marginTop: 12 }}>
            {sessions.map((s, i) => (
              <div key={i}>
                <div className="row" style={{ gap: 8 }}>
                  <span style={{ fontWeight: 600 }}>{s.title}</span>
                  <span className="pill">{s.kind}</span>
                  <span className="muted" style={{ fontSize: '0.84rem' }}>
                    {day(s.date)} · {s.score}/{s.out_of}
                  </span>
                </div>
                <div className="dim" style={{ fontSize: '0.9rem', marginTop: 2 }}>
                  {s.verdict}
                </div>
              </div>
            ))}
          </div>
        </details>
      )}
    </div>
  )
}

function Stat({ icon, label, value, unit, tone }: { icon: React.ReactNode; label: string; value: string; unit?: string; tone?: 'ok' }) {
  return (
    <div className="card pad stat">
      <div className="stat-label">
        {icon} {label}
      </div>
      <div className={`stat-value ${tone ?? ''}`}>
        {value}
        {unit && <span className="stat-unit">{unit}</span>}
      </div>
    </div>
  )
}

function FlipCard({ n, front, back }: { n: number; front: string; back: string }) {
  const [shown, setShown] = useState(false)
  return (
    <button className={`flip ${shown ? 'shown' : ''}`} onClick={() => setShown(!shown)} aria-pressed={shown}>
      <span className="flip-no">{n}</span>
      {shown ? (
        <>
          <span className="flip-tag ok">
            <Check size={13} /> The fix
          </span>
          <span className="flip-text">{back}</span>
          <span className="flip-hint">
            <RotateCcw size={12} /> tap to hide
          </span>
        </>
      ) : (
        <>
          <span className="flip-tag warn">
            <CircleAlert size={13} /> Gap
          </span>
          <span className="flip-text">{front}</span>
          <span className="flip-hint">What's the fix? Tap to check</span>
        </>
      )}
    </button>
  )
}

function Encouragement({ e }: { e: any }) {
  const [open, setOpen] = useState(false)
  const title = typeof e === 'string' ? e : e.title
  const points: string[] = typeof e === 'string' ? [] : (e.points ?? [])
  return (
    <section className="callout ok encourage">
      <Sparkles size={20} />
      <div className="grow">
        <div className="encourage-title">{title}</div>
        {points.length > 0 &&
          (open ? (
            <ul className="followups" style={{ marginTop: 8 }}>
              {points.map((p, i) => (
                <li key={i}>{p}</li>
              ))}
            </ul>
          ) : (
            <button className="link-btn" onClick={() => setOpen(true)}>
              Why? ({points.length} reasons)
            </button>
          ))}
      </div>
    </section>
  )
}

/** One series, so no legend: a 2px line, labelled points and a dashed target line. */
function TrendChart({ mocks, target }: { mocks: any[]; target?: number }) {
  const [hover, setHover] = useState<number | null>(null)
  if (!mocks.length) return <p className="muted">No mocks yet.</p>
  const W = 560, H = 220, L = 34, R = 24, T = 18, B = 44
  const x = (i: number) => (mocks.length === 1 ? (L + W - R) / 2 : L + 24 + (i * (W - L - R - 48)) / (mocks.length - 1))
  const y = (v: number) => T + ((10 - v) * (H - T - B)) / 10
  const h = hover != null ? mocks[hover] : null

  return (
    <div className="trend-wrap">
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="trend-chart"
        role="img"
        aria-label={`Mock scores: ${mocks.map((s) => `${day(s.date)} ${s.score}`).join(', ')}`}
        onMouseLeave={() => setHover(null)}
      >
        {[0, 2, 4, 6, 8, 10].map((v) => (
          <g key={v}>
            <line x1={L} x2={W - R} y1={y(v)} y2={y(v)} className="grid" />
            <text x={L - 8} y={y(v) + 4} textAnchor="end" className="axis">
              {v}
            </text>
          </g>
        ))}
        {target != null && (
          <g>
            <line x1={L} x2={W - R} y1={y(target)} y2={y(target)} className="target" />
            <text x={W - R} y={y(target) - 6} textAnchor="end" className="axis">
              Target {target}
            </text>
          </g>
        )}
        <polyline points={mocks.map((s, i) => `${x(i)},${y(s.score)}`).join(' ')} className="line" />
        {mocks.map((s, i) => (
          <g key={i} onMouseEnter={() => setHover(i)} onClick={() => setHover(hover === i ? null : i)} style={{ cursor: 'pointer' }}>
            <rect x={x(i) - 40} y={T} width={80} height={H - T - B} fill="transparent" />
            <circle cx={x(i)} cy={y(s.score)} r={hover === i ? 7 : 5} className="dot" />
            <text x={x(i)} y={y(s.score) - 13} textAnchor="middle" className="value">
              {s.score}
            </text>
            <text x={x(i)} y={H - B + 20} textAnchor="middle" className="axis">
              Mock {i + 1}
            </text>
            <text x={x(i)} y={H - B + 35} textAnchor="middle" className="axis">
              {day(s.date)}
            </text>
          </g>
        ))}
      </svg>
      <div className="trend-tip" aria-live="polite">
        {h ? (
          <>
            <strong>
              {h.title} · {h.score}/10
            </strong>{' '}
            <span className="dim">{h.verdict}</span>
          </>
        ) : (
          <span className="muted">Hover or tap a point to see what happened in that mock.</span>
        )}
      </div>
    </div>
  )
}
