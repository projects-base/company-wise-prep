import type { Note } from './api'

/**
 * Marking saved highlights in a rendered lesson. Pure functions over react-markdown's hast tree, kept apart from the
 * UI so the matching rules live in one place.
 */

/** How long the prefix context saved with a highlight is (characters, whitespace removed). */
export const PREFIX_LENGTH = 30

/**
 * One form for a section heading, wherever it comes from: the raw "## " title the Stepper shows (Markdown links,
 * `code`, *emphasis* still in it) or the rendered h2 text. Links become their text, inline markers go, and whitespace
 * collapses, so a highlight saved in either reading mode matches in the other.
 */
export function normaliseSection(s: string | null | undefined): string | null {
  if (!s) return null
  const t = s
    .replace(/!?\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[*_`~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return t || null
}

/** Text with all whitespace removed: how quotes and prefixes are compared. */
export const compact = (s: string) => s.replace(/\s+/g, '')

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
 * Where a quote sits in whitespace-free text: the occurrence whose preceding text ends with the saved prefix, else
 * the first one. -1 when the quote is not there.
 */
export function findQuote(flat: string, needle: string, prefix: string | null): number {
  let first = -1
  for (let at = flat.indexOf(needle); at >= 0; at = flat.indexOf(needle, at + 1)) {
    if (first < 0) first = at
    if (!prefix) break
    if (flat.slice(Math.max(0, at - prefix.length), at) === prefix) return at
  }
  return first
}

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
    let section = normaliseSection(startSection)
    const walk = (parent: HParent) => {
      for (const child of parent.children) {
        if (isText(child)) segs.push({ node: child, parent, section })
        else if (isElement(child) && child.tagName === 'h2') section = normaliseSection(hastText(child))
        else if (isElement(child) && child.tagName === 'pre') continue
        else if (hasChildren(child)) walk(child)
      }
    }
    walk(tree)

    const ranges = new Map<HText, Range[]>()
    highlights.forEach((h, i) => {
      const needle = compact(h.quote ?? '')
      if (!needle) return
      const wanted = normaliseSection(h.section)
      const pool = segs.filter((s) => !wanted || s.section === wanted)
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
      const at = findQuote(flat, needle, h.prefix ? compact(h.prefix) : null)
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
