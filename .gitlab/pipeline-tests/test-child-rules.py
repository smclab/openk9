#!/usr/bin/env python3
"""
Child-pipeline rules test matrix for openk9 (Issue #2132).

While test-pipeline-rules.py tests the PARENT triggers (which domain starts),
this script tests the CHILD job rules INSIDE each module pipeline:
which jobs fire on main vs release branch vs a v-prefixed tag.

It covers the #2132 refactor:
  - v-prefixed tags (vN.N.N) drive the RELEASE line (Build Release + Copy-to-DockerHub),
    never the main line (no Build image, no Container Scanning, no dev restart).
  - a bare tag without 'v' does NOT trigger the Docker Hub copy.
  - release-branch push runs Build Release + Restart Release (single env), not the main jobs.
  - main push runs Build image + Container Scanning + dev Restart, never the Release jobs.

Strategy:
  Each module child file is self-contained (includes templates + child-rules).
  Child rules do NOT use `changes:`, so NO git commit is needed — we only set
  the GitLab predefined variables and read `gitlab-ci-local --list`.

Requirements:
  - gitlab-ci-local in PATH
  - Python 3
  - Run from the repo root.

Usage:
  python3 .gitlab/pipeline-tests/test-child-rules.py           # all tests
  python3 .gitlab/pipeline-tests/test-child-rules.py -v        # verbose (show fired jobs)
  python3 .gitlab/pipeline-tests/test-child-rules.py -k datasource   # filter by module file
"""

import subprocess
import sys
import argparse

# ── ANSI colors ────────────────────────────────────────────────────────────────
GREEN  = "\033[32m"
RED    = "\033[31m"
YELLOW = "\033[33m"
CYAN   = "\033[36m"
BOLD   = "\033[1m"
DIM    = "\033[2m"
RESET  = "\033[0m"

def c(color, text):
    return f"{color}{text}{RESET}"


# ── gitlab-ci-local: list jobs for a given child file + variables ───────────────

def list_jobs(child_file: str, branch: str = None, tag: str = None,
              source: str = "pipeline", extra: dict = None) -> list:
    cmd = ["gitlab-ci-local", "--file", f".gitlab/{child_file}", "--list",
           f"--variable=CI_PIPELINE_SOURCE={source}"]
    if branch:
        cmd.append(f"--variable=CI_COMMIT_BRANCH={branch}")
    if tag:
        cmd.append(f"--variable=CI_COMMIT_TAG={tag}")
    for k, v in (extra or {}).items():
        cmd.append(f"--variable={k}={v}")

    result = subprocess.run(cmd, capture_output=True, text=True)
    jobs = []
    for line in result.stdout.splitlines():
        line = line.rstrip()
        if not line or line.startswith(("name ", "name\t")) or "WARN" in line:
            continue
        if line.lower().startswith(("parsing", "json")):
            continue
        # job name is the first column (jobs may contain spaces; columns are 2+ spaces apart)
        parts = line.split("  ")
        name = parts[0].strip()
        if name and name != "name":
            jobs.append(name)
    return jobs


# ── Assertion ────────────────────────────────────────────────────────────────

results = []

def check(label, jobs, fire=None, no_fire=None, verbose=False):
    fire = fire or []
    no_fire = no_fire or []
    failures = []
    passed = True

    for j in fire:
        if j not in jobs:
            failures.append(f"    {c(RED, 'MISSING ')}: {j}")
            passed = False
    for j in no_fire:
        if j in jobs:
            failures.append(f"    {c(YELLOW, 'UNWANTED')}: {j}")
            passed = False

    status = c(BOLD + GREEN, "PASS") if passed else c(BOLD + RED, "FAIL")
    print(f"  [{status}] {label}")
    if verbose or not passed:
        print(f"         {c(DIM, 'fired:')} {', '.join(jobs) if jobs else c(DIM, '(none)')}")
    for f in failures:
        print(f)
    results.append(passed)


_section = [None]
def section(name):
    if _section[0] != name:
        _section[0] = name
        print(f"\n{c(CYAN + BOLD, name)}")
        print(c(DIM, "  " + "-" * 58))


# ── Tests ──────────────────────────────────────────────────────────────────────

