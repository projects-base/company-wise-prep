import { isValidElement, useMemo, type ReactNode } from 'react'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { MdPre } from './Diagram'
import { MdLink } from './ui'

/**
 * A model answer, laid out for reading: the short answer as a highlighted summary (split into
 * bullets when it is long), jump links to the sections, and tables that scroll on their own.
 */

const slugify = (s: string) =>
  'ans-' +
  s
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')

function textOf(node: ReactNode): string {
  if (typeof node === 'string' || typeof node === 'number') return String(node)
  if (Array.isArray(node)) return node.map(textOf).join('')
  if (isValidElement<{ children?: ReactNode }>(node)) return textOf(node.props.children)
  return ''
}

/** Splits off a leading "**Short answer:** ..." paragraph; long ones become a lead line plus bullets. */
function splitSummary(text: string): { summary: string | null; rest: string } {
  const m = text.match(/^\*\*Short answer:\*\*\s*([\s\S]*?)(?:\n\s*\n|$)/)
  if (!m) return { summary: null, rest: text }
  const body = m[1].replace(/\s*\n\s*/g, ' ').trim()
  const rest = text.slice(m[0].length)
  if (body.length < 320) return { summary: body, rest }
  // Sentence ends: ". " (or ".) ") followed by a capital or a bold term. Skips "e.g. x" and "1.3×".
  const sentences = body.split(/(?<=[.!?])\s+(?=\*\*|[A-Z`(])/).filter(Boolean)
  if (sentences.length < 3) return { summary: body, rest }
  return { summary: `${sentences[0]}\n\n${sentences.slice(1).map((s) => `- ${s}`).join('\n')}`, rest }
}

/** `## Heading` lines outside code fences. */
function headingsOf(text: string): string[] {
  const out: string[] = []
  let fenced = false
  for (const line of text.split('\n')) {
    if (/^\s*```/.test(line)) fenced = !fenced
    else if (!fenced) {
      const h = line.match(/^##\s+(.+?)\s*$/)
      if (h) out.push(h[1].replace(/[*_`]/g, ''))
    }
  }
  return out
}

const components = {
  a: MdLink,
  pre: MdPre,
  h2: ({ children }: { children?: ReactNode }) => <h2 id={slugify(textOf(children))}>{children}</h2>,
  table: ({ children }: { children?: ReactNode }) => (
    <div className="table-wrap">
      <table>{children}</table>
    </div>
  ),
}

export function AnswerProse({ text }: { text: string }) {
  const { summary, rest } = useMemo(() => splitSummary(text.trim()), [text])
  const headings = useMemo(() => headingsOf(rest), [rest])
  const jump = (h: string) => document.getElementById(slugify(h))?.scrollIntoView({ behavior: 'smooth', block: 'start' })

  return (
    <>
      {summary && (
        <div className="tldr">
          <div className="tldr-label">Short answer</div>
          <Markdown remarkPlugins={[remarkGfm]} components={components}>
            {summary}
          </Markdown>
        </div>
      )}
      {headings.length >= 3 && (
        <nav className="answer-toc" aria-label="Sections">
          <span className="muted">Jump to</span>
          {headings.map((h) => (
            <button key={h} className="chip-btn" onClick={() => jump(h)}>
              {h}
            </button>
          ))}
        </nav>
      )}
      <Markdown remarkPlugins={[remarkGfm]} components={components}>
        {rest}
      </Markdown>
    </>
  )
}
