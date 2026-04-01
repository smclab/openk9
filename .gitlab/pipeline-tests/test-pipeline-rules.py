#!/usr/bin/env python3
"""
Pipeline rules test matrix for openk9.
Tests which triggers fire for each user/branch/changed-files combination.

Strategy:
  - Creates temporary files in ALL folders that match the target domain
    (so every trigger in that domain sees at least one changed file)
  - Commits them locally (never pushed)
  - Runs gitlab-ci-local --list (reads real git diff to evaluate `changes` rules)
  - Resets the commit and removes the files

Domain → folders covered:
  - backend:   core/common/resources-common/ + core/service/
               (covers all backend triggers; core/service is needed for Datasource)
  - frontend:  js-packages/admin-ui/ + search-frontend/ + tenant-ui/
               + talk-to/ + openk9-chatbot/
  - ai:        ai-packages/rag-module/ + agentic-rag-module/
               + embedding-modules/ + chunk-evaluation-module/
  - unrelated: helm-charts/  (no trigger should fire)

Usage:
  python3 .gitlab/pipeline-tests/test-pipeline-rules.py           # run all tests
  python3 .gitlab/pipeline-tests/test-pipeline-rules.py -v        # verbose
  python3 .gitlab/pipeline-tests/test-pipeline-rules.py -u mirko  # filter by user
"""

import subprocess
import sys
import os
import argparse

CI_FILE = ".gitlab/.gitlab-ci.yaml"
TEST_FILENAME = ".pipeline-test-changes"

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

# ── Users ──────────────────────────────────────────────────────────────────────
BACKEND_DESIGNATED  = ["mirko.zizzari", "michele.bastianelli"]
FRONTEND_DESIGNATED = ["lorenzo.venneri", "giorgio.bartolomeo"]
AI_DESIGNATED       = ["luca.callocchia"]
GENERIC_USERS       = ["luca.siligato", "other.user"]

# ── Trigger job names by domain ────────────────────────────────────────────────
BACKEND_TRIGGERS = [
    "Trigger Datasource", "Trigger Searcher", "Trigger Ingestion",
    "Trigger K8S-Client", "Trigger File-Manager", "Trigger Tenant-Manager",
    "Trigger API-Gateway", "Trigger Tika", "Trigger Entity-Manager",
    "Trigger Resources-Validator",
]
FRONTEND_TRIGGERS = [
    "Trigger Search Frontend", "Trigger Admin Frontend",
    "Trigger Tenant Frontend", "Trigger Talk-To Frontend",
    "Trigger OpenK9-Chatbot",
]
AI_TRIGGERS = [
    "Trigger Rag Module", "Trigger Agentic Rag Module",
    "Trigger Embedding Modules", "Trigger Chunk Evaluation Module",
]

# ── Domain → folders (multiple files to cover all triggers in the domain) ──────
# backend: core/common/resources-common/ covers ALL backend triggers except
#   Datasource (which needs core/app/datasource/ or core/service/).
#   We create files in both to cover everything.
DOMAIN_FOLDERS = {
    "backend":   ["core/common/resources-common", "core/service"],
    "frontend":  [
        "js-packages/admin-ui",
        "js-packages/search-frontend",
        "js-packages/tenant-ui",
        "js-packages/talk-to",
        "js-packages/openk9-chatbot",
    ],
    "ai":        [
        "ai-packages/rag-module",
        "ai-packages/agentic-rag-module",
        "ai-packages/embedding-modules",
        "ai-packages/chunk-evaluation-module",
    ],
    "unrelated": ["helm-charts"],
}

# ── Git helpers ────────────────────────────────────────────────────────────────

def git(*args) -> str:
    result = subprocess.run(["git"] + list(args), capture_output=True, text=True)
    return result.stdout.strip()


def create_test_commit(domain: str) -> list:
    """Create temp files in all domain folders and commit locally. Returns file paths."""
    folders = DOMAIN_FOLDERS[domain]
    filepaths = []
    for folder in folders:
        os.makedirs(folder, exist_ok=True)
        filepath = os.path.join(folder, TEST_FILENAME)
        with open(filepath, "w") as f:
            f.write("pipeline test\n")
        git("add", filepath)
        filepaths.append(filepath)
    git("commit", "-m", f"test: pipeline rules ({domain})", "--no-verify")
    return filepaths


