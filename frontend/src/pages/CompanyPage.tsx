import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { ArrowRight, ExternalLink, Info } from 'lucide-react'
import { api } from '../api'
import { fmtDate, MdLink, Status, useLoad } from '../ui'

type Tab = 'strategy' | 'loop' | 'stack' | 'postings' | 'sources'
const TABS: [Tab, string][] = [
  ['strategy', 'Strategy'],
  ['loop', 'Interview loop'],
  ['stack', 'Tech stack'],
  ['postings', 'Job postings'],
  ['sources', 'Sources'],
]

/* The company.yaml shape is documented in docs/COMPANY-PREP.md §1; every field is optional here. */
/* eslint-disable @typescript-eslint/no-explicit-any */

export default function CompanyPage() {
  const { slug = '' } = useParams()
  const { data: c, error } = useLoad(() => api.company(slug), [slug])
  const [tab, setTab] = useState<Tab>('strategy')

  if (!c) return <div className="container"><Status error={error} /></div>
  const d = c.data
  const official = d.official ?? {}

  return (
    <div className="container narrow">
      <header className="page-head">
        <div className="row" style={{ gap: 16, alignItems: 'center' }}>
          <span className="avatar" style={{ width: 52, height: 52, fontSize: '1.3rem', borderRadius: 14 }}>
            {c.name[0]}
          </span>
          <div>
            <h1>{c.name}</h1>
            <p className="sub" style={{ marginTop: 2 }}>
              Researched {c.researchedOn ? fmtDate(c.researchedOn, { day: 'numeric', month: 'long', year: 'numeric' }) : '—'}
            </p>
          </div>
        </div>
        <Link to={`/bank?company=${c.slug}`} className="btn">
          Questions asked here <ArrowRight size={15} />
        </Link>
      </header>

      <div className="tabs" role="tablist">
        {TABS.map(([t, label]) => (
          <button key={t} role="tab" aria-selected={tab === t} onClick={() => setTab(t)}>
            {label}
          </button>
        ))}
      </div>

      {tab === 'strategy' && (
        <div className="prose">
          <Markdown remarkPlugins={[remarkGfm]} components={{ a: MdLink }}>
            {(c.dossier || '_No dossier yet._').replace(/^# .*\n/, '')}
          </Markdown>
        </div>
      )}

      {tab === 'loop' && <Loop name={c.name} d={d} official={official} />}
      {tab === 'stack' && <TechStack d={d} />}
      {tab === 'postings' && <Postings official={official} />}

      {tab === 'sources' && (
        <div className="card">
          {(d.sources ?? []).map((s: any, i: number) => (
            <div key={i} className="list-row">
              <div className="grow">
                <a href={s.url} target="_blank" rel="noreferrer" style={{ fontWeight: 550 }}>
                  {s.title ?? s.url} <ExternalLink size={12} style={{ verticalAlign: -1 }} />
                </a>
                <div className="muted" style={{ fontSize: '0.8rem' }}>
                  {[s.kind, s.accessed].filter(Boolean).join(' · ')}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function Loop({ name, d, official }: { name: string; d: any; official: any }) {
  return (
    <div className="stack" style={{ gap: 32 }}>
      {(official.hiring_process ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>The process, as {name} describes it</h2>
          </div>
          <div className="card rounds">
            {official.hiring_process.map((s: any, i: number) => (
              <div key={i} className="list-row">
                <span className="round-no">{i + 1}</span>
                <div className="grow">
                  <div style={{ fontWeight: 600 }}>{s.step}</div>
                  <div className="dim" style={{ fontSize: '0.9rem' }}>
                    {s.detail}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {(d.roles ?? []).map((r: any, i: number) => (
        <section key={i}>
          <div className="section-head">
            <h2>
              What candidates report: {r.role} {r.level && <span className="muted">({r.level})</span>}
            </h2>
          </div>
          {r.bar && <p className="dim" style={{ marginBottom: 12, fontSize: '0.92rem' }}>{r.bar}</p>}
          <div className="card rounds">
            {(r.loop ?? []).map((x: any, j: number) => (
              <div key={j} className="list-row">
                <span className="round-no">{j + 1}</span>
                <div className="grow">
                  <div className="row" style={{ gap: 8 }}>
                    <span style={{ fontWeight: 600 }}>{x.round}</span>
                    {x.type && <span className="pill">{x.type}</span>}
                    {x.duration_min && <span className="muted" style={{ fontSize: '0.84rem' }}>{x.duration_min} min</span>}
                  </div>
                  <div className="dim" style={{ fontSize: '0.9rem', marginTop: 2 }}>
                    {x.assesses}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      ))}

      {(official.conflicts_with_reports ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>Where reports disagree with {name}</h2>
          </div>
          <div className="stack">
            {official.conflicts_with_reports.map((t: any, i: number) => (
              <div key={i} className="callout warn">
                <Info size={18} />
                <span>{asText(t)}</span>
              </div>
            ))}
          </div>
        </section>
      )}

      {(d.values ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>Values — prepare one story for each</h2>
          </div>
          <div className="row" style={{ gap: 8 }}>
            {d.values.map((v: any, i: number) => (
              <span key={i} className="pill accent" style={{ height: 28, padding: '0 12px', fontSize: '0.84rem' }}>
                {typeof v === 'string' ? v : v.name}
              </span>
            ))}
          </div>
        </section>
      )}

      {(official.interview_tips ?? []).length > 0 && (
        <section>
          <div className="section-head">
            <h2>{name}'s own interview tips</h2>
          </div>
          <ul className="followups dim">
            {official.interview_tips.map((t: any, i: number) => (
              <li key={i}>{asText(t)}</li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}

const GROUPS: { title: string; match: (category: string) => boolean }[] = [
  { title: 'Patterns', match: (c) => c.endsWith('-pattern') },
  { title: 'Tools', match: (c) => c === 'tool' },
  { title: 'Frameworks & languages', match: () => true },
]
const IMPORTANCE_ORDER: Record<string, number> = { core: 0, common: 1, mentioned: 2 }

function TechStack({ d }: { d: any }) {
  const stack: any[] = d.tech_stack ?? []
  const prep = d.frameworks_to_prepare ?? {}
  if (!stack.length && !prep.revise && !prep.learn) {
    return (
      <p className="muted">
        No tech stack researched yet. Re-run <code>/prep-company</code> for this company to add it.
      </p>
    )
  }
  // Each item lands in the first group whose rule matches; frameworks & languages is the catch-all.
  const grouped = GROUPS.map((g) => ({ title: g.title, items: [] as any[] }))
  for (const item of stack) {
    const i = GROUPS.findIndex((g) => g.match(String(item.category ?? '')))
    grouped[i].items.push(item)
  }
  const ordered = [grouped[2], grouped[0], grouped[1]].filter((g) => g.items.length)

  return (
    <div className="stack" style={{ gap: 32 }}>
      {(prep.learn?.length > 0 || prep.revise?.length > 0) && (
        <div className="prep-grid">
          <PrepList title="Learn" hint="New or weak for you, but this role uses it" items={prep.learn ?? []} tone="learn" />
          <PrepList title="Revise" hint="You know it — interviewers will go deep" items={prep.revise ?? []} tone="revise" />
        </div>
      )}
      {ordered.map((g) => (
        <section key={g.title}>
          <div className="section-head">
            <h2>{g.title}</h2>
            <span className="muted" style={{ fontSize: '0.84rem' }}>
              {g.items.length}
            </span>
          </div>
          <div className="card">
            {[...g.items]
              .sort((a, b) => (IMPORTANCE_ORDER[a.importance] ?? 3) - (IMPORTANCE_ORDER[b.importance] ?? 3))
              .map((t, i) => (
                <div key={i} className="list-row" style={{ alignItems: 'flex-start' }}>
                  <div className="grow">
                    <div className="row" style={{ gap: 8 }}>
                      <span style={{ fontWeight: 600 }}>{t.name}</span>
                      <span className={`pill imp-${t.importance}`}>{t.importance}</span>
                      <span className="muted" style={{ fontSize: '0.8rem' }}>
                        {String(t.category ?? '').replace(/-/g, ' ')}
                      </span>
                    </div>
                    {t.evidence && (
                      <div className="dim" style={{ fontSize: '0.86rem', marginTop: 2 }}>
                        {t.evidence}
                      </div>
                    )}
                  </div>
                  <SourceLinks sources={t.sources} />
                </div>
              ))}
          </div>
        </section>
      ))}
    </div>
  )
}

function PrepList({ title, hint, items, tone }: { title: string; hint: string; items: any[]; tone: string }) {
  return (
    <section className={`card pad prep-${tone}`}>
      <div className="section-head" style={{ marginBottom: 4 }}>
        <h2>{title}</h2>
        <span className="muted" style={{ fontSize: '0.84rem' }}>
          {items.length}
        </span>
      </div>
      <p className="muted" style={{ fontSize: '0.84rem', marginBottom: 12 }}>
        {hint}
      </p>
      {items.length === 0 ? (
        <p className="muted">Nothing here.</p>
      ) : (
        <ol className="prep-items">
          {items.map((x, i) => (
            <li key={i}>
              <div style={{ fontWeight: 600 }}>{x.name}</div>
              {x.why && <div className="dim" style={{ fontSize: '0.86rem' }}>{x.why}</div>}
              {x.plan && <div style={{ fontSize: '0.86rem', marginTop: 2 }}>→ {x.plan}</div>}
              <div className="row" style={{ gap: 6, marginTop: 6 }}>
                {(x.academy ?? []).map((a: string) => (
                  <Link key={a} to={`/academy/${a}`} className="pill accent">
                    {a}
                  </Link>
                ))}
                {(x.resources ?? []).map((r: string, j: number) => (
                  <a key={j} href={r} target="_blank" rel="noreferrer" className="pill">
                    docs <ExternalLink size={10} />
                  </a>
                ))}
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}

function SourceLinks({ sources }: { sources: any }) {
  const list: string[] = Array.isArray(sources) ? sources : sources ? [sources] : []
  return (
    <span className="row" style={{ gap: 6, flexShrink: 0 }}>
      {list.slice(0, 3).map((s, i) =>
        /^https?:/.test(s) ? (
          <a key={i} href={s} target="_blank" rel="noreferrer" title={s} className="muted">
            <ExternalLink size={13} />
          </a>
        ) : (
          <span key={i} className="muted" title={s} style={{ fontSize: '0.78rem' }}>
            {s.length > 24 ? s.slice(0, 24) + '…' : s}
          </span>
        ),
      )}
    </span>
  )
}

function Postings({ official }: { official: any }) {
  const freq: [string, number][] = Object.entries(official.skill_frequency ?? {})
    .map(([k, v]) => [k, Number(v)] as [string, number])
    .sort((a, b) => b[1] - a[1])
  const postings: any[] = official.job_postings ?? []
  const max = freq[0]?.[1] ?? 1

  return (
    <div className="stack" style={{ gap: 32 }}>
      {freq.length > 0 && (
        <section>
          <div className="section-head">
            <h2>Most-requested skills</h2>
            <span className="muted" style={{ fontSize: '0.84rem' }}>
              across {postings.length} current postings
            </span>
          </div>
          <div className="card pad skills">
            {freq.slice(0, 16).map(([k, v]) => (
              <div className="skill" key={k}>
                <span className="dim">{k.replace(/-/g, ' ')}</span>
                <div className="bar">
                  <span style={{ width: `${(v / max) * 100}%` }} />
                </div>
                <span className="num muted" style={{ textAlign: 'right' }}>
                  {v}
                </span>
              </div>
            ))}
          </div>
        </section>
      )}
      <section>
        <div className="section-head">
          <h2>Postings</h2>
        </div>
        <div className="stack">
          {postings.map((p, i) => (
            <details key={i} className="card posting">
              <summary>
                <span style={{ fontWeight: 600 }}>{p.title}</span>
                <span className="muted" style={{ fontSize: '0.86rem' }}>
                  {[p.team, p.location].filter(Boolean).join(' · ')}
                </span>
              </summary>
              <div className="body">
                {p.required?.length > 0 && (
                  <>
                    <div className="eyebrow">Required</div>
                    <ul>{p.required.map((r: string, j: number) => <li key={j}>{r}</li>)}</ul>
                  </>
                )}
                {p.preferred?.length > 0 && (
                  <>
                    <div className="eyebrow">Preferred</div>
                    <ul>{p.preferred.map((r: string, j: number) => <li key={j}>{r}</li>)}</ul>
                  </>
                )}
                {p.url && (
                  <a href={p.url} target="_blank" rel="noreferrer">
                    Open posting <ExternalLink size={12} style={{ verticalAlign: -1 }} />
                  </a>
                )}
              </div>
            </details>
          ))}
        </div>
      </section>
    </div>
  )
}

/** YAML list entries are sometimes plain strings, sometimes {name|tip|text|..., source}. */
function asText(v: any): string {
  if (typeof v === 'string') return v
  const main = v.name ?? v.tip ?? v.text ?? v.claim ?? v.official ?? v.point
  if (main) return [main, v.detail ?? v.reports].filter(Boolean).join(' — ')
  return Object.entries(v)
    .filter(([k]) => k !== 'source')
    .map(([k, x]) => `${k}: ${x}`)
    .join(' · ')
}
