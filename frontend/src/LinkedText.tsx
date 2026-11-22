import { Fragment } from 'react'
import { Link } from 'react-router-dom'

/** [[Concept]] in note text (the same rule as notes.ConceptIndex on the server: no brackets or line breaks inside). */
const LINK = /\[\[([^[\]\n]{1,100})\]\]/g

export const conceptHref = (name: string) => `/concepts?name=${encodeURIComponent(name.trim().replace(/\s+/g, ' '))}`

/** The text with [[Concept]] shown as the bare name, for places that are already a link. */
export const unlinked = (text: string) => text.replace(LINK, (_, name: string) => name.trim())

/** Note text with every [[Concept]] shown as a link to its concept page. */
export function LinkedText({ text }: { text: string }) {
  const parts: (string | { name: string })[] = []
  let last = 0
  for (const m of text.matchAll(LINK)) {
    const name = m[1].trim()
    if (!name) continue
    parts.push(text.slice(last, m.index), { name })
    last = m.index + m[0].length
  }
  parts.push(text.slice(last))
  return (
    <>
      {parts.map((p, i) =>
        typeof p === 'string' ? (
          <Fragment key={i}>{p}</Fragment>
        ) : (
          <Link key={i} to={conceptHref(p.name)} className="concept-link">
            {p.name}
          </Link>
        ),
      )}
    </>
  )
}