def reset_test_commit(filepaths: list):
    """Undo the test commit and remove the files."""
    git("reset", "HEAD~1")
    for filepath in filepaths:
        if os.path.exists(filepath):
            os.remove(filepath)


# ── gitlab-ci-local helper ─────────────────────────────────────────────────────

def run_list(user: str, source: str, branch: str = None, tag: str = None) -> list:
    cmd = [
        "gitlab-ci-local",
        "--file", CI_FILE,
        "--list",
        f"--variable=CI_PIPELINE_SOURCE={source}",
        f"--variable=GITLAB_USER_LOGIN={user}",
    ]
    if branch:
        cmd.append(f"--variable=CI_COMMIT_BRANCH={branch}")
    if tag:
        cmd.append(f"--variable=CI_COMMIT_TAG={tag}")

    result = subprocess.run(cmd, capture_output=True, text=True)
    jobs = []
    for line in result.stdout.splitlines():
        line = line.strip()
        if not line:
            continue
        if any(line.startswith(p) for p in ("name ", "parsing", "json", "WARN", " WARN")):
            continue
        if "  " in line:
            job_name = line.split("  ")[0].strip()
            if job_name:
                jobs.append(job_name)
    return jobs


# ── Assertions ─────────────────────────────────────────────────────────────────

def check(label, jobs, should_fire, should_not_fire, verbose=False) -> bool:
    passed = True
    failures = []

    for j in should_fire:
        if j not in jobs:
            failures.append(f"    {c(RED, 'MISSING' )} : {j}")
            passed = False

    for j in should_not_fire:
        if j in jobs:
            failures.append(f"    {c(YELLOW, 'UNWANTED')} : {j}")
            passed = False

    if passed:
        status = c(BOLD + GREEN, "PASS")
    else:
        status = c(BOLD + RED, "FAIL")

    print(f"  [{status}] {label}")
    if verbose or not passed:
        fired_str = ", ".join(jobs) if jobs else c(DIM, "(none)")
        print(f"         {c(DIM, 'fired:')} {fired_str}")
    for f in failures:
        print(f)
    return passed


# ── Section header ─────────────────────────────────────────────────────────────

_current_section = [None]

def section(name):
    if _current_section[0] != name:
        _current_section[0] = name
        print(f"\n{c(CYAN + BOLD, name)}")
        print(c(DIM, "  " + "-" * 56))


# ── Test runner ────────────────────────────────────────────────────────────────

