import { Children, createContext, isValidElement, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { AnimBlock, animSource } from '../Anim'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import Markdown from 'react-markdown'
import { Diagram, mermaidSource } from '../Diagram'
import { QuizBlock, quizQuestions, QuizReporterContext, quizSource, type QuizReporter } from '../Quiz'
import remarkGfm from 'remark-gfm'
import { ArrowLeft, ArrowRight, Check, ChevronRight, Copy, Info, ListTree, NotebookPen, Play, X } from 'lucide-react'
import { api, type Card, type LessonNotes, type Mistake, type ModuleSummary, type Ref } from '../api'
import { highlightPlugin } from '../highlights'
import { NotesPanel, SelectionToolbar, type CardDraft, type NotesTab } from '../LessonNotes'
import { ExplainIt } from '../ExplainIt'

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

/** What a lesson's code blocks need from the page: whether code can run, and how to open it in the Playground. */
const LessonCodeContext = createContext<{ codeRunner: boolean; run: (code: string) => void }>({
  codeRunner: false,
  run: () => {},
})

function LessonHeading({ children }: { children?: ReactNode }) {
  return <h2 id={slugify(textOf(children))}>{children}</h2>
}

/** A fenced block: a diagram, an animation, a quiz, or code with Copy (and Run for a Java main). */
function LessonPre({ children }: { children?: ReactNode }) {
  const { codeRunner, run } = useContext(LessonCodeContext)
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
          <button className="btn sm" onClick={() => run(code)}>
            <Play size={13} /> Run
          </button>
        )}
      </div>
      <pre>{children}</pre>
    </div>
  )
}

/**
 * Module-level and therefore stable: components created inside the page would be a new type on every render, and
 * React would remount every quiz (losing the answer), diagram and animation each time the page re-renders.
 */
const LESSON_COMPONENTS = { h2: LessonHeading, pre: LessonPre, a: MdLink }
const REMARK_PLUGINS = [remarkGfm]

/** What other pages can pass when they open a lesson: a quiz to retry, or the notes panel on a tab. */
export type LessonLinkState = { quiz?: string; notes?: NotesTab } | null

const upsertMistake = (list: Mistake[], m: Mistake) =>
  list.some((x) => x.id === m.id) ? list.map((x) => (x.id === m.id ? m : x)) : [m, ...list]

const PANEL_KEY = 'notes.panel'

