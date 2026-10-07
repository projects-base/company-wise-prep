import { Link } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'
import { api } from '../api'
import { fmtDate, Status, useLoad } from '../ui'

export default function CompaniesPage() {
  const { data, error } = useLoad(() => api.companies(), [])
  if (!data) return <div className="container"><Status error={error} /></div>

  return (
    <div className="container">
      <header className="page-head">
        <div>
          <div className="eyebrow">Companies</div>
          <h1 style={{ marginTop: 6 }}>Researched companies</h1>
          <p className="sub">Interview loop, what the job postings ask for, and the strategy for each.</p>
        </div>
      </header>
      <div className="co-grid">
        {data.map((c) => (
          <Link key={c.slug} to={`/companies/${c.slug}`} className="card co">
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <span className="avatar">{c.name[0]}</span>
              <ArrowRight size={16} className="muted" />
            </div>
            <div>
              <div className="name">{c.name}</div>
              <div className="muted" style={{ fontSize: '0.86rem', marginTop: 2 }}>
                {c.questions} reported questions
              </div>
            </div>
            <div className="muted" style={{ fontSize: '0.8rem' }}>
              Researched {c.researchedOn ? fmtDate(c.researchedOn, { day: 'numeric', month: 'short', year: 'numeric' }) : '—'}
            </div>
          </Link>
        ))}
      </div>
      <p className="hint" style={{ marginTop: 20 }}>
        Add a company by running <code>/prep-company &lt;name&gt;</code> in Claude Code inside this repo, then restart the app.
      </p>
    </div>
  )
}
