import { useCallback, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { CheckCircle2, Loader2, Play, XCircle } from 'lucide-react'
import { api, type PlaygroundResult } from '../api'
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

/** Free-form Java: any public class with a main method, optional stdin. */
export default function PlaygroundPage() {
  const location = useLocation()
  const incoming = (location.state as { code?: string } | null)?.code
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
            <button className="btn sm ghost" onClick={() => change(DEFAULT)}>
              New file
            </button>
            <button className="btn sm primary" onClick={run} disabled={running}>
              {running ? <Loader2 size={14} className="spin" /> : <Play size={14} />} Run
            </button>
          </div>
          <div className="ws-editor">
            <CodeEditor value={code} onChange={change} onRun={run} />
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
    </div>
  )
}
