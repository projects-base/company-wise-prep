// Parses every ```mermaid block in data/answers, data/academy/lessons and data/companies, so a
// broken diagram is caught before it reaches the app (which would show the raw source instead).
// Usage (from frontend/): node scripts/check-diagrams.mjs [file ...]
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join, resolve } from 'node:path'
import DOMPurify from 'dompurify'

// mermaid.parse only checks syntax, but it registers DOMPurify hooks, which need a browser window.
// Node has none, so stub the hooks out: parsing still rejects invalid diagrams.
DOMPurify.addHook ??= () => {}
DOMPurify.removeHook ??= () => {}
DOMPurify.removeAllHooks ??= () => {}
DOMPurify.sanitize ??= (s) => s
const { default: mermaid } = await import('mermaid')

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
// ```quiz and ```anim blocks are JSON: a broken one would show raw source. A quiz needs a question, at least two
// options and exactly one correct option (the same rule as parseQuiz in src/Quiz.tsx).
let quizzes = 0
let anims = 0
for (const file of files) {
  const text = readFileSync(file, 'utf8').replace(/\r\n/g, '\n')
  for (const m of text.matchAll(/^```(quiz|anim)\n([\s\S]*?)^```/gm)) {
    const line = text.slice(0, m.index).split('\n').length
    let problem = null
    try {
      const j = JSON.parse(m[2])
      if (m[1] === 'quiz') {
        quizzes++
        const correct = Array.isArray(j.options) ? j.options.filter((o) => o.correct).length : 0
        if (typeof j.question !== 'string' || !Array.isArray(j.options) || j.options.length < 2) problem = 'needs question and 2+ options'
        else if (correct !== 1) problem = `needs exactly one correct option, has ${correct}`
        else if (j.options.some((o) => typeof o.text !== 'string' || typeof o.why !== 'string')) problem = 'every option needs text and why'
        else if (!['easy', 'medium', 'hard'].includes(j.level)) problem = 'needs level: easy, medium or hard'
      } else {
        anims++
        if (!Array.isArray(j.frames) || !j.frames.length) problem = 'needs frames'
      }
    } catch (e) {
      problem = `invalid JSON: ${e.message}`
    }
    if (problem) {
      bad++
      console.log(`${file}:${line}: ${m[1]}: ${problem}`)
    }
  }
}
console.log(`${blocks} diagrams, ${quizzes} quizzes, ${anims} animations in ${files.length} files, ${bad} broken`)
process.exit(bad ? 1 : 0)
