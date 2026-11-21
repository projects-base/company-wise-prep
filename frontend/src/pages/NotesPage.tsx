import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Download, Search } from 'lucide-react'
import { api, type ExportScope, type LessonNotes, type NoteHit, type NoteHitKind, type NoteSearch } from '../api'
import { useCampaign } from '../App'
import { downloadText } from '../LessonNotes'
import type { LessonLinkState } from './ModulePage'
import { daysBetween, fmtDate, localToday, Status, useLoad } from '../ui'

const KIND_LABEL: Record<NoteHitKind, string> = {
  HIGHLIGHT: 'highlights',
  SUMMARY: 'summaries',
  SNIPPET: 'snippets',
  MISTAKE: 'mistakes',
}
const KIND_PILL: Record<NoteHitKind, string> = {
  HIGHLIGHT: 'Highlight',
  SUMMARY: 'Summary',
  SNIPPET: 'Playground',
  MISTAKE: 'Mistake',
}
const short = (iso: string) => fmtDate(iso, { day: 'numeric', month: 'short' })
const plural = (n: number, word: string) => `${n} ${word}${n === 1 ? '' : 's'}`
const SUMMARY_ROWS = [
  ['idea', 'Idea'],
  ['inside', 'How it works'],
  ['trap', 'The trap'],
  ['line', 'Interview line'],
] as const

/** Plan week of a day, numbered as on the Plan page: week 1 runs to the first Sunday, then Monday to Sunday. */
function planWeek(start: string, day: string) {
  const fromStart = daysBetween(start, day)
  const toMonday = (8 - new Date(`${start}T00:00:00`).getDay()) % 7
  if (fromStart < toMonday) return 1
  return (toMonday === 0 ? 1 : 2) + Math.floor((fromStart - toMonday) / 7)
}

/** Everything you wrote while studying: search, lessons grouped by plan week or track, and a cheat-sheet export. */
export default function NotesPage() {
  const campaign = useCampaign()
  const { data, error } = useLoad(() => api.notes(campaign?.id), [campaign?.id])
  const [q, setQ] = useState('')
  const [hits, setHits] = useState<NoteSearch | null>(null)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [group, setGroup] = useState<'week' | 'track'>('week')

  useEffect(() => {
    const query = q.trim()
    if (query.length < 2) {
      setHits(null)
      return
    }
    let live = true
    const t = setTimeout(() => {
      api.searchNotes(query).then(
        (r) => live && (setHits(r), setSearchError(null)),
        (e: Error) => live && setSearchError(e.message),
      )
    }, 250)
    return () => {
      live = false
      clearTimeout(t)
    }
  }, [q])

  const groups = useMemo(() => {
    const out = new Map<string, LessonNotes[]>()
    for (const l of data?.lessons ?? []) {
      const key =
        group === 'track'
          ? l.trackTitle
          : l.week && l.weekFrom && l.weekTo
            ? `Week ${l.week} · ${short(l.weekFrom)} – ${short(l.weekTo)}`
            : 'Not in the plan'
      out.set(key, [...(out.get(key) ?? []), l])
    }
    // Plan order already; "Not in the plan" goes last when grouping by week.
    return [...out.entries()].sort(([a], [b]) => Number(a === 'Not in the plan') - Number(b === 'Not in the plan'))
  }, [data, group])

  if (!data) return <div className="container"><Status error={error} /></div>
  const empty = data.lessons.length === 0 && data.general.length === 0

  return (
    <div className="container wide">
      <header className="page-head">
        <div>
          <h1>My notes</h1>
          <p className="sub">Your highlights, summaries, Playground snippets and quiz mistakes, in plan order.</p>
        </div>
      </header>

      <div className="notes-page">
        <div>
          <div className="search notes-search">
            <Search size={16} />
            <input
              type="search"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Search notes, highlights and mistakes"
              aria-label="Search notes, highlights and mistakes"
            />
          </div>

          {q.trim().length >= 2 ? (
            <SearchResults hits={hits} error={searchError} />
          ) : empty ? (
            <div className="callout" style={{ marginTop: 18 }}>
              <span>
                No notes yet. In any lesson, select a sentence and choose <strong>Highlight</strong> or{' '}
                <strong>Add note</strong>, or write the lesson summary from <strong>My notes</strong> beside the lesson.
                Quiz mistakes land here by themselves.
              </span>
            </div>
          ) : (
            <>
              <div className="row notes-group-by" role="group" aria-label="Group by">
                {(['week', 'track'] as const).map((g) => (
                  <button key={g} className={`chip ${group === g ? 'on' : ''}`} aria-pressed={group === g}
                    onClick={() => setGroup(g)}>
                    By {g}
                  </button>
                ))}
              </div>
              {groups.map(([label, lessons]) => (
                <section key={label} className="notes-group">
                  <h2 className="eyebrow">{label}</h2>
                  {lessons.map((l) => (
                    <LessonCard key={l.moduleId} l={l} />
                  ))}
                </section>
              ))}
              {data.general.length > 0 && (
                <section className="notes-group">
                  <h2 className="eyebrow">General notes (from the Playground)</h2>
                  {data.general.map((n) => (
                    <div key={n.id} className="card pad notes-card">
                      {n.note && <p>{n.note}</p>}
                      <pre className="notes-code"><code>{n.code}</code></pre>
                      {n.output && <pre className="notes-code out">{n.output}</pre>}
                    </div>
                  ))}
                </section>
              )}
            </>
          )}
        </div>

        <ExportPanel lessons={data.lessons} campaignId={campaign?.id} campaignStart={campaign?.start} />
      </div>
    </div>
  )
}

