// Parses every ```mermaid block in data/answers, data/academy/lessons and data/companies, so a
// broken diagram is caught before it reaches the app (which would show the raw source instead).
// Usage (from frontend/): node scripts/check-diagrams.mjs [file ...]
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join, resolve } from 'node:path'
import mermaid from 'mermaid'

const data = resolve(import.meta.dirname, '../../data')
const walk = (dir) =>
  readdirSync(dir).flatMap((f) => {
    const p = join(dir, f)
    return statSync(p).isDirectory() ? walk(p) : p.endsWith('.md') ? [p] : []
  })
const files = process.argv.length > 2
  ? process.argv.slice(2).map((f) => resolve(f))
  : ['answers', 'academy/lessons', 'companies'].flatMap((d) => walk(join(data, d)))

let blocks = 0
let bad = 0
for (const file of files) {
  const text = readFileSync(file, 'utf8').replace(/\r\n/g, '\n')
  for (const m of text.matchAll(/^```mermaid\n([\s\S]*?)^```/gm)) {
    blocks++
    try {
      await mermaid.parse(m[1])
    } catch (e) {
      bad++
      const line = text.slice(0, m.index).split('\n').length
      console.log(`${file}:${line}: ${String(e.message ?? e).split('\n').slice(0, 4).join(' | ')}`)
    }
  }
}
console.log(`${blocks} diagrams in ${files.length} files, ${bad} broken`)
process.exit(bad ? 1 : 0)
