"""End-to-end check: submit every challenge's reference solution through the running app.

The app's judge is what the learner actually meets (its time and memory limits, parallel test
runs, the exact output comparison), so a challenge is only "done" when its reference is
ACCEPTED here — not just by tools/check_challenge.py.

Usage: python -I tools/verify_via_app.py [slug ...]      (app must be running on :8090)
"""
import json
import sys
import time
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CODE = ROOT / "data" / "code"
BASE = "http://127.0.0.1:8090"


def submit(slug: str, code: str) -> dict:
    body = json.dumps({"code": code, "mode": "SUBMIT"}).encode("utf-8")
    req = urllib.request.Request(f"{BASE}/api/code/{slug}/run", data=body, method="POST",
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=600) as r:
        return json.loads(r.read())


def main(argv):
    slugs = argv or sorted(p.name for p in CODE.iterdir() if p.is_dir() and not p.name.startswith("_"))
    bad = []
    start = time.time()
    for i, slug in enumerate(slugs, 1):
        ref = CODE / slug / "reference" / "Solution.java"
        if not ref.exists():
            bad.append((slug, "no reference"))
            continue
        try:
            r = submit(slug, ref.read_text(encoding="utf-8"))
        except Exception as e:  # 404 = not imported (incomplete), or the app is down
            bad.append((slug, f"request failed: {e}"))
            print(f"[{i}/{len(slugs)}] {slug}: request failed: {e}", flush=True)
            continue
        line = f"{r['verdict']} {r['passed']}/{r['total']} slowest {r['maxMillis']} ms"
        print(f"[{i}/{len(slugs)}] {slug}: {line}", flush=True)
        if r["verdict"] != "ACCEPTED":
            first = next((t for t in r["results"] if t["verdict"] != "ACCEPTED"), None)
            detail = f"{first['name']}: {first['verdict']} {(first.get('stderr') or '')[:200]}" if first else ""
            if r.get("compileErrors"):
                detail = "; ".join(f"{e['file']}:{e['line']} {e['message']}" for e in r["compileErrors"])[:300]
            bad.append((slug, f"{line} — {detail}"))
    print(f"\n{len(slugs) - len(bad)}/{len(slugs)} accepted in {time.time() - start:.0f}s")
    for slug, why in bad:
        print(f"  ✗ {slug}: {why}")
    return 0 if not bad else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
