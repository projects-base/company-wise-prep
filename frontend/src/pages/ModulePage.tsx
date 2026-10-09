import { Children, isValidElement, useEffect, useMemo, useState, type ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { ArrowLeft, ArrowRight, Check, ChevronRight, Info, Play } from 'lucide-react'
import { api, type Ref } from '../api'
import { LevelChip, Status, useLoad, MdLink } from '../ui'
import { useFeatures } from '../features'

const slugify = (s: string) =>
  s
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')

function textOf(node: ReactNode): string {
  if (typeof node === 'string' || typeof node === 'number') return String(node)
  if (Array.isArray(node)) return node.map(textOf).join('')
  if (isValidElement<{ children?: ReactNode }>(node)) return textOf(node.props.children)
  return ''
}

export default function ModulePage() {
  const { id = '' } = useParams()
  const { data: v, error, reload } = useLoad(() => api.module(id), [id])
  const navigate = useNavigate()
  const { codeRunner } = useFeatures()

  const headings = useMemo(
    () =>
      (v?.lesson ?? '')
        .replace(/```[\s\S]*?```/g, '')
        .split('\n')
        .filter((l) => l.startsWith('## '))
        .map((l) => l.slice(3).trim()),
    [v?.lesson],
  )
  const active = useActiveHeading(headings.map(slugify), v?.lesson)

  if (!v) return <div className="container"><Status error={error} /></div>
  const m = v.module

  const toggle = async () => {
    await api.moduleDone(m.id, !m.done)
    reload()
  }

  return (
    <div className="reader">
      <article>
        <header className="reader-head">
          <nav className="crumbs" aria-label="Breadcrumb">
            <Link to="/academy">Academy</Link>
            <ChevronRight size={14} />
            <span>{v.trackTitle}</span>
            {v.pathPosition > 0 && (
              <>
                <ChevronRight size={14} />
                <span>
                  Step {v.pathPosition} of {v.pathLength}
                </span>
              </>
            )}
          </nav>
          <h1>{m.title}</h1>
          <div className="meta">
            <span className="num">{m.id}</span>
            <span className="sep" />
            <LevelChip level={m.level} />
            <span className="sep" />
            <span>~{m.minutes} min</span>
            {m.done && (
              <>
                <span className="sep" />
                <span className="pill ok">
                  <Check size={12} strokeWidth={3} /> Done
                </span>
              </>
            )}
          </div>
          {v.prerequisites.length > 0 && !m.ready && (
            <div className="callout warn" style={{ marginTop: 18 }}>
              <Info size={18} />
              <span>
                Builds on lessons you haven't marked done: <Refs refs={v.prerequisites.filter((r) => !r.done)} />. Fine to
                read now — easier after those.
              </span>
            </div>
          )}
        </header>

        {m.written ? (
          <div className="prose">
            <Markdown
              remarkPlugins={[remarkGfm]}
              components={{
                h2: ({ children }) => <h2 id={slugify(textOf(children))}>{children}</h2>,
                pre: ({ children }) => {
                  const code = textOf(children)
                  const child = Children.toArray(children)[0]
                  const lang = isValidElement<{ className?: string }>(child) ? child.props.className ?? '' : ''
                  const runnable = codeRunner && lang.includes('language-java') && /static\s+void\s+main\s*\(/.test(code)
                  return (
                    <div className="code-block">
                      {runnable && (
                        <button className="btn sm run-btn" onClick={() => navigate('/playground', { state: { code } })}>
                          <Play size={13} /> Run
                        </button>
                      )}
                      <pre>{children}</pre>
                    </div>
                  )
                },
                a: MdLink,
              }}
            >
              {v.lesson}
            </Markdown>
          </div>
        ) : (
          <div className="callout">
            <Info size={18} />
            <span>This lesson is still being written.</span>
          </div>
        )}

        <footer className="lesson-foot">
          <div className="row">
            <button className={`btn lg ${m.done ? '' : 'primary'}`} onClick={toggle} disabled={!m.written && !m.done}>
              <Check size={16} /> {m.done ? 'Completed — undo' : 'Mark lesson complete'}
            </button>
            {v.unlocks.length > 0 && (
              <span className="muted" style={{ fontSize: '0.86rem' }}>
                Unlocks <Refs refs={v.unlocks} />
              </span>
            )}
          </div>
          <nav className="pager" aria-label="Lesson navigation">
            {v.previous ? (
              <Link to={`/academy/${v.previous.id}`}>
                <span className="dir">
                  <ArrowLeft size={13} /> Previous
                </span>
                <span className="t">{v.previous.title}</span>
              </Link>
            ) : (
              <span />
            )}
            {v.next && (
              <Link to={`/academy/${v.next.id}`} className="next">
                <span className="dir">
                  Next <ArrowRight size={13} />
                </span>
                <span className="t">{v.next.title}</span>
              </Link>
            )}
          </nav>
        </footer>
      </article>

      {headings.length > 0 && (
        <aside className="toc" aria-label="On this page">
          <div className="eyebrow">On this page</div>
          <ol>
            {headings.map((h) => (
              <li key={h}>
                <a href={`#${slugify(h)}`} className={active === slugify(h) ? 'on' : ''} onClick={(e) => jump(e, slugify(h))}>
                  {h}
                </a>
              </li>
            ))}
          </ol>
        </aside>
      )}
    </div>
  )
}

/** In-page jumps without touching the hash router's URL. */
function jump(e: React.MouseEvent, id: string) {
  e.preventDefault()
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function useActiveHeading(ids: string[], content: string | undefined) {
  const [active, setActive] = useState<string | null>(null)
  useEffect(() => {
    const els = ids.map((i) => document.getElementById(i)).filter((e): e is HTMLElement => !!e)
    if (!els.length) return
    const obs = new IntersectionObserver(
      (entries) => {
        const visible = entries.filter((e) => e.isIntersecting).sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
        if (visible[0]) setActive(visible[0].target.id)
      },
      { rootMargin: '0px 0px -70% 0px' },
    )
    els.forEach((e) => obs.observe(e))
    return () => obs.disconnect()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [content])
  return active
}

function Refs({ refs }: { refs: Ref[] }) {
  return (
    <>
      {Children.toArray(
        refs.map((r, i) => (
          <span>
            {i > 0 && ', '}
            <Link to={`/academy/${r.id}`}>{r.title}</Link>
          </span>
        )),
      )}
    </>
  )
}
