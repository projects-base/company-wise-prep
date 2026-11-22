import { useState } from 'react'
import { BookmarkPlus, Check, MessageSquareText } from 'lucide-react'
import { api, type Explanation } from './api'
import { SpokenJudge } from './AnswerJudge'

/**
 * "Explain it in your words" at the end of a lesson: type or speak an explanation, have the app's reviewer check it
 * against the lesson (the 4 layers, misconceptions, an improved version), then save the improved version to notes,
 * where it also becomes a review card.
 */
export function ExplainIt({ moduleId, code, title, hidden, onSaved }: {
  moduleId: string
  code: string
  title: string
  /** Hidden, not unmounted, away from the lesson's end, so the typed text and the result survive Back / Next. */
  hidden: boolean
  onSaved: (e: Explanation) => void
}) {
  const [attempt, setAttempt] = useState<number | null>(null)
  const [saved, setSaved] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const save = async () => {
    if (attempt === null) return
    setError(null)
    try {
      const e = await api.saveExplanation(attempt)
      setSaved(e.id)
      onSaved(e)
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <div className="explain-it" hidden={hidden}>
      <div className="eyebrow">{code} · end of lesson</div>
      <SpokenJudge
        heading={<><MessageSquareText size={18} style={{ verticalAlign: -3 }} /> Explain it in your words: {title}</>}
        intro={'Type it or speak it, as you would in an interview: '
          + 'what it is → how it works → where you would use it → what goes wrong.'}
        submitLabel="Check my explanation"
        submit={async (text) => {
          const r = await api.explain(moduleId, text)
          setAttempt(r.id)
          setSaved(null)
          return r.review
        }}
        extra={(review) =>
          review.improvedAnswer && attempt !== null ? (
            <>
              {error && <span className="error">{error}</span>}
              <button className="btn" onClick={save} disabled={saved === attempt}
                title="The improved version goes into your notes and becomes a review card">
                {saved === attempt ? <Check size={14} /> : <BookmarkPlus size={14} />}
                {saved === attempt ? 'Saved to notes' : 'Save to notes'}
              </button>
            </>
          ) : null
        }
      />
    </div>
  )
}
