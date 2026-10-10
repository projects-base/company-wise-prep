import { Children, isValidElement, useEffect, useId, useState, type ReactNode } from 'react'

type MermaidApi = typeof import('mermaid').default

// Mermaid is large, so it loads only when a page actually has a diagram.
let mermaid: Promise<MermaidApi> | null = null
let theme: 'dark' | 'default' | null = null

function load(dark: boolean): Promise<MermaidApi> {
  mermaid ??= import('mermaid').then((m) => m.default)
  return mermaid.then((api) => {
    const wanted = dark ? 'dark' : 'default'
    if (theme !== wanted) {
      api.initialize({ startOnLoad: false, theme: wanted, securityLevel: 'strict', fontFamily: 'inherit' })
      theme = wanted
    }
    return api
  })
}

/** Renders a ```mermaid block from an answer or lesson; shows the source if it fails to parse. */
export function Diagram({ source }: { source: string }) {
  const id = 'd' + useId().replace(/[^a-zA-Z0-9]/g, '')
  const [svg, setSvg] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

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

  if (failed) return <pre className="diagram-fallback">{source}</pre>
  if (!svg) return <div className="diagram loading muted">Drawing diagram…</div>
  return <div className="diagram" dangerouslySetInnerHTML={{ __html: svg }} />
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
