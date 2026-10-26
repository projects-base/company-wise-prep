import { useEffect, useRef, useState } from 'react'
import Markdown from 'react-markdown'
import { AlertTriangle, Check, Gavel, Mic, MicOff, RotateCcw, Sparkles, Volume2, X } from 'lucide-react'
import { api, type AnswerReview } from './api'
import { Link } from 'react-router-dom'
import { useFeatures } from './features'
import { celebrate, recordActivity } from './rewards'

/**
 * Answer a theory question out loud (browser speech-to-text) or by typing, then have it judged:
 * by Claude and/or Gemini when keys are set (Settings), otherwise by the offline key-term check. Scores the four
 * layers of a strong answer and keeps the score trend per question.
 */

/* eslint-disable @typescript-eslint/no-explicit-any */
const Recognition: any = typeof window !== 'undefined' ? (window as any).SpeechRecognition ?? (window as any).webkitSpeechRecognition : undefined

const REVIEWER_LABEL: Record<string, string> = { claude: 'Claude', gemini: 'Gemini', both: 'Claude + Gemini', offline: 'Offline check' }
const NAME: Record<string, string> = { claude: 'Claude', gemini: 'Gemini', offline: 'Offline' }

const WIN_H = 'Click in the box and press Win + H to use Windows voice typing instead.'
const SPEECH_ERRORS: Record<string, string> = {
  'not-allowed': `Microphone access is blocked. Click the lock icon left of the address bar → Microphone → Allow, then reload. ${WIN_H}`,
  'service-not-allowed': `This browser won't run speech recognition here. Use Edge or Google Chrome. ${WIN_H}`,
  'audio-capture': `No microphone was found. Check it is plugged in and selected in Windows sound settings. ${WIN_H}`,
  network: `The browser's speech service could not be reached. It needs internet, and only works in Edge and Google Chrome (not Brave, Opera or other Chromium browsers). ${WIN_H}`,
  'language-not-supported': 'Indian English is not available in this browser; switched to US English. Press Speak again.',
}

const tone = (s: number | null) => (s == null ? '' : s >= 7.5 ? 'ok' : s >= 5 ? 'warn' : 'bad')
const words = (s: string) => s.trim().split(/\s+/).filter(Boolean).length

