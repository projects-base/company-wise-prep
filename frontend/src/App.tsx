import { createContext, useContext, useEffect, useState } from 'react'
import { NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { Building2, CalendarCheck2, CalendarRange, Code2, GraduationCap, Library } from 'lucide-react'
import { api, LOGGED_OUT_EVENT, type CampaignConfig } from './api'
import LoginPage from './pages/LoginPage'
import { daysBetween, localToday } from './ui'
import TodayPage from './pages/TodayPage'
import PlanPage from './pages/PlanPage'
import BankPage from './pages/BankPage'
import QuestionPage from './pages/QuestionPage'
import CompaniesPage from './pages/CompaniesPage'
import CompanyPage from './pages/CompanyPage'
import AcademyPage from './pages/AcademyPage'
import ModulePage from './pages/ModulePage'
import PlaygroundPage from './pages/PlaygroundPage'

const CampaignContext = createContext<CampaignConfig | null>(null)
export const useCampaign = () => useContext(CampaignContext)

const STORAGE_KEY = 'cwp.campaign'

const NAV = [
  { to: '/', label: 'Today', icon: CalendarCheck2, end: true },
  { to: '/plan', label: 'Plan', icon: CalendarRange },
  { to: '/academy', label: 'Academy', icon: GraduationCap },
  { to: '/bank', label: 'Question bank', icon: Library },
  { to: '/playground', label: 'Playground', icon: Code2 },
  { to: '/companies', label: 'Companies', icon: Building2 },
]

export default function App() {
  const [campaigns, setCampaigns] = useState<CampaignConfig[]>([])
  const [selected, setSelected] = useState<string | null>(() => {
    try {
      return localStorage.getItem(STORAGE_KEY)
    } catch {
      return null
    }
  })
  const { pathname } = useLocation()
  // 'checking' until the server says whether a login is needed (only on the hosted app).
  const [auth, setAuth] = useState<'checking' | 'login' | 'ok' | 'down'>('checking')

  useEffect(() => {
    api
      .authStatus()
      .then((s) => setAuth(s.authRequired && !s.authenticated ? 'login' : 'ok'))
      .catch(() => setAuth('down'))
    const onLoggedOut = () => setAuth('login')
    window.addEventListener(LOGGED_OUT_EVENT, onLoggedOut)
    return () => window.removeEventListener(LOGGED_OUT_EVENT, onLoggedOut)
  }, [])

  useEffect(() => {
    if (auth === 'ok') api.campaigns().then(setCampaigns).catch(() => setCampaigns([]))
  }, [auth])
  // Braces matter: newer Chromium returns a Promise from scrollTo, which React would treat as a cleanup.
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  const campaign = campaigns.find((c) => c.id === selected) ?? campaigns[0] ?? null
  const choose = (id: string) => {
    setSelected(id)
    try {
      localStorage.setItem(STORAGE_KEY, id)
    } catch {
      /* private mode: just not remembered */
    }
  }
  if (auth === 'checking') return <p className="loading" style={{ padding: 40 }}>Loading…</p>
  if (auth === 'login') return <LoginPage onLoggedIn={() => setAuth('ok')} />
  if (auth === 'down') {
    return (
      <div className="login">
        <div className="card raised login-card">
          <h1 style={{ fontSize: '1.3rem' }}>Can't reach the server</h1>
          <p className="muted" style={{ marginTop: 8 }}>It may be starting up or redeploying. Try again in a minute.</p>
          <button className="btn primary" style={{ marginTop: 16 }} onClick={() => window.location.reload()}>
            Retry
          </button>
        </div>
      </div>
    )
  }

  const daysLeft = campaign ? Math.max(0, daysBetween(localToday(), campaign.interview)) : null

  return (
    <CampaignContext.Provider value={campaign}>
      <div className="shell">
        <aside className="sidebar">
          <div className="brand">
            <span className="brand-mark">CW</span>
            <span className="brand-name">CompanyWisePrep</span>
          </div>
          <nav className="nav" aria-label="Main">
            {NAV.map(({ to, label, icon: Icon, end }) => (
              <NavLink key={to} to={to} end={end} title={label}>
                <Icon size={18} strokeWidth={1.9} />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>
          {campaign && (
            <div className="sidebar-foot">
              <div className="eyebrow">Interview in</div>
              <div className="big num">{daysLeft} days</div>
              <div className="muted" style={{ fontSize: '0.82rem', marginTop: 2 }}>
                {campaign.name}
              </div>
              {campaigns.length > 1 && (
                <select value={campaign.id} onChange={(e) => choose(e.target.value)} aria-label="Campaign">
                  {campaigns.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              )}
            </div>
          )}
        </aside>
        <main className={`main ${pathname.startsWith('/q/') ? 'main-tight' : ''}`}>
          <Routes>
            <Route path="/" element={<TodayPage />} />
            <Route path="/plan" element={<PlanPage />} />
            <Route path="/academy" element={<AcademyPage />} />
            <Route path="/academy/:id" element={<ModulePage />} />
            <Route path="/bank" element={<BankPage />} />
            <Route path="/q/:slug" element={<QuestionPage />} />
            <Route path="/playground" element={<PlaygroundPage />} />
            <Route path="/companies" element={<CompaniesPage />} />
            <Route path="/companies/:slug" element={<CompanyPage />} />
          </Routes>
        </main>
      </div>
    </CampaignContext.Provider>
  )
}
