import type { ReactNode } from 'react'

/**
 * A mermaid sequenceDiagram read as a numbered list of steps ("1. Client → Write API: POST /links")
 * instead of lifelines. Sequence diagrams are hard to follow once they have many participants;
 * the same content as a list reads top to bottom like a story.
 */

type Kind = 'call' | 'reply' | 'async' | 'lost'
type Row =
  | { t: 'msg'; n: number; from: string; to: string; text: string; kind: Kind; depth: number }
  | { t: 'note'; text: string; who: string; depth: number }
  | { t: 'group'; label: string; depth: number }

const ARROW = /^(.+?)\s*(-->>|->>|--\)|-\)|--x|-x|-->|->)\s*[+-]?\s*([^:]+?)\s*:\s*(.*)$/
const GROUP = /^(alt|else|opt|loop|par|and|critical|option|break)\b\s*(.*)$/
const GROUP_LABEL: Record<string, string> = {
  alt: 'If',
  else: 'Otherwise',
  opt: 'Only if',
  loop: 'Repeat',
  par: 'At the same time',
  and: 'and',
  critical: 'Critical',
  option: 'Or',
  break: 'Stop if',
}

/** Parses a sequence diagram; null when it isn't one (or uses syntax this view doesn't cover). */
export function parseSequence(source: string): { rows: Row[]; names: Map<string, string> } | null {
  const lines = source.split('\n').map((l) => l.trim())
  if (lines[0] !== 'sequenceDiagram') return null
  const names = new Map<string, string>()
  const rows: Row[] = []
  const clean = (s: string) => s.replace(/<br\s*\/?>/gi, ' ').replace(/#59;/g, ';').trim()
  const name = (id: string) => names.get(id) ?? id
  let depth = 0
  let n = 0
  for (const line of lines.slice(1)) {
    if (!line || line.startsWith('%%') || /^(autonumber|activate|deactivate|rect|box)\b/.test(line)) {
      if (/^(rect|box)\b/.test(line)) depth++ // closed by a matching `end`, which pops it below
      continue
    }
    const p = line.match(/^(?:participant|actor)\s+(\S+)(?:\s+as\s+(.+))?$/)
    if (p) {
      names.set(p[1], clean(p[2] ?? p[1]))
      continue
    }
    if (line === 'end') {
      depth = Math.max(0, depth - 1)
      continue
    }
    const g = line.match(GROUP)
    if (g) {
      const label = `${GROUP_LABEL[g[1]]}${g[2] ? ': ' + clean(g[2]) : ''}`
      if (g[1] === 'else' || g[1] === 'and' || g[1] === 'option') rows.push({ t: 'group', label, depth: depth - 1 })
      else rows.push({ t: 'group', label, depth: depth++ })
      continue
    }
    const note = line.match(/^note\s+(?:over|left of|right of)\s+([^:]+):\s*(.*)$/i)
    if (note) {
      rows.push({ t: 'note', who: note[1].split(',').map((s) => name(s.trim())).join(', '), text: clean(note[2]), depth })
      continue
    }
    const m = line.match(ARROW)
    if (!m) return null
    const kind: Kind = m[2].startsWith('--') && m[2].endsWith('>>') ? 'reply' : m[2].includes(')') ? 'async' : m[2].includes('x') ? 'lost' : 'call'
    rows.push({ t: 'msg', n: ++n, from: m[1].trim(), to: m[3].trim(), text: clean(m[4]), kind, depth })
  }
  if (n === 0) return null
  for (const r of rows) if (r.t === 'msg') (r.from = name(r.from)), (r.to = name(r.to))
  return { rows, names }
}

const KIND_TAG: Record<Kind, string | null> = { call: null, reply: 'reply', async: 'async, no wait', lost: 'fails' }

export function FlowSteps({ rows }: { rows: Row[] }) {
  return (
    <ol className="flow">
      {rows.map((r, i): ReactNode => {
        const indent = { paddingLeft: `${r.depth * 22}px` }
        if (r.t === 'group')
          return (
            <li key={i} className="flow-group" style={indent}>
              {r.label}
            </li>
          )
        if (r.t === 'note')
          return (
            <li key={i} className="flow-note" style={indent}>
              <span className="flow-note-who">{r.who}</span> {r.text}
            </li>
          )
        const self = r.from === r.to
        return (
          <li key={i} className={`flow-step flow-${r.kind}`} style={indent}>
            <span className="flow-n num">{r.n}</span>
            <div className="flow-body">
              <div className="flow-who">
                <span className="flow-actor">{r.from}</span>
                {self ? (
                  <span className="flow-arrow">itself</span>
                ) : (
                  <>
                    <span className="flow-arrow">{r.kind === 'reply' ? '⟵ back to' : '→'}</span>
                    <span className="flow-actor">{r.to}</span>
                  </>
                )}
                {KIND_TAG[r.kind] && <span className="flow-tag">{KIND_TAG[r.kind]}</span>}
              </div>
              <div className="flow-what">{r.text}</div>
            </div>
          </li>
        )
      })}
    </ol>
  )
}
