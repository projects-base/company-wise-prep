import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, Check, Clock, ExternalLink, Flame, Info, RotateCcw, Sparkles, Star } from 'lucide-react'
import { api, leetcodeUrl, type PlanItem } from '../api'
import { HotBadge } from '../HotBadge'
import { useCampaign } from '../App'
import { readTicks, SessionRow, sessionSteps } from './PlanPage'
import { celebrate, recordActivity, TICKS_CHANGED, useStreak } from '../rewards'
import { CompanyPills, Difficulty, fmtDate, LevelChip, Meta, Ring, Status, TypeTag, useLoad } from '../ui'
import { CalendarClock } from 'lucide-react'

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
  const streak = useStreak()
  const [, bump] = useState(0)
  const [short, setShort] = useState(false)
  const day = t ? dayProgress(t.date, t.sessions ?? []) : { done: 0, total: 0 }

  // Re-count when a step is ticked; the first time every step of the day is done, celebrate it.
  useEffect(() => {
    if (!t) return
    const on = () => {
      bump((n) => n + 1)
      const p = dayProgress(t.date, t.sessions ?? [])
      const flag = `daydone.${t.date}`
      try {
        if (p.total > 0 && p.done >= p.total && localStorage.getItem(flag) !== '1') {
          localStorage.setItem(flag, '1')
          const s = recordActivity()
          celebrate('Day complete!', s > 1 ? `${s} days in a row. See you tomorrow.` : 'Day one done. See you tomorrow.', true)
        }
      } catch {
        /* private window */
      }
    }
    window.addEventListener(TICKS_CHANGED, on)
    return () => window.removeEventListener(TICKS_CHANGED, on)
  }, [t])

  if (!campaign) return <div className="container"><p className="muted">No campaign yet — add one under data/campaigns/ and reload.</p></div>
  if (!t) return <div className="container"><Status error={error} /></div>

  const act = async (slug: string, action: 'DONE' | 'REMEMBERED' | 'FORGOT') => {
    await api.progress(slug, action)
    if (action !== 'FORGOT') {
      const s = recordActivity()
      if (action === 'DONE') celebrate('Marked done', s > 1 ? `${s}-day streak` : undefined)
    }
    reload()
  }
  const sessions = t.sessions ?? []
  const core = sessions.filter((s) => s.tier !== 'stretch')
  const extra = sessions.filter((s) => s.tier === 'stretch')
  const floor = sessions.find((s) => s.floor)
  const stretchMinutes = extra.reduce((m, s) => m + s.minutes, 0)
  // Questions of stretch sessions are shown under Stretch, not as today's focus.
  const stretchSlugs = new Set(extra.flatMap((s) => s.questions))
  const coreSlugs = new Set(core.flatMap((s) => s.questions))
  const focusItems = t.items.filter((i) => coreSlugs.has(i.slug) || !stretchSlugs.has(i.slug))
  const stretchItems = t.items.filter((i) => stretchSlugs.has(i.slug) && !coreSlugs.has(i.slug))
  const minutes = sessions.length > 0 ? t.budget : t.items.reduce((m, i) => m + i.minutes, 0)
  const path = academy?.paths[0]
  const nextLesson = path?.next ? academy?.tracks.flatMap((tr) => tr.modules).find((m) => m.id === path.next) : undefined

  return (
    <div className="container">
      <header className="page-head">
        <div>
          <div className="eyebrow">{fmtDate(t.date, { weekday: 'long', day: 'numeric', month: 'long' })}</div>
          <h1 style={{ marginTop: 6 }}>{greeting()}</h1>
          <p className="sub">
            {t.items.length > 0 || sessions.length > 0
              ? `About ${minutes} minutes today${stretchMinutes ? `, +${stretchMinutes} if you have more time` : ''}${t.weekend ? ' — a design day' : ''}.`
              : 'Nothing new scheduled today.'}
          </p>
        </div>
        {t.phase !== 'FINISHED' && (
          <div className={`countdown ${t.daysLeft <= 3 ? 'soon' : ''}`}>
            <CalendarClock size={18} />
            <div>
              <div className="countdown-n num">
                {t.daysLeft} day{t.daysLeft === 1 ? '' : 's'}
              </div>
              <div className="countdown-l">to the interview · {fmtDate(t.campaign.interview, { weekday: 'short', day: 'numeric', month: 'short' })}</div>
            </div>
          </div>
        )}
      </header>

      {t.phase === 'REVIEW' && sessions.length === 0 && (
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
          {sessions.length > 0 && floor && (
            <div className={`floor-bar ${short ? 'on' : ''}`}>
              <Clock size={16} />
              {short ? (
                <span>
                  <strong>Short day:</strong> just this one. It keeps your streak; anything else waits for Sunday's catch-up.
                </span>
              ) : (
                <span>Short on time?</span>
              )}
              <button className="btn sm" onClick={() => setShort(!short)}>
                {short ? 'Show the full day' : 'Do just this'}
              </button>
            </div>
          )}
          {sessions.length > 0 && (
            <div className="card" style={{ marginBottom: 12 }}>
              <div className="list" style={{ padding: '0 16px' }}>
                {(short && floor ? [floor] : core).map((s) => (
                  <SessionRow key={s.title} session={s} date={t.date} />
                ))}
              </div>
            </div>
          )}
          {short ? null : focusItems.length === 0 && sessions.length > 0 ? null : focusItems.length === 0 ? (
            <div className="card">
              <div className="empty">
                <Check size={22} />
                <strong>All planned items are done</strong>
                <span>Use the time for reviews or the next Academy lesson.</span>
              </div>
            </div>
          ) : (
            focusItems.map((i) => <FocusCard key={i.slug} item={i} onDone={() => act(i.slug, 'DONE')} />)
          )}

          {!short && extra.length > 0 && (
            <section className="section stretch">
              <div className="section-head">
                <h2>
                  <Star size={17} style={{ verticalAlign: -2 }} /> Stretch
                </h2>
                <span className="muted" style={{ fontSize: '0.86rem' }}>
                  Have more time? +{stretchMinutes} min. Skipping it never puts you behind.
                </span>
              </div>
              <div className="card" style={{ marginBottom: 12 }}>
                <div className="list" style={{ padding: '0 16px' }}>
                  {extra.map((s) => (
                    <SessionRow key={s.title} session={s} date={t.date} open={false} />
                  ))}
                </div>
              </div>
              {stretchItems.map((i) => (
                <FocusCard key={i.slug} item={i} onDone={() => act(i.slug, 'DONE')} />
              ))}
            </section>
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
          {(day.total > 0 || streak.current > 0) && (
            <div className="card side-card today-card">
              <div className="eyebrow">Today</div>
              <div className="ring" style={{ marginTop: 12 }}>
                <Ring value={day.done} max={Math.max(1, day.total)} />
                <div>
                  <div style={{ fontWeight: 650, fontSize: '1.1rem' }} className="num">
                    {day.total > 0 ? `${day.done} / ${day.total} steps` : 'No sessions today'}
                  </div>
                  <div className={`streak ${streak.current > 0 ? 'on' : ''}`}>
                    <Flame size={15} /> {streak.current > 0 ? `${streak.current}-day streak` : 'Start your streak today'}
                  </div>
                  {streak.best > streak.current && (
                    <div className="muted" style={{ fontSize: '0.78rem' }}>
                      Best: {streak.best} days
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
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
                <span className="eyebrow">
                  Step {path.modules.indexOf(nextLesson.id) + 1} of {path.modules.length} · {nextLesson.code}
                </span>
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
        <HotBadge slug={item.slug} max={4} />
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

/** Ticked steps over all steps of today's core sessions: the day is complete when core is done. */
function dayProgress(date: string, sessions: { title: string; detail: string | null; tier?: string }[]) {
  let done = 0
  let total = 0
  for (const s of sessions) {
    if (s.tier === 'stretch') continue
    const n = Math.max(1, sessionSteps(s.detail ?? '').length)
    total += n
    done += Math.min(n, readTicks(`ticks.${date}.${s.title}`).length)
  }
  return { done, total }
}
