import { Children, createContext, isValidElement, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { Check, HelpCircle, NotebookPen, RotateCcw, X } from 'lucide-react'

/**
 * A click-to-answer question from a ```quiz block (JSON) in a lesson or answer: pick an option and see at once
 * whether it is right and why. On its own nothing is stored; a lesson page wraps its quizzes in a QuizReporter so
 * answers feed the mistakes notebook.
 *
 *   {"question": "What does this print?", "code": ["int x = 1;", "System.out.println(x++);"],
 *    "options": [{"text": "1", "correct": true, "why": "Post-increment returns the old value."},
 *                {"text": "2", "why": "That is ++x."}]}
 */
export interface QuizOption {
  text: string
  correct?: boolean
  why?: string
}

export type QuizLevel = 'easy' | 'medium' | 'hard'
const LEVEL_LABEL: Record<QuizLevel, string> = { easy: 'Check', medium: 'Apply', hard: 'Interview trap' }

export interface QuizSpec {
  question: string
  level?: QuizLevel
  code?: string[]
  options: QuizOption[]
}

export function quizSource(children: ReactNode): string | null {
  const code = Children.toArray(children)[0]
  if (!isValidElement<{ className?: string; children?: ReactNode }>(code)) return null
  if (!(code.props.className ?? '').includes('language-quiz')) return null
  return Children.toArray(code.props.children).join('').trim()
}

/**
 * The questions of the ```quiz blocks in raw Markdown. Fences follow CommonMark: three or more backticks or tildes,
 * closed by a fence of the same character at least as long, so a quiz written with ```` is found too.
 */
export function quizQuestions(markdown: string): string[] {
  const out: string[] = []
  let open: { fence: string; quiz: boolean; body: string[] } | null = null
  for (const line of markdown.split('\n')) {
    if (!open) {
      const m = /^\s{0,3}(`{3,}|~{3,})\s*([^\s`]*)/.exec(line)
      if (m) open = { fence: m[1], quiz: m[2] === 'quiz', body: [] }
      continue
    }
    const close = /^\s{0,3}(`{3,}|~{3,})\s*$/.exec(line)
    if (close && close[1][0] === open.fence[0] && close[1].length >= open.fence.length) {
      const q = open.quiz ? parseQuiz(open.body.join('\n').trim()) : null
      if (q) out.push(q.question)
      open = null
    } else {
      open.body.push(line)
    }
  }
  return out
}

export function parseQuiz(source: string): QuizSpec | null {
  try {
    const q = JSON.parse(source) as QuizSpec
    const valid = typeof q.question === 'string' && Array.isArray(q.options) && q.options.length >= 2
      && q.options.filter((o) => o.correct).length === 1
    return valid ? q : null
  } catch {
    return null
  }
}

/** What the notebook did with an answer: a wrong one saved as a mistake, a right one fixing an earlier mistake. */
export type QuizOutcome = 'saved' | 'fixed' | null

export interface QuizReporter {
  report: (spec: QuizSpec, picked: number) => Promise<QuizOutcome>
  /** The question to scroll to and mark (Retry from the mistakes notebook). */
  focus?: string
  /** Called once the focused quiz has scrolled into view, so the page can drop the request. */
  onFocused?: () => void
}

export const QuizReporterContext = createContext<QuizReporter | null>(null)

export function QuizBlock({ source }: { source: string }) {
  const spec = useMemo(() => parseQuiz(source), [source])
  if (!spec) return <pre>{source}</pre>
  return <Quiz spec={spec} />
}

export function Quiz({ spec }: { spec: QuizSpec }) {
  const [picked, setPicked] = useState<number | null>(null)
  const [outcome, setOutcome] = useState<QuizOutcome>(null)
  const reporter = useContext(QuizReporterContext)
  const box = useRef<HTMLDivElement>(null)
  const focused = !!reporter?.focus && reporter.focus === spec.question
  // Scroll to a retried quiz once, keep it marked, and tell the page it has been shown.
  const [marked, setMarked] = useState(false)
  const scrolled = useRef(false)
  useEffect(() => {
    if (!focused || scrolled.current) return
    scrolled.current = true
    setMarked(true)
    box.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    reporter?.onFocused?.()
  }, [focused, reporter])
  const answered = picked !== null
  const right = answered && !!spec.options[picked].correct
  const pick = (i: number) => {
    setPicked(i)
    setOutcome(null)
    // Best effort: a failed save must not get in the way of the self-check.
    reporter?.report(spec, i).then(setOutcome, () => setOutcome(null))
  }
  return (
    <div className={`quiz ${marked ? 'focus' : ''}`} role="group" aria-label="Quick check" ref={box}>
      <div className="quiz-q">
        <HelpCircle size={16} aria-hidden />
        <span>{spec.question}</span>
        {spec.level && LEVEL_LABEL[spec.level] && (
          <span className={`quiz-level ${spec.level}`}>{LEVEL_LABEL[spec.level]}</span>
        )}
      </div>
      {spec.code && <pre className="quiz-code"><code>{spec.code.join('\n')}</code></pre>}
      <div className="quiz-options">
        {spec.options.map((o, i) => {
          const state = !answered ? '' : o.correct ? 'right' : i === picked ? 'wrong' : 'dim'
          return (
            <button key={i} className={`quiz-opt ${state}`} disabled={answered} onClick={() => pick(i)}>
              <span className="quiz-mark" aria-hidden>
                {state === 'right' ? <Check size={14} /> : state === 'wrong' ? <X size={14} /> : String.fromCharCode(65 + i)}
              </span>
              <span>{o.text}</span>
            </button>
          )
        })}
      </div>
      {answered && (
        <div className={`quiz-why ${right ? 'right' : 'wrong'}`} aria-live="polite">
          <strong>{right ? 'Right.' : 'Not quite.'}</strong>{' '}
          {spec.options[picked].why}
          {!right && spec.options.find((o) => o.correct)?.why && (
            <> <em>Correct answer:</em> {spec.options.find((o) => o.correct)!.why}</>
          )}
          <button className="btn sm quiz-retry" onClick={() => setPicked(null)}>
            <RotateCcw size={13} /> Try again
          </button>
          {outcome && (
            <div className="quiz-saved">
              <NotebookPen size={13} aria-hidden />
              {outcome === 'saved' ? 'Saved to your Mistakes notebook.' : 'Marked fixed in your Mistakes notebook.'}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
