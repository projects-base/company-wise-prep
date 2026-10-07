"""Verify code challenges in data/code/<slug>/ (docs/CODE-CHALLENGES.md).

For each slug: compile IO + harness + reference and run every test, comparing with `expected`;
then check the learner's starter compiles against the harness.

Usage:
  python -I tools/check_challenge.py <slug> [<slug> ...]     check
  python -I tools/check_challenge.py --all                   check every challenge
  python -I tools/check_challenge.py --write-expected <slug> fill `expected` from the reference
"""
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
CODE = ROOT / "data" / "code"
LIB = CODE / "_lib" / "IO.java"
TIMEOUT = 10


class Literal(str):
    pass


yaml.add_representer(Literal, lambda d, s: d.represent_scalar("tag:yaml.org,2002:str", s, style="|"),
                     Dumper=yaml.SafeDumper)


def norm(s: str) -> str:
    return "\n".join(line.rstrip() for line in (s or "").strip("\n").splitlines()).rstrip()


def compile_dir(files: dict) -> tuple:
    d = Path(tempfile.mkdtemp(prefix="cwp-check-"))
    for name, src in files.items():
        shutil.copy(src, d / name)
    r = subprocess.run(["javac", "-encoding", "UTF-8", "-d", str(d), *[str(d / n) for n in files]],
                       capture_output=True, text=True)
    return d, r


def run(d: Path, stdin: str) -> tuple:
    try:
        r = subprocess.run(["java", "-Xss64m", "-Xmx512m", "-cp", str(d), "Main"], input=stdin,
                           capture_output=True, text=True, timeout=TIMEOUT)
        return r.returncode, r.stdout, r.stderr
    except subprocess.TimeoutExpired:
        return -1, "", f"timed out after {TIMEOUT}s"


def check(slug: str, write_expected: bool = False) -> bool:
    c = CODE / slug
    missing = [p for p in ("problem.md", "Solution.java", "Main.java", "reference/Solution.java", "tests.yaml") if not (c / p).exists()]
    if missing:
        print(f"[{slug}] MISSING {missing}")
        return False
    spec = yaml.safe_load((c / "tests.yaml").read_text(encoding="utf-8"))
    tests = spec.get("tests") or []
    ok = True

    d, r = compile_dir({"IO.java": LIB, "Main.java": c / "Main.java", "Solution.java": c / "reference" / "Solution.java"})
    if r.returncode != 0:
        print(f"[{slug}] reference does not compile:\n{r.stderr}")
        return False
    visible = sum(1 for t in tests if not t.get("hidden"))
    if visible < 1 or len(tests) < 3:
        print(f"[{slug}] needs >=1 visible and >=3 total tests (has {visible}/{len(tests)})")
        ok = False
    for i, t in enumerate(tests):
        code, out, err = run(d, t.get("input", ""))
        name = t.get("name", f"test {i + 1}")
        if code != 0:
            print(f"[{slug}] {name}: reference crashed ({code}): {err.strip()[:400]}")
            ok = False
            continue
        if write_expected:
            t["expected"] = Literal(norm(out) + "\n")
        elif norm(out) != norm(t.get("expected", "")):
            print(f"[{slug}] {name}: reference output differs\n  expected: {norm(t.get('expected'))[:200]}\n  actual:   {norm(out)[:200]}")
            ok = False
    shutil.rmtree(d, ignore_errors=True)

    d, r = compile_dir({"IO.java": LIB, "Main.java": c / "Main.java", "Solution.java": c / "Solution.java"})
    if r.returncode != 0:
        print(f"[{slug}] starter does not compile with the harness:\n{r.stderr}")
        ok = False
    shutil.rmtree(d, ignore_errors=True)

    if write_expected:
        for t in tests:
            t["input"] = Literal(norm(t.get("input", "")) + "\n")
        (c / "tests.yaml").write_text(yaml.safe_dump(spec, sort_keys=False, allow_unicode=True, width=1000),
                                      encoding="utf-8")
        print(f"[{slug}] wrote expected for {len(tests)} tests")
    elif ok:
        print(f"[{slug}] OK — {len(tests)} tests ({visible} visible)")
    return ok


def main(argv):
    write = "--write-expected" in argv
    slugs = [a for a in argv if not a.startswith("--")]
    if "--all" in argv:
        slugs = sorted(p.name for p in CODE.iterdir() if p.is_dir() and not p.name.startswith("_"))
    results = [check(s, write) for s in slugs]
    print(f"\n{sum(results)}/{len(results)} passed")
    return 0 if all(results) else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
