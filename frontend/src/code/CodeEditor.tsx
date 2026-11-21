import { useEffect, useState } from 'react'
import CodeMirror, { keymap, type Extension, type ViewUpdate } from '@uiw/react-codemirror'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import { Prec } from '@codemirror/state'

function usePrefersDark() {
  const query = '(prefers-color-scheme: dark)'
  const [dark, setDark] = useState(() => window.matchMedia(query).matches)
  useEffect(() => {
    const m = window.matchMedia(query)
    const on = (e: MediaQueryListEvent) => setDark(e.matches)
    m.addEventListener('change', on)
    return () => m.removeEventListener('change', on)
  }, [])
  return dark
}

/** Java editor that follows the system theme. Ctrl/Cmd+Enter runs, +Shift submits. `onSelect` gets the selected text. */
export default function CodeEditor({
  value,
  onChange,
  onRun,
  onSubmit,
  onSelect,
  height = '100%',
}: {
  value: string
  onChange: (v: string) => void
  onRun?: () => void
  onSubmit?: () => void
  onSelect?: (selected: string) => void
  height?: string
}) {
  const dark = usePrefersDark()
  const extensions: Extension[] = [
    java(),
    Prec.highest(
      keymap.of([
        { key: 'Mod-Enter', run: () => (onRun?.(), true) },
        { key: 'Mod-Shift-Enter', run: () => (onSubmit?.(), true) },
      ]),
    ),
  ]
  return (
    <CodeMirror
      className="editor"
      value={value}
      height={height}
      theme={dark ? oneDark : 'light'}
      extensions={extensions}
      onChange={onChange}
      onUpdate={onSelect && ((u: ViewUpdate) => {
        if (!u.selectionSet) return
        const { from, to } = u.state.selection.main
        onSelect(u.state.sliceDoc(from, to))
      })}
      basicSetup={{ tabSize: 4, foldGutter: true, highlightActiveLine: true, autocompletion: true }}
      indentWithTab
    />
  )
}
