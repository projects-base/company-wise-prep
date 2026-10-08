import { useEffect, useState, type ReactNode } from 'react'
import { Check } from 'lucide-react'

/** Loads once per change of `deps`; returns data, error, and a manual reload. */
export function useLoad<T>(load: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [tick, setTick] = useState(0)
  useEffect(() => {
    let live = true
    load()
      .then((d) => live && (setData(d), setError(null)))
      .catch((e: Error) => live && setError(e.message))
    return () => {
      live = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick])
  return { data, error, reload: () => setTick((t) => t + 1) }
}

export function Status({ error }: { error: string | null }) {
  if (error) return <p className="error">Couldn't load this page ({error}). Is the server running?</p>
  return <p className="loading">Loading…</p>
}

const TYPE_LABEL: Record<string, string> = {
  DSA: 'DSA',
  LLD: 'LLD',
  HLD: 'System design',
  BEHAVIORAL: 'Behavioural',
  DOMAIN: 'Systems',
  JAVA: 'Java',
  SPRING: 'Spring',
  SQL: 'SQL',
}

export function TypeTag({ type }: { type: string }) {
  return (
    <span className={`type t-${type.toLowerCase()}`}>
      <span className="dot" />
      {TYPE_LABEL[type] ?? type}
    </span>
  )
}

export function Difficulty({ value }: { value: string }) {
  return <span className={`diff diff-${value}`}>{value[0].toUpperCase() + value.slice(1)}</span>
}

const COMPANY_LABEL: Record<string, string> = {
  microsoft: 'Microsoft',
  google: 'Google',
  'tower-research': 'Tower',
}
export const companyLabel = (slug: string) => COMPANY_LABEL[slug] ?? slug

export function CompanyPills({ slugs }: { slugs: string[] }) {
  return (
    <span className="companies">
      {slugs.map((c) => (
        <span key={c} className="pill">
          {companyLabel(c)}
        </span>
      ))}
    </span>
  )
}

/** A row of metadata separated by small dots; falsy children are skipped. */
export function Meta({ children }: { children: ReactNode[] }) {
  const items = children.filter(Boolean)
  return (
    <div className="meta">
      {items.map((c, i) => (
        <span key={i} className="row" style={{ gap: 12 }}>
          {i > 0 && <span className="sep" />}
          {c}
        </span>
      ))}
    </div>
  )
}

export function CheckDot({ on }: { on: boolean }) {
  return (
    <span className={`check ${on ? 'on' : ''}`} aria-hidden>
      <Check size={13} strokeWidth={3} />
    </span>
  )
}

export function Bar({ value, max }: { value: number; max: number }) {
  return (
    <div className="bar" role="progressbar" aria-valuenow={value} aria-valuemax={max}>
      <span style={{ width: `${Math.min(100, (value / Math.max(1, max)) * 100)}%` }} />
    </div>
  )
}

export function Ring({ value, max, size = 64 }: { value: number; max: number; size?: number }) {
  const r = (size - 8) / 2
  const c = 2 * Math.PI * r
  const pct = Math.min(1, value / Math.max(1, max))
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} aria-hidden>
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--surface-2)" strokeWidth="7" />
      {pct > 0 && (
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke="var(--accent)"
          strokeWidth="7"
          strokeLinecap="round"
          strokeDasharray={`${c * pct} ${c}`}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
      )}
      <text x="50%" y="50%" dominantBaseline="central" textAnchor="middle" fontSize="13" fontWeight="650" fill="var(--text)">
        {Math.round(pct * 100)}%
      </text>
    </svg>
  )
}

export const LEVELS = ['', 'Explorer', 'Builder', 'Engineer', 'Senior', 'Architect']

export function LevelChip({ level }: { level: number }) {
  return (
    <span className="level-chip" title={`Level ${level} of 5`}>
      L{level} · {LEVELS[level] ?? ''}
    </span>
  )
}

export const fmtDate = (
  iso: string,
  opts: Intl.DateTimeFormatOptions = { weekday: 'short', day: 'numeric', month: 'short' },
) => new Date(iso + 'T00:00:00').toLocaleDateString(undefined, opts)

/** Today's date in the browser's own time zone, as YYYY-MM-DD. */
export function localToday() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export function daysBetween(fromIso: string, toIso: string) {
  return Math.round(
    (new Date(toIso + 'T00:00:00').getTime() - new Date(fromIso + 'T00:00:00').getTime()) / 86_400_000,
  )
}
