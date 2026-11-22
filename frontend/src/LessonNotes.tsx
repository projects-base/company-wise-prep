import { useEffect, useRef, useState, type RefObject } from 'react'
import { Link } from 'react-router-dom'
import { Copy, Download, Highlighter, NotebookPen, Pencil, StickyNote, Trash, X } from 'lucide-react'
import { api, type Card, type LessonNotes, type LessonSummary, type Mistake, type Note, type SummaryDraft } from './api'
import { compact, normaliseSection, PREFIX_LENGTH } from './highlights'
import { LinkedText } from './LinkedText'

/**
 * Lesson notes on a lesson page: select text to highlight it (optionally with a margin note), see highlights marked
 * in the lesson (highlights.ts), turn a highlight into a flashcard, and keep a notes panel with the highlights, the
 * 4-field summary and the lesson's quiz mistakes.
 */

export const QUOTE_MAX = 1000
export const NOTE_MAX = 4000
const SUMMARY_MAX = 2000
export const FRONT_MAX = 1000
export const BACK_MAX = 4000

/** A card for a new highlight. */
export type CardDraft = { front: string; back: string }

/** The suggested front for a highlight's card: the first half of the quote to finish from memory (cloze style). */
export function suggestFront(quote: string, section: string | null): string {
  const words = quote.trim().split(/\s+/)
  const lead = words.slice(0, Math.max(3, Math.min(12, Math.floor(words.length / 2)))).join(' ')
  return `${section ? `${section}: ` : ''}finish the thought: "${lead} …"`
}

/** The default back of a highlight's card: the quote, then the margin note. */
export const cardBack = (quote: string, note: string | null) => (note ? `${quote}\n\nMy note: ${note}` : quote)

/** Front and back of a flashcard, the front pre-filled with a suggestion. */
export function CardEditor({ initial, onSave, onCancel, idPrefix }: {
  initial: CardDraft
  onSave: (c: CardDraft) => Promise<void> | void
  onCancel: () => void
  idPrefix: string
}) {
  const [front, setFront] = useState(initial.front)
  const [back, setBack] = useState(initial.back)
  return (
    <form
      className="card-editor"
      onSubmit={(e) => {
        e.preventDefault()
        if (front.trim()) onSave({ front: front.trim(), back: back.trim() })
      }}
    >
      <label htmlFor={`${idPrefix}-front`} className="card-editor-label">Front: the question</label>
      <textarea id={`${idPrefix}-front`} rows={2} autoFocus maxLength={FRONT_MAX} value={front}
        onChange={(e) => setFront(e.target.value)} />
      <label htmlFor={`${idPrefix}-back`} className="card-editor-label">Back: the answer</label>
      <textarea id={`${idPrefix}-back`} rows={3} maxLength={BACK_MAX} value={back} onChange={(e) => setBack(e.target.value)} />
      <div className="row">
        <button type="submit" className="btn sm primary" disabled={!front.trim()}>Make card</button>
        <button type="button" className="btn sm ghost" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  )
}

/** The "## " heading a node sits under: the nearest h2 before it, looking outwards from the node. */
function sectionOf(node: Node, root: HTMLElement): string | null {
  let el: Element | null = node.nodeType === Node.ELEMENT_NODE ? (node as Element) : node.parentElement
  while (el && el !== root) {
    if (el.tagName === 'H2') return normaliseSection(el.textContent)
    for (let p = el.previousElementSibling; p; p = p.previousElementSibling) {
      if (p.tagName === 'H2') return normaliseSection(p.textContent)
    }
    el = el.parentElement
  }
  return null
}

/** The selected text without the numbers of highlights it crosses (their <sup> markers are not lesson text). */
function selectedText(range: Range, sel: Selection): string {
  const frag = range.cloneContents()
  const markers = frag.querySelectorAll('sup.hl-num')
  if (!markers.length) return sel.toString()
  markers.forEach((n) => n.remove())
  // textContent runs blocks together; keep a line break between them, as Selection.toString would.
  frag.querySelectorAll('p, li, div, blockquote, tr, h3, h4, h5, h6').forEach((el) => el.append('\n'))
  return frag.textContent ?? ''
}

