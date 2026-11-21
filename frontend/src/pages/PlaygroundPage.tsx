import { useCallback, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { BookmarkPlus, CheckCircle2, Loader2, Play, X, XCircle } from 'lucide-react'
import { api, type PlaygroundResult } from '../api'
import { NOTE_MAX } from '../LessonNotes'
import CodeEditor from '../code/CodeEditor'
import { VERDICT_LABEL } from '../code/Workspace'
import { LOCAL_ONLY_NOTE, useFeatures } from '../features'

const KEY = 'cwp.playground'
const DEFAULT = `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String name = in.hasNextLine() ? in.nextLine() : "world";
        System.out.println("Hello, " + name + "!");
    }
}
`

/** The lesson a Run button opened the Playground from, so "Save to notes" files the snippet under it. */
type FromLesson = { id: string; code: string }
const CODE_MAX = 20_000

/** Free-form Java: any public class with a main method, optional stdin. */
export default function PlaygroundPage() {
  const location = useLocation()
  const state = location.state as { code?: string; lesson?: FromLesson } | null
  const incoming = state?.code
  const [lesson, setLesson] = useState<FromLesson | null>(state?.lesson ?? null)
  const [selected, setSelected] = useState('')
  const [saving, setSaving] = useState(false)
  const [code, setCode] = useState(() => {
    if (incoming) return incoming
    try {
      return localStorage.getItem(KEY) ?? DEFAULT
    } catch {
      return DEFAULT
    }
  })
  const [stdin, setStdin] = useState('')
  const [result, setResult] = useState<PlaygroundResult | null>(null)
  const [running, setRunning] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const change = (v: string) => {
    setCode(v)
    try {
      localStorage.setItem(KEY, v)
    } catch {
      /* not remembered in private mode */
    }
  }

  const { codeRunner } = useFeatures()

  const run = useCallback(async () => {
    if (running) return
    setRunning(true)
    setError(null)
    try {
      setResult(await api.playground(code, stdin))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setRunning(false)
    }
  }, [code, stdin, running])

  if (!codeRunner) {
    return (
      <div className="container">
        <div className="callout" style={{ marginTop: 24 }}>
          <span>{LOCAL_ONLY_NOTE}.</span>
        </div>
      </div>
    )
  }

  return (
    <div className="container wide">
      <header className="page-head" style={{ marginBottom: 18 }}>
        <div>
          <div className="eyebrow">Playground</div>
          <h1 style={{ marginTop: 6 }}>Run any Java program</h1>
          <p className="sub">Any public class with a main method. Labs from the Academy open here with one click.</p>
        </div>
      </header>

      <div className="pg">
        <div className="card pg-editor">
          <div className="ws-bar">
            <span className="ws-lang">Java 21</span>
            <span className="spacer" />
            {lesson && (
              <span className="muted pg-from">
                Opened from <Link to={`/academy/${lesson.id}`}>{lesson.code}</Link>
              </span>
            )}
            <button
              className="btn sm ghost"
              onClick={() => {
                change(DEFAULT)
                setLesson(null) // a new file is no longer the lesson's lab
              }}
            >
              New file
            </button>
            <button className="btn sm" onClick={() => setSaving(true)} aria-haspopup="dialog">
              <BookmarkPlus size={14} /> Save to notes
            </button>
            <button className="btn sm primary" onClick={run} disabled={running}>
              {running ? <Loader2 size={14} className="spin" /> : <Play size={14} />} Run
            </button>
          </div>
          <div className="ws-editor">
            <CodeEditor value={code} onChange={change} onRun={run} onSelect={setSelected} />
          </div>
        </div>

        <div className="pg-side">
          <div className="card pad">
            <label className="io-label" style={{ marginTop: 0 }}>
              Standard input
            </label>
            <textarea className="io" rows={5} value={stdin} onChange={(e) => setStdin(e.target.value)} spellCheck={false} placeholder="Optional" />
          </div>
          <div className="card pad pg-out">
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <span className="eyebrow">Output</span>
              {result && (
                <span className={`verdict-inline ${result.verdict === 'ACCEPTED' ? 'ok' : 'bad'}`}>
                  {result.verdict === 'ACCEPTED' ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                  {result.verdict === 'ACCEPTED' ? `Exited normally · ${result.millis} ms` : VERDICT_LABEL[result.verdict]}
                </span>
              )}
            </div>
            {running && <p className="muted">Compiling and running…</p>}
            {error && <p className="error">{error}</p>}
            {result && !running && (
              <>
                {result.compileErrors.length > 0 && (
                  <pre className="io err">{result.compileErrors.map((e) => `${e.file}:${e.line}: ${e.message}`).join('\n\n')}</pre>
                )}
                {result.stdout && <pre className="io">{result.stdout}</pre>}
                {result.stderr && <pre className="io err">{result.stderr}</pre>}
                {!result.stdout && !result.stderr && result.compileErrors.length === 0 && <p className="muted">(no output)</p>}
                {result.truncated && <p className="hint">Output was cut at 64 KB.</p>}
              </>
            )}
            {!result && !running && !error && <p className="muted">Press Run or Ctrl+Enter.</p>}
          </div>
        </div>
      </div>
      {saving && (
        <SaveToNotes
          code={code}
          selected={selected}
          output={outputText(result)}
          lesson={lesson}
          onClose={() => setSaving(false)}
        />
      )}
    </div>
  )
}

/** What the last run printed, as one text (compile errors, stdout, stderr), or null before a run. */
function outputText(r: PlaygroundResult | null): string | null {
  if (!r) return null
  const parts = [
    r.compileErrors.map((e) => `${e.file}:${e.line}: ${e.message}`).join('\n'),
    r.stdout,
    r.stderr,
  ].filter((s) => s && s.trim())
  return parts.length ? parts.join('\n').slice(0, CODE_MAX) : null
}

function SaveToNotes({ code, selected, output, lesson, onClose }: {
  code: string
  selected: string
  output: string | null
  lesson: FromLesson | null
  onClose: () => void
}) {
  const hasSelection = selected.trim().length > 0
  const [what, setWhat] = useState<'selection' | 'file'>(hasSelection ? 'selection' : 'file')
  const [withOutput, setWithOutput] = useState(output !== null)
  const [note, setNote] = useState('')
  const [state, setState] = useState<'idle' | 'saving' | 'saved'>('idle')
  const [error, setError] = useState<string | null>(null)
  const snippet = what === 'selection' && hasSelection ? selected : code
  const tooLong = snippet.length > CODE_MAX

  const save = async () => {
    setState('saving')
    setError(null)
    try {
      await api.addSnippet({
        moduleId: lesson?.id ?? null,
        code: snippet,
        output: withOutput ? output : null,
        note: note.trim() || null,
      })
      setState('saved')
    } catch (e) {
      setError((e as Error).message)
      setState('idle')
    }
  }

  return (
    <div className="card raised pg-save" role="dialog" aria-label="Save to notes">
      <div className="row" style={{ justifyContent: 'space-between' }}>
        <strong>Save to notes</strong>
        <button className="icon-btn" onClick={onClose} aria-label="Close">
          <X size={16} />
        </button>
      </div>
      {state === 'saved' ? (
        <>
          <p className="pg-save-done">
            <CheckCircle2 size={15} /> Saved to {lesson ? lesson.code : 'your general notes'}.
          </p>
          <div className="row">
            <Link to="/notes" className="btn sm">Open my notes</Link>
            <button className="btn sm ghost" onClick={onClose}>Close</button>
          </div>
        </>
      ) : (
        <form
          onSubmit={(e) => {
            e.preventDefault()
            save()
          }}
        >
          <fieldset className="pg-save-what">
            <legend className="io-label">What to save</legend>
            <label>
              <input type="radio" name="what" checked={what === 'selection'} disabled={!hasSelection}
                onChange={() => setWhat('selection')} />
              Selected code{!hasSelection && <span className="muted"> (select some first)</span>}
            </label>
            <label>
              <input type="radio" name="what" checked={what === 'file'} onChange={() => setWhat('file')} /> Whole file
            </label>
            <label>
              <input type="checkbox" checked={withOutput} disabled={output === null}
                onChange={(e) => setWithOutput(e.target.checked)} />
              {output === null ? 'Output (run it first)' : <>Output: <code>{output.split('\n')[0].slice(0, 40)}</code></>}
            </label>
          </fieldset>
          <label className="io-label" htmlFor="pg-note">Your note</label>
          <textarea id="pg-note" rows={3} maxLength={NOTE_MAX} value={note} onChange={(e) => setNote(e.target.value)}
            placeholder="What did this show you?" />
          <p className="hint">Goes to {lesson ? lesson.code : 'general notes (not opened from a lesson)'}</p>
          {tooLong && <p className="error">That is more than 20,000 characters; select less.</p>}
          {error && <p className="error">Not saved: {error}</p>}
          <div className="row" style={{ marginTop: 10 }}>
            <button type="submit" className="btn sm primary" disabled={state === 'saving' || tooLong || !snippet.trim()}>
              {state === 'saving' ? <Loader2 size={14} className="spin" /> : <BookmarkPlus size={14} />} Save
            </button>
          </div>
        </form>
      )}
    </div>
  )
}
