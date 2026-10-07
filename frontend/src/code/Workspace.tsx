import { useCallback, useEffect, useRef, useState } from 'react'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import remarkBreaks from 'remark-breaks'
import { CheckCircle2, Eye, Loader2, Play, RotateCcw, Send, XCircle } from 'lucide-react'
import { api, type Challenge, type JudgeResult, type Verdict } from '../api'
import CodeEditor from './CodeEditor'

export const VERDICT_LABEL: Record<Verdict, string> = {
  ACCEPTED: 'Accepted',
  WRONG_ANSWER: 'Wrong answer',
  RUNTIME_ERROR: 'Runtime error',
  TIME_LIMIT: 'Time limit exceeded',
  COMPILE_ERROR: 'Compile error',
}

type Pane = 'problem' | 'code' | 'console'

/**
 * LeetCode-style workspace: statement left, editor right, console below the editor.
 * Below 1100px the three become tabs.
 */
export default function Workspace({
  challenge,
  header,
  extra,
  onAccepted,
}: {
  challenge: Challenge
  header: React.ReactNode
  extra?: React.ReactNode
  onAccepted: () => void
}) {
  const [code, setCode] = useState(challenge.savedCode ?? challenge.starter)
  const [result, setResult] = useState<JudgeResult | null>(null)
  const [running, setRunning] = useState<'RUN' | 'SUBMIT' | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [consoleTab, setConsoleTab] = useState<'tests' | 'result'>('tests')
  const [exampleIdx, setExampleIdx] = useState(0)
  const [custom, setCustom] = useState<string | null>(null)
  const [pane, setPane] = useState<Pane>('problem')
  const [solution, setSolution] = useState<string | null>(null)
  const saveTimer = useRef<number | undefined>(undefined)

  // Autosave: one second after typing stops.
  useEffect(() => {
    if (code === (challenge.savedCode ?? challenge.starter)) return
    window.clearTimeout(saveTimer.current)
    saveTimer.current = window.setTimeout(() => void api.saveDraft(challenge.slug, code), 1000)
    return () => window.clearTimeout(saveTimer.current)
  }, [code, challenge])

  const go = useCallback(
    async (mode: 'RUN' | 'SUBMIT') => {
      if (running) return
      setRunning(mode)
      setError(null)
      setConsoleTab('result')
      setPane((p) => (p === 'problem' ? 'console' : p === 'code' ? 'console' : p))
      try {
        const r = await api.runCode(challenge.slug, code, mode, mode === 'RUN' && custom !== null ? custom : undefined)
        setResult(r)
        if (mode === 'SUBMIT' && r.verdict === 'ACCEPTED') onAccepted()
      } catch (e) {
        setError((e as Error).message)
      } finally {
        setRunning(null)
      }
    },
    [challenge.slug, code, custom, running, onAccepted],
  )

  const reset = () => {
    if (confirmReset()) setCode(challenge.starter)
  }
  const showSolution = async () => {
    if (solution) return setSolution(null)
    setSolution((await api.solution(challenge.slug)).code)
  }

  return (
    <div className="ws">
      <div className="ws-tabs" role="tablist">
        {(['problem', 'code', 'console'] as Pane[]).map((p) => (
          <button key={p} role="tab" aria-selected={pane === p} onClick={() => setPane(p)}>
            {p === 'problem' ? 'Problem' : p === 'code' ? 'Code' : 'Result'}
          </button>
        ))}
      </div>

      <section className={`ws-problem ${pane === 'problem' ? 'show' : ''}`}>
        {header}
        <div className="prose ws-statement">
          <Markdown remarkPlugins={[remarkGfm, remarkBreaks]}>{challenge.problem}</Markdown>
        </div>
        {extra}
        {solution && (
          <div className="ws-solution">
            <div className="row" style={{ justifyContent: 'space-between', marginBottom: 8 }}>
              <span className="eyebrow">Reference solution</span>
              <button className="btn sm ghost" onClick={() => setSolution(null)}>
                Hide
              </button>
            </div>
            <pre className="ws-pre">{solution}</pre>
          </div>
        )}
      </section>

      <section className={`ws-code ${pane === 'code' ? 'show' : ''}`}>
        <div className="ws-bar">
          <span className="ws-lang">Java 21</span>
          <span className="spacer" />
          <button className="btn sm ghost" onClick={reset} title="Reset to the starter code">
            <RotateCcw size={14} /> Reset
          </button>
          <button className="btn sm ghost" onClick={showSolution} title="Show the reference solution">
            <Eye size={14} /> {solution ? 'Hide solution' : 'Solution'}
          </button>
        </div>
        <div className="ws-editor">
          <CodeEditor value={code} onChange={setCode} onRun={() => go('RUN')} onSubmit={() => go('SUBMIT')} />
        </div>
        <div className="ws-actions">
          <span className="muted ws-keys">Ctrl+Enter run · Ctrl+Shift+Enter submit</span>
          <span className="spacer" />
          <button className="btn" onClick={() => go('RUN')} disabled={!!running}>
            {running === 'RUN' ? <Loader2 size={15} className="spin" /> : <Play size={15} />} Run
          </button>
          <button className="btn primary" onClick={() => go('SUBMIT')} disabled={!!running}>
            {running === 'SUBMIT' ? <Loader2 size={15} className="spin" /> : <Send size={15} />} Submit
          </button>
        </div>
      </section>

      <section className={`ws-console ${pane === 'console' ? 'show' : ''}`}>
        <div className="ws-console-tabs">
          <button aria-selected={consoleTab === 'tests'} onClick={() => setConsoleTab('tests')}>
            Test cases
          </button>
          <button aria-selected={consoleTab === 'result'} onClick={() => setConsoleTab('result')}>
            Result
          </button>
        </div>
        <div className="ws-console-body">
          {consoleTab === 'tests' ? (
            <Tests
              challenge={challenge}
              idx={exampleIdx}
              setIdx={setExampleIdx}
              custom={custom}
              setCustom={setCustom}
            />
          ) : (
            <Result result={result} running={running} error={error} />
          )}
        </div>
      </section>
    </div>
  )
}

