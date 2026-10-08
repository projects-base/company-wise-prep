import { createContext, useContext } from 'react'

/** What this server can do. The hosted (free-tier) app has no code runner; the local app does. */
export interface Features {
  codeRunner: boolean
}

export const FeaturesContext = createContext<Features>({ codeRunner: true })
export const useFeatures = () => useContext(FeaturesContext)

export const LOCAL_ONLY_NOTE = 'Running code is available in the local app only — start it with .\\run.ps1'
