import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Code2, Search, Star } from 'lucide-react'
import { api } from '../api'
import { CheckDot, companyLabel, CompanyPills, Difficulty, Status, TypeTag, useLoad } from '../ui'

const TYPES: [string, string][] = [
  ['DSA', 'DSA'],
  ['LLD', 'LLD'],
  ['HLD', 'System design'],
  ['DOMAIN', 'Systems'],
  ['JAVA', 'Java'],
  ['SPRING', 'Spring'],
  ['SQL', 'SQL'],
  ['BEHAVIORAL', 'Behavioural'],
]

export default function BankPage() {
  const { data, error } = useLoad(() => api.questions(), [])
  const [params, setParams] = useSearchParams()
  const [text, setText] = useState('')
  const searchRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === '/' && document.activeElement?.tagName !== 'INPUT' && document.activeElement?.tagName !== 'TEXTAREA') {
        e.preventDefault()
        searchRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  const company = params.get('company') ?? ''
  const type = params.get('type') ?? ''
  const difficulty = params.get('difficulty') ?? ''
  const status = params.get('status') ?? ''
  const shared = params.get('shared') === '1'

  const set = (key: string, value: string) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    setParams(next, { replace: true })
  }

  const companies = useMemo(() => [...new Set((data ?? []).flatMap((q) => q.companies))].sort(), [data])
  const rows = useMemo(() => {
    const needle = text.trim().toLowerCase()
    return (data ?? []).filter(
      (q) =>
        (!company || q.companies.includes(company)) &&
        (!type || q.type === type) &&
        (!difficulty || q.difficulty === difficulty) &&
        (!shared || q.companies.length > 1) &&
        (status !== 'todo' || !q.done) &&
        (status !== 'done' || q.done) &&
        (status !== 'starred' || q.starred) &&
        (status !== 'runnable' || q.runnable) &&
        (!needle || q.title.toLowerCase().includes(needle) || q.tags.some((t) => t.toLowerCase().includes(needle))),
    )
  }, [data, text, company, type, difficulty, status, shared])

  if (!data) return <div className="container"><Status error={error} /></div>
  const filtered = company || type || difficulty || status || shared || text

  return (
    <div className="container">
      <header className="page-head">
        <div>
          <div className="eyebrow">Question bank</div>
          <h1 style={{ marginTop: 6 }}>{data.length} questions, researched company by company</h1>
          <p className="sub">Each question is stored once; every company that asked it is listed against it.</p>
        </div>
      </header>

      <div className="toolbar">
        <label className="search">
          <Search size={16} />
          <span className="sr-only">Search</span>
          <input ref={searchRef} placeholder="Search titles and tags" value={text} onChange={(e) => setText(e.target.value)} />
          {!text && <kbd>/</kbd>}
        </label>
        <select value={company} onChange={(e) => set('company', e.target.value)} aria-label="Company">
          <option value="">All companies</option>
          {companies.map((c) => (
            <option key={c} value={c}>
              {companyLabel(c)}
            </option>
          ))}
        </select>
        <select value={type} onChange={(e) => set('type', e.target.value)} aria-label="Type">
          <option value="">All types</option>
          {TYPES.map(([v, l]) => (
            <option key={v} value={v}>
              {l}
            </option>
          ))}
        </select>
        <select value={difficulty} onChange={(e) => set('difficulty', e.target.value)} aria-label="Difficulty">
          <option value="">Any difficulty</option>
          <option value="easy">Easy</option>
          <option value="medium">Medium</option>
          <option value="hard">Hard</option>
        </select>
        <select value={status} onChange={(e) => set('status', e.target.value)} aria-label="Status">
          <option value="">Any status</option>
          <option value="todo">Not done</option>
          <option value="done">Done</option>
          <option value="starred">Starred</option>
          <option value="runnable">Runnable in editor</option>
        </select>
        <label className="toggle">
          <input type="checkbox" checked={shared} onChange={(e) => set('shared', e.target.checked ? '1' : '')} />
          Asked at 2+ companies
        </label>
      </div>
      <div className="row" style={{ justifyContent: 'space-between', marginBottom: 10 }}>
        <span className="muted" style={{ fontSize: '0.86rem' }}>
          {rows.length} {rows.length === 1 ? 'question' : 'questions'}
          {filtered ? ' match' : ''}
        </span>
        {filtered && (
          <button
            className="btn sm ghost"
            onClick={() => {
              setText('')
              setParams({}, { replace: true })
            }}
          >
            Clear filters
          </button>
        )}
      </div>

      <div className="card table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th style={{ width: 36 }} aria-label="Done" />
              <th>Question</th>
              <th>Type</th>
              <th className="hide-sm">Level</th>
              <th className="hide-sm">Asked at</th>
              <th className="hide-sm" style={{ textAlign: 'right' }}>
                Reports
              </th>
            </tr>
          </thead>
          <tbody>
            {rows.map((q) => (
              <tr key={q.slug} className={q.done ? 'done' : ''}>
                <td>
                  <CheckDot on={q.done} />
                </td>
                <td>
                  <Link to={`/q/${q.slug}`} className="q">
                    {q.title}
                  </Link>
                  {q.runnable && (
                    <span className="code-mark" title="Has an online editor with tests">
                      <Code2 size={14} />
                    </span>
                  )}
                  {q.starred && <Star size={13} style={{ marginLeft: 6, verticalAlign: -1, color: 'var(--warn)' }} fill="currentColor" />}
                </td>
                <td>
                  <TypeTag type={q.type} />
                </td>
                <td className="hide-sm">
                  <Difficulty value={q.difficulty} />
                </td>
                <td className="hide-sm">
                  <CompanyPills slugs={q.companies} />
                </td>
                <td className="hide-sm num muted" style={{ textAlign: 'right' }}>
                  {q.sightings}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rows.length === 0 && (
          <div className="empty">
            <Search size={22} />
            <strong>No questions match</strong>
            <span>Try removing a filter.</span>
          </div>
        )}
      </div>
    </div>
  )
}
