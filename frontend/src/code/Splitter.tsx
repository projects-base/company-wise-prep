import { useState, type PointerEvent } from 'react'

/** A size in px remembered across visits; null means "use the CSS default". */
export function useStoredSize(key: string) {
  const [size, setSize] = useState<number | null>(() => {
    try {
      const v = Number(localStorage.getItem(key))
      return v > 0 ? v : null
    } catch {
      return null
    }
  })
  const save = (v: number | null) => {
    setSize(v)
    try {
      if (v === null) localStorage.removeItem(key)
      else localStorage.setItem(key, String(Math.round(v)))
    } catch {
      /* private window: the size just isn't remembered */
    }
  }
  return [size, save] as const
}

/**
 * Drag handle between two panes. `onDrag` gets the pointer position (clientX for a vertical bar,
 * clientY for a horizontal one); double-click restores the default layout.
 */
export function Splitter({
  axis,
  area,
  onDrag,
  onReset,
}: {
  axis: 'x' | 'y'
  area?: string
  onDrag: (pos: number) => void
  onReset: () => void
}) {
  const [dragging, setDragging] = useState(false)
  const down = (e: PointerEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.currentTarget.setPointerCapture(e.pointerId)
    setDragging(true)
  }
  const move = (e: PointerEvent<HTMLDivElement>) => {
    if (dragging) onDrag(axis === 'x' ? e.clientX : e.clientY)
  }
  const up = () => setDragging(false)
  return (
    <div
      className={`splitter splitter-${axis} ${dragging ? 'dragging' : ''}`}
      style={area ? { gridArea: area } : undefined}
      role="separator"
      aria-orientation={axis === 'x' ? 'vertical' : 'horizontal'}
      title="Drag to resize · double-click to reset"
      onPointerDown={down}
      onPointerMove={move}
      onPointerUp={up}
      onPointerCancel={up}
      onDoubleClick={onReset}
    />
  )
}

export const clamp = (v: number, min: number, max: number) => Math.min(Math.max(v, min), Math.max(min, max))
