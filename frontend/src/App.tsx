import { createContext, useContext, useEffect, useState } from 'react'
import { NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { Building2, CalendarCheck2, CalendarRange, Code2, GraduationCap, Library } from 'lucide-react'
import { api, type CampaignConfig } from './api'
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

  useEffect(() => {
    api.campaigns().then(setCampaigns).catch(() => setCampaigns([]))
  }, [])
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