export function AnswerJudge({ slug }: { slug: string }) {
  const { reviewer } = useFeatures()
  const ai = reviewer !== 'offline'
  const [text, setText] = useState('')
  const [interim, setInterim] = useState('')
  const [listening, setListening] = useState(false)
  const [seconds, setSeconds] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [review, setReview] = useState<AnswerReview | null>(null)
  const [practising, setPractising] = useState<string | null>(null)
  const rec = useRef<any>(null)
  const wanted = useRef(false)
  const lang = useRef('en-IN')

  useEffect(() => {
    if (!listening) return
    const t = window.setInterval(() => setSeconds((s) => s + 1), 1000)
    return () => window.clearInterval(t)
  }, [listening])
  useEffect(() => () => stop(), [])

  const start = () => {
    if (!Recognition) return
    const r = new Recognition()
    r.lang = lang.current
    r.continuous = true
    r.interimResults = true
    r.onresult = (e: any) => {
      let finals = ''
      let live = ''
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const chunk = e.results[i][0].transcript
        if (e.results[i].isFinal) finals += chunk
        else live += chunk
      }
      if (finals) setText((t) => (t ? `${t.trimEnd()} ` : '') + finals.trim())
      setInterim(live)
    }
    // Browsers stop after a pause; keep listening until the user presses Stop.
    r.onend = () => {
      setInterim('')
      if (wanted.current) {
        try {
          r.start()
        } catch {
          setListening(false)
        }
      } else setListening(false)
    }
    r.onerror = (e: any) => {
      // A pause in speech is normal: keep listening.
      if (e.error === 'no-speech' || e.error === 'aborted') return
      wanted.current = false
      setListening(false)
      if (e.error === 'language-not-supported' && r.lang !== 'en-US') {
        r.lang = 'en-US'
        lang.current = 'en-US'
      }
      setError(SPEECH_ERRORS[e.error] ?? `Speech recognition stopped (${e.error}). ${WIN_H}`)
    }
    rec.current = r
    wanted.current = true
    setSeconds(0)
    setListening(true)
    setError(null)
    try {
      r.start()
    } catch (e) {
      wanted.current = false
      setListening(false)
      setError(`Could not start the microphone (${e instanceof Error ? e.message : e}). ${WIN_H}`)
    }
  }

  function stop() {
    wanted.current = false
    rec.current?.stop()
    setListening(false)
  }

  const judge = async () => {
    stop()
    setBusy(true)
    setError(null)
    try {
      const prefix = practising ? `(Answering the follow-up: ${practising})\n` : ''
      const r = await api.review(slug, prefix + text)
      setReview(r)
      const days = recordActivity()
      if (r.score >= 8) celebrate('Strong answer!', days > 1 ? `${r.score}/10 · ${days}-day streak` : `${r.score}/10`)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not judge the answer.')
    } finally {
      setBusy(false)
    }
  }

  const again = (followUp?: string | null) => {
    setReview(null)
    setText('')
    setSeconds(0)
    setPractising(followUp ?? null)
  }

  const n = words(text)
  return (
    <section className="section judge">
      <div className="section-head">
        <h2>
          <Gavel size={18} style={{ verticalAlign: -3 }} /> Judge my answer
        </h2>
        <Link to="/settings" className={`pill ${ai ? 'accent' : ''}`} title="Change the reviewer in Settings">
          {REVIEWER_LABEL[reviewer] ?? 'Offline check'}
        </Link>
      </div>

      {!review && (
        <div className="card pad">
          {practising ? (
            <p className="judge-follow">
              <strong>Follow-up:</strong> {practising}
            </p>
          ) : (
            <p className="muted" style={{ marginTop: 0 }}>
              Answer as in the interview, out loud, about 60–90 seconds: what it is → how it works → where you used it → what goes wrong.
            </p>
          )}
          <div className="judge-input">
            <textarea
              value={text + (interim ? ` ${interim}` : '')}
              onChange={(e) => setText(e.target.value)}
              readOnly={listening}
              placeholder={listening ? 'Listening… start talking' : 'Press Speak and talk, or type here. Win + H (Windows voice typing) also works.'}
              rows={6}
            />
            <div className="judge-bar">
              {Recognition ? (
                <button className={`btn ${listening ? 'danger' : ''}`} onClick={listening ? stop : start}>
                  {listening ? <MicOff size={15} /> : <Mic size={15} />} {listening ? 'Stop' : 'Speak'}
                </button>
              ) : (
                <span className="muted" style={{ fontSize: '0.82rem' }}>
                  No speech-to-text in this browser: click the box and press Win + H, or use Edge / Chrome.
                </span>
              )}
              {listening && (
                <span className={`judge-timer num ${seconds > 120 ? 'over' : ''}`}>
                  <span className="rec-dot" /> {Math.floor(seconds / 60)}:{String(seconds % 60).padStart(2, '0')}
                </span>
              )}
              <span className="spacer" />
              <span className="muted num" style={{ fontSize: '0.8rem' }}>
                {n} words
              </span>
              <button className="btn primary" onClick={judge} disabled={busy || n < 5}>
                <Gavel size={15} /> {busy ? (ai ? `${REVIEWER_LABEL[reviewer]} is reading…` : 'Checking…') : 'Judge'}
              </button>
            </div>
          </div>
          {error && <p className="judge-error">{error}</p>}
        </div>
      )}

      {review && <Result r={review} onAgain={() => again()} onFollowUp={() => again(review.followUp)} />}
    </section>
  )
}

