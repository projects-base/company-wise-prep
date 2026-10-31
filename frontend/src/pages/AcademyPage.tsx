import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Check, ListOrdered, Network } from 'lucide-react'
import { api, type ModuleSummary } from '../api'
import { Bar, LevelChip, Status, useLoad } from '../ui'
import { LevelTree, OrderList } from '../LessonTree'

export const PATH_KEY = 'cwp.academyPath'
const VIEW_KEY = 'cwp.academyView'

export default function AcademyPage() {
  const { data, error } = useLoad(() => api.academy(), [])
  const [pathId, setPathId] = useState(() => {
    try {
      return localStorage.getItem(PATH_KEY) ?? 'zero-to-pro'
    } catch {
      return 'zero-to-pro'
    }
  })

  const [view, setView] = useState<'tree' | 'order'>(() => {
    try {
      return localStorage.getItem(VIEW_KEY) === 'order' ? 'order' : 'tree'
    } catch {
      return 'tree'
    }
  })
  const chooseView = (v: 'tree' | 'order') => {
    setView(v)
    try {
      localStorage.setItem(VIEW_KEY, v)
    } catch {
      /* not remembered in private mode */
    }
  }

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
                <div className="eyebrow">{path.done === 0 ? 'Start here' : 'Continue'} · Step {path.modules.indexOf(next.id) + 1} of {path.modules.length} · {next.code}</div>
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

          <div className="row tree-toolbar">
            <span className="eyebrow">{path.modules.length} lessons</span>
            <div className="segmented small" role="tablist" aria-label="Path view">
              <button role="tab" aria-selected={view === 'tree'} onClick={() => chooseView('tree')}>
                <Network size={14} /> By level
              </button>
              <button role="tab" aria-selected={view === 'order'} onClick={() => chooseView('order')}>
                <ListOrdered size={14} /> In order
              </button>
            </div>
          </div>
          {view === 'tree' ? (
            <LevelTree ids={path.modules} byId={byId} tracks={data.tracks} next={path.next} />
          ) : (
            <OrderList ids={path.modules} byId={byId} trackTitle={trackTitle} next={path.next} />
          )}
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
                <div className="row" style={{ justifyContent: 'space-between', gap: 8 }}>
                  <h3>{t.title}</h3>
                  <span className="track-code">{t.code}</span>
                </div>
                <p className="track-desc">{t.description}</p>
                <div className="row track-progress">
                  <Bar value={done} max={t.modules.length} />
                  <span className="muted num">
                    {done}/{t.modules.length}
                  </span>
                </div>
                <ul>
                  {t.modules.map((m, i) => (
                    <li key={m.id}>
                      <Link
                        to={`/academy/${m.id}`}
                        title={m.code}
                        className={`${m.done ? 'done' : ''} ${m.id === path?.next ? 'next' : ''}`}
                      >
                        <span className="id">{m.done ? '✓' : i + 1}</span>
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