/** A little text just before the selection in its block (whitespace removed), so a repeated quote marks the right one. */
function prefixOf(range: Range, root: HTMLElement): string | null {
  const node = range.startContainer
  const start = node.nodeType === Node.ELEMENT_NODE ? (node as Element) : node.parentElement
  const block = start?.closest('p, li, td, th, blockquote, dd, dt, h3, h4, h5, h6')
  if (!block || !root.contains(block)) return null
  const before = document.createRange()
  before.setStart(block, 0)
  before.setEnd(range.startContainer, range.startOffset)
  const frag = before.cloneContents()
  frag.querySelectorAll('sup.hl-num').forEach((n) => n.remove())
  const text = compact(frag.textContent ?? '')
  return text ? text.slice(-PREFIX_LENGTH) : null
}

// ── The selection toolbar ──────────────────────────────────────────────────────────────────

type Picked = { quote: string; section: string | null; prefix: string | null; top: number; left: number }

/**
 * A small toolbar above selected lesson text: Highlight, Add note (a highlight with a margin note), or Make flashcard
 * (a highlight with a review card).
 */
export function SelectionToolbar({ container, onSave }: {
  container: RefObject<HTMLElement | null>
  onSave: (quote: string, section: string | null, prefix: string | null, note: string | null, card?: CardDraft) => Promise<void>
}) {
  const [picked, setPicked] = useState<Picked | null>(null)
  const [mode, setMode] = useState<'note' | 'card' | null>(null)
  const writing = mode !== null
  const setWriting = (on: boolean) => setMode(on ? 'note' : null)
  const [note, setNote] = useState('')
  const [error, setError] = useState<string | null>(null)
  const bar = useRef<HTMLDivElement>(null)
  const saving = useRef(false)
  const writingRef = useRef(writing)
  writingRef.current = writing

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout> | undefined
    const close = () => {
      setPicked(null)
      setWriting(false)
      setNote('')
      setError(null)
    }
    const check = (e: Event) => {
      if (bar.current?.contains(e.target as Node)) return
      // After the browser has updated the selection.
      clearTimeout(timer)
      timer = setTimeout(() => {
        const sel = window.getSelection()
        const root = container.current
        if (!sel || sel.isCollapsed || !sel.rangeCount || !root) {
          if (!writingRef.current) close()
          return
        }
        const range = sel.getRangeAt(0)
        const inside = root.contains(range.commonAncestorContainer)
        const start = range.startContainer.parentElement
        if (!inside || start?.closest('.quiz, .stepper-nav, .stepper-bar, .code-actions, button')) return
        const quote = selectedText(range, sel).trim()
        if (quote.length < 3) return
        const rect = range.getBoundingClientRect()
        const top = rect.top > 60 ? rect.top - 46 : rect.bottom + 8
        const left = Math.max(12, Math.min(window.innerWidth - 300, rect.left + rect.width / 2 - 120))
        setPicked({ quote, section: sectionOf(range.startContainer, root), prefix: prefixOf(range, root), top, left })
        setWriting(false)
        setNote('')
        setError(null)
      }, 0)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') close()
      else check(e)
    }
    const onScroll = () => {
      if (!writingRef.current) close()
    }
    document.addEventListener('mouseup', check)
    document.addEventListener('touchend', check)
    document.addEventListener('keyup', onKey)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => {
      clearTimeout(timer)
      document.removeEventListener('mouseup', check)
      document.removeEventListener('touchend', check)
      document.removeEventListener('keyup', onKey)
      window.removeEventListener('scroll', onScroll)
    }
  }, [container])

  if (!picked) return null
  const tooLong = picked.quote.length > QUOTE_MAX
  // A double click or a second Enter must not save the highlight (and its card) twice.
  const save = async (withNote: string | null, card?: CardDraft) => {
    if (saving.current) return
    saving.current = true
    try {
      await onSave(picked.quote, picked.section, picked.prefix, withNote, card)
      window.getSelection()?.removeAllRanges()
      setPicked(null)
      setWriting(false)
      setNote('')
    } catch (e) {
      setError((e as Error).message)
    } finally {
      saving.current = false
    }
  }

  return (
    <div className={`sel-bar ${writing ? 'writing' : ''}`} ref={bar} style={{ top: picked.top, left: picked.left }} role="toolbar"
      aria-label="Selected text">
      {tooLong ? (
        <span className="sel-bar-msg">Select under 1,000 characters to highlight</span>
      ) : mode === 'card' ? (
        <CardEditor idPrefix="sel-card" initial={{ front: suggestFront(picked.quote, picked.section), back: picked.quote }}
          onSave={(c) => save(null, c)} onCancel={() => setMode(null)} />
      ) : writing ? (
        <form
          className="sel-bar-note"
          onSubmit={(e) => {
            e.preventDefault()
            save(note.trim() || null)
          }}
        >
          <label className="sr-only" htmlFor="sel-note">Your note</label>
          <textarea id="sel-note" rows={3} autoFocus maxLength={NOTE_MAX} value={note} onChange={(e) => setNote(e.target.value)}
            placeholder="In your own words…" />
          <div className="row">
            <button type="submit" className="btn sm primary">Save</button>
            <button type="button" className="btn sm ghost" onClick={() => setWriting(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <>
          <button type="button" onClick={() => save(null)}>
            <Highlighter size={14} /> Highlight
          </button>
          <button type="button" onClick={() => setWriting(true)}>
            <StickyNote size={14} /> Add note
          </button>
          <button type="button" onClick={() => setMode('card')}>
            <Copy size={14} /> Make flashcard
          </button>
        </>
      )}
      {error && <span className="sel-bar-msg error">Not saved: {error}</span>}
    </div>
  )
}

// ── The notes panel ────────────────────────────────────────────────────────────────────────

export type NotesTab = 'highlights' | 'summary' | 'mistakes'

/** A file download from text the browser already has (the cheat-sheet export). */
export function downloadText(filename: string, text: string) {
  const url = URL.createObjectURL(new Blob([text], { type: 'text/markdown;charset=utf-8' }))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export function NotesPanel({ moduleId, code, data, cards, onCardMade, reload, onSummarySaved, tab, setTab, focusId,
  onClose }: {
  moduleId: string
  code: string
  data: LessonNotes | null
  /** The lesson's review cards, for the "Flashcard on" chips. */
  cards: Card[]
  onCardMade: (c: Card) => void
  reload: () => void
  /** Each saved summary, so the page's notes stay current (reopening the panel must not show older text). */
  onSummarySaved: (s: LessonSummary | null) => void
  tab: NotesTab
  setTab: (t: NotesTab) => void
  /** A highlight to scroll to (clicked in the lesson text). */
  focusId: number | null
  onClose: () => void
}) {
  const [status, setStatus] = useState<string>('')
  const [exportError, setExportError] = useState<string | null>(null)
  const summary = useSummaryDraft(moduleId, data ? data.summary : undefined, onSummarySaved, setStatus)
  const highlights = data?.highlights ?? []
  const snippets = data?.snippets ?? []
  const mistakes = data?.mistakes ?? []
  const explanations = data?.explanations ?? []
  const openMistakes = mistakes.filter((m) => !m.fixed).length
  const carded = (kind: Card['sourceKind'], id: number) => cards.some((c) => c.sourceKind === kind && c.sourceId === id)

  const exportLesson = async () => {
    setExportError(null)
    try {
      const { filename, markdown } = await api.exportNotes('lesson', moduleId)
      downloadText(filename, markdown)
    } catch (e) {
      setExportError((e as Error).message)
    }
  }

  return (
    <aside className="notes-panel" aria-label="My notes for this lesson">
      <div className="notes-head">
        <NotebookPen size={17} />
        <strong>My notes · {code}</strong>
        <span className="muted notes-status" aria-live="polite">{status}</span>
        <button className="icon-btn" onClick={onClose} aria-label="Close my notes">
          <X size={16} />
        </button>
      </div>
      <div className="tabs notes-tabs" role="tablist">
        <button role="tab" aria-selected={tab === 'highlights'} onClick={() => setTab('highlights')}>
          Highlights <span className="num">{highlights.length + snippets.length}</span>
        </button>
        <button role="tab" aria-selected={tab === 'summary'} onClick={() => setTab('summary')}>
          Summary
        </button>
        <button role="tab" aria-selected={tab === 'mistakes'} onClick={() => setTab('mistakes')}>
          Mistakes <span className="num">{openMistakes}</span>
        </button>
      </div>
      <div className="notes-body">
        {!data ? (
          <p className="muted">Loading…</p>
        ) : tab === 'highlights' ? (
          <>
            {highlights.length === 0 && snippets.length === 0 && (
              <p className="muted notes-empty">Select any sentence in the lesson, then choose Highlight or Add note.</p>
            )}
            <ol className="notes-list">
              {highlights.map((h, i) => (
                <HighlightItem key={h.id} h={h} num={i + 1} focus={focusId === h.id} onChange={reload}
                  hasCard={carded('HIGHLIGHT', h.id)} onCardMade={onCardMade} />
              ))}
            </ol>
            {explanations.length > 0 && (
              <>
                <div className="eyebrow notes-sub">My explanation</div>
                <ul className="notes-list">
                  {explanations.map((e) => (
                    <li key={e.id} className="notes-item">
                      <div className="notes-item-main">
                        <p className="notes-note">{e.improved && <LinkedText text={e.improved} />}</p>
                        <div className="notes-item-meta">
                          <span className="muted">Checked {e.score}/10</span>
                          {carded('EXPLANATION', e.id) && <span className="pill accent">Flashcard on</span>}
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              </>
            )}
            {snippets.length > 0 && (
              <>
                <div className="eyebrow notes-sub">From the Playground</div>
                <ul className="notes-list">
                  {snippets.map((n) => (
                    <SnippetItem key={n.id} n={n} onChange={reload} hasCard={carded('SNIPPET', n.id)} />
                  ))}
                </ul>
              </>
            )}
          </>
        ) : tab === 'summary' ? (
          <SummaryEditor {...summary} />
        ) : (
          <MistakeList mistakes={mistakes} />
        )}
      </div>
      <div className="notes-foot">
        <button className="btn sm" onClick={exportLesson}>
          <Download size={14} /> Export .md
        </button>
        <Link to="/notes" className="btn sm ghost">Open all notes</Link>
        {exportError && <span className="error">{exportError}</span>}
      </div>
    </aside>
  )
}

function HighlightItem({ h, num, focus, onChange, hasCard, onCardMade }: {
  h: Note
  num: number
  focus: boolean
  onChange: () => void
  hasCard: boolean
  onCardMade: (c: Card) => void
}) {
  const [editing, setEditing] = useState(false)
  const [carding, setCarding] = useState(false)
  const [text, setText] = useState(h.note ?? '')
  const [error, setError] = useState<string | null>(null)
  const item = useRef<HTMLLIElement>(null)
  useEffect(() => {
    if (focus) item.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }, [focus])
  const save = async () => {
    try {
      await api.editNote(h.id, text.trim() || null)
      setEditing(false)
      onChange()
    } catch (e) {
      setError((e as Error).message)
    }
  }
  const makeCard = async (c: CardDraft) => {
    try {
      onCardMade(await api.makeCard({ sourceKind: 'HIGHLIGHT', sourceId: h.id, front: c.front, back: c.back || null }))
      setCarding(false)
    } catch (e) {
      setError((e as Error).message)
    }
  }
  const remove = async () => {
    if (!window.confirm('Delete this highlight and its note? Its flashcard, if any, stays.')) return
    try {
      await api.deleteNote(h.id)
      onChange()
    } catch (e) {
      setError((e as Error).message)
    }
  }
  return (
    <li className={`notes-item ${focus ? 'focus' : ''}`} ref={item}>
      <span className="hl-badge" aria-hidden>{num}</span>
      <div className="notes-item-main">
        <blockquote className="notes-quote">{h.quote}</blockquote>
        {editing ? (
          <div className="notes-edit">
            <label className="sr-only" htmlFor={`note-${h.id}`}>Your note</label>
            <textarea id={`note-${h.id}`} rows={3} autoFocus maxLength={NOTE_MAX} value={text}
              onChange={(e) => setText(e.target.value)} />
            <div className="row">
              <button className="btn sm primary" onClick={save}>Save</button>
              <button className="btn sm ghost" onClick={() => setEditing(false)}>Cancel</button>
            </div>
          </div>
        ) : h.note ? (
          <p className="notes-note"><strong>My note:</strong> <LinkedText text={h.note} /></p>
        ) : null}
        {carding && (
          <CardEditor idPrefix={`card-${h.id}`}
            initial={{ front: suggestFront(h.quote ?? '', h.section), back: cardBack(h.quote ?? '', h.note) }}
            onSave={makeCard} onCancel={() => setCarding(false)} />
        )}
        <div className="notes-item-meta">
          {h.section && <span className="muted">{h.section}</span>}
          {hasCard ? (
            <span className="pill accent">Flashcard on</span>
          ) : !carding && (
            <button className="btn sm ghost" onClick={() => setCarding(true)}>
              <Copy size={13} /> Make flashcard
            </button>
          )}
          {!editing && (
            <button className="btn sm ghost" onClick={() => setEditing(true)}>
              <Pencil size={13} /> {h.note ? 'Edit note' : 'Add a note'}
            </button>
          )}
          <button className="btn sm ghost" onClick={remove} aria-label="Delete this highlight">
            <Trash size={13} />
          </button>
        </div>
        {error && <p className="error">{error}</p>}
      </div>
    </li>
  )
}

function SnippetItem({ n, onChange, hasCard }: { n: Note; onChange: () => void; hasCard: boolean }) {
  const [error, setError] = useState<string | null>(null)
  const remove = async () => {
    if (!window.confirm('Delete this saved snippet?')) return
    try {
      await api.deleteNote(n.id)
      onChange()
    } catch (e) {
      setError((e as Error).message)
    }
  }
  return (
    <li className="notes-item">
      <div className="notes-item-main">
        {n.note && <p className="notes-note"><LinkedText text={n.note} /></p>}
        <pre className="notes-code"><code>{n.code}</code></pre>
        {n.output && <pre className="notes-code out">{n.output}</pre>}
        <div className="notes-item-meta">
          {hasCard && <span className="pill accent">Flashcard on</span>}
          <button className="btn sm ghost" onClick={remove} aria-label="Delete this snippet">
            <Trash size={13} />
          </button>
        </div>
        {error && <p className="error">{error}</p>}
      </div>
    </li>
  )
}

const SUMMARY_FIELDS: { key: keyof SummaryDraft; label: string; placeholder: string }[] = [
  { key: 'idea', label: 'Idea', placeholder: 'The one idea, in a line' },
  { key: 'inside', label: 'How it works inside', placeholder: 'The mechanism under the hood' },
  { key: 'trap', label: 'The trap', placeholder: 'What you got wrong, or what catches people' },
  { key: 'line', label: 'Interview line', placeholder: 'Your 30-second answer' },
]

const toDraft = (s: LessonSummary | null): SummaryDraft => ({
  idea: s?.idea ?? '',
  inside: s?.inside ?? '',
  trap: s?.trap ?? '',
  line: s?.line ?? '',
})

/**
 * Saves summary drafts one request at a time: while a save is in flight, newer drafts replace each other and the
 * latest goes out when it returns, so an older draft can never land after a newer one.
 */
function summarySaver(moduleId: string, onSaved: (s: LessonSummary | null) => void, onStatus: (s: string) => void) {
  let queued: SummaryDraft | null = null
  let inFlight = false
  const pump = () => {
    if (inFlight || !queued) return
    const draft = queued
    queued = null
    inFlight = true
    onStatus('Saving…')
    api
      .saveSummary(moduleId, draft)
      .then(
        (saved) => {
          const empty = !saved.idea && !saved.inside && !saved.trap && !saved.line
          onSaved(empty ? null : saved)
          if (!queued) onStatus(`Saved ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`)
        },
        () => onStatus('Not saved — type again to retry'),
      )
      .finally(() => {
        inFlight = false
        pump()
      })
  }
  return (draft: SummaryDraft) => {
    queued = draft
    pump()
  }
}

/**
 * The summary draft for a lesson, owned by the notes panel so it survives switching tabs. Autosaves 800 ms after
 * typing stops, on blur, and when the panel closes; each saved summary is pushed up to the page's notes.
 */
function useSummaryDraft(moduleId: string, loaded: LessonSummary | null | undefined,
  onSaved: (s: LessonSummary | null) => void, onStatus: (s: string) => void) {
  const [draft, setDraft] = useState<SummaryDraft | null>(loaded === undefined ? null : toDraft(loaded))
  // The notes may still be loading when the panel opens.
  useEffect(() => {
    if (draft === null && loaded !== undefined) setDraft(toDraft(loaded))
  }, [loaded, draft])
  const callbacks = useRef({ onSaved, onStatus })
  callbacks.current = { onSaved, onStatus }
  const save = useRef<((d: SummaryDraft) => void) | null>(null)
  if (!save.current) {
    save.current = summarySaver(moduleId, (s) => callbacks.current.onSaved(s), (s) => callbacks.current.onStatus(s))
  }
  const unsaved = useRef<SummaryDraft | null>(null)
  const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  const flush = () => {
    clearTimeout(timer.current)
    if (!unsaved.current) return
    save.current!(unsaved.current)
    unsaved.current = null
  }
  useEffect(() => () => flush(), []) // eslint-disable-line react-hooks/exhaustive-deps

  const change = (key: keyof SummaryDraft, value: string) => {
    const next = { ...(draft ?? toDraft(null)), [key]: value }
    setDraft(next)
    unsaved.current = next
    clearTimeout(timer.current)
    timer.current = setTimeout(flush, 800)
  }
  return { draft, change, flush }
}

/** The 4-field lesson summary form; the draft and its saving live in the notes panel. */
function SummaryEditor({ draft, change, flush }: ReturnType<typeof useSummaryDraft>) {
  if (!draft) return <p className="muted">Loading…</p>
  return (
    <div className="notes-summary">
      <p className="muted notes-hint">Lesson summary: write it after the lesson, without peeking.</p>
      {SUMMARY_FIELDS.map((f) => (
        <label key={f.key} className="notes-field">
          <span>{f.label}</span>
          <textarea rows={2} maxLength={SUMMARY_MAX} value={draft[f.key] ?? ''} placeholder={f.placeholder}
            onChange={(e) => change(f.key, e.target.value)} onBlur={flush} />
        </label>
      ))}
    </div>
  )
}

function MistakeList({ mistakes }: { mistakes: Mistake[] }) {
  if (!mistakes.length) {
    return <p className="muted notes-empty">No quiz mistakes in this lesson. Wrong answers land here and in Mistakes.</p>
  }
  return (
    <ul className="notes-list">
      {mistakes.map((m) => (
        <li key={m.id} className="notes-item">
          <div className="notes-item-main">
            <p className="notes-q">{m.question}</p>
            <p className="notes-answer">
              <span className="bad-text">Your answer:</span> {m.chosen}
            </p>
            <p className="notes-answer">
              <span className="ok-text">Correct:</span> {m.correct}
            </p>
            <div className="notes-item-meta">
              {m.fixed ? <span className="pill ok">Fixed</span> : <span className="pill warn">Open</span>}
              {m.wrongCount > 1 && <span className="muted">wrong {m.wrongCount} times</span>}
            </div>
          </div>
        </li>
      ))}
    </ul>
  )
}