function confirmReset() {
  // An inline confirm keeps the flow simple; the draft is autosaved anyway.
  return window.confirm('Replace your code with the starter? Your current code will be lost.')
}

function Tests({
  challenge,
  idx,
  setIdx,
  custom,
  setCustom,
}: {
  challenge: Challenge
  idx: number
  setIdx: (i: number) => void
  custom: string | null
  setCustom: (s: string | null) => void
}) {
  const ex = challenge.examples[idx]
  return (
    <div>
      <div className="row" style={{ gap: 6, marginBottom: 12 }}>
        {challenge.examples.map((_, i) => (
          <button key={i} className={`chip-btn ${custom === null && i === idx ? 'on' : ''}`} onClick={() => (setCustom(null), setIdx(i))}>
            Case {i + 1}
          </button>
        ))}
        <button className={`chip-btn ${custom !== null ? 'on' : ''}`} onClick={() => setCustom(custom ?? ex?.input ?? '')}>
          Custom input
        </button>
        <span className="spacer" />
        <span className="muted" style={{ fontSize: '0.8rem' }}>
          + {challenge.hiddenCount} hidden on Submit
        </span>
      </div>
      {custom !== null ? (
        <>
          <label className="io-label">Your input (one argument per line)</label>
          <textarea className="io" rows={5} value={custom} onChange={(e) => setCustom(e.target.value)} spellCheck={false} />
          <p className="hint" style={{ marginTop: 6 }}>
            Run uses this input; the expected output comes from the reference solution.
          </p>
        </>
      ) : ex ? (
        <>
          <label className="io-label">Input</label>
          <pre className="io">{ex.input}</pre>
          <label className="io-label">Expected output</label>
          <pre className="io">{ex.expected}</pre>
        </>
      ) : null}
    </div>
  )
}

function Result({ result, running, error }: { result: JudgeResult | null; running: string | null; error: string | null }) {
  const [open, setOpen] = useState(0)
  useEffect(() => {
    if (!result) return
    const firstFail = result.results.findIndex((r) => r.verdict !== 'ACCEPTED')
    setOpen(firstFail >= 0 ? firstFail : 0)
  }, [result])

  if (running) {
    return (
      <div className="empty">
        <Loader2 size={22} className="spin" />
        <strong>{running === 'RUN' ? 'Running the examples…' : 'Judging against every test…'}</strong>
        <span>Each test runs in its own JVM, so the first run takes a moment.</span>
      </div>
    )
  }
  if (error) return <p className="error">{error}</p>
  if (!result) {
    return (
      <div className="empty">
        <Play size={22} />
        <strong>Run your code to see results</strong>
        <span>Run checks the examples. Submit checks every test, including hidden ones.</span>
      </div>
    )
  }

  const ok = result.verdict === 'ACCEPTED'
  const r = result.results[open]
  return (
    <div>
      <div className={`verdict ${ok ? 'ok' : 'bad'}`}>
        {ok ? <CheckCircle2 size={20} /> : <XCircle size={20} />}
        <span>{VERDICT_LABEL[result.verdict]}</span>
        {result.total > 0 && (
          <span className="muted verdict-meta">
            {result.passed}/{result.total} passed · slowest {result.maxMillis} ms
          </span>
        )}
      </div>

      {result.compileErrors.length > 0 && (
        <pre className="io err">
          {result.compileErrors
            .map((e) => `${e.file}${e.line ? `:${e.line}` : ''}: ${e.message}`)
            .join('\n\n')}
        </pre>
      )}

      {result.results.length > 0 && (
        <>
          <div className="row" style={{ gap: 6, margin: '12px 0' }}>
            {result.results.map((t, i) => (
              <button key={i} className={`chip-btn ${i === open ? 'on' : ''} ${t.verdict === 'ACCEPTED' ? 'pass' : 'fail'}`} onClick={() => setOpen(i)}>
                {t.verdict === 'ACCEPTED' ? '✓' : '✗'} {t.hidden ? `Hidden ${i + 1}` : t.name}
              </button>
            ))}
          </div>
          {r &&
            (r.input === null ? (
              <p className="muted" style={{ fontSize: '0.88rem' }}>
                Hidden test — {r.verdict === 'ACCEPTED' ? 'passed' : VERDICT_LABEL[r.verdict].toLowerCase()} in {r.millis} ms.
              </p>
            ) : (
              <>
                <label className="io-label">Input</label>
                <pre className="io">{r.input}</pre>
                <div className="io-grid">
                  <div>
                    <label className="io-label">Your output</label>
                    <pre className={`io ${r.verdict === 'ACCEPTED' ? '' : 'diff-bad'}`}>{r.actual || '(nothing printed)'}</pre>
                  </div>
                  <div>
                    <label className="io-label">Expected</label>
                    <pre className="io">{r.expected}</pre>
                  </div>
                </div>
                {r.stderr && (
                  <>
                    <label className="io-label">{r.verdict === 'RUNTIME_ERROR' ? 'Error' : 'stderr'}</label>
                    <pre className="io err">{r.stderr}</pre>
                  </>
                )}
                <p className="muted" style={{ fontSize: '0.78rem', marginTop: 6 }}>
                  {r.millis} ms, including JVM start-up
                </p>
              </>
            ))}
        </>
      )}
    </div>
  )
}
