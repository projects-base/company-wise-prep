import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import Markdown from 'react-markdown'
import { ExternalLink, Lightbulb, LifeBuoy, MessageCircleQuestion, Send, Sparkles, X } from 'lucide-react'
import { api, type LifelineHelp, type LifelineKind } from './api'

/**
 * A help panel that slides in beside the question or lesson, so getting unstuck never means
 * leaving the page. Help comes in rungs (a nudge, the key idea, the full explanation) so you
 * take only as much as you need, plus a box for your own doubts. On every other page it is "Ask AI"
 * (context general): no rungs, just doubts, and the conversation follows you between pages.
 */

const RUNGS: { kind: LifelineKind; label: string; icon: typeof Lightbulb }[] = [
  { kind: 'NUDGE', label: 'Nudge me', icon: Lightbulb },
  { kind: 'IDEA', label: 'Key idea', icon: Sparkles },
  { kind: 'EXPLAIN', label: 'Explain it', icon: MessageCircleQuestion },
]
const ASKED: Record<LifelineKind, string> = { NUDGE: 'Nudge me', IDEA: 'Key idea', EXPLAIN: 'Explain it', ASK: '' }
const BY: Record<string, string> = { claude: 'Claude', gemini: 'Gemini', offline: 'From the model answer' }

type Entry = { q: string; help?: LifelineHelp; error?: string }

export function Lifeline({ context, refId, title }: { context: 'question' | 'lesson' | 'general'; refId: string; title: string }) {
  const general = context === 'general'
  const [open, setOpen] = useState(false)
  const [entries, setEntries] = useState<Entry[]>([])
  const [doubt, setDoubt] = useState('')
  const [busy, setBusy] = useState(false)
  const end = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open])
  useEffect(() => {
    end.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [entries])

  const ask = async (kind: LifelineKind, text?: string) => {
    const q = kind === 'ASK' ? (text ?? '').trim() : ASKED[kind]
    if (!q || busy) return
    const history = entries.filter((e) => e.help && e.help.mode !== 'offline').map((e) => ({ q: e.q, a: e.help!.text }))
    setEntries((es) => [...es, { q }])
    setBusy(true)
    if (kind === 'ASK') setDoubt('')
    try {
      const help = await api.lifeline({ context, ref: refId, kind, doubt: kind === 'ASK' ? q : undefined, history })
      setEntries((es) => es.map((e, i) => (i === es.length - 1 ? { ...e, help } : e)))
    } catch (err) {
      setEntries((es) => es.map((e, i) => (i === es.length - 1 ? { ...e, error: err instanceof Error ? err.message : 'No help came back' } : e)))
    } finally {
      setBusy(false)
    }
  }

  const used = entries.length
  const lastDoubt = [...entries].reverse().find((e) => e.q)?.q
  const google = `https://www.google.com/search?udm=50&q=${encodeURIComponent(
    general ? `For a senior Java backend interview: ${doubt.trim() || lastDoubt || 'how to prepare'}` : `Explain for a senior Java backend interview: ${title}`,
  )}`

  // Portalled to <body>: an animated (transformed) page wrapper would otherwise trap the fixed panel.
  return createPortal(
    <>
      {!open && (
        <button className="lifeline-fab" onClick={() => setOpen(true)} aria-label={general ? 'Ask AI a doubt' : 'Open Lifeline: hints and doubts'}>
          {general ? <Sparkles size={18} /> : <LifeBuoy size={18} />} <span>{general ? 'Ask AI' : 'Stuck?'}</span>
          {used > 0 && <b className="num">{used}</b>}
        </button>
      )}
      {open && (
        <aside className="lifeline" aria-label="Lifeline">
          <div className="lifeline-head">
            {general ? <Sparkles size={18} /> : <LifeBuoy size={18} />}
            <strong>{general ? 'Ask AI' : 'Lifeline'}</strong>
            <span className="muted lifeline-used">
              {general ? `On: ${title}` : used === 0 ? 'Take only the help you need' : `${used} used`}
            </span>
            <button className="btn ghost sm" onClick={() => setOpen(false)} aria-label="Close Lifeline">
              <X size={16} />
            </button>
          </div>

          {!general && <div className="lifeline-rungs">
            {RUNGS.map((r) => (
              <button key={r.kind} className="btn sm" disabled={busy} onClick={() => ask(r.kind)}>
                <r.icon size={14} /> {r.label}
              </button>
            ))}
          </div>}

          <div className="lifeline-log">
            {entries.length === 0 && general && (
              <p className="muted lifeline-empty">
                Any doubt, any time: Java, Spring, SQL, system design, DSA, interviews, or how to use your plan. Short answers, so
                you get back to studying.
              </p>
            )}
            {entries.length === 0 && !general && (
              <p className="muted lifeline-empty">
                Try to recall first. Then take a <strong>nudge</strong>, and climb to the <strong>key idea</strong> or a full{' '}
                <strong>explanation</strong> only if you still need it. Or type your own doubt below.
              </p>
            )}
            {entries.map((e, i) => (
              <div key={i} className="lifeline-entry">
                <div className="lifeline-q">{e.q}</div>
                {e.help ? (
                  <div className="lifeline-a">
                    <Markdown>{e.help.text}</Markdown>
                    <div className="lifeline-by muted">{BY[e.help.mode] ?? e.help.mode}</div>
                    {e.help.note && <div className="lifeline-note muted">{e.help.note}</div>}
                  </div>
                ) : e.error ? (
                  <div className="lifeline-a judge-error">{e.error}</div>
                ) : (
                  <div className="lifeline-a muted lifeline-thinking">Thinking…</div>
                )}
              </div>
            ))}
            <div ref={end} />
          </div>

          <form
            className="lifeline-ask"
            onSubmit={(e) => {
              e.preventDefault()
              void ask('ASK', doubt)
            }}
          >
            <input value={doubt} onChange={(e) => setDoubt(e.target.value)} placeholder={general ? 'Ask anything: Java, Spring, system design, your plan…' : 'Ask a doubt about this page…'} maxLength={2000} />
            <button className="btn primary sm" disabled={busy || !doubt.trim()} aria-label="Ask">
              <Send size={14} />
            </button>
          </form>
          <a className="lifeline-google muted" href={google} target="_blank" rel="noreferrer" title="Right-click → open in an InPrivate / incognito window">
            Ask Google AI Mode instead <ExternalLink size={12} />
          </a>
        </aside>
      )}
    </>,
    document.body,
  )
}
