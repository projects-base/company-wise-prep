import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Check, ChevronRight } from 'lucide-react'
import type { ModuleSummary } from './api'
import { Bar, LEVELS } from './ui'

export type Track = { id: string; code: string; title: string; description: string; modules: ModuleSummary[] }

/** Zero to Pro as a tree: level → section → lessons, each lesson keeping its step number. */
export function LevelTree({
  ids,
  byId,
  tracks,
  next,
  current,
  compact,
}: {
  ids: string[]
  byId: Map<string, ModuleSummary>
  tracks: Track[]
  next: string | null
  /** The lesson being read: highlighted, and its level and section open. */
  current?: string
  /** Sidebar variant: smaller rows, no minutes. */
  compact?: boolean
}) {
  const step = new Map(ids.map((id, i) => [id, i + 1]))
  const levels = [...new Set(ids.map((id) => byId.get(id)?.level ?? 0))].filter(Boolean).sort((a, b) => a - b)
  return (
    <div className={`tree ${compact ? 'compact' : ''}`}>
      {levels.map((level) => {
        const inLevel = ids.map((id) => byId.get(id)!).filter((m) => m && m.level === level)
        const done = inLevel.filter((m) => m.done).length
        const focus = current ?? next
        const open = inLevel.some((m) => m.id === focus) || (!current && done > 0 && done < inLevel.length)
        return (
          <Node
            key={level}
            open={open}
            head={
              <>
                <span className="tree-level-badge">L{level}</span>
                <span className="tree-title">
                  Level {level} · {LEVELS[level]}
                </span>
                <Progress done={done} total={inLevel.length} />
              </>
            }
          >
            {tracks
              .map((t) => ({ t, ms: inLevel.filter((m) => m.trackId === t.id) }))
              .filter(({ ms }) => ms.length > 0)
              .sort((a, b) => step.get(a.ms[0].id)! - step.get(b.ms[0].id)!)
              .map(({ t, ms }) => (
                <Node
                  key={t.id}
                  nested
                  open={current ? ms.some((m) => m.id === current) : open && ms.some((m) => !m.done)}
                  head={
                    <>
                      <span className="tree-title">{t.title}</span>
                      <span className="track-code">{t.code}</span>
                      <Progress done={ms.filter((m) => m.done).length} total={ms.length} />
                    </>
                  }
                >
                  {ms.map((m) => (
                    <LessonRow key={m.id} m={m} step={step.get(m.id)} next={next} current={current} compact={compact} />
                  ))}
                </Node>
              ))}
          </Node>
        )
      })}
    </div>
  )
}

/** The path as one numbered sequence, the order the plan follows. */
export function OrderList({ ids, byId, trackTitle, next }: { ids: string[]; byId: Map<string, ModuleSummary>; trackTitle: Map<string, string>; next: string | null }) {
  return (
    <div className="card tree-flat">
      {ids.map((id, i) => {
        const m = byId.get(id)
        return m ? <LessonRow key={id} m={m} step={i + 1} next={next} sub={trackTitle.get(m.trackId)} /> : null
      })}
    </div>
  )
}

function Node({ head, children, open, nested }: { head: ReactNode; children: ReactNode; open: boolean; nested?: boolean }) {
  return (
    <details className={`tree-node ${nested ? 'nested' : 'card'}`} open={open}>
      <summary>
        <ChevronRight size={15} className="tree-chev" />
        {head}
      </summary>
      <div className="tree-children">{children}</div>
    </details>
  )
}

function Progress({ done, total }: { done: number; total: number }) {
  return (
    <span className="tree-progress">
      <span className="num">
        {done}/{total}
      </span>
      <Bar value={done} max={total} />
    </span>
  )
}

function LessonRow({ m, step, next, sub, current, compact }: {
  m: ModuleSummary
  step?: number
  next: string | null
  sub?: string
  current?: string
  compact?: boolean
}) {
  const here = m.id === current
  return (
    <Link to={`/academy/${m.id}`} className={`tree-lesson ${m.done ? 'done' : ''} ${m.id === next ? 'next' : ''} ${here ? 'current' : ''}`}
      aria-current={here ? 'page' : undefined}
      data-lesson={m.id}
    >
      <span className="tree-dot">{m.done ? <Check size={12} strokeWidth={3} /> : null}</span>
      <span className="tree-code num">{m.code}</span>
      <span className="tree-lesson-title">
        {m.title}
        {sub && <span className="tree-sub"> · {sub}</span>}
      </span>
      {m.id === next && !here && <span className="pill tree-next">Next</span>}
      <span className="tree-meta num">
        {step ? (compact ? step : `Step ${step} · `) : ''}
        {compact ? '' : m.written ? `${m.minutes} min` : 'soon'}
      </span>
    </Link>
  )
}
