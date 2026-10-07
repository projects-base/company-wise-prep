import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Check } from 'lucide-react'
import { api, type ModuleSummary } from '../api'
import { Bar, LevelChip, LEVELS, Status, useLoad } from '../ui'

const PATH_KEY = 'cwp.academyPath'

export default function AcademyPage() {
  const { data, error } = useLoad(() => api.academy(), [])
  const [pathId, setPathId] = useState(() => {
    try {
      return localStorage.getItem(PATH_KEY) ?? 'zero-to-pro'
    } catch {
      return 'zero-to-pro'
    }
  })

  if (!data) return <div className="container"><Status error={error} /></div>

  const byId = new Map<string, ModuleSummary>(data.tracks.flatMap((t) => t.modules).map((m) => [m.id, m]))
  const trackTitle = new Map(data.tracks.map((t) => [t.id, t.title]))
  const path = data.paths.find((p) => p.id === pathId) ?? data.paths[0]
  const next = path?.next ? byId.get(path.next) : undefined

  const choose = (id: string) => {
    setPathId(id)
    try {
      localStorage.setItem(PATH_KEY, id)
    } catch {
      /* not remembered in private mode */
    }
  }

  return (
    <div className="container narrow">
      <header className="page-head">
        <div>
          <div className="eyebrow">Academy</div>
          <h1 style={{ marginTop: 6 }}>From scratch to architect</h1>
          <p className="sub">
            {data.total} lessons of about 25 minutes, each building only on what came before.
          </p>
        </div>
        <div style={{ minWidth: 200 }}>
          <div className="row" style={{ justifyContent: 'space-between', fontSize: '0.84rem' }}>
            <span className="muted">Overall</span>
            <span className="num" style={{ fontWeight: 600 }}>
              {data.done} / {data.total}
            </span>
          </div>
          <div style={{ marginTop: 8 }}>
            <Bar value={data.done} max={data.total} />
          </div>
        </div>
      </header>

      <div className="segmented" role="tablist" aria-label="Learning path">
        {data.paths.map((p) => (
          <button key={p.id} role="tab" aria-selected={p.id === path?.id} onClick={() => choose(p.id)}>
            {p.title.replace(' (start here)', '')}
          </button>
        ))}
      </div>

      {path && (
        <>
          <p className="muted" style={{ margin: '14px 0 18px', fontSize: '0.92rem' }}>
            {path.description}
          </p>

          {next ? (
            <div className="card raised continue-card">
              <div>
                <div className="eyebrow">{path.done === 0 ? 'Start here' : 'Continue'} · step {path.modules.indexOf(next.id) + 1} of {path.modules.length}</div>
                <Link to={`/academy/${next.id}`} className="title">
                  {next.title}
                </Link>
                <div className="meta" style={{ marginTop: 8 }}>
                  <span>{trackTitle.get(next.trackId)}</span>
                  <span className="sep" />
                  <LevelChip level={next.level} />
                  <span className="sep" />
                  <span>~{next.minutes} min</span>
                </div>
              </div>
              <Link to={`/academy/${next.id}`} className="btn lg primary">
                {path.done === 0 ? 'Start' : 'Continue'} <ArrowRight size={16} />
              </Link>
            </div>
          ) : (
            <div className="callout">
              <Check size={18} />
              <span>Path complete. Pick another path, or revisit any lesson below.</span>
            </div>
          )}

          <PathList ids={path.modules} byId={byId} trackTitle={trackTitle} next={path.next} />
        </>
      )}

      <section className="section">
        <div className="section-head">
          <h2>Browse by track</h2>
        </div>
        <div className="tracks">
          {data.tracks.map((t) => {
            const done = t.modules.filter((m) => m.done).length
            return (
              <div key={t.id} className="card track">
                <div className="row" style={{ justifyContent: 'space-between' }}>
                  <h3>{t.title}</h3>
                  <span className="muted num" style={{ fontSize: '0.8rem' }}>
                    {done}/{t.modules.length}
                  </span>
                </div>
                <p className="track-desc">{t.description}</p>
                <Bar value={done} max={t.modules.length} />
                <ul>
                  {t.modules.map((m) => (
                    <li key={m.id}>
                      <Link to={`/academy/${m.id}`} className={m.done ? 'done' : ''}>
                        <span className="id">{m.done ? '✓' : m.id}</span>
                        <span>{m.title}</span>
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            )
          })}
        </div>
      </section>
    </div>
  )
}

function PathList({
  ids,
  byId,
  trackTitle,
  next,
}: {
  ids: string[]
  byId: Map<string, ModuleSummary>
  trackTitle: Map<string, string>
  next: string | null
}) {
  // Group consecutive modules of the same level, so the rising curve is visible.
  const groups: { level: number; items: { m: ModuleSummary; n: number }[] }[] = []
  ids.forEach((id, i) => {
    const m = byId.get(id)
    if (!m) return
    const last = groups[groups.length - 1]
    if (last && last.level === m.level) last.items.push({ m, n: i + 1 })
    else groups.push({ level: m.level, items: [{ m, n: i + 1 }] })
  })

  return (
    <div style={{ marginTop: 8 }}>
      {groups.map((g, gi) => (
        <div key={gi}>
          <div className="level-head">
            <span className="eyebrow">
              Level {g.level} · {LEVELS[g.level]}
            </span>
            <span className="line" />
          </div>
          <div className="card">
            {g.items.map(({ m, n }) => (
              <Link key={m.id} to={`/academy/${m.id}`} className={`step ${m.done ? 'done' : ''} ${m.id === next ? 'next' : ''}`}>
                <span className="step-no">{m.done ? <Check size={13} strokeWidth={3} /> : n}</span>
                <span>
                  <div className="step-title">{m.title}</div>
                  <div className="step-track">{trackTitle.get(m.trackId)}</div>
                </span>
                <span className="muted num" style={{ fontSize: '0.82rem' }}>
                  {m.written ? `${m.minutes} min` : 'soon'}
                </span>
              </Link>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}
