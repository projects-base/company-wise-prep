import { useState } from 'react'
import { Loader2, Lock } from 'lucide-react'
import { api } from '../api'

export default function LoginPage({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!password || busy) return
    setBusy(true)
    setError(null)
    try {
      const r = await api.login(password)
      if (r.ok) onLoggedIn()
      else setError(r.message)
    } catch {
      setError("Couldn't reach the server. It may be starting up — try again in a moment.")
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login">
      <form className="card raised login-card" onSubmit={submit}>
        <span className="brand-mark" style={{ width: 40, height: 40, borderRadius: 11 }}>
          CW
        </span>
        <h1 style={{ fontSize: '1.4rem', marginTop: 14 }}>CompanyWisePrep</h1>
        <p className="muted" style={{ margin: '6px 0 20px', fontSize: '0.92rem' }}>
          Enter your password to continue.
        </p>
        <label className="sr-only" htmlFor="pw">
          Password
        </label>
        <input
          id="pw"
          type="password"
          autoComplete="current-password"
          autoFocus
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          style={{ width: '100%', height: 42 }}
        />
        {error && (
          <p className="error" style={{ marginTop: 10, fontSize: '0.88rem' }}>
            {error}
          </p>
        )}
        <button className="btn lg primary" type="submit" disabled={busy || !password} style={{ width: '100%', marginTop: 14 }}>
          {busy ? <Loader2 size={16} className="spin" /> : <Lock size={16} />} Sign in
        </button>
      </form>
    </div>
  )
}
