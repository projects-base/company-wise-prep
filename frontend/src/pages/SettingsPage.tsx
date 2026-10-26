import { useState } from 'react'
import { Check, ExternalLink, KeyRound, Lock, PlugZap, Trash2, X } from 'lucide-react'
import { api, type ProviderSettings, type Settings, type ProviderTest } from '../api'
import { useFeatures } from '../features'
import { Status, useLoad } from '../ui'

const REVIEWERS: { id: Settings['reviewer']; label: string; hint: string }[] = [
  { id: 'auto', label: 'Automatic', hint: 'Claude if it has a key, else Gemini, else the offline check' },
  { id: 'claude', label: 'Claude', hint: 'Only Claude' },
  { id: 'gemini', label: 'Gemini', hint: 'Only Gemini' },
  { id: 'both', label: 'Both, side by side', hint: 'A second opinion on every answer (two requests)' },
  { id: 'offline', label: 'Offline only', hint: 'Free and private key-term check' },
]

/**
 * Keys for the answer reviewers. A key is sent once, when saved, and never shown again: the
 * server returns only whether it is set and its last four characters.
 */
export default function SettingsPage() {
  const { data, error, reload } = useLoad(() => api.settings(), [])
  const { setReviewer } = useFeatures()
  const [saving, setSaving] = useState(false)

  if (!data) return <div className="container narrow"><Status error={error} /></div>

  const save = async (patch: Parameters<typeof api.saveSettings>[0]) => {
    setSaving(true)
    try {
      const s = await api.saveSettings(patch)
      setReviewer(s.effective)
      reload()
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container narrow">
      <header className="page-head">
        <div>
          <div className="eyebrow">Settings</div>
          <h1 style={{ marginTop: 6 }}>Answer reviewers</h1>
          <p className="sub">
            Who judges your spoken theory answers. Now using: <strong>{label(data.effective)}</strong>
          </p>
        </div>
      </header>

      <section className="card pad">
        <h2 className="settings-h">Reviewer</h2>
        <div className="reviewer-grid" role="radiogroup">
          {REVIEWERS.map((r) => (
            <button
              key={r.id}
              role="radio"
              aria-checked={data.reviewer === r.id}
              className="reviewer-opt"
              disabled={saving}
              onClick={() => save({ reviewer: r.id })}
            >
              <strong>{r.label}</strong>
              <span>{r.hint}</span>
            </button>
          ))}
        </div>
      </section>

      <ProviderCard
        id="claude"
        title="Claude (Anthropic)"
        p={data.claude}
        keyUrl="https://console.anthropic.com/settings/keys"
        cost="About ₹3–4 per judged answer with Claude Opus 5.5; switch the model to claude-sonnet-5-5 for about half."
        onSave={save}
      />
      <ProviderCard
        id="gemini"
        title="Gemini (Google)"
        p={data.gemini}
        keyUrl="https://aistudio.google.com/apikey"
        cost="Google AI Studio has a free tier with daily limits; paid use is billed by Google."
        onSave={save}
      />

      <p className="hint settings-privacy">
        <Lock size={13} style={{ verticalAlign: -2 }} /> Keys are stored{' '}
        {data.encrypted ? 'encrypted (AES-GCM) in the app database' : "in this computer's local app database (git-ignored)"}, are never sent
        back to the browser, and are used only to send your answer and the model answer for review. A key saved here wins over
        ANTHROPIC_API_KEY / GEMINI_API_KEY in the environment.
      </p>
    </div>
  )
}

function ProviderCard({ id, title, p, keyUrl, cost, onSave }: {
  id: 'claude' | 'gemini'
  title: string
  p: ProviderSettings
  keyUrl: string
  cost: string
  onSave: (patch: Parameters<typeof api.saveSettings>[0]) => Promise<void>
}) {
  const [key, setKey] = useState('')
  const [model, setModel] = useState(p.model)
  const [test, setTest] = useState<ProviderTest | null>(null)
  const [fallbacks, setFallbacks] = useState(p.fallbacks.join(', '))
  const [testing, setTesting] = useState(false)
  const keyField = id === 'claude' ? 'claudeKey' : 'geminiKey'
  const modelField = id === 'claude' ? 'claudeModel' : 'geminiModel'

  const runTest = async () => {
    setTesting(true)
    setTest(null)
    try {
      setTest(await api.testProvider(id))
    } catch (e) {
      setTest({ ok: false, model: p.model, message: e instanceof Error ? e.message : 'Test failed', checks: null })
    } finally {
      setTesting(false)
    }
  }

  return (
    <section className="card pad provider-card">
      <div className="row" style={{ justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
        <h2 className="settings-h" style={{ margin: 0 }}>
          {title}
        </h2>
        {p.keySet ? (
          <span className="pill ok">
            <Check size={12} /> Key {p.keyHint} {p.keySource === 'environment' ? '(from environment)' : ''}
          </span>
        ) : (
          <span className="pill">No key</span>
        )}
      </div>

      <label className="field">
        <span>
          <KeyRound size={14} /> API key
        </span>
        <div className="row" style={{ gap: 8 }}>
          <input
            type="password"
            autoComplete="off"
            spellCheck={false}
            value={key}
            onChange={(e) => setKey(e.target.value)}
            placeholder={p.keySet ? 'Paste a new key to replace it' : 'Paste your key'}
          />
          <button
            className="btn primary"
            disabled={!key.trim()}
            onClick={async () => {
              await onSave({ [keyField]: key.trim() })
              setKey('')
              setTest(null)
            }}
          >
            Save
          </button>
          {p.keySource === 'settings' && (
            <button className="btn ghost" title="Remove the saved key" onClick={() => onSave({ [keyField]: '' })}>
              <Trash2 size={14} />
            </button>
          )}
        </div>
        <a href={keyUrl} target="_blank" rel="noreferrer" className="field-hint">
          Get a key <ExternalLink size={12} />
        </a>
      </label>

      <label className="field">
        <span>Model</span>
        <div className="row" style={{ gap: 8 }}>
          <input value={model} onChange={(e) => setModel(e.target.value)} spellCheck={false} />
          <button className="btn" disabled={model.trim() === p.model} onClick={() => onSave({ [modelField]: model.trim() })}>
            Save
          </button>
          {p.model !== p.defaultModel && (
            <button className="btn ghost" onClick={() => { setModel(p.defaultModel); void onSave({ [modelField]: '' }) }}>
              Reset
            </button>
          )}
        </div>
        <span className="field-hint muted">Default: {p.defaultModel}. {cost}</span>
      </label>

      {id === 'gemini' && (
        <label className="field">
          <span>Fallback models, in order</span>
          <div className="row" style={{ gap: 8 }}>
            <input value={fallbacks} onChange={(e) => setFallbacks(e.target.value)} spellCheck={false} placeholder="model-a, model-b, model-c" />
            <button className="btn" disabled={fallbacks.trim() === p.fallbacks.join(', ')} onClick={() => onSave({ geminiFallbacks: fallbacks })}>
              Save
            </button>
            {p.fallbacks.join(',') !== p.defaultFallbacks.join(',') && (
              <button className="btn ghost" onClick={() => { setFallbacks(p.defaultFallbacks.join(', ')); void onSave({ geminiFallbacks: '' }) }}>
                Reset
              </button>
            )}
          </div>
          <span className="field-hint muted">
            When {p.model} is busy, out of quota or failing, these are tried one by one, so a review or transcript still comes back.
          </span>
        </label>
      )}

      <div className="row" style={{ gap: 10, flexWrap: 'wrap' }}>
        <button className="btn" disabled={!p.keySet || testing} onClick={runTest}>
          <PlugZap size={14} /> {testing ? 'Testing every model…' : 'Test connection'}
        </button>
        {test && (
          <span className={`test-result ${test.ok ? 'ok' : 'bad'}`}>
            {test.ok ? <Check size={14} /> : <X size={14} />} {test.message}
          </span>
        )}
      </div>
      {test?.checks && (
        <ol className="model-checks">
          {test.checks.map((c, i) => (
            <li key={c.model} className={c.ok ? 'ok' : 'bad'}>
              {c.ok ? <Check size={13} /> : <X size={13} />}
              <code>{c.model}</code>
              <span className="muted">{i === 0 ? 'main' : `fallback ${i}`}</span>
              <span className="muted num">{(c.millis / 1000).toFixed(1)} s</span>
              {!c.ok && <span className="model-check-why">{c.message.replace(/^Gemini \([^)]*\) returned /, '')}</span>}
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}

function label(effective: string) {
  return { claude: 'Claude', gemini: 'Gemini', both: 'Claude and Gemini', offline: 'the offline check' }[effective] ?? effective
}
