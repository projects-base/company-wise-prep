import { useEffect, useState } from 'react'
import { localToday } from './ui'

/**
 * Small rewards for showing up: a daily streak (any ticked step or finished question counts) and
 * a short celebration burst. Kept in this browser only; nothing here affects the plan.
 */

const DAYS_KEY = 'activity.days'
const CELEBRATE = 'cwp:celebrate'
export const TICKS_CHANGED = 'cwp:ticks'

function days(): string[] {
  try {
    return JSON.parse(localStorage.getItem(DAYS_KEY) ?? '[]')
  } catch {
    return []
  }
}

/** Marks today as active. Returns the streak including today. */
export function recordActivity(): number {
  const today = localToday()
  const all = days()
  if (!all.includes(today)) {
    all.push(today)
    try {
      localStorage.setItem(DAYS_KEY, JSON.stringify(all.slice(-400)))
    } catch {
      /* private window: not remembered */
    }
  }
  return streak().current
}

const shift = (iso: string, n: number) => {
  const d = new Date(`${iso}T00:00:00`)
  d.setDate(d.getDate() + n)
  return d.toLocaleDateString('en-CA')
}

/** Consecutive active days ending today (or yesterday, so the streak survives until tonight). */
export function streak(): { current: number; best: number; today: boolean } {
  const set = new Set(days())
  const today = localToday()
  let start = set.has(today) ? today : shift(today, -1)
  let current = 0
  while (set.has(start)) {
    current++
    start = shift(start, -1)
  }
  const sorted = [...set].sort()
  let best = 0
  let run = 0
  for (let i = 0; i < sorted.length; i++) {
    run = i > 0 && shift(sorted[i - 1], 1) === sorted[i] ? run + 1 : 1
    best = Math.max(best, run)
  }
  return { current, best, today: set.has(today) }
}

export function useStreak() {
  const [s, setS] = useState(streak)
  useEffect(() => {
    const on = () => setS(streak())
    window.addEventListener(TICKS_CHANGED, on)
    window.addEventListener(CELEBRATE, on)
    return () => {
      window.removeEventListener(TICKS_CHANGED, on)
      window.removeEventListener(CELEBRATE, on)
    }
  }, [])
  return s
}

export function celebrate(title: string, sub?: string, big = false) {
  window.dispatchEvent(new CustomEvent(CELEBRATE, { detail: { title, sub, big } }))
}

const COLORS = ['var(--accent)', 'var(--ok)', 'var(--warn)', '#e879f9', '#38bdf8']

/** Mounted once in App: shows a burst and a toast whenever celebrate() is called. */
export function Celebrations() {
  const [shown, setShown] = useState<{ id: number; title: string; sub?: string; big: boolean } | null>(null)
  useEffect(() => {
    let t: number | undefined
    const on = (e: Event) => {
      const d = (e as CustomEvent).detail
      setShown({ id: Date.now(), ...d })
      window.clearTimeout(t)
      t = window.setTimeout(() => setShown(null), d.big ? 3200 : 2200)
    }
    window.addEventListener(CELEBRATE, on)
    return () => {
      window.removeEventListener(CELEBRATE, on)
      window.clearTimeout(t)
    }
  }, [])
  if (!shown) return null
  const count = shown.big ? 36 : 18
  return (
    <div className={`celebrate ${shown.big ? 'big' : ''}`} key={shown.id} aria-live="polite">
      <div className="burst" aria-hidden>
        {Array.from({ length: count }, (_, i) => (
          <span
            key={i}
            style={
              {
                '--a': `${(360 / count) * i + (i % 3) * 7}deg`,
                '--d': `${(shown.big ? 120 : 80) + (i % 4) * 22}px`,
                background: COLORS[i % COLORS.length],
                animationDelay: `${(i % 5) * 18}ms`,
              } as React.CSSProperties
            }
          />
        ))}
      </div>
      <div className="toast">
        <strong>{shown.title}</strong>
        {shown.sub && <span>{shown.sub}</span>}
      </div>
    </div>
  )
}