function SearchResults({ hits, error }: { hits: NoteSearch | null; error: string | null }) {
  if (error) return <p className="error">Search failed: {error}</p>
  if (!hits) return <p className="muted">Searching…</p>
  const covers = (Object.keys(KIND_LABEL) as NoteHitKind[]).map((k) => `${KIND_LABEL[k]} ${hits.counts[k] ?? 0}`).join(' · ')
  return (
    <div className="notes-hits">
      <p className="muted" aria-live="polite">
        {hits.hits.length} {hits.hits.length === 1 ? 'hit' : 'hits'} · Search covers: {covers}
      </p>
      <ul>
        {hits.hits.map((h) => (
          <li key={`${h.kind}-${h.id}`} className="card pad notes-card">
            <div className="row">
              <span className="pill">{KIND_PILL[h.kind]}</span>
              {h.moduleCode && <span className="num code-chip">{h.moduleCode}</span>}
              {h.moduleTitle && <span className="muted">{h.moduleTitle}</span>}
            </div>
            <Link
              to={h.link}
              state={hitState(h)}
              className="notes-hit-text"
            >
              {h.text}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

/** Opens the lesson on the matching notes tab, or at the quiz for a mistake (as Retry now does). */
function hitState(h: NoteHit): LessonLinkState {
  if (h.kind === 'MISTAKE') return { quiz: h.text }
  return h.kind === 'SUMMARY' ? { notes: 'summary' } : h.kind === 'HIGHLIGHT' ? { notes: 'highlights' } : null
}

function LessonCard({ l }: { l: LessonNotes }) {
  const open = l.mistakes.filter((m) => !m.fixed).length
  const shown = l.highlights.slice(0, 3)
  return (
    <article className="card pad notes-card">
      <div className="row" style={{ justifyContent: 'space-between' }}>
        <Link to={`/academy/${l.moduleId}`} className="notes-card-title">
          <span className="num code-chip">{l.code}</span> {l.title}
        </Link>
        <span className="row" style={{ gap: 6 }}>
          {l.highlights.length > 0 && <span className="pill">{plural(l.highlights.length, 'highlight')}</span>}
          {l.snippets.length > 0 && <span className="pill">{plural(l.snippets.length, 'snippet')}</span>}
          {open > 0 && <span className="pill warn">{plural(open, 'mistake')}</span>}
        </span>
      </div>
      {l.summary ? (
        <dl className="notes-summary-view">
          {SUMMARY_ROWS.filter(([k]) => l.summary?.[k]).map(([k, label]) => (
            <div key={k}>
              <dt>{label}</dt>
              <dd>{l.summary?.[k]}</dd>
            </div>
          ))}
        </dl>
      ) : (
        <p className="muted notes-no-summary">
          No summary yet ·{' '}
          <Link to={`/academy/${l.moduleId}`} state={{ notes: 'summary' } satisfies LessonLinkState}>
            Write it now (2 min)
          </Link>
        </p>
      )}
      {shown.length > 0 && (
        <ul className="notes-quotes">
          {shown.map((h) => (
            <li key={h.id}>
              <q>{h.quote}</q>
              {h.note && <span className="muted"> — {h.note}</span>}
            </li>
          ))}
          {l.highlights.length > shown.length && (
            <li>
              <Link to={`/academy/${l.moduleId}`} state={{ notes: 'highlights' } satisfies LessonLinkState}>
                +{l.highlights.length - shown.length} more
              </Link>
            </li>
          )}
        </ul>
      )}
    </article>
  )
}

type ScopeChoice = 'this-week' | 'week' | 'track' | 'lesson' | 'all'

function ExportPanel({ lessons, campaignId, campaignStart }: {
  lessons: LessonNotes[]
  campaignId?: string
  campaignStart?: string
}) {
  const weeks = useMemo(
    () => [...new Set(lessons.map((l) => l.week).filter((w): w is number => w !== null))].sort((a, b) => a - b),
    [lessons],
  )
  const tracks = useMemo(() => [...new Map(lessons.map((l) => [l.trackId, l.trackTitle])).entries()], [lessons])
  const thisWeek = campaignStart ? planWeek(campaignStart, localToday()) : null
  const [choice, setChoice] = useState<ScopeChoice>(thisWeek ? 'this-week' : 'all')
  // The campaign can load after the page; default to this week then, unless a scope was already picked.
  const picked = useRef(false)
  useEffect(() => {
    if (!picked.current) setChoice(thisWeek ? 'this-week' : 'all')
  }, [thisWeek])
  const [week, setWeek] = useState<string>('')
  const [track, setTrack] = useState<string>('')
  const [lesson, setLesson] = useState<string>('')
  const [preview, setPreview] = useState<{ filename: string; markdown: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const request: [ExportScope, string | undefined] | null =
    choice === 'this-week' ? (thisWeek ? ['week', String(thisWeek)] : null)
      : choice === 'week' ? (week || weeks[0] ? ['week', week || String(weeks[0])] : null)
        : choice === 'track' ? (track || tracks[0] ? ['track', track || tracks[0][0]] : null)
          : choice === 'lesson' ? (lesson || lessons[0] ? ['lesson', lesson || lessons[0].moduleId] : null)
            : ['all', undefined]
  const key = request ? request.join(':') : ''

  useEffect(() => {
    if (!request) {
      setPreview(null)
      return
    }
    let live = true
    setError(null)
    api.exportNotes(request[0], request[1], campaignId).then(
      (r) => live && setPreview(r),
      (e: Error) => live && setError(e.message),
    )
    return () => {
      live = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, campaignId])

  const option = (value: ScopeChoice, label: string, disabled = false) => (
    <label className={disabled ? 'muted' : ''}>
      <input type="radio" name="scope" value={value} checked={choice === value} disabled={disabled}
        onChange={() => {
          picked.current = true
          setChoice(value)
        }} />
      {label}
    </label>
  )

  return (
    <aside className="card pad notes-export" aria-label="Build a cheat sheet">
      <h2>Build a cheat sheet</h2>
      <p className="muted">From your own notes, highlights and mistakes — for revision the night before.</p>
      <fieldset className="notes-scope">
        <legend className="io-label">Scope</legend>
        {option('this-week', thisWeek ? `This week (week ${thisWeek})` : 'This week (needs a plan)', !thisWeek)}
        <div className="notes-scope-row">
          {option('week', 'A week', !weeks.length || !campaignId)}
          {choice === 'week' && (
            <select aria-label="Week" value={week || String(weeks[0] ?? '')} onChange={(e) => setWeek(e.target.value)}>
              {weeks.map((w) => (
                <option key={w} value={w}>Week {w}</option>
              ))}
            </select>
          )}
        </div>
        <div className="notes-scope-row">
          {option('track', 'A track', !tracks.length)}
          {choice === 'track' && (
            <select aria-label="Track" value={track || tracks[0]?.[0]} onChange={(e) => setTrack(e.target.value)}>
              {tracks.map(([id, title]) => (
                <option key={id} value={id}>{title}</option>
              ))}
            </select>
          )}
        </div>
        <div className="notes-scope-row">
          {option('lesson', 'A lesson', !lessons.length)}
          {choice === 'lesson' && (
            <select aria-label="Lesson" value={lesson || lessons[0]?.moduleId} onChange={(e) => setLesson(e.target.value)}>
              {lessons.map((l) => (
                <option key={l.moduleId} value={l.moduleId}>{l.code} · {l.title}</option>
              ))}
            </select>
          )}
        </div>
        {option('all', 'Everything')}
      </fieldset>
      {error && <p className="error">{error}</p>}
      {preview && (
        <>
          <pre className="notes-preview" aria-label="Preview">{preview.markdown}</pre>
          <button className="btn primary" onClick={() => downloadText(preview.filename, preview.markdown)}>
            <Download size={15} /> Download {preview.filename}
          </button>
        </>
      )}
    </aside>
  )
}
