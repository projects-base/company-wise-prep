import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, Check, ExternalLink, Info, RotateCcw, Sparkles } from 'lucide-react'
import { api, leetcodeUrl, type PlanItem } from '../api'
import { useCampaign } from '../App'
import { CompanyPills, Difficulty, fmtDate, LevelChip, Meta, Ring, Status, TypeTag, useLoad } from '../ui'

const LADDER = ['3-day', '10-day', '30-day']

function greeting() {
  const h = new Date().getHours()
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
}

export default function TodayPage() {
  const campaign = useCampaign()
  const { data: t, error, reload } = useLoad(
    () => (campaign ? api.today(campaign.id) : Promise.resolve(null)),
    [campaign?.id],
  )
  const { data: academy } = useLoad(() => api.academy(), [])

  if (!campaign) return <div className="container"><p className="muted">No campaign yet — add one under data/campaigns/ and reload.</p></div>
  if (!t) return <div className="container"><Status error={error} /></div>

  const act = async (slug: string, action: 'DONE' | 'REMEMBERED' | 'FORGOT') => {
    await api.progress(slug, action)
    reload()
  }
  const minutes = t.items.reduce((m, i) => m + i.minutes, 0)
  const path = academy?.paths[0]
  const nextLesson = path?.next ? academy?.tracks.flatMap((tr) => tr.modules).find((m) => m.id === path.next) : undefined

  return (
    <div className="container">
      <header className="page-head">
        <div>
          <div className="eyebrow">{fmtDate(t.date, { weekday: 'long', day: 'numeric', month: 'long' })}</div>
          <h1 style={{ marginTop: 6 }}>{greeting()}</h1>
          <p className="sub">
            {t.items.length > 0
              ? `About ${minutes} minutes today${t.weekend ? ' — a design day' : ''}.`
              : 'Nothing new scheduled today.'}
          </p>
        </div>
      </header>

      {t.phase === 'REVIEW' && (
        <div className="callout" style={{ marginBottom: 20 }}>
          <Sparkles size={18} />
          <span>Final days: no new material. Clear your reviews and do one timed mock interview.</span>
        </div>
      )}
      {t.phase === 'FINISHED' && (
        <div className="callout" style={{ marginBottom: 20 }}>
          <Sparkles size={18} />
          <span>The interview date has passed. Good luck — and note down what they asked.</span>
        </div>
      )}
      {t.behind > 0 && (
        <div className="callout warn" style={{ marginBottom: 20 }}>
          <Info size={18} />
          <span>
            {t.behind} planned item{t.behind === 1 ? '' : 's'} behind. No need to catch up — just do today's and the plan
            moves with you.
          </span>
        </div>
      )}

      <div className="today-grid">
        <div>
          <div className="section-head">
            <h2>Today's focus</h2>
            <span className="muted" style={{ fontSize: '0.86rem' }}>
              ~{minutes} of {t.budget} min
            </span>
          </div>
          {t.items.length === 0 ? (
            <div className="card">
              <div className="empty">
                <Check size={22} />
                <strong>All planned items are done</strong>
                <span>Use the time for reviews or the next Academy lesson.</span>
              </div>
            </div>
          ) : (
            t.items.map((i) => <FocusCard key={i.slug} item={i} onDone={() => act(i.slug, 'DONE')} />)
          )}

          <section className="section">
            <div className="section-head">
              <h2>Reviews due</h2>
              {t.reviews.length > 0 && <span className="pill accent">{t.reviews.length}</span>}
            </div>
            <div className="card">
              {t.reviews.length === 0 ? (
                <div className="empty">
                  <RotateCcw size={22} />
                  <strong>Nothing to review</strong>
                  <span>Solved questions come back after 3, 10 and 30 days.</span>
                </div>
              ) : (
                <div className="list">
                  {t.reviews.map((r) => (
                    <div className="list-row" key={r.slug}>
                      <div className="grow">
                        <Link to={`/q/${r.slug}`} className="title">
                          {r.title}
                        </Link>
                        <Meta>
                          <TypeTag type={r.type} />
                          <span>{LADDER[r.stage] ?? ''} review</span>
                          <span>due {fmtDate(r.due)}</span>
                        </Meta>
                      </div>
                      <button className="btn sm ghost" onClick={() => act(r.slug, 'FORGOT')}>
                        Forgot
                      </button>
                      <button className="btn sm primary" onClick={() => act(r.slug, 'REMEMBERED')}>
                        Remembered
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <p className="hint">A review means solving it again from a blank page in under 10 minutes, without peeking.</p>
          </section>
        </div>

        <aside>
          <div className="card side-card">
            <div className="eyebrow">Plan progress</div>
            <div className="ring" style={{ marginTop: 12 }}>
              <Ring value={t.done} max={t.total} />
              <div>
                <div style={{ fontWeight: 650, fontSize: '1.1rem' }} className="num">
                  {t.done} / {t.total}
                </div>
                <div className="muted" style={{ fontSize: '0.84rem' }}>
                  planned questions done
                </div>
                <Link to="/plan" style={{ fontSize: '0.84rem' }}>
                  See the plan
                </Link>
              </div>
            </div>
          </div>

          {nextLesson && path && (
            <div className="card side-card">
              <div className="row" style={{ justifyContent: 'space-between' }}>
                <span className="eyebrow">Continue learning</span>
                <BookOpen size={16} className="muted" />
              </div>
              <Link to={`/academy/${nextLesson.id}`} className="continue-link">
                {nextLesson.title}
              </Link>
              <div className="meta" style={{ marginTop: 8 }}>
                <LevelChip level={nextLesson.level} />
                <span>~{nextLesson.minutes} min</span>
              </div>
              <div style={{ marginTop: 14 }}>
                <div className="row" style={{ justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span className="muted">{path.title.replace(' (start here)', '')}</span>
                  <span className="muted num">
                    {path.done}/{path.modules.length}
                  </span>
                </div>
                <div className="bar" style={{ marginTop: 6 }}>
                  <span style={{ width: `${(path.done / path.modules.length) * 100}%` }} />
                </div>
              </div>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}

function FocusCard({ item, onDone }: { item: PlanItem; onDone: () => void }) {
  return (
    <article className="card raised focus">
      <Meta>
        <TypeTag type={item.type} />
        <Difficulty value={item.difficulty} />
        <span>~{item.minutes} min</span>
      </Meta>
      <Link to={`/q/${item.slug}`} className="focus-title">
        {item.title}
      </Link>
      <div className="row" style={{ gap: 8 }}>
        <span className="muted" style={{ fontSize: '0.84rem' }}>
          Asked at
        </span>
        <CompanyPills slugs={item.companies} />
      </div>
      <div className="focus-actions">
        <Link to={`/q/${item.slug}`} className="btn lg primary">
          Open question <ArrowRight size={16} />
        </Link>
        {item.leetcode && (
          <a className="btn lg" href={leetcodeUrl(item.leetcode)} target="_blank" rel="noreferrer">
            Solve on LeetCode <ExternalLink size={15} />
          </a>
        )}
        <span className="spacer" />
        <button className="btn lg ghost" onClick={onDone}>
          <Check size={16} /> Mark done
        </button>
      </div>
    </article>
  )
}
