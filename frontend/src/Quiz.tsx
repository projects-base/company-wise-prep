import { Children, isValidElement, useMemo, useState, type ReactNode } from 'react'
import { Check, HelpCircle, RotateCcw, X } from 'lucide-react'

/**
 * A click-to-answer question from a ```quiz block (JSON) in a lesson or answer: pick an option and see at once
 * whether it is right and why. Nothing is stored; it is a self-check while reading.
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

export function QuizBlock({ source }: { source: string }) {
  const spec = useMemo(() => parseQuiz(source), [source])
  if (!spec) return <pre>{source}</pre>
  return <Quiz spec={spec} />
}

export function Quiz({ spec }: { spec: QuizSpec }) {
  const [picked, setPicked] = useState<number | null>(null)
  const answered = picked !== null
  const right = answered && !!spec.options[picked].correct
  return (
    <div className="quiz" role="group" aria-label="Quick check">
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
            <button key={i} className={`quiz-opt ${state}`} disabled={answered} onClick={() => setPicked(i)}>
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
        </div>
      )}
    </div>
  )
}
