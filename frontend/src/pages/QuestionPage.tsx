import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Check, ExternalLink, Eye, Star } from 'lucide-react'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { api, leetcodeUrl, type Action, type Challenge } from '../api'
import Workspace from '../code/Workspace'
import { companyLabel, Difficulty, fmtDate, MdLink, Meta, Status, TypeTag, useLoad } from '../ui'

export default function QuestionPage() {
  const { slug = '' } = useParams()
  const navigate = useNavigate()
  const { data: q, error, reload } = useLoad(() => api.question(slug), [slug])
  const [notes, setNotes] = useState('')
  const [saved, setSaved] = useState(false)
  const [challenge, setChallenge] = useState<Challenge | null>(null)

  useEffect(() => {
    setNotes(q?.progress.notes ?? '')
  }, [q])
  useEffect(() => {
    setChallenge(null)
    if (q?.runnable && q.slug === slug) api.challenge(slug).then(setChallenge).catch(() => setChallenge(null))
  }, [q?.runnable, q?.slug, slug])
  const onAccepted = useCallback(() => {
    if (!q?.progress.done) void api.progress(slug, 'DONE').then(reload)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q?.progress.done, slug])

  if (!q) return <div className="container"><Status error={error} /></div>

  const act = async (action: Action) => {
    await api.progress(slug, action, action === 'NOTES' ? notes : undefined)
    if (action === 'NOTES') {
      setSaved(true)
      setTimeout(() => setSaved(false), 1800)
    }
    reload()
  }
  const p = q.progress
  const companies = [...new Set(q.sightings.map((s) => s.company))]
  const dirty = notes !== (p.notes ?? '')

  if (q.runnable && challenge) {
    const header = (
      <div className="ws-head">
        <button className="btn sm ghost" onClick={() => navigate(-1)} style={{ marginLeft: -10 }}>
          <ArrowLeft size={15} /> Back
        </button>
        <h1 style={{ marginTop: 8, fontSize: '1.45rem' }}>{q.title}</h1>
        <div className="row" style={{ marginTop: 8, gap: 10 }}>
          <Meta>
            <TypeTag type={q.type} />
            <Difficulty value={q.difficulty} />
          </Meta>
          <span className="spacer" />
          {p.done ? (
            <span className="pill ok">
              <Check size={12} strokeWidth={3} /> Solved
            </span>
          ) : null}
          <button className="btn sm ghost" onClick={() => act(p.starred ? 'UNSTAR' : 'STAR')} title="Star">
            <Star size={14} fill={p.starred ? 'currentColor' : 'none'} style={{ color: p.starred ? 'var(--warn)' : undefined }} />
          </button>
        </div>
        <div className="row" style={{ gap: 6, marginTop: 10 }}>
          <span className="muted" style={{ fontSize: '0.82rem' }}>
            Asked at
          </span>
          {companies.map((c) => (
            <Link key={c} to={`/companies/${c}`} className="pill">
              {companyLabel(c)}
            </Link>
          ))}
        </div>
      </div>
    )
    const extra = (
      <div className="ws-extra">
        {q.followUps.length > 0 && (
          <details className="ws-details" open>
            <summary>Follow-ups interviewers asked</summary>
            <ul className="followups">
              {q.followUps.map((f) => (
                <li key={f}>{f}</li>
              ))}
            </ul>
          </details>
        )}
        {q.answer && (
          <details className="ws-details">
            <summary>Model answer</summary>
            <AnswerBody text={q.answer} />
          </details>
        )}
        <details className="ws-details">
          <summary>Where it was asked ({q.sightings.length})</summary>
          {q.sightings.map((s, i) => (
            <div key={i} className="ws-sighting">
              <strong>{companyLabel(s.company)}</strong>{' '}
              <span className="muted">
                {[s.role, s.round, s.seenOn].filter(Boolean).join(' · ')}
              </span>{' '}
              {s.source && (
                <a href={s.source} target="_blank" rel="noreferrer">
                  source <ExternalLink size={11} style={{ verticalAlign: -1 }} />
                </a>
              )}
            </div>
          ))}
        </details>
        <details className="ws-details">
          <summary>My notes</summary>
          <textarea rows={5} value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Approach, complexity, the trick to remember…" />
          <div className="row" style={{ marginTop: 8 }}>
            <button className="btn sm" onClick={() => act('NOTES')} disabled={!dirty}>
              Save notes
            </button>
            {saved && <span className="pill ok">Saved</span>}
          </div>
        </details>
      </div>
    )
    return <Workspace key={challenge.slug} challenge={challenge} header={header} extra={extra} onAccepted={onAccepted} />
  }

  return (
    <div className="container">
      <button className="btn sm ghost" onClick={() => navigate(-1)} style={{ marginLeft: -10, marginBottom: 14 }}>
        <ArrowLeft size={15} /> Back
      </button>

      <div className="q-grid">
        <div>
          <Meta>
            <TypeTag type={q.type} />
            <Difficulty value={q.difficulty} />
            {q.tags.length > 0 && <span>{q.tags.slice(0, 5).join(' · ')}</span>}
          </Meta>
          <h1 style={{ marginTop: 10 }}>{q.title}</h1>

          {q.prompt && (
            <section className="section" style={{ marginTop: 24 }}>
              <div className="eyebrow" style={{ marginBottom: 8 }}>
                Problem
              </div>
              <p className="prompt">{q.prompt}</p>
            </section>
          )}

          {q.followUps.length > 0 && (
            <section className="section" style={{ marginTop: 28 }}>
              <div className="eyebrow" style={{ marginBottom: 8 }}>
                Follow-ups interviewers asked
              </div>
              <ul className="followups">
                {q.followUps.map((f) => (
                  <li key={f}>{f}</li>
                ))}
              </ul>
            </section>
          )}

          {q.answer && <Answer key={q.slug} text={q.answer} />}

          <section className="section">
            <div className="section-head">
              <h2>Where it was asked</h2>
              <span className="muted" style={{ fontSize: '0.84rem' }}>
                {q.sightings.length} report{q.sightings.length === 1 ? '' : 's'}
              </span>
            </div>
            <div className="card">
              {q.sightings.map((s, i) => (
                <div key={i} className="sighting">
                  <span>
                    <Link to={`/companies/${s.company}`} style={{ fontWeight: 600 }}>
                      {companyLabel(s.company)}
                    </Link>
                    <span className="muted"> · {[s.role, s.round].filter(Boolean).join(' · ')}</span>
                  </span>
                  <span className="muted num">{s.seenOn ?? '—'}</span>
                  {s.source && (
                    <a className="src" href={s.source} target="_blank" rel="noreferrer">
                      {hostOf(s.source)} <ExternalLink size={11} style={{ verticalAlign: -1 }} />
                      {s.confidence !== 'verified' && <span className="muted"> · reported online, not yet confirmed by you</span>}
                    </a>
                  )}
                </div>
              ))}
            </div>
          </section>

          <section className="section">
            <div className="section-head">
              <h2>My notes</h2>
              {saved && <span className="pill ok">Saved</span>}
            </div>
            <textarea
              rows={7}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Approach, complexity, the bug you made, the trick to remember…"
            />
            <div className="row" style={{ marginTop: 10 }}>
              <button className="btn" onClick={() => act('NOTES')} disabled={!dirty}>
                Save notes
              </button>
            </div>
          </section>
        </div>

        <aside className="aside">
          <div className="card">
            {p.done ? (
              <>
                <div className="row" style={{ gap: 8 }}>
                  <span className="pill ok">
                    <Check size={12} strokeWidth={3} /> Done
                  </span>
                  {p.doneOn && <span className="muted" style={{ fontSize: '0.84rem' }}>{fmtDate(p.doneOn)}</span>}
                </div>
                <p className="muted" style={{ fontSize: '0.86rem', margin: '10px 0 14px' }}>
                  {p.nextReviewOn ? `Next review ${fmtDate(p.nextReviewOn)}.` : 'Retired from review.'}
                </p>
                <button className="btn" onClick={() => act('UNDO')}>
                  Mark not done
                </button>
              </>
            ) : (
              <>
                <button className="btn lg primary" onClick={() => act('DONE')}>
                  <Check size={16} /> Mark done
                </button>
                <p className="muted" style={{ fontSize: '0.82rem', marginTop: 10 }}>
                  Comes back for review in 3, 10 and 30 days.
                </p>
              </>
            )}
          </div>
          <div className="card stack">
            {q.leetcode && (
              <a className="btn" href={leetcodeUrl(q.leetcode)} target="_blank" rel="noreferrer">
                Solve on LeetCode <ExternalLink size={14} />
              </a>
            )}
            <button className="btn" onClick={() => act(p.starred ? 'UNSTAR' : 'STAR')}>
              <Star size={15} fill={p.starred ? 'currentColor' : 'none'} style={{ color: p.starred ? 'var(--warn)' : undefined }} />
              {p.starred ? 'Starred' : 'Star'}
            </button>
          </div>
          <div className="card">
            <div className="eyebrow" style={{ marginBottom: 8 }}>
              Asked at
            </div>
            <div className="stack" style={{ gap: 6 }}>
              {companies.map((c) => (
                <Link key={c} to={`/companies/${c}`}>
                  {companyLabel(c)}
                </Link>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </div>
  )
}

function hostOf(url: string) {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return 'source'
  }
}

/** Hidden until asked for, so you try the question first. */
function Answer({ text }: { text: string }) {
  const [shown, setShown] = useState(false)
  return (
    <section className="section">
      <div className="section-head">
        <h2>Answer</h2>
        {shown && (
          <button className="btn sm ghost" onClick={() => setShown(false)}>
            Hide
          </button>
        )}
      </div>
      {shown ? (
        <div className="card pad">
          <AnswerBody text={text} />
        </div>
      ) : (
        <div className="card pad answer-hidden">
          <p className="muted">Try it yourself first: say the answer out loud or write it in your notes.</p>
          <button className="btn" onClick={() => setShown(true)}>
            <Eye size={15} /> Show answer
          </button>
        </div>
      )}
    </section>
  )
}

function AnswerBody({ text }: { text: string }) {
  return (
    <div className="prose answer">
      <Markdown remarkPlugins={[remarkGfm]} components={{ a: MdLink }}>
        {text}
      </Markdown>
      <p className="muted answer-note">Written by Claude. Check anything version-specific against the official docs.</p>
    </div>
  )
}
