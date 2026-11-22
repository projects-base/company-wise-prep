import { Link, useSearchParams } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { api, type ConceptDetail, type ConceptItem, type ConceptOverview } from '../api'
import { conceptHref, unlinked } from '../LinkedText'
import { Status, useLoad } from '../ui'

/**
 * Linked concepts: every [[Concept]] written in a note, strongest first, and the Reference glossary terms of finished
 * lessons that no note links yet (gaps). With ?name= it shows one concept: where it shows up, and related concepts.
 */

const ITEM_LABEL: Record<ConceptItem['kind'], string> = {
  HIGHLIGHT: 'Your highlight note',
  SNIPPET: 'Your Playground note',
  SUMMARY: 'Your summary',
  EXPLANATION: 'Your explanation',
}
const GAPS_SHOWN = 12

export default function ConceptsPage() {
  const [params] = useSearchParams()
  const name = params.get('name')?.trim() || null
  const overview = useLoad(() => api.concepts(), [])
  return (
    <div className="container narrow">
      <nav className="crumbs" aria-label="Breadcrumb">
        <Link to="/notes">My notes</Link>
        <ChevronRight size={14} />
        {name ? <Link to="/concepts">Concepts</Link> : <span>Concepts</span>}
      </nav>
      {name ? <Detail name={name} overview={overview.data} /> : <List overview={overview.data} error={overview.error} />}
    </div>
  )
}

function List({ overview, error }: { overview: ConceptOverview | null; error: string | null }) {
  if (!overview) return <Status error={error} />
  const max = Math.max(1, ...overview.concepts.map((c) => c.count))
  return (
    <>
      <header className="page-head">
        <div>
          <h1>Concepts</h1>
          <p className="sub">
            Type <code>[[Concept]]</code> in any note, summary or explanation to link it. The most linked concepts are your
            strongest interview topics; terms from lessons you have finished with no link are gaps.
          </p>
        </div>
      </header>
      <div className="concepts-page">
        <section>
          <h2 className="eyebrow">Your concepts</h2>
          {overview.concepts.length === 0 ? (
            <p className="muted">No links yet. Write <code>[[HashMap]]</code> in a highlight note to start.</p>
          ) : (
            <ul className="concept-list">
              {overview.concepts.map((c) => (
                <li key={c.name}>
                  <Link to={conceptHref(c.name)}>{c.name}</Link>
                  <div className="bar"><span style={{ width: `${(c.count / max) * 100}%` }} /></div>
                  <strong className="num">{c.count}</strong>
                </li>
              ))}
            </ul>
          )}
        </section>
        <aside aria-label="Gaps">
          <h2 className="eyebrow">Gaps · {overview.gaps.length}</h2>
          {overview.gaps.length === 0 ? (
            <p className="muted">
              None. Gaps are glossary terms (lesson R1) taught in lessons you have marked complete that no note links yet.
            </p>
          ) : (
            <ul className="concept-gaps">
              {overview.gaps.map((g) => (
                <li key={g.term}>
                  <Link to={conceptHref(g.term)}>{g.term}</Link>
                  <Link to={`/academy/${g.moduleId}`} className="muted num">{g.moduleCode}</Link>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </>
  )
}

function Detail({ name, overview }: { name: string; overview: ConceptOverview | null }) {
  const { data, error } = useLoad(() => api.concept(name), [name])
  if (!data) return <Status error={error} />
  return (
    <>
      <header className="concept-head">
        <h1>{data.name}</h1>
        <span className="muted">
          Linked from {data.count} place{data.count === 1 ? '' : 's'} · type <code>[[{data.name}]]</code> in any note
        </span>
      </header>
      <div className="concepts-page">
        <WhereItShowsUp d={data} />
        <aside className="concept-side" aria-label="Related concepts">
          <div className="eyebrow">Related concepts</div>
          {data.related.length ? (
            <div className="concept-chips">
              {data.related.map((r) => (
                <Link key={r.name} to={conceptHref(r.name)} className="chip">{r.name}</Link>
              ))}
            </div>
          ) : (
            <p className="muted">None yet: link another concept in the same note to relate them.</p>
          )}
          <hr />
          <p className="muted">
            Concepts with the most links become your strongest interview topics. Concepts with none are gaps.
          </p>
          {overview && (
            <ul className="concept-ranks">
              {overview.concepts.slice(0, 8).map((c) => (
                <li key={c.name}>
                  <Link to={conceptHref(c.name)}>{c.name}</Link>
                  <strong className="num">{c.count}</strong>
                </li>
              ))}
              {overview.gaps.slice(0, GAPS_SHOWN).map((g) => (
                <li key={g.term}>
                  <Link to={conceptHref(g.term)}>{g.term}</Link>
                  <strong className="num warn-text">0 · gap</strong>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </>
  )
}

function WhereItShowsUp({ d }: { d: ConceptDetail }) {
  const empty = !d.items.length && !d.lessons.length && !d.mistakes.length
  return (
    <section className="concept-where">
      <h2 className="eyebrow">Where it shows up</h2>
      {empty && <p className="muted">Nothing yet: no note links it, and no lesson or mistake names it.</p>}
      {d.items.map((i) => (
        <Link key={`${i.kind}-${i.id}`} to={i.link} className="card concept-hit">
          <div className="concept-hit-title">{i.moduleCode ? `${i.moduleCode} · ${i.moduleTitle}` : 'General notes'}</div>
          <div className="muted">{ITEM_LABEL[i.kind]} · {unlinked(i.text)}</div>
        </Link>
      ))}
      {d.lessons.map((l) => (
        <Link key={l.moduleId} to={`/academy/${l.moduleId}`} className="card concept-hit">
          <div className="concept-hit-title">{l.code} · {l.title}</div>
          <div className="muted">Lesson · {l.excerpt}</div>
        </Link>
      ))}
      {d.mistakes.map((m) => (
        <Link key={m.id} to={`/academy/${m.moduleId}`} state={{ quiz: m.question }} className="card concept-hit">
          <div className="concept-hit-title">Mistake · "{m.question}"</div>
          <div className="muted">
            {m.moduleCode} · wrong {m.wrongCount === 1 ? 'once' : `${m.wrongCount} times`}
            {' · '}{m.fixed ? 'fixed on retry' : 'still open'}
          </div>
        </Link>
      ))}
    </section>
  )
}
