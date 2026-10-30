import { api, hosted, lastApiCall } from './api'

/**
 * Keeps the hosted API awake while you are studying, so a long read of a lesson doesn't end in a
 * one-minute cold start. Free hosts stop a service after ~15 minutes without requests; this pings
 * /api/auth/status (no login, no database) every 10 minutes, but only while the tab is visible and
 * you have touched the page in the last 30 minutes. A tab left open overnight lets the API sleep,
 * which is what keeps Render inside its free hours and Cloud Run inside its free tier.
 */
export const PING_EVERY = 10 * 60_000
export const IDLE_AFTER = 30 * 60_000
const TICK = 60_000

/** Pure rule: ping when visible, recently used, and no request reached the API for PING_EVERY. */
export function shouldPing(now: number, lastCall: number, lastActivity: number, visible: boolean): boolean {
  return visible && now - lastActivity < IDLE_AFTER && now - lastCall >= PING_EVERY
}

/** Starts the pinger and returns its stop function. Does nothing locally (same-origin API). */
export function startKeepAlive(): () => void {
  if (!hosted) return () => {}
  let lastActivity = Date.now()
  const ping = () => {
    if (shouldPing(Date.now(), lastApiCall(), lastActivity, document.visibilityState === 'visible')) {
      api.authStatus().catch(() => {
        /* the next real request reports a down host */
      })
    }
  }
  const touched = () => {
    const wasIdle = Date.now() - lastActivity >= IDLE_AFTER
    lastActivity = Date.now()
    if (wasIdle) ping() // back after a break: start waking the API before the first click needs it
  }
  const onVisible = () => {
    if (document.visibilityState === 'visible') touched()
  }
  const activity = ['pointerdown', 'keydown', 'scroll', 'wheel'] as const
  activity.forEach((e) => window.addEventListener(e, touched, { passive: true }))
  document.addEventListener('visibilitychange', onVisible)
  const timer = window.setInterval(ping, TICK)
  return () => {
    window.clearInterval(timer)
    activity.forEach((e) => window.removeEventListener(e, touched))
    document.removeEventListener('visibilitychange', onVisible)
  }
}
