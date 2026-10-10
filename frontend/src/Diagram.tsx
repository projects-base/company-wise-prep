import { Children, isValidElement, useEffect, useId, useLayoutEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { ListOrdered, Maximize2, Minus, Plus, Workflow, X } from 'lucide-react'
import { FlowSteps, parseSequence } from './FlowSteps'

type MermaidApi = typeof import('mermaid').default

// Mermaid is large, so it loads only when a page actually has a diagram.
let mermaid: Promise<MermaidApi> | null = null
let theme: 'dark' | 'default' | null = null

function load(dark: boolean): Promise<MermaidApi> {
  mermaid ??= import('mermaid').then((m) => m.default)
  return mermaid.then((api) => {
    const wanted = dark ? 'dark' : 'default'
    if (theme !== wanted) {
      api.initialize({
        startOnLoad: false,
        theme: wanted,
        securityLevel: 'strict',
        fontFamily: 'inherit',
        fontSize: 15,
        // Natural size: the page decides whether to fit or scroll (see fit below), so text never
        // shrinks to unreadable.
        flowchart: { useMaxWidth: false, nodeSpacing: 36, rankSpacing: 44, padding: 10 },
        // Sequence ("waterfall") diagrams: wrap long messages instead of widening every column,
        // keep columns close and skip the repeated actor boxes at the bottom.
        sequence: {
          useMaxWidth: false,
          wrap: true,
          width: 132,
          actorMargin: 34,
          messageMargin: 30,
          boxMargin: 8,
          noteMargin: 8,
          mirrorActors: false,
        },
        state: { useMaxWidth: false },
        class: { useMaxWidth: false },
      })
      theme = wanted
    }
    return api
  })
}

/**
 * Renders a ```mermaid block from an answer or lesson. Sequence diagrams show first as a numbered
 * list of steps (easier to follow than lifelines), with the drawn diagram one click away. Any
 * drawing can be enlarged. Shows the source if it fails to parse.
 */
export function Diagram({ source }: { source: string }) {
  const sequence = useMemo(() => parseSequence(source), [source])
  const [view, setView] = useState<'steps' | 'diagram'>(() => {
    try {
      return localStorage.getItem('diagram.view') === 'diagram' ? 'diagram' : 'steps'
    } catch {
      return 'steps'
    }
  })
  if (!sequence) return <Drawing source={source} />
  const choose = (v: 'steps' | 'diagram') => {
    setView(v)
    try {
      localStorage.setItem('diagram.view', v)
    } catch {
      /* not remembered in a private window */
    }
  }
  const toggle = (
    <div className="seg" role="tablist" aria-label="Show as">
      <button role="tab" aria-selected={view === 'steps'} onClick={() => choose('steps')}>
        <ListOrdered size={14} /> Steps
      </button>
      <button role="tab" aria-selected={view === 'diagram'} onClick={() => choose('diagram')}>
        <Workflow size={14} /> Diagram
      </button>
    </div>
  )
  return view === 'steps' ? (
    <figure className="diagram-wrap">
      <div className="diagram-bar">{toggle}</div>
      <div className="diagram flow-box">
        <FlowSteps rows={sequence.rows} />
      </div>
    </figure>
  ) : (
    <Drawing source={source} bar={toggle} />
  )
}

/** The mermaid drawing itself, with an Enlarge button. */
function Drawing({ source, bar }: { source: string; bar?: ReactNode }) {
  const id = 'd' + useId().replace(/[^a-zA-Z0-9]/g, '')
  const [svg, setSvg] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)
  const [big, setBig] = useState(false)
  const box = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let live = true
    const dark = window.matchMedia('(prefers-color-scheme: dark)').matches
    load(dark)
      .then((api) => api.render(id, source))
      .then((r) => live && setSvg(r.svg))
      .catch(() => {
        document.getElementById('d' + id)?.remove() // mermaid leaves an error node behind
        if (live) setFailed(true)
      })
    return () => {
      live = false
    }
  }, [id, source])

  // Fit to the column when that keeps text readable; otherwise keep the natural size and scroll.
  useLayoutEffect(() => {
    const el = box.current?.querySelector('svg')
    if (!el || !box.current) return
    const natural = el.viewBox.baseVal?.width || el.getBoundingClientRect().width
    el.style.maxWidth = natural <= box.current.clientWidth * 1.3 ? '100%' : 'none'
    el.style.width = `${natural}px`
    el.style.height = 'auto'
  }, [svg])

  if (failed) return <pre className="diagram-fallback">{source}</pre>
  return (
    <figure className="diagram-wrap">
      <div className="diagram-bar">
        {bar}
        <span className="spacer" />
        {svg && (
          <button className="btn sm ghost" onClick={() => setBig(true)} title="Open full screen">
            <Maximize2 size={14} /> Enlarge
          </button>
        )}
      </div>
      {svg ? (
        <div className="diagram" ref={box} dangerouslySetInnerHTML={{ __html: svg }} />
      ) : (
        <div className="diagram loading muted">Drawing diagram…</div>
      )}
      {big && svg && <Enlarged svg={svg} onClose={() => setBig(false)} />}
    </figure>
  )
}