function Result({ r: top, onAgain, onFollowUp }: { r: AnswerReview; onAgain: () => void; onFollowUp: () => void }) {
  const [which, setWhich] = useState(0)
  const r = which === 1 && top.second ? { ...top.second, history: top.history, note: top.note } : top
  const speak = () => {
    if (!r.improvedAnswer || !('speechSynthesis' in window)) return
    window.speechSynthesis.cancel()
    const u = new SpeechSynthesisUtterance(r.improvedAnswer)
    u.lang = 'en-IN'
    u.rate = 0.95
    window.speechSynthesis.speak(u)
  }
  return (
    <div className="card pad judge-result">
      {top.second && (
        <div className="judge-switch">
          <div className="seg" role="tablist" aria-label="Reviewer">
            {[top, top.second].map((x, i) => (
              <button key={i} role="tab" aria-selected={which === i} onClick={() => setWhich(i)}>
                {NAME[x.mode] ?? x.mode} <b className={`num ${tone(x.score)}-text`}>{x.score}</b>
              </button>
            ))}
          </div>
          <span className="muted" style={{ fontSize: '0.84rem' }}>
            Average {(Math.round((top.score + top.second.score) * 5) / 10).toFixed(1)}
            {Math.abs(top.score - top.second.score) >= 2 ? ' · they disagree, read both' : ''}
          </span>
        </div>
      )}
      <div className="judge-head">
        <div className={`judge-score ${tone(r.score)}`}>
          <span className="num">{r.score}</span>
          <small>/10</small>
        </div>
        <div className="grow">
          <div className="judge-verdict">{r.verdict}</div>
          {r.history.length > 1 && <Trend scores={r.history} />}
        </div>
      </div>

      <div className="judge-layers">
        {r.layers.map((l) => (
          <div key={l.key} className="judge-layer">
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <span style={{ fontWeight: 600 }}>{l.name}</span>
              <span className="num muted">{l.score == null ? '—' : `${l.score}/10`}</span>
            </div>
            <div className={`judge-meter ${tone(l.score)}`}>
              <span style={{ width: `${(l.score ?? 0) * 10}%` }} />
            </div>
            {l.comment && <div className="muted judge-comment">{l.comment}</div>}
          </div>
        ))}
      </div>

      {r.mistakes.length > 0 && (
        <div className="callout judge-mistakes">
          <AlertTriangle size={18} />
          <div>
            <strong>Fix these</strong>
            <ul>
              {r.mistakes.map((m, i) => (
                <li key={i}>{m}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      <div className="judge-cols">
        {r.covered.length > 0 && (
          <div>
            <div className="eyebrow">You covered</div>
            <div className="judge-chips">
              {r.covered.map((c, i) => (
                <span key={i} className="chip-ok">
                  <Check size={13} /> {c}
                </span>
              ))}
            </div>
          </div>
        )}
        {r.missed.length > 0 && (
          <div>
            <div className="eyebrow">You missed</div>
            <div className="judge-chips">
              {r.missed.map((c, i) => (
                <span key={i} className="chip-miss">
                  <X size={13} /> {c}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {r.improvedAnswer && (
        <div className="tldr" style={{ maxWidth: 'none' }}>
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <div className="tldr-label">{r.mode !== 'offline' ? 'Say it like this' : 'The model short answer'}</div>
            {'speechSynthesis' in window && (
              <button className="link-btn" onClick={speak}>
                <Volume2 size={14} style={{ verticalAlign: -2 }} /> Read aloud
              </button>
            )}
          </div>
          <Markdown>{r.improvedAnswer}</Markdown>
        </div>
      )}

      <div className="judge-actions">
        {r.followUp && (
          <div className="judge-next">
            <Sparkles size={16} />
            <span>
              <strong>Next they'd ask:</strong> {r.followUp}
            </span>
          </div>
        )}
        <div className="row" style={{ gap: 8, marginLeft: 'auto' }}>
          <button className="btn" onClick={onAgain}>
            <RotateCcw size={14} /> Try again
          </button>
          {r.followUp && (
            <button className="btn primary" onClick={onFollowUp}>
              Answer the follow-up
            </button>
          )}
        </div>
      </div>
      {r.note && <p className="muted judge-note">{r.note}</p>}
    </div>
  )
}

/** Past scores for this question, oldest first: a tiny line so progress is visible at a glance. */
function Trend({ scores }: { scores: number[] }) {
  const W = 160
  const H = 34
  const x = (i: number) => 4 + (i * (W - 8)) / Math.max(1, scores.length - 1)
  const y = (v: number) => H - 4 - (v / 10) * (H - 8)
  return (
    <div className="judge-trend">
      <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} aria-label={`Your scores: ${scores.join(', ')}`}>
        <polyline points={scores.map((s, i) => `${x(i)},${y(s)}`).join(' ')} fill="none" stroke="var(--accent)" strokeWidth="2" />
        {scores.map((s, i) => (
          <circle key={i} cx={x(i)} cy={y(s)} r={i === scores.length - 1 ? 3.5 : 2.5} fill="var(--accent)" />
        ))}
      </svg>
      <span className="muted">{scores.map((s) => s.toFixed(1)).join(' → ')}</span>
    </div>
  )
}