def run(verbose=False, kfilter=None):
    print(c(BOLD, "\nChild-pipeline rules test matrix (Issue #2132)"))
    print(c(DIM, "=" * 62))

    TAG_V   = "v2026.1.0"     # team convention: v-prefixed release tag
    TAG_BIG = "v2026.10.3"    # multi-digit minor/patch — regex must still match
    TAG_NOV = "2026.1.0"      # bare tag without v — must NOT copy to Docker Hub
    BRANCH  = "2026.1.x"      # release branch

    def want(child_file):
        return (not kfilter) or (kfilter in child_file)

    # ── BACKEND (datasource as representative; same template for all Quarkus) ──
    f = ".gitlab-ci-datasource.yaml"
    if want(f):
        section("Backend — datasource (Quarkus, .maven-build-logic-release)")

        check("main → Build image + Scanning + dev Restart; NO Release jobs",
              list_jobs(f, branch="main", source="push"),
              fire=["Build Datasource image", "Container Scanning Datasource",
                    "Trigger Restart Datasource"],
              no_fire=["Build Datasource Release", "Copy Datasource to DockerHub",
                       "Trigger Restart Datasource Release"], verbose=verbose)

        check(f"release branch {BRANCH} → Build Release + Restart Release; NO main jobs, NO Copy",
              list_jobs(f, branch=BRANCH, source="push"),
              fire=["Build Datasource Release", "Trigger Restart Datasource Release"],
              no_fire=["Build Datasource image", "Container Scanning Datasource",
                       "Trigger Restart Datasource", "Copy Datasource to DockerHub"],
              verbose=verbose)

        check(f"tag {TAG_V} → Build Release + Copy; NO main jobs, NO restart",
              list_jobs(f, tag=TAG_V),
              fire=["Build Datasource Release", "Copy Datasource to DockerHub"],
              no_fire=["Build Datasource image", "Container Scanning Datasource",
                       "Trigger Restart Datasource", "Trigger Restart Datasource Release"],
              verbose=verbose)

        check(f"tag {TAG_BIG} (multi-digit) → Build Release + Copy",
              list_jobs(f, tag=TAG_BIG),
              fire=["Build Datasource Release", "Copy Datasource to DockerHub"],
              no_fire=["Build Datasource image"], verbose=verbose)

        check(f"tag {TAG_NOV} (no v) → nothing fires (rules require ^v)",
              list_jobs(f, tag=TAG_NOV),
              fire=[],
              no_fire=["Build Datasource image", "Build Datasource Release",
                       "Copy Datasource to DockerHub"], verbose=verbose)

        check("release MR (RELEASE_MR=true) → Build Verifier Release only",
              list_jobs(f, source="parent_pipeline", extra={"RELEASE_MR": "true"}),
              fire=["Build Verifier Datasource Release MR"],
              no_fire=["Build Datasource Release", "Copy Datasource to DockerHub"],
              verbose=verbose)

    # ── AI — embedding (single image: OpenAI — ST removed, see #2162) ──────────
    f = ".gitlab-ci-embedding-module.yaml"
    if want(f):
        section("AI — embedding (Kaniko)")

        check(f"tag {TAG_V} → Release build + Copy job",
              list_jobs(f, tag=TAG_V),
              fire=["Build Embedding OpenAI Release",
                    "Copy Embedding OpenAI to DockerHub"],
              no_fire=["Trigger Restart Embedding Release"], verbose=verbose)

        check(f"release branch {BRANCH} → Release build + Restart Release; NO Copy",
              list_jobs(f, branch=BRANCH, source="push"),
              fire=["Build Embedding OpenAI Release",
                    "Trigger Restart Embedding Release"],
              no_fire=["Copy Embedding OpenAI to DockerHub"],
              verbose=verbose)

        check(f"tag {TAG_NOV} (no v) → NO Copy job (Docker Hub clean only)",
              list_jobs(f, tag=TAG_NOV),
              fire=[],
              no_fire=["Copy Embedding OpenAI to DockerHub"],
              verbose=verbose)

    # ── AI — rag (single image) ────────────────────────────────────────────────
    f = ".gitlab-ci-rag-module.yaml"
    if want(f):
        section("AI — rag-module")
        check(f"tag {TAG_V} → Build Release + Copy",
              list_jobs(f, tag=TAG_V),
              fire=["Build Rag Release", "Copy Rag Module to DockerHub"],
              verbose=verbose)
        check(f"tag {TAG_NOV} (no v) → NO Copy",
              list_jobs(f, tag=TAG_NOV),
              no_fire=["Copy Rag Module to DockerHub"], verbose=verbose)

    # ── Frontend — admin-ui (Kaniko, version.env) ──────────────────────────────
    f = ".gitlab-ci-admin-frontend.yaml"
    if want(f):
        section("Frontend — admin-ui")
        check(f"release branch {BRANCH} → Build Release + Restart Release",
              list_jobs(f, branch=BRANCH, source="push"),
              fire=["Build Admin Frontend Release", "Trigger Restart Admin Frontend Release"],
              no_fire=["Build Admin Frontend"], verbose=verbose)
        check(f"tag {TAG_V} → Build Release + Copy (regex now matches v-prefixed)",
              list_jobs(f, tag=TAG_V),
              fire=["Build Admin Frontend Release", "Copy Admin UI to DockerHub"],
              no_fire=["Trigger Restart Admin Frontend Release"], verbose=verbose)

    # ── Connectors (6 connectors: build + scan + copy) ─────────────────────────
    f = ".gitlab-ci-connectors.yaml"
    if want(f):
        section("Connectors")
        connectors = ["Web", "Email", "Database", "YouTube", "GitLab", "Minio"]

        copy_jobs  = [f"Copy {n} Connector to DockerHub" for n in connectors]
        build_jobs = [f"Build {n} Connector image" for n in connectors]

        check(f"tag {TAG_V} → all 6 builds + all 6 Copy jobs",
              list_jobs(f, tag=TAG_V),
              fire=build_jobs + copy_jobs, verbose=verbose)

        check(f"tag {TAG_NOV} (no v) → builds fire, but NO Copy (clean-only on Docker Hub)",
              list_jobs(f, tag=TAG_NOV),
              fire=build_jobs,
              no_fire=copy_jobs, verbose=verbose)

        # ── #2133: release-branch and porting-MR parity ────────────────────────
        # On the release branch / porting MR the connector Build jobs are gated
        # by `changes:` against the connector code paths. We can't simulate
        # `changes:` from an isolated child run without a real diff, so we verify
        # the observable behaviour: Fetch config (rule: always) fires, while
        # the Copy-to-DockerHub jobs do NOT fire (they require a v-tag).
        # The parent-trigger and per-job rules are exercised end-to-end by the
        # empirical reproduction in test-pipeline-rules.py once it includes the
        # `connectors/<x>/connector` paths in its commit fixture (follow-up).
        check(f"release branch {BRANCH} → Fetch config fires, NO Copy to Docker Hub",
              list_jobs(f, branch=BRANCH, source="push"),
              fire=["Fetch config"],
              no_fire=copy_jobs, verbose=verbose)

        check(f"porting MR (→{BRANCH}) → Fetch config fires, NO Copy to Docker Hub",
              list_jobs(f, source="merge_request_event",
                        extra={"CI_MERGE_REQUEST_TARGET_BRANCH_NAME": BRANCH}),
              fire=["Fetch config"],
              no_fire=copy_jobs, verbose=verbose)

    # ── Summary ────────────────────────────────────────────────────────────────
    total = len(results)
    passed = sum(results)
    failed = total - passed
    print(f"\n{c(DIM, '=' * 62)}")
    if failed:
        print(f"Results: {c(BOLD + RED, f'{passed}/{total} passed, {failed} failed')}")
        print(c(RED, "Some child-rule tests FAILED — review the pipeline rules."))
        sys.exit(1)
    print(f"Results: {c(BOLD + GREEN, f'{passed}/{total} passed')}")
    print(c(GREEN, "All child-rule tests PASSED."))


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("-v", "--verbose", action="store_true")
    ap.add_argument("-k", "--filter", help="Filter by child file substring (e.g. datasource)")
    args = ap.parse_args()
    run(verbose=args.verbose, kfilter=args.filter)
