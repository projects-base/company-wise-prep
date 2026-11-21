import { Children, isValidElement, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { AnimBlock, animSource } from '../Anim'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import Markdown from 'react-markdown'
import { Diagram, mermaidSource } from '../Diagram'
import { parseQuiz, QuizBlock, QuizReporterContext, quizSource, type QuizReporter } from '../Quiz'
import remarkGfm from 'remark-gfm'
import { ArrowLeft, ArrowRight, Check, ChevronRight, Copy, Info, ListTree, NotebookPen, Play, X } from 'lucide-react'
import { api, type ModuleSummary, type Ref } from '../api'
import { highlightPlugin, NotesPanel, SelectionToolbar, type NotesTab } from '../LessonNotes'

import { LevelTree } from '../LessonTree'
import { PATH_KEY } from './AcademyPage'
import { Bar, LevelChip, Status, useLoad, MdLink } from '../ui'
import { useFeatures } from '../features'
import { Lifeline } from '../Lifeline'
import { ModeToggle, StepperView, useStepper } from '../Stepper'

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

function CopyButton({ text }: { text: string }) {
  const [state, setState] = useState<'idle' | 'copied' | 'failed'>('idle')
  useEffect(() => {
    if (state === 'idle') return
    const t = setTimeout(() => setState('idle'), 1500)
    return () => clearTimeout(t)
  }, [state])
  const copy = () =>
    navigator.clipboard
      .writeText(text.replace(/\n$/, ''))
      .then(() => setState('copied'))
      .catch(() => setState('failed'))
  return (
    <button className="btn sm" onClick={copy} aria-label="Copy code">
      {state === 'copied' ? <Check size={13} /> : <Copy size={13} />}
      {state === 'copied' ? 'Copied' : state === 'failed' ? 'Copy failed' : 'Copy'}
    </button>
  )
}

/** What other pages can pass when they open a lesson: a quiz to retry, or the notes panel on a tab. */
export type LessonLinkState = { quiz?: string; notes?: NotesTab } | null

const PANEL_KEY = 'notes.panel'

export default function ModulePage() {
  const { id = '' } = useParams()
  const { data: v, error, reload } = useLoad(() => api.module(id), [id])
  const notes = useLoad(() => api.lessonNotes(id), [id])
  const navigate = useNavigate()
  const linkState = useLocation().state as LessonLinkState
  const { codeRunner } = useFeatures()
  const prose = useRef<HTMLDivElement>(null)
  const [panelOpen, setPanelOpen] = useState(() => {
    try {
      return localStorage.getItem(PANEL_KEY) === '1'
    } catch {
      return false
    }
  })
  const [tab, setTab] = useState<NotesTab>('highlights')
  const [focusId, setFocusId] = useState<number | null>(null)
  const openPanel = (open: boolean) => {
    setPanelOpen(open)
    try {
      localStorage.setItem(PANEL_KEY, open ? '1' : '0')
    } catch {
      /* not remembered in a private window */
    }
  }
  useEffect(() => {
    if (linkState?.notes) {
      setTab(linkState.notes)
      setPanelOpen(true)
    }
  }, [linkState?.notes, id])

  const headings = useMemo(
    () =>
      (v?.lesson ?? '')
        .replace(/```[\s\S]*?```/g, '')
        .split('\n')
        .filter((l) => l.startsWith('## '))
        .map((l) => l.slice(3).trim()),
    [v?.lesson],
  )
  const s = useStepper(v?.lesson ?? '', id)
  // Retry from the mistakes notebook: open the part that holds the quiz (the quiz scrolls itself into view).
  const retry = linkState?.quiz
  useEffect(() => {
    if (!retry || s.mode !== 'step') return
    const i = s.parts.findIndex((p) =>
      [...p.body.matchAll(/```quiz\s*\n([\s\S]*?)```/g)].some((q) => parseQuiz(q[1].trim())?.question === retry),
    )
    if (i >= 0) s.setStep(i)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [retry, s.parts, s.mode])
  const active = useActiveHeading(headings.map(slugify), s.mode === 'all' ? v?.lesson : undefined)
  // Parts ticked off, saved on the server so phone and desktop agree; updated optimistically.
  const [partsDone, setPartsDone] = useState<Set<string>>(new Set())
  useEffect(() => setPartsDone(new Set(v?.partsDone ?? [])), [v])

  if (!v) return <div className="container"><Status error={error} /></div>
  const m = v.module
  const highlights = notes.data?.highlights ?? []
  const noteCount = highlights.length + (notes.data?.snippets.length ?? 0)

  const reporter: QuizReporter = {
    focus: retry,
    report: async (spec, picked) => {
      const chosen = spec.options[picked]
      const correct = spec.options.find((o) => o.correct)
      const level = spec.level && ['easy', 'medium', 'hard'].includes(spec.level) ? spec.level : null
      const result = await api.quizAttempt({
        moduleId: m.id,
        question: spec.question,
        chosen: chosen.text,
        correct: correct?.text ?? null,
        why: correct?.why ?? chosen.why ?? null,
        level,
        right: !!chosen.correct,
      })
      notes.reload()
      if (!result) return null
      return chosen.correct ? 'fixed' : 'saved'
    },
  }
  const saveHighlight = async (quote: string, section: string | null, note: string | null) => {
    await api.addHighlight({ moduleId: m.id, section, quote, note })
    notes.reload()
  }
  // Clicking a marked highlight opens it in the notes panel.
  const openHighlight = (e: React.MouseEvent) => {
    const hl = (e.target as Element).closest('[data-hl]')
    if (!hl) return
    setFocusId(Number(hl.getAttribute('data-hl')))
    setTab('highlights')
    openPanel(true)
  }
  /** The section a piece of Markdown starts in: the part's title in one-part-at-a-time mode. */
  const sectionFor = (md: string) => (s.mode === 'step' && s.parts[s.step]?.body === md ? s.parts[s.step].title : null)

  const toggle = async () => {
    await api.moduleDone(m.id, !m.done)
    reload()
  }
  const tickPart = (title: string, done: boolean) => {
    const before = partsDone
    const next = new Set(before)
    if (done) next.add(title)
    else next.delete(title)
    setPartsDone(next)
    api.modulePart(m.id, title, done).then((list) => setPartsDone(new Set(list)), () => setPartsDone(before))
  }
  const finish = async () => {
    const lastPart = s.parts[s.parts.length - 1]
    if (lastPart && !partsDone.has(lastPart.title)) tickPart(lastPart.title, true)
    await toggle()
  }

  return (
    <div className="course">
    <CourseNav current={m.id} refresh={m.done} />
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
            <span className="num code-chip">{m.code}</span>
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
          <div className="row" style={{ marginTop: 16 }}>
            <ModeToggle s={s} />
            {m.written && (
              <button
                className={`btn sm notes-toggle ${panelOpen ? 'on' : ''}`}
                aria-pressed={panelOpen}
                onClick={() => openPanel(!panelOpen)}
              >
                <NotebookPen size={14} /> My notes{noteCount > 0 && <span className="num"> · {noteCount}</span>}
              </button>
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
          <QuizReporterContext.Provider value={reporter}>
          <div className="prose" ref={prose} onClick={openHighlight}>
            <SelectionToolbar container={prose} onSave={saveHighlight} />
            <StepperView
              s={s}
              full={v.lesson ?? ''}
              onFinish={m.done ? undefined : finish}
              doneParts={partsDone}
              onPartDone={tickPart}
              middle={
                <button className={`btn sm ${m.done ? 'lesson-done' : ''}`} onClick={toggle}>
                  <Check size={15} /> {m.done ? 'Completed — undo' : 'Mark lesson complete'}
                </button>
              }
              lastAction={
                m.done ? (
                  v.next ? (
                    <Link to={`/academy/${v.next.id}`} className="btn lg primary">
                      Next lesson: {v.next.code} <ArrowRight size={16} />
                    </Link>
                  ) : (
                    <span className="pill ok">
                      <Check size={12} /> {v.pathPosition > 0 ? 'Last lesson on the path' : 'Lesson complete'}
                    </span>
                  )
                ) : undefined
              }
              finishLabel="Mark lesson complete"
              render={(md) => (
            <Markdown
              remarkPlugins={[remarkGfm]}
              rehypePlugins={[highlightPlugin(highlights, sectionFor(md))]}
              components={{
                h2: ({ children }) => <h2 id={slugify(textOf(children))}>{children}</h2>,
                pre: ({ children }) => {
                  const diagram = mermaidSource(children)
                  if (diagram !== null) return <Diagram source={diagram} />
                  const anim = animSource(children)
                  if (anim !== null) return <AnimBlock source={anim} />
                  const quiz = quizSource(children)
                  if (quiz !== null) return <QuizBlock source={quiz} />
                  const code = textOf(children)
                  const child = Children.toArray(children)[0]
                  const lang = isValidElement<{ className?: string }>(child) ? child.props.className ?? '' : ''
                  const runnable = codeRunner && lang.includes('language-java') && /static\s+void\s+main\s*\(/.test(code)
                  return (
                    <div className="code-block">
                      <div className="code-actions">
                        <CopyButton text={code} />
                        {runnable && (
                          <button
                            className="btn sm"
                            onClick={() => navigate('/playground', { state: { code, lesson: { id: m.id, code: m.code } } })}
                          >
                            <Play size={13} /> Run
                          </button>
                        )}
                      </div>
                      <pre>{children}</pre>
                    </div>
                  )
                },
                a: MdLink,
              }}
            >
              {md}
            </Markdown>
              )}
            />
          </div>
          </QuizReporterContext.Provider>
        ) : (
          <div className="callout">
            <Info size={18} />
            <span>This lesson is still being written.</span>
          </div>
        )}

        <footer className="lesson-foot">
          <div className="row">
            {/* In one-part-at-a-time mode the complete / undo button sits between Back and Next instead. */}
            {(s.mode === 'all' || !s.usable) && (
              <button className={`btn lg ${m.done ? '' : 'primary'}`} onClick={toggle} disabled={!m.written && !m.done}>
                <Check size={16} /> {m.done ? 'Completed — undo' : 'Mark lesson complete'}
              </button>
            )}
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
                  <ArrowLeft size={13} /> Step {v.pathPosition - 1} · {v.previous.code}
                </span>
                <span className="t">{v.previous.title}</span>
              </Link>
            ) : (
              <span />
            )}
            {v.next && (
              <Link to={`/academy/${v.next.id}`} className="next">
                <span className="dir">
                  Step {v.pathPosition + 1} · {v.next.code} <ArrowRight size={13} />
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
            {s.mode === 'step'
              ? s.parts.map((p, i) => (
                  <li key={i}>
                    <a
                      href={`#${slugify(p.title)}`}
                      className={`${s.step === i ? 'on' : ''} ${partsDone.has(p.title) ? 'seen' : ''}`}
                      onClick={(e) => {
                        e.preventDefault()
                        s.setStep(i)
                        window.scrollTo({ top: 0, behavior: 'smooth' })
                      }}
                    >
                      {partsDone.has(p.title) && <Check size={12} strokeWidth={3} className="toc-tick" />}
                      {p.title}
                    </a>
                  </li>
                ))
              : headings.map((h) => (
                  <li key={h}>
                    <a href={`#${slugify(h)}`} className={active === slugify(h) ? 'on' : ''} onClick={(e) => jump(e, slugify(h))}>
                      {h}
                    </a>
                  </li>
                ))}
          </ol>
        </aside>
      )}
      {panelOpen && m.written && (
        <NotesPanel
          moduleId={m.id}
          code={m.code}
          data={notes.data}
          reload={notes.reload}
          tab={tab}
          setTab={setTab}
          focusId={focusId}
          onClose={() => openPanel(false)}
        />
      )}
      <Lifeline key={m.id} context="lesson" refId={m.id} title={m.title} />
    </div>
    </div>
  )
}

/**
 * The course outline beside the lesson: the chosen path as a level → section → lesson tree, so any
 * lesson is one click away. On narrow screens it folds into a "Lessons" button that opens a drawer.
 */
function CourseNav({ current, refresh }: { current: string; refresh: boolean }) {
  const { data } = useLoad(() => api.academy(), [refresh])
  const [open, setOpen] = useState(false)
  useEffect(() => setOpen(false), [current])
  useEffect(() => {
    document.querySelector(`.course-nav [data-lesson="${current}"]`)?.scrollIntoView({ block: 'nearest' })
  }, [current, data])
  if (!data) return <aside className="course-nav" aria-label="Lessons" />

  let chosen = 'zero-to-pro'
  try {
    chosen = localStorage.getItem(PATH_KEY) ?? chosen
  } catch {
    /* default path */
  }
  const path =
    data.paths.find((p) => p.id === chosen && p.modules.includes(current)) ??
    data.paths.find((p) => p.modules.includes(current)) ??
    data.paths[0]
  const byId = new Map<string, ModuleSummary>(data.tracks.flatMap((t) => t.modules).map((m) => [m.id, m]))

  return (
    <>
      <button className="btn sm course-nav-open" onClick={() => setOpen(true)}>
        <ListTree size={15} /> Lessons
      </button>
      <aside className={`course-nav ${open ? 'open' : ''}`} aria-label="Lessons">
        <div className="course-nav-head">
          <div>
            <Link to="/academy" className="eyebrow">
              {path.title.replace(' (start here)', '')}
            </Link>
            <div className="course-nav-progress">
              <Bar value={path.done} max={path.modules.length} />
              <span className="num">
                {path.done}/{path.modules.length}
              </span>
            </div>
          </div>
          <button className="icon-btn course-nav-close" onClick={() => setOpen(false)} aria-label="Close the lesson list">
            <X size={16} />
          </button>
        </div>
        <LevelTree ids={path.modules} byId={byId} tracks={data.tracks} next={path.next} current={current} compact />
      </aside>
      {open && <div className="course-nav-scrim" onClick={() => setOpen(false)} />}
    </>
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
