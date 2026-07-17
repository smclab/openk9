#!/usr/bin/env python3
"""Fail the pipeline when a Trivy suppression has an expired `expired-at` date.

Why this exists (Issue #2004): Trivy PARSES `expired-at` in .trivyignore.yaml but
does NOT enforce it — a suppression with a past date keeps hiding the CVE forever.
So a "temporary" suppression silently becomes permanent. This script closes that
gap: it reads every area .trivyignore.yaml, finds entries whose expired-at is in
the past, and exits non-zero so CI turns red. The fix is then to re-evaluate the
suppression (drop it if the CVE is fixed, or renew the date with a fresh reason).

Scope: the 5 area files. Entries without `expired-at` (permanent false positives)
are ignored by design. Covers the `vulnerabilities` section (the only one used);
`misconfigurations`/`secrets`/`licenses` are checked too if present, for free.
"""

import datetime
import glob
import sys

try:
    import yaml
except ImportError:
    sys.exit("ERROR: pyyaml is required (pip install pyyaml)")

# Area ignore files, relative to the repo root (the job runs from CI_PROJECT_DIR).
IGNORE_FILES = [
    "core/.trivyignore.yaml",
    "js-packages/.trivyignore.yaml",
    "ai-packages/.trivyignore.yaml",
    "connectors/.trivyignore.yaml",
    "enrichers/.trivyignore.yaml",
]

# Trivy ignore-file sections that carry per-entry expired-at.
SECTIONS = ["vulnerabilities", "misconfigurations", "secrets", "licenses"]


def parse_date(value):
    """Accept a date, a datetime, or a YYYY-MM-DD / RFC3339 string. Return a date."""
    if isinstance(value, datetime.datetime):
        return value.date()
    if isinstance(value, datetime.date):
        return value
    text = str(value).strip()
    # Trim a time/zone suffix if present (2026-09-23T00:00:00Z -> 2026-09-23).
    text = text.replace("T", " ").split(" ", 1)[0]
    return datetime.date.fromisoformat(text)


def main():
    today = datetime.date.today()
    expired = []   # (file, section, id, date)
    malformed = [] # (file, section, id, raw, error)

    for path in IGNORE_FILES:
        try:
            with open(path, encoding="utf-8") as fh:
                data = yaml.safe_load(fh) or {}
        except FileNotFoundError:
            # A missing area file is not an error — an area may simply have no
            # suppressions yet. Skip it silently.
            continue

        for section in SECTIONS:
            for entry in (data.get(section) or []):
                if not isinstance(entry, dict) or "expired-at" not in entry:
                    continue
                cve = entry.get("id", "<no-id>")
                raw = entry["expired-at"]
                try:
                    when = parse_date(raw)
                except (ValueError, TypeError) as exc:
                    malformed.append((path, section, cve, raw, str(exc)))
                    continue
                if when < today:
                    expired.append((path, section, cve, when))

    if malformed:
        print("Malformed expired-at values (must be YYYY-MM-DD):")
        for path, section, cve, raw, err in malformed:
            print(f"  {path}  [{section}]  {cve}  ->  {raw!r}  ({err})")

    if expired:
        print(f"\nExpired Trivy suppressions ({today.isoformat()}) — "
              "re-evaluate the CVE or renew expired-at with a fresh rationale:")
        for path, section, cve, when in sorted(expired):
            print(f"  {path}  [{section}]  {cve}  expired on {when.isoformat()}")

    if expired or malformed:
        sys.exit(1)

    print(f"OK — no expired Trivy suppressions as of {today.isoformat()}.")


if __name__ == "__main__":
    main()
