/** Colour theme: auto follows the device; paper is sepia for long reading. Stored per browser. */
export type Theme = 'auto' | 'light' | 'dark' | 'paper'
export const THEMES: Theme[] = ['auto', 'light', 'dark', 'paper']
const KEY = 'cwp.theme'

export function savedTheme(): Theme {
  try {
    const t = localStorage.getItem(KEY) as Theme | null
    return t && THEMES.includes(t) ? t : 'auto'
  } catch {
    return 'auto'
  }
}

/** Sets data-theme on <html> (styles.css keys every token off it) and remembers the choice. */
export function applyTheme(theme: Theme) {
  if (theme === 'auto') document.documentElement.removeAttribute('data-theme')
  else document.documentElement.setAttribute('data-theme', theme)
  try {
    localStorage.setItem(KEY, theme)
  } catch {
    /* private window: lasts for this page only */
  }
}
