import { useState } from 'react'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { ChevronRight, Clock, LifeBuoy } from 'lucide-react'
import { MdPre } from '../Diagram'
import { Bar, MdLink } from '../ui'

/* The `brushup:` block of company.yaml: an interview-day checklist of short, timed steps. */
/* eslint-disable @typescript-eslint/no-explicit-any */

const md = { a: MdLink, pre: MdPre, table: ({ children }: any) => <div className="table-wrap"><table>{children}</table></div> }

const readTicks = (k: string): number[] => {
  try {
    return JSON.parse(localStorage.getItem(k) ?? '[]')
  } catch {
    return []
  }
}

/**
 * Ten small steps instead of one long page: tick a step and the next one opens, so there is
 * always exactly one thing to do. Ticks are remembered in this browser.
 */
export function BrushUp({ b }: { b: any }) {
  const steps: any[] = b.steps ?? []
  const key = `brushup.${b.for ?? ''}`
  const [ticks, setTicks] = useState<number[]>(() => readTicks(key))
  const firstOpen = steps.findIndex((_, i) => !ticks.includes(i))
  const [open, setOpen] = useState<number | null>(firstOpen < 0 ? null : firstOpen)
  const total = steps.reduce((n, s) => n + (s.minutes ?? 0), 0)
  const left = steps.reduce((n, s, i) => n + (ticks.includes(i) ? 0 : (s.minutes ?? 0)), 0)

  const save = (next: number[]) => {
    setTicks(next)
    try {
      localStorage.setItem(key, JSON.stringify(next))
    } catch {
      /* private window: not remembered */
    }
  }
  const tick = (i: number) => {
    if (ticks.includes(i)) return save(ticks.filter((t) => t !== i))
    const next = [...ticks, i]
    save(next)
    const after = steps.findIndex((_, j) => j > i && !next.includes(j))
    setOpen(after < 0 ? null : after)
  }

  return (
    <div className="stack" style={{ gap: 18 }}>
      <div className="card pad">
        <div className="row" style={{ justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
          <span style={{ fontWeight: 650 }}>{b.for}</span>
          <span className="muted num" style={{ fontSize: '0.86rem' }}>
            <Clock size={13} style={{ verticalAlign: -2 }} /> {ticks.length}/{steps.length} done · ~{left} of {total} min left
          </span>
        </div>
        <div style={{ marginTop: 10 }}>
          <Bar value={ticks.length} max={steps.length} />
        </div>
        {b.rule && <p className="hint" style={{ marginBottom: 0 }}>{b.rule}</p>}
      </div>

      <ol className="brush-list">
        {steps.map((s, i) => {
          const done = ticks.includes(i)
          const isOpen = open === i
          return (
            <li key={i} className={`card brush ${done ? 'done' : ''} ${isOpen ? 'open' : ''}`}>
              <div className="brush-head">
                <input type="checkbox" checked={done} onChange={() => tick(i)} aria-label={`Done: ${s.title}`} />
                <button className="brush-title" onClick={() => setOpen(isOpen ? null : i)} aria-expanded={isOpen}>
                  <ChevronRight size={15} className={`chev ${isOpen ? 'open' : ''}`} />
                  <span>{s.title}</span>
                </button>
                {s.minutes != null && <span className="pill">{s.minutes}m</span>}
              </div>
              {isOpen && (
                <div className="brush-body prose">
                  <Markdown remarkPlugins={[remarkGfm]} components={md}>
                    {s.body ?? ''}
                  </Markdown>
                  {!done && (
                    <button className="btn sm primary" style={{ marginTop: 12 }} onClick={() => tick(i)}>
                      Done, next step
                    </button>
                  )}
                </div>
              )}
            </li>
          )
        })}
      </ol>

      {b.blank && (
        <div className="callout ok">
          <LifeBuoy size={18} />
          <div>{b.blank}</div>
        </div>
      )}
    </div>
  )
}
