import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Check, ExternalLink, Info } from 'lucide-react'
import { api, leetcodeUrl, type FreqList, type FreqWindow } from '../api'
import { Difficulty } from '../ui'

const WINDOWS: [FreqWindow, string][] = [
  ['thirty-days', '30 days'],
  ['three-months', '3 months'],
  ['six-months', '6 months'],
  ['all', 'All time'],
]
type Filter = 'all' | 'todo' | 'missing'

/**
 * The company's LeetCode-tagged problems by frequency, read live from the community repo, with
 * each one matched to the bank and your progress. Shows what to practise next, not what was asked:
 * frequency comes from LeetCode's company tags, not from interview reports.
 */
export function Frequency({ slug, name }: { slug: string; name: string }) {
  const [window, setWindow] = useState<FreqWindow>('three-months')
  const [folder, setFolder] = useState<string | undefined>(undefined)
  const [folders, setFolders] = useState<string[] | null>(null)
  const [data, setData] = useState<{ folder: string | null; list: FreqList | null } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<Filter>('all')

  useEffect(() => {
    let live = true
    setError(null)
    api
      .companyFrequency(slug, window, folder)
      .then((d) => live && setData(d))
      .catch((e) => live && setError(e instanceof Error ? e.message : 'Could not load the list'))
    return () => {
      live = false
    }
  }, [slug, window, folder])

  const pickOther = () => {
    if (!folders) api.frequencyFolders().then(setFolders).catch(() => setFolders([]))
  }

  const rows = useMemo(() => {
    const all = data?.list?.rows ?? []
    return filter === 'todo' ? all.filter((r) => !r.done) : filter === 'missing' ? all.filter((r) => !r.question) : all
  }, [data, filter])

  const list = data?.list
  return (
    <section className="section freq">
      <div className="callout" style={{ marginBottom: 16 }}>
        <Info size={18} />
        <div>
          Problems LeetCode tagged for <strong>{data?.folder ?? name}</strong>, most frequent first, from the community list{' '}
          <a href="https://github.com/liquidslr/leetcode-company-wise-problems" target="_blank" rel="noreferrer">
            liquidslr/leetcode-company-wise-problems
          </a>
          . Use it to choose <em>which</em> problems to practise. It is not proof that a problem was asked in India.
        </div>
      </div>

      <div className="freq-bar">
        <div className="seg" role="tablist" aria-label="Time window">
          {WINDOWS.map(([w, label]) => (
            <button key={w} role="tab" aria-selected={window === w} onClick={() => setWindow(w)}>
              {label}
            </button>
          ))}
        </div>
        <label className="freq-folder" onFocus={pickOther} onMouseDown={pickOther}>
          <span className="muted">List</span>
          <select value={data?.folder ?? ''} onChange={(e) => setFolder(e.target.value || undefined)}>
            {data?.folder && <option value={data.folder}>{data.folder}</option>}
            {!data?.folder && <option value="">Pick a company…</option>}
            {(folders ?? []).filter((f) => f !== data?.folder).map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && <p className="judge-error">{error}</p>}
      {!error && !data && <p className="muted">Loading from GitHub…</p>}
      {data && !data.folder && (
        <p className="muted">
          The repo has no list for {name}. Pick a similar company from the List menu, or add <code>leetcode_company:</code> to its company.yaml.
        </p>
      )}

      {list && (
        <>
          <div className="freq-stats">
            <span>
              <strong className="num">{list.rows.length}</strong> problems
            </span>
            <span>
              <strong className="num">{list.inBank}</strong> in your bank
            </span>
            <span>
              <strong className="num">{list.done}</strong> done
            </span>
            <span className="spacer" />
            <div className="seg" role="tablist" aria-label="Filter">
              {(
                [
                  ['all', 'All'],
                  ['todo', 'Not done'],
                  ['missing', 'Not in bank'],
                ] as [Filter, string][]
              ).map(([f, label]) => (
                <button key={f} role="tab" aria-selected={filter === f} onClick={() => setFilter(f)}>
                  {label}
                </button>
              ))}
            </div>
          </div>

          <div className="card freq-list">
            {rows.length === 0 && <p className="muted pad">Nothing here for this filter.</p>}
            {rows.slice(0, 150).map((r, i) => (
              <div key={r.leetcode} className={`list-row ${r.done ? 'done' : ''}`}>
                <span className="muted num freq-rank">{i + 1}</span>
                <div className="grow">
                  {r.question ? (
                    <Link to={`/q/${r.question}`} className="title">
                      {r.title}
                    </Link>
                  ) : (
                    <a href={leetcodeUrl(r.leetcode)} target="_blank" rel="noreferrer" className="title">
                      {r.title}
                    </a>
                  )}
                  <div className="muted freq-topics">{r.topics.slice(0, 3).join(' · ')}</div>
                </div>
                {r.done ? (
                  <span className="pill ok">
                    <Check size={12} strokeWidth={3} /> Done
                  </span>
                ) : r.question ? (
                  <span className="pill accent">In bank</span>
                ) : (
                  <a className="pill" href={leetcodeUrl(r.leetcode)} target="_blank" rel="noreferrer" title="Not in your bank: solve it on LeetCode">
                    LeetCode <ExternalLink size={11} />
                  </a>
                )}
                <Difficulty value={r.difficulty.toLowerCase()} />
                <span className="freq-meter" title={`Frequency ${r.frequency}`}>
                  <span style={{ width: `${Math.max(4, r.frequency)}%` }} />
                </span>
              </div>
            ))}
          </div>
          <p className="hint">
            {rows.length > 150 && `Showing the top 150 of ${rows.length}. `}
            Fetched {new Date(list.fetchedAt).toLocaleString()} ·{' '}
            <a href={list.source} target="_blank" rel="noreferrer">
              source file
            </a>
          </p>
        </>
      )}
    </section>
  )
}
