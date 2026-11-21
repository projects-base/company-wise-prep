import { useEffect, useRef, useState, type RefObject } from 'react'
import { Link } from 'react-router-dom'
import { Download, Highlighter, NotebookPen, Pencil, StickyNote, Trash, X } from 'lucide-react'
import { api, type LessonNotes, type LessonSummary, type Mistake, type Note, type SummaryDraft } from './api'

/**
 * Lesson notes on a lesson page: select text to highlight it (optionally with a margin note), see highlights marked
 * in the lesson, and keep a notes panel with the highlights, the 4-field summary and the lesson's quiz mistakes.
 */

export const QUOTE_MAX = 1000
export const NOTE_MAX = 4000
const SUMMARY_MAX = 2000

// ── Marking highlights in the rendered lesson ──────────────────────────────────────────────

/** The few hast shapes the plugin touches (react-markdown's tree after remark-rehype). */
type HText = { type: 'text'; value: string }
type HElement = { type: 'element'; tagName: string; properties: Record<string, unknown>; children: HNode[] }
type HNode = HText | HElement | { type: string; children?: HNode[] }
type HParent = { children: HNode[] }

const isText = (n: HNode): n is HText => n.type === 'text'
const isElement = (n: HNode): n is HElement => n.type === 'element'
const hasChildren = (n: HNode): n is HNode & HParent => Array.isArray((n as HParent).children)
const hastText = (n: HNode): string => (isText(n) ? n.value : hasChildren(n) ? n.children.map(hastText).join('') : '')

type Range = { start: number; end: number; id: number; num: number; last: boolean }

/**
 * A rehype plugin that wraps each highlight's quote in <mark class="hl">, inside its own section ("## " heading).
 * Whitespace is ignored when matching, because a browser selection and the rendered text break lines differently.
 * Code blocks and headings are never marked; a quote that is no longer in the lesson is simply not marked.
 *
 * @param startSection the section the Markdown starts in (the part's title in one-part-at-a-time mode)
 */
export function highlightPlugin(highlights: Note[], startSection: string | null) {
  return () => (tree: HParent) => {
    if (!highlights.length) return
    const segs: { node: HText; parent: HParent; section: string | null }[] = []
    let section = startSection
    const walk = (parent: HParent) => {
      for (const child of parent.children) {
        if (isText(child)) segs.push({ node: child, parent, section })
        else if (isElement(child) && child.tagName === 'h2') section = hastText(child).trim()
        else if (isElement(child) && child.tagName === 'pre') continue
        else if (hasChildren(child)) walk(child)
      }
    }
    walk(tree)

    const ranges = new Map<HText, Range[]>()
    highlights.forEach((h, i) => {
      const needle = (h.quote ?? '').replace(/\s+/g, '')
      if (!needle) return
      const pool = segs.filter((s) => !h.section || s.section === h.section.trim())
      // Whitespace-free text of the pool, with each character's segment and offset.
      let flat = ''
      const where: [number, number][] = []
      pool.forEach((s, si) => {
        for (let o = 0; o < s.node.value.length; o++) {
          if (/\s/.test(s.node.value[o])) continue
          flat += s.node.value[o]
          where.push([si, o])
        }
      })
      const at = flat.indexOf(needle)
      if (at < 0) return
      const first = where[at]
      const lastChar = where[at + needle.length - 1]
      const mine: [HText, Range][] = []
      for (let si = first[0]; si <= lastChar[0]; si++) {
        const node = pool[si].node
        const start = si === first[0] ? first[1] : 0
        const end = si === lastChar[0] ? lastChar[1] + 1 : node.value.length
        if (end > start) mine.push([node, { start, end, id: h.id, num: i + 1, last: si === lastChar[0] }])
      }
      const overlaps = mine.some(([node, r]) => (ranges.get(node) ?? []).some((o) => r.start < o.end && o.start < r.end))
      if (overlaps) return
      for (const [node, r] of mine) ranges.set(node, [...(ranges.get(node) ?? []), r])
    })

    const parents = new Set(segs.filter((s) => ranges.has(s.node)).map((s) => s.parent))
    for (const parent of parents) {
      parent.children = parent.children.flatMap((child) =>
        isText(child) && ranges.has(child) ? split(child, ranges.get(child)!) : [child],
      )
    }
  }
}

function split(node: HText, ranges: Range[]): HNode[] {
  const out: HNode[] = []
  let pos = 0
  for (const r of [...ranges].sort((a, b) => a.start - b.start)) {
    if (r.start > pos) out.push({ type: 'text', value: node.value.slice(pos, r.start) })
    out.push({
      type: 'element',
      tagName: 'mark',
      properties: { className: ['hl'], dataHl: String(r.id), title: 'Your highlight: open it in My notes' },
      children: [{ type: 'text', value: node.value.slice(r.start, r.end) }],
    })
    if (r.last) {
      out.push({ type: 'element', tagName: 'sup', properties: { className: ['hl-num'], dataHl: String(r.id) }, children: [
        { type: 'text', value: String(r.num) },
      ] })
    }
    pos = r.end
  }
  if (pos < node.value.length) out.push({ type: 'text', value: node.value.slice(pos) })
  return out
}

/** The "## " heading a node sits under: the nearest h2 before it, looking outwards from the node. */
function sectionOf(node: Node, root: HTMLElement): string | null {
  let el: Element | null = node.nodeType === Node.ELEMENT_NODE ? (node as Element) : node.parentElement
  while (el && el !== root) {
    if (el.tagName === 'H2') return el.textContent?.trim() || null
    for (let p = el.previousElementSibling; p; p = p.previousElementSibling) {
      if (p.tagName === 'H2') return p.textContent?.trim() || null
    }
    el = el.parentElement
  }
  return null
}

