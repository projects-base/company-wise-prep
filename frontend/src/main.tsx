import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { HashRouter } from 'react-router-dom'
import App from './App'
import './styles.css'
import { applyTheme, savedTheme } from './theme'

applyTheme(savedTheme()) // before the first paint, so there is no flash of the wrong theme

// HashRouter: Spring only has to serve index.html at "/", no SPA fallback rules needed.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <HashRouter>
      <App />
    </HashRouter>
  </StrictMode>,
)
