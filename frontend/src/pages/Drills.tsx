import { useState } from 'react'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Eye, EyeOff, Mic } from 'lucide-react'
import { MdPre } from '../Diagram'
import { MdLink } from '../ui'

/* The `drills:` block of company.yaml: rapid-fire rounds with a model answer per question. */
/* eslint-disable @typescript-eslint/no-explicit-any */

const md = { a: MdLink, pre: MdPre, table: ({ children }: any) => <div className="table-wrap"><table>{children}</table></div> }

/** Share of the points → pill colour; no score means the round wasn't tried. */
const tone = (score?: number, outOf = 1) => (score == null ? '' : score / outOf >= 0.75 ? 'ok' : score / outOf >= 0.5 ? 'warn' : 'bad')

/**
 * One round on screen at a time. Each question hides its model answer, so the order is the one
 * the interview forces: answer out loud first, then check.
 */
export function Drills({ d }: { d: any }) {
  const rounds: any[] = d.rounds ?? []
  // The newest round first: it is the one being worked on.
  const [at, setAt] = useState(Math.max(0, rounds.length - 1))
  const [all, setAll] = useState(false)
  const r = rounds[at]
  if (!r) return <p className="muted">No drills yet.</p>
  const pick = (i: number) => {
    setAt(i)
    setAll(false)
  }

  return (
    <div className="stack" style={{ gap: 20 }}>
      <div className="drill-rounds" role="tablist" aria-label="Rounds">
        {rounds.map((x, i) => (
          <button key={i} role="tab" aria-selected={i === at} className="drill-round" onClick={() => pick(i)}>
            <span>{x.title}</span>
            <span className={`pill ${tone(x.score, x.out_of)}`}>{x.score != null ? `${x.score}/${x.out_of}` : 'Not tried'}</span>
          </button>
        ))}
      </div>

      {(d.how || d.lesson) && (
        <div className="callout">
          <Mic size={18} />
          <div>
            {d.how}
            {d.lesson && <div style={{ marginTop: 4, fontWeight: 600 }}>{d.lesson}</div>}
          </div>
        </div>
      )}

      <div className="section-head">
        <h2>
          {r.title} <span className="muted" style={{ fontSize: '0.9rem', fontWeight: 500 }}>· {(r.questions ?? []).length} questions</span>
        </h2>
        <button className="btn ghost sm" onClick={() => setAll(!all)}>
          {all ? <EyeOff size={14} /> : <Eye size={14} />} {all ? 'Hide' : 'Reveal'} all answers
        </button>
      </div>

      <ol className="drill-list">
        {(r.questions ?? []).map((q: any, i: number) => (
          <DrillCard key={`${at}.${i}`} n={i + 1} q={q} forced={all} />
        ))}
      </ol>
    </div>
  )
}

function DrillCard({ n, q, forced }: { n: number; q: any; forced: boolean }) {
  const [open, setOpen] = useState(false)
  const shown = open || forced
  return (
    <li className={`card pad drill ${shown ? 'shown' : ''}`}>
      <div className="row" style={{ gap: 10, alignItems: 'flex-start' }}>
        <span className="drill-no">{n}</span>
        <div className="grow">
          <div className="drill-q">{q.q}</div>
          {!shown && (
            <button className="btn sm" style={{ marginTop: 10 }} onClick={() => setOpen(true)}>
              <Eye size={14} /> Said it? Reveal the answer
            </button>
          )}
          {shown && (
            <>
              {q.score != null && (
                <div className="row" style={{ gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
                  <span className={`pill ${tone(q.score)}`}>You: {q.score}/1</span>
                  <span className="dim" style={{ fontSize: '0.88rem' }}>{q.verdict}</span>
                </div>
              )}
              <div className="tldr drill-model prose">
                <div className="tldr-label">Say this</div>
                <Markdown remarkPlugins={[remarkGfm]} components={md}>
                  {q.model ?? ''}
                </Markdown>
              </div>
              {(q.followups ?? []).length > 0 && (
                <div className="drill-follow">
                  <div className="tldr-label">They may ask next</div>
                  {q.followups.map((f: any, i: number) => (
                    <details key={i}>
                      <summary>{f.q}</summary>
                      <div className="prose">
                        <Markdown remarkPlugins={[remarkGfm]} components={md}>
                          {f.a ?? ''}
                        </Markdown>
                      </div>
                    </details>
                  ))}
                </div>
              )}
              {!forced && (
                <button className="link-btn" onClick={() => setOpen(false)}>
                  Hide and try again
                </button>
              )}
            </>
          )}
        </div>
      </div>
    </li>
  )
}
