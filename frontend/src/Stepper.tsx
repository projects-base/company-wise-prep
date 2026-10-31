import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { ArrowLeft, ArrowRight, Check, Rows3, StretchHorizontal } from 'lucide-react'

/**
 * Long Markdown (a lesson, a model answer) read one "## " part at a time: a segmented progress
 * bar, the part's title, time left and one big Next button. Less on screen makes it easier to
 * start and to keep going; "Show all" brings back the full page.
 */

export interface Part {
  title: string
  body: string
  words: number
}

const words = (s: string) => s.split(/\s+/).filter(Boolean).length
const WPM = 200

/** Splits on "## " lines outside code fences. The text before the first one is the intro. */
export function splitParts(text: string): { intro: string; parts: Part[] } {
  const lines = text.split('\n')
  const intro: string[] = []
  const parts: { title: string; body: string[] }[] = []
  let fenced = false
  for (const line of lines) {
    if (/^\s*```/.test(line)) fenced = !fenced
    const h = !fenced && line.match(/^##\s+(.+?)\s*$/)
    if (h) parts.push({ title: h[1].replace(/[*_`]/g, ''), body: [] })
    else (parts.length ? parts[parts.length - 1].body : intro).push(line)
  }
  return {
    intro: intro.join('\n'),
    parts: parts.map((p) => {
      const body = p.body.join('\n')
      return { title: p.title, body, words: words(body) }
    }),
  }
}

const read = (k: string) => {
  try {
    return localStorage.getItem(k)
  } catch {
    return null
  }
}
const write = (k: string, v: string) => {
  try {
    localStorage.setItem(k, v)
  } catch {
    /* private window: just not remembered */
  }
}

export function useStepper(text: string, key: string) {
  const { intro, parts } = useMemo(() => splitParts(text), [text])
  const [mode, setModeState] = useState<'step' | 'all'>(() => (read('reader.mode') === 'all' ? 'all' : 'step'))
  const [step, setStepState] = useState(0)
  useEffect(() => {
    const saved = Number(read(`reader.pos.${key}`) ?? 0)
    setStepState(Number.isFinite(saved) && saved < parts.length ? saved : 0)
  }, [key, parts.length])
  const setStep = (n: number) => {
    const v = Math.max(0, Math.min(parts.length - 1, n))
    setStepState(v)
    write(`reader.pos.${key}`, String(v))
  }
  const setMode = (m: 'step' | 'all') => {
    setModeState(m)
    write('reader.mode', m)
  }
  // Fewer than 3 parts is short enough to read whole.
  const usable = parts.length >= 3
  return { intro, parts, mode: usable ? mode : 'all', usable, setMode, step, setStep }
}

export type StepperState = ReturnType<typeof useStepper>

export function ModeToggle({ s }: { s: StepperState }) {
  if (!s.usable) return null
  return (
    <div className="mode-toggle" role="group" aria-label="Reading mode">
      <button aria-pressed={s.mode === 'step'} onClick={() => s.setMode('step')}>
        <StretchHorizontal size={14} /> One part at a time
      </button>
      <button aria-pressed={s.mode === 'all'} onClick={() => s.setMode('all')}>
        <Rows3 size={14} /> Show all
      </button>
    </div>
  )
}

/** The reader. `render` turns Markdown into the page's own elements (diagrams, Run buttons…). */
export function StepperView({ s, render, full, onFinish, finishLabel, middle }: {
  s: StepperState
  render: (md: string) => ReactNode
  /** The whole text, used in "Show all" mode. */
  full: string
  onFinish?: () => void
  finishLabel?: string
  /** Shown between Back and Next (the lesson page puts "Mark lesson complete" here). */
  middle?: ReactNode
}) {
  const top = useRef<HTMLDivElement>(null)
  const { parts, step } = s

  useEffect(() => {
    if (s.mode !== 'step') return
    const onKey = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement
      if (t.closest('input, textarea, select, [contenteditable], .cm-editor')) return
      if (e.key === 'ArrowRight') go(step + 1)
      if (e.key === 'ArrowLeft') go(step - 1)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  })

  if (s.mode === 'all') return <>{render(full)}</>

  const go = (n: number) => {
    if (n < 0 || n >= parts.length) return
    s.setStep(n)
    // Turn the page: if you have scrolled past the start of the parts, jump straight back to it (no
    // long smooth scroll); if it is still on screen, stay put. The phone's sticky top bar is allowed for.
    requestAnimationFrame(() => {
      const y = top.current?.getBoundingClientRect().top ?? 0
      const bar = document.querySelector('.sidebar')
      const offset = bar && getComputedStyle(bar).position === 'sticky' ? bar.getBoundingClientRect().height : 0
      if (y < offset) window.scrollTo({ top: window.scrollY + y - offset - 8, behavior: 'instant' })
    })
  }
  const part = parts[step]
  const left = Math.max(1, Math.round(parts.slice(step).reduce((n, p) => n + p.words, 0) / WPM))
  const last = step === parts.length - 1

  return (
    <div className="stepper" ref={top}>
      <div className="stepper-bar">
        <div className="stepper-segs" role="tablist" aria-label="Parts">
          {parts.map((p, i) => (
            <button
              key={i}
              role="tab"
              aria-selected={i === step}
              aria-label={`Part ${i + 1}: ${p.title}`}
              title={`${i + 1}. ${p.title}`}
              className={`seg ${i < step ? 'done' : ''} ${i === step ? 'on' : ''}`}
              onClick={() => go(i)}
            />
          ))}
        </div>
        <div className="stepper-label">
          <span>
            Part {step + 1} of {parts.length}
          </span>
          <span className="muted">~{left} min left</span>
        </div>
      </div>

      {step === 0 && s.intro.trim() && render(s.intro)}
      <h2 className="stepper-title">{part.title}</h2>
      {render(part.body)}

      <div className="stepper-nav">
        {step > 0 ? (
          <button className="btn" onClick={() => go(step - 1)}>
            <ArrowLeft size={15} /> Back
          </button>
        ) : (
          <span />
        )}
        {!last && middle}
        {!last ? (
          <button className="btn lg primary" onClick={() => go(step + 1)}>
            Next: {parts[step + 1].title} <ArrowRight size={16} />
          </button>
        ) : onFinish ? (
          <button className="btn lg primary" onClick={onFinish}>
            <Check size={16} /> {finishLabel ?? 'Done'}
          </button>
        ) : (
          <span className="pill ok">
            <Check size={12} /> That's the last part
          </span>
        )}
      </div>
      <p className="hint stepper-keys">Tip: ← and → keys move between parts.</p>
    </div>
  )
}