/** Full-screen view with zoom; Esc or the backdrop closes it. */
function Enlarged({ svg, onClose }: { svg: string; onClose: () => void }) {
  const [zoom, setZoom] = useState(1)
  const body = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const key = (e: KeyboardEvent) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', key)
    document.body.style.overflow = 'hidden'
    return () => {
      window.removeEventListener('keydown', key)
      document.body.style.overflow = ''
    }
  }, [onClose])

  // Start at "fit the screen" (but never above 1.6×).
  useLayoutEffect(() => {
    const el = body.current?.querySelector('svg')
    if (!el || !body.current) return
    const w = el.viewBox.baseVal?.width || 800
    const h = el.viewBox.baseVal?.height || 600
    const fit = Math.min(body.current.clientWidth / w, body.current.clientHeight / h) * 0.95
    setZoom(Math.min(1.6, Math.max(0.4, fit)))
  }, [])

  useLayoutEffect(() => {
    const el = body.current?.querySelector('svg')
    if (!el) return
    const w = el.viewBox.baseVal?.width || 800
    el.style.maxWidth = 'none'
    el.style.width = `${w * zoom}px`
    el.style.height = 'auto'
  }, [zoom])

  return (
    <div className="diagram-modal" onClick={onClose} role="dialog" aria-modal="true">
      <div className="diagram-modal-bar" onClick={(e) => e.stopPropagation()}>
        <button className="btn sm" onClick={() => setZoom((z) => Math.max(0.3, z / 1.2))} title="Zoom out">
          <Minus size={14} />
        </button>
        <span className="num" style={{ minWidth: 48, textAlign: 'center' }}>{Math.round(zoom * 100)}%</span>
        <button className="btn sm" onClick={() => setZoom((z) => Math.min(4, z * 1.2))} title="Zoom in">
          <Plus size={14} />
        </button>
        <span className="spacer" />
        <button className="btn sm" onClick={onClose} title="Close (Esc)">
          <X size={14} /> Close
        </button>
      </div>
      <div className="diagram-modal-body" ref={body} onClick={(e) => e.stopPropagation()} dangerouslySetInnerHTML={{ __html: svg }} />
    </div>
  )
}

/** The text of a ```mermaid fence inside a Markdown <pre>, or null for any other code block. */
export function mermaidSource(children: ReactNode): string | null {
  const code = Children.toArray(children)[0]
  if (!isValidElement<{ className?: string; children?: ReactNode }>(code)) return null
  if (!(code.props.className ?? '').includes('language-mermaid')) return null
  return Children.toArray(code.props.children).join('').trim()
}

/** Markdown `pre` that draws ```mermaid fences and leaves other code blocks alone. */
export function MdPre({ children }: { children?: ReactNode }) {
  const source = mermaidSource(children)
  return source !== null ? <Diagram source={source} /> : <pre>{children}</pre>
}
