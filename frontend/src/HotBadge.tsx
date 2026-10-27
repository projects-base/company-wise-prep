import { useEffect, useState } from 'react'
import { Flame } from 'lucide-react'
import { api, type Hot } from './api'

/**
 * "Hot at Google · Amazon +3": which big product companies LeetCode tagged a problem for in the
 * last six months. Loaded once per page load and shared; renders nothing until it arrives, or if
 * GitHub can't be reached.
 */
let pending: Promise<Hot | null> | null = null
const load = () => (pending ??= api.hot().catch(() => null))

export function useHot() {
  const [hot, setHot] = useState<Hot | null>(null)
  useEffect(() => {
    let live = true
    void load().then((h) => live && setHot(h))
    return () => {
      live = false
    }
  }, [])
  return hot
}

export function HotBadge({ slug, max = 2 }: { slug: string; max?: number }) {
  const hot = useHot()
  const tags = hot?.tags[slug]
  if (!tags?.length) return null
  const names = tags.map((t) => t.company)
  return (
    <span
      className="hot-badge"
      title={`LeetCode tagged this for ${names.join(', ')} in the last six months (frequency: ${tags.map((t) => `${t.company} ${t.frequency}`).join(', ')}).`}
    >
      <Flame size={12} /> {names.slice(0, max).join(' · ')}
      {names.length > max && <b>+{names.length - max}</b>}
    </span>
  )
}