export default function ModulePage() {
  const { id = '' } = useParams()
  const { data: v, error, reload } = useLoad(() => api.module(id), [id])
  const notesLoad = useLoad(() => api.lessonNotes(id), [id])
  // A local copy, so quiz answers and summary saves update it without reloading (and re-rendering) the lesson.
  const [notesState, setNotes] = useState<LessonNotes | null>(null)
  useEffect(() => setNotes(notesLoad.data), [notesLoad.data])
  // Never show (or autosave into) the previous lesson's notes while this one loads.
  const notes = notesState && notesState.moduleId === v?.module.id ? notesState : null
  // The lesson's flashcards, for the "Flashcard on" chips; kept locally as cards are made.
  const cardsLoad = useLoad(() => api.cards(id), [id])
  const [cardsState, setCards] = useState<{ moduleId: string; list: Card[] } | null>(null)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => setCards(cardsLoad.data ? { moduleId: id, list: cardsLoad.data } : null), [cardsLoad.data])
  const cards = cardsState && cardsState.moduleId === v?.module.id ? cardsState.list : []
  const addCard = (c: Card) =>
    setCards((prev) => (prev ? { ...prev, list: [...prev.list.filter((x) => x.id !== c.id), c] } : prev))
  const navigate = useNavigate()
  const location = useLocation()
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
  // A quiz to retry, or a notes tab to open, arrives once in the link's state. It is taken into local state and the
  // history entry is cleared, so a reload, Back or any re-render cannot jump to the quiz again.
  const [retry, setRetry] = useState<string | undefined>(undefined)
  useEffect(() => setRetry(undefined), [id])
  useEffect(() => {
    const linkState = location.state as LessonLinkState
    if (!linkState?.quiz && !linkState?.notes) return
    if (linkState.quiz) setRetry(linkState.quiz)
    if (linkState.notes) {
      setTab(linkState.notes)
      setPanelOpen(true)
    }
    navigate(location.pathname, { replace: true, state: null })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.key])
  const code = useMemo(
    () => ({
      codeRunner,
      run: (source: string) =>
        navigate('/playground', { state: { code: source, lesson: v ? { id: v.module.id, code: v.module.code } : undefined } }),
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [codeRunner, navigate, v?.module.id, v?.module.code],
  )
  // One plugin list per section and set of highlights: the plugin walks the whole tree, so don't rebuild it per render.
  const highlights = notes?.highlights
  const pluginsFor = useMemo(() => {
    const cache = new Map<string, ReturnType<typeof highlightPlugin>[]>()
    return (section: string | null) => {
      const key = section ?? '' // a normalised section is never empty
      if (!cache.has(key)) cache.set(key, [highlightPlugin(highlights ?? [], section)])
      return cache.get(key)!
    }
  }, [highlights])

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
  const lessonReady = v?.module.id === id && s.parts.length > 0
  useEffect(() => {
    if (!retry || !lessonReady || s.mode !== 'step') return
    const i = s.parts.findIndex((p) => quizQuestions(p.body).includes(retry))
    if (i >= 0) s.setStep(i)
    else setRetry(undefined) // the quiz is no longer in the lesson
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [retry, lessonReady, s.parts, s.mode])
  const active = useActiveHeading(headings.map(slugify), s.mode === 'all' ? v?.lesson : undefined)
  // Parts ticked off, saved on the server so phone and desktop agree; updated optimistically.
  const [partsDone, setPartsDone] = useState<Set<string>>(new Set())
  useEffect(() => setPartsDone(new Set(v?.partsDone ?? [])), [v])

  if (!v) return <div className="container"><Status error={error} /></div>
  const m = v.module
  const noteCount = (notes?.highlights.length ?? 0) + (notes?.snippets.length ?? 0)

  const reporter: QuizReporter = {
    focus: retry,
    onFocused: () => setRetry(undefined),
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
        code: spec.code?.length ? spec.code.join('\n') : null,
      })
      if (!result) return null
      setNotes((prev) => (prev && prev.moduleId === m.id ? { ...prev, mistakes: upsertMistake(prev.mistakes, result) } : prev))
      return chosen.correct ? 'fixed' : 'saved'
    },
  }
  const saveHighlight = async (quote: string, section: string | null, prefix: string | null, note: string | null,
    card?: CardDraft) => {
    const saved = await api.addHighlight({ moduleId: m.id, section, quote, prefix, note })
    setNotes((prev) => (prev && prev.moduleId === m.id ? { ...prev, highlights: [...prev.highlights, saved] } : prev))
    if (!card) return
    try {
      addCard(await api.makeCard({ sourceKind: 'HIGHLIGHT', sourceId: saved.id, front: card.front, back: card.back || null }))
    } catch {
      // The highlight is saved; show it in the notes panel, where its Make flashcard button tries again.
      setFocusId(saved.id)
      setTab('highlights')
      openPanel(true)
    }
  }
  // At the end of the lesson: below everything in full mode, on the last part in one-part-at-a-time mode.
  const atEnd = s.mode === 'all' || !s.usable || s.step === s.parts.length - 1
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
          <LessonCodeContext.Provider value={code}>
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
              remarkPlugins={REMARK_PLUGINS}
              rehypePlugins={pluginsFor(sectionFor(md))}
              components={LESSON_COMPONENTS}
            >
              {md}
            </Markdown>
              )}
            />
          </div>
          <ExplainIt
            key={m.id}
            moduleId={m.id}
            code={m.code}
            title={m.title}
            hidden={!atEnd}
            onSaved={(e) => {
              setNotes((prev) => (prev && prev.moduleId === m.id
                ? { ...prev, explanations: [...prev.explanations.filter((x) => x.id !== e.id), e] } : prev))
              cardsLoad.reload()
            }}
          />
          </LessonCodeContext.Provider>
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
          key={m.id}
          moduleId={m.id}
          code={m.code}
          data={notes}
          cards={cards}
          onCardMade={addCard}
          reload={notesLoad.reload}
          onSummarySaved={(summary) => setNotes((prev) => (prev && prev.moduleId === m.id ? { ...prev, summary } : prev))}
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