// ── The selection toolbar ──────────────────────────────────────────────────────────────────

type Picked = { quote: string; section: string | null; top: number; left: number }

/** A small toolbar above selected lesson text: Highlight, or Add note (a highlight with a margin note). */
export function SelectionToolbar({ container, onSave }: {
  container: RefObject<HTMLElement | null>
  onSave: (quote: string, section: string | null, note: string | null) => Promise<void>
}) {
  const [picked, setPicked] = useState<Picked | null>(null)
  const [writing, setWriting] = useState(false)
  const [note, setNote] = useState('')
  const [error, setError] = useState<string | null>(null)
  const bar = useRef<HTMLDivElement>(null)
  const writingRef = useRef(writing)
  writingRef.current = writing

  useEffect(() => {
    const close = () => {
      setPicked(null)
      setWriting(false)
      setNote('')
      setError(null)
    }
    const check = (e: Event) => {
      if (bar.current?.contains(e.target as Node)) return
      // After the browser has updated the selection.
      setTimeout(() => {
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
        const quote = sel.toString().trim()
        if (quote.length < 3) return
        const rect = range.getBoundingClientRect()
        const top = rect.top > 60 ? rect.top - 46 : rect.bottom + 8
        const left = Math.max(12, Math.min(window.innerWidth - 300, rect.left + rect.width / 2 - 120))
        setPicked({ quote, section: sectionOf(range.startContainer, root), top, left })
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
      document.removeEventListener('mouseup', check)
      document.removeEventListener('touchend', check)
      document.removeEventListener('keyup', onKey)
      window.removeEventListener('scroll', onScroll)
    }
  }, [container])

  if (!picked) return null
  const tooLong = picked.quote.length > QUOTE_MAX
  const save = async (withNote: string | null) => {
    try {
      await onSave(picked.quote, picked.section, withNote)
      window.getSelection()?.removeAllRanges()
      setPicked(null)
      setWriting(false)
      setNote('')
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <div className={`sel-bar ${writing ? 'writing' : ''}`} ref={bar} style={{ top: picked.top, left: picked.left }} role="toolbar"
      aria-label="Selected text">
      {tooLong ? (
        <span className="sel-bar-msg">Select under 1,000 characters to highlight</span>
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

export function NotesPanel({ moduleId, code, data, reload, tab, setTab, focusId, onClose }: {
  moduleId: string
  code: string
  data: LessonNotes | null
  reload: () => void
  tab: NotesTab
  setTab: (t: NotesTab) => void
  /** A highlight to scroll to (clicked in the lesson text). */
  focusId: number | null
  onClose: () => void
}) {
  const [status, setStatus] = useState<string>('')
  const [exportError, setExportError] = useState<string | null>(null)
  const highlights = data?.highlights ?? []
  const snippets = data?.snippets ?? []
  const mistakes = data?.mistakes ?? []
  const openMistakes = mistakes.filter((m) => !m.fixed).length

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
                <HighlightItem key={h.id} h={h} num={i + 1} focus={focusId === h.id} onChange={reload} />
              ))}
            </ol>
            {snippets.length > 0 && (
              <>
                <div className="eyebrow notes-sub">From the Playground</div>
                <ul className="notes-list">
                  {snippets.map((n) => (
                    <SnippetItem key={n.id} n={n} onChange={reload} />
                  ))}
                </ul>
              </>
            )}
          </>
        ) : tab === 'summary' ? (
          <SummaryEditor key={moduleId} moduleId={moduleId} initial={data.summary} onStatus={setStatus} />
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

function HighlightItem({ h, num, focus, onChange }: { h: Note; num: number; focus: boolean; onChange: () => void }) {
  const [editing, setEditing] = useState(false)
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
  const remove = async () => {
    if (!window.confirm('Delete this highlight and its note?')) return
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
          <p className="notes-note"><strong>My note:</strong> {h.note}</p>
        ) : null}
        <div className="notes-item-meta">
          {h.section && <span className="muted">{h.section}</span>}
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

function SnippetItem({ n, onChange }: { n: Note; onChange: () => void }) {
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
        {n.note && <p className="notes-note">{n.note}</p>}
        <pre className="notes-code"><code>{n.code}</code></pre>
        {n.output && <pre className="notes-code out">{n.output}</pre>}
        <div className="notes-item-meta">
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

/** The 4-field lesson summary, autosaved a moment after you stop typing (and when you leave). */
export function SummaryEditor({ moduleId, initial, onStatus }: {
  moduleId: string
  initial: LessonSummary | null
  onStatus: (s: string) => void
}) {
  const [draft, setDraft] = useState<SummaryDraft>({
    idea: initial?.idea ?? '',
    inside: initial?.inside ?? '',
    trap: initial?.trap ?? '',
    line: initial?.line ?? '',
  })
  const pending = useRef<SummaryDraft | null>(null)
  const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  const flush = () => {
    clearTimeout(timer.current)
    const d = pending.current
    if (!d) return
    pending.current = null
    onStatus('Saving…')
    api.saveSummary(moduleId, d).then(
      () => onStatus(`Saved ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`),
      () => onStatus('Not saved — type again to retry'),
    )
  }
  // Leaving the tab or the lesson saves what was typed.
  useEffect(() => () => flush(), []) // eslint-disable-line react-hooks/exhaustive-deps

  const change = (key: keyof SummaryDraft, value: string) => {
    const next = { ...draft, [key]: value }
    setDraft(next)
    pending.current = next
    clearTimeout(timer.current)
    timer.current = setTimeout(flush, 800)
  }

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
