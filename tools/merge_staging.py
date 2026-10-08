"""Merge data/_staging/<company>.json into data/questions/<type>/<slug>.yaml.

A question exists once; each staged record either creates its file or appends a
sighting to the existing one. Matching is by LeetCode slug first, then slug (after data/aliases.yaml).
Near-duplicate titles that are not exact matches are reported, not merged —
a human (or Claude, reviewing the report) decides.

Usage: python -I tools/merge_staging.py [--dry-run] [company ...]
Staging files that merge cleanly are moved to data/_staging/merged/.
"""
import difflib
import json
import re
import shutil
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
QUESTIONS = ROOT / "data" / "questions"
STAGING = ROOT / "data" / "_staging"
ALIASES = ROOT / "data" / "aliases.yaml"  # duplicate-slug: canonical-slug, decided by review
TYPES = {"DSA", "LLD", "HLD", "BEHAVIORAL", "DOMAIN", "JAVA", "SPRING", "SQL"}
DIFFICULTIES = {"easy", "medium", "hard"}
FIELD_ORDER = ["slug", "title", "type", "difficulty", "tags", "leetcode",
               "prompt", "follow_ups", "academy", "sightings"]
SIGHTING_ORDER = ["company", "role", "round", "seen_on", "confidence", "source"]
SLUG = re.compile(r"^[a-z0-9]+(-[a-z0-9]+)*$")


class Literal(str):
    pass


def _literal(dumper, s):
    return dumper.represent_scalar("tag:yaml.org,2002:str", s, style="|")


yaml.add_representer(Literal, _literal, Dumper=yaml.SafeDumper)


def load_bank():
    bank, by_leetcode = {}, {}
    for f in QUESTIONS.glob("*/*.yaml"):
        q = yaml.safe_load(f.read_text(encoding="utf-8"))
        bank[q["slug"]] = (f, q)
        if q.get("leetcode"):
            by_leetcode[q["leetcode"]] = q["slug"]
    return bank, by_leetcode


def validate(rec, where):
    errs = []
    if rec.get("type") not in TYPES:
        errs.append(f"type {rec.get('type')!r}")
    if rec.get("difficulty") not in DIFFICULTIES:
        errs.append(f"difficulty {rec.get('difficulty')!r}")
    if not SLUG.match(rec.get("slug") or ""):
        errs.append(f"slug {rec.get('slug')!r}")
    s = rec.get("sighting") or {}
    for k in ("company", "source", "seen_on"):
        if not s.get(k):
            errs.append(f"sighting.{k} missing")
    if s.get("confidence") != "claimed":
        errs.append("sighting.confidence must be 'claimed' (only the owner marks verified)")
    return [f"{where}: {e}" for e in errs]


def ordered(q):
    out = {k: q[k] for k in FIELD_ORDER if k in q}
    out.update({k: v for k, v in q.items() if k not in out})
    if isinstance(out.get("prompt"), str) and out["prompt"]:
        out["prompt"] = Literal(out["prompt"].rstrip() + "\n")
    out["sightings"] = [{k: s[k] for k in SIGHTING_ORDER if k in s} for s in out["sightings"]]
    return out


def write(path, q):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(yaml.safe_dump(ordered(q), sort_keys=False, allow_unicode=True, width=100),
                    encoding="utf-8")


def same_sighting(a, b):
    return all(a.get(k) == b.get(k) for k in ("company", "round", "seen_on", "source"))


def main(argv):
    dry = "--dry-run" in argv
    names = [a for a in argv if not a.startswith("--")]
    files = [STAGING / f"{n}.json" for n in names] if names else sorted(STAGING.glob("*.json"))
    bank, by_leetcode = load_bank()
    aliases = (yaml.safe_load(ALIASES.read_text(encoding="utf-8")) or {}) if ALIASES.exists() else {}
    created = appended = skipped = 0
    errors, near = [], []

    for f in files:
        records = json.loads(f.read_text(encoding="utf-8"))
        file_errors = [e for i, r in enumerate(records) for e in validate(r, f"{f.name}[{i}]")]
        if file_errors:
            errors += file_errors
            continue
        for rec in records:
            sighting = rec.pop("sighting")
            rec["slug"] = aliases.get(rec["slug"], rec["slug"])
            key = by_leetcode.get(rec.get("leetcode")) if rec.get("leetcode") else None
            key = key or (rec["slug"] if rec["slug"] in bank else None)
            if key:
                path, q = bank[key]
                if any(same_sighting(s, sighting) for s in q["sightings"]):
                    skipped += 1
                    continue
                q["sightings"].append(sighting)
                for tag in rec.get("tags") or []:
                    if tag not in q.setdefault("tags", []):
                        q["tags"].append(tag)
                for fu in rec.get("follow_ups") or []:
                    if fu not in q.setdefault("follow_ups", []):
                        q["follow_ups"].append(fu)
                appended += 1
            else:
                for other_slug, (_, other) in bank.items():
                    if other["type"] == rec["type"] and difflib.SequenceMatcher(
                            None, other["title"].lower(), rec["title"].lower()).ratio() >= 0.8:
                        near.append(f"{rec['slug']}  ~  {other_slug}")
                q = {**rec, "sightings": [sighting]}
                q.setdefault("follow_ups", [])
                q.setdefault("academy", [])
                path = QUESTIONS / rec["type"].lower() / f"{rec['slug']}.yaml"
                bank[rec["slug"]] = (path, q)
                if q.get("leetcode"):
                    by_leetcode[q["leetcode"]] = rec["slug"]
                created += 1
            if not dry:
                write(path, q)
        if not dry:
            (STAGING / "merged").mkdir(exist_ok=True)
            shutil.move(str(f), STAGING / "merged" / f.name)

    print(f"created {created}, sightings appended {appended}, duplicates skipped {skipped}"
          + ("  (dry run)" if dry else ""))
    if near:
        print("\nNear-duplicate titles to review (not merged):")
        print("\n".join(f"  {n}" for n in near))
    if errors:
        print("\nRejected staging files — fix and re-run:")
        print("\n".join(f"  {e}" for e in errors))
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