def run_tests(verbose=False, user_filter=None):
    results = []

    print(c(BOLD, "\nPipeline rules test matrix"))
    print(c(DIM, "=" * 60))

    def t(label, user, source, domain, branch=None, tag=None,
          should_fire=None, should_not_fire=None, sec=None):
        if user_filter and user_filter not in user:
            return

        if sec:
            section(sec)

        filepaths = None
        try:
            if domain:
                filepaths = create_test_commit(domain)
            jobs = run_list(user, source, branch=branch, tag=tag)
        finally:
            if filepaths:
                reset_test_commit(filepaths)

        ok = check(label, jobs, should_fire or [], should_not_fire or [], verbose=verbose)
        results.append(ok)

    # ── BACKEND DESIGNATED (mirko, michele) ────────────────────────────────────
    for user in BACKEND_DESIGNATED:
        short = user.split(".")[0]

        t(f"{short} | feature branch | core/service → backend fires, FE+AI silent",
          user, "push", "backend", branch="1234-fix-something",
          should_fire=BACKEND_TRIGGERS,
          should_not_fire=FRONTEND_TRIGGERS + AI_TRIGGERS,
          sec=f"Backend designated — {short}")

        t(f"{short} | feature branch | frontend files → nothing fires",
          user, "push", "frontend", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | feature branch | AI files → nothing fires",
          user, "push", "ai", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | MR | core/service → backend fires, FE+AI silent",
          user, "merge_request_event", "backend",
          should_fire=BACKEND_TRIGGERS,
          should_not_fire=FRONTEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | main | core/service → backend fires, FE+AI silent",
          user, "push", "backend", branch="main",
          should_fire=BACKEND_TRIGGERS,
          should_not_fire=FRONTEND_TRIGGERS + AI_TRIGGERS)

        # tag: skipped — gitlab-ci-local does not reliably simulate CI_COMMIT_TAG with changes

    # ── FRONTEND DESIGNATED (lorenzo, giorgio) ─────────────────────────────────
    for user in FRONTEND_DESIGNATED:
        short = user.split(".")[0]

        t(f"{short} | feature branch | frontend files → frontend fires, BE+AI silent",
          user, "push", "frontend", branch="1234-fix-something",
          should_fire=FRONTEND_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + AI_TRIGGERS,
          sec=f"Frontend designated — {short}")

        t(f"{short} | feature branch | backend files → nothing fires",
          user, "push", "backend", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=FRONTEND_TRIGGERS + BACKEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | feature branch | AI files → nothing fires",
          user, "push", "ai", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=FRONTEND_TRIGGERS + BACKEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | MR | frontend files → frontend fires",
          user, "merge_request_event", "frontend",
          should_fire=FRONTEND_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | main | frontend files → frontend fires",
          user, "push", "frontend", branch="main",
          should_fire=FRONTEND_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + AI_TRIGGERS)

    # ── AI DESIGNATED (luca.callocchia) ────────────────────────────────────────
    for user in AI_DESIGNATED:
        short = user.split(".")[0]

        t(f"{short} | feature branch | AI files → AI fires, BE+FE silent",
          user, "push", "ai", branch="1234-fix-something",
          should_fire=AI_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS,
          sec=f"AI designated — {short}")

        t(f"{short} | feature branch | backend files → nothing fires",
          user, "push", "backend", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=AI_TRIGGERS + BACKEND_TRIGGERS + FRONTEND_TRIGGERS)

        t(f"{short} | feature branch | frontend files → nothing fires",
          user, "push", "frontend", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=AI_TRIGGERS + BACKEND_TRIGGERS + FRONTEND_TRIGGERS)

        t(f"{short} | MR | AI files → AI fires",
          user, "merge_request_event", "ai",
          should_fire=AI_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS)

        t(f"{short} | main | AI files → AI fires",
          user, "push", "ai", branch="main",
          should_fire=AI_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS)

    # ── GENERIC USERS ──────────────────────────────────────────────────────────
    for user in GENERIC_USERS:
        short = user.split(".")[0]

        t(f"{short} | feature branch | backend files → backend fires (generic)",
          user, "push", "backend", branch="1234-fix-something",
          should_fire=BACKEND_TRIGGERS,
          should_not_fire=FRONTEND_TRIGGERS + AI_TRIGGERS,
          sec=f"Generic — {short}")

        t(f"{short} | feature branch | frontend files → frontend fires (generic)",
          user, "push", "frontend", branch="1234-fix-something",
          should_fire=FRONTEND_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + AI_TRIGGERS)

        t(f"{short} | feature branch | AI files → AI fires (generic)",
          user, "push", "ai", branch="1234-fix-something",
          should_fire=AI_TRIGGERS,
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS)

        t(f"{short} | feature branch | unrelated files → nothing fires",
          user, "push", "unrelated", branch="1234-fix-something",
          should_fire=[],
          should_not_fire=BACKEND_TRIGGERS + FRONTEND_TRIGGERS + AI_TRIGGERS)

    # ── Summary ────────────────────────────────────────────────────────────────
    total  = len(results)
    passed = sum(results)
    failed = total - passed

    print(f"\n{c(DIM, '=' * 60)}")
    if failed:
        summary = c(BOLD + RED, f"{passed}/{total} passed, {failed} failed")
        print(f"Results: {summary}")
        print(c(RED, "Some tests FAILED — review the pipeline rules."))
        sys.exit(1)
    else:
        summary = c(BOLD + GREEN, f"{passed}/{total} passed")
        print(f"Results: {summary}")
        print(c(GREEN, "All tests PASSED."))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-v", "--verbose", action="store_true")
    parser.add_argument("-u", "--user", help="Filter tests by username substring")
    args = parser.parse_args()
    run_tests(verbose=args.verbose, user_filter=args.user)
