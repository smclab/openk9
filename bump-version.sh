#!/usr/bin/env bash
# Bump the OpenK9 project version across Maven, Helm, JS, Python and Docker Compose.
# Usage: ./bump-version.sh <new-version>
# Example: ./bump-version.sh 2026.2.0-SNAPSHOT
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <new-version>" >&2
  exit 1
fi

NEW="$1"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for cmd in mvn yq sed find; do
  command -v "$cmd" >/dev/null || { echo "Missing required command: $cmd" >&2; exit 1; }
done

OLD=$(sed -n 's|.*<version>\(.*\)</version>.*|\1|p' "$ROOT/core/pom.xml" | head -n1)
if [[ -z "$OLD" ]]; then
  echo "Could not detect current version from core/pom.xml" >&2
  exit 1
fi

echo "Current version: $OLD"
echo "New version:     $NEW"
echo

# Escape OLD for use as a regex literal (dots, etc.)
OLD_RE=$(printf '%s' "$OLD" | sed 's/[][\.*^$/]/\\&/g')

# ---------------------------------------------------------------------------
# 1) Maven — core reactor (handles all child POMs including api-gateway).
#    The base connector/enricher POMs are intentionally NOT included:
#    they follow an independent versioning scheme (1.0.0-SNAPSHOT).
#    The vendor/hibernate-rx-multitenancy module is excluded: it is a forked
#    upstream extension with its own version line, not tied to the OpenK9
#    release cadence.
# ---------------------------------------------------------------------------
# Capture core/pom.xml's reproducible-build timestamp before versions:set:
# the versions plugin rewrites project.build.outputTimestamp to "now" on every
# run, which is unrelated to the version bump and would break idempotency.
OUTPUT_TS=$(sed -n 's|.*<project.build.outputTimestamp>\(.*\)</project.build.outputTimestamp>.*|\1|p' "$ROOT/core/pom.xml" | head -n1)

echo "==> [1/9] Maven: core reactor (excluding vendor/)"
(cd "$ROOT/core" && mvn -q versions:set \
  -DnewVersion="$NEW" \
  -DgenerateBackupPoms=false \
  -DprocessAllModules=true)

# versions:set processes the whole reactor (vendor/ is wired in as a module of
# core/pom.xml). Restore the vendor POMs from git so its independent version
# line (forked Quarkus extension, currently 3.20.5) is preserved.
if [[ -d "$ROOT/vendor/hibernate-rx-multitenancy" ]]; then
  git -C "$ROOT" checkout -- vendor/hibernate-rx-multitenancy
  echo "    restored vendor/hibernate-rx-multitenancy from git"
fi

# Restore the reproducible-build timestamp that versions:set rewrote to "now",
# so the bump diff stays free of spurious timestamp churn and the script is
# idempotent.
if [[ -n "$OUTPUT_TS" ]]; then
  sed -i -E "s|(<project.build.outputTimestamp>).*(</project.build.outputTimestamp>)|\1${OUTPUT_TS}\2|" "$ROOT/core/pom.xml"
  echo "    restored core/pom.xml project.build.outputTimestamp"
fi

# ---------------------------------------------------------------------------
# 2) Helm Chart.yaml — version + appVersion on every chart.
# ---------------------------------------------------------------------------
echo "==> [2/9] Helm Chart.yaml (version + appVersion)"
while IFS= read -r -d '' chart; do
  yq -i ".version = \"$NEW\" | .appVersion = \"$NEW\"" "$chart"
  echo "    $chart"
done < <(find "$ROOT/helm-charts" "$ROOT/connectors" "$ROOT/enrichers" \
          -name Chart.yaml -print0 2>/dev/null)

# ---------------------------------------------------------------------------
# 3) Helm values.yaml — image.tag, only when it matches the old version
#    (avoids clobbering third-party image tags like postgres:17).
# ---------------------------------------------------------------------------
echo "==> [3/9] Helm values.yaml (image.tag)"
while IFS= read -r -d '' values; do
  cur=$(yq '.image.tag // ""' "$values")
  if [[ "$cur" == "$OLD" ]]; then
    yq -i ".image.tag = \"$NEW\"" "$values"
    echo "    $values"
  fi
done < <(find "$ROOT/helm-charts" "$ROOT/connectors" "$ROOT/enrichers" \
          -name values.yaml -print0 2>/dev/null)

# ---------------------------------------------------------------------------
# 4) JS package.json — aligns every OpenK9 JS package to the new version,
#    including search-frontend which currently diverges.
# ---------------------------------------------------------------------------
echo "==> [4/9] JS package.json"
for f in \
  "$ROOT/js-packages/admin-ui/package.json" \
  "$ROOT/js-packages/openk9-chatbot/package.json" \
  "$ROOT/js-packages/search-frontend/package.json" \
  "$ROOT/js-packages/talk-to/package.json" \
  "$ROOT/js-packages/tenant-ui/package.json"; do
  [[ -f "$f" ]] || continue
  sed -i -E "0,/\"version\"[[:space:]]*:/ s/(\"version\"[[:space:]]*:[[:space:]]*\")[^\"]+(\")/\1$NEW\2/" "$f"
  echo "    $f"
done

# ---------------------------------------------------------------------------
# 5) Python pyproject.toml — top-level `version = "..."`.
# ---------------------------------------------------------------------------
echo "==> [5/9] Python pyproject.toml"
for f in \
  "$ROOT/ai-packages/rag-module/pyproject.toml" \
  "$ROOT/ai-packages/embedding-modules/pyproject.toml"; do
  [[ -f "$f" ]] || continue
  sed -i -E "s/^(version[[:space:]]*=[[:space:]]*\")[^\"]+(\")/\1$NEW\2/" "$f"
  echo "    $f"
done

# ---------------------------------------------------------------------------
# 6) Python FastAPI — `version="..."` argument in the service entrypoints
#    (rag-module / agentic-rag-module server.py and the docling-processor
#    enricher file_manager modules).
# ---------------------------------------------------------------------------
echo "==> [6/9] Python FastAPI version"
for f in \
  "$ROOT/ai-packages/rag-module/app/server.py" \
  "$ROOT/ai-packages/agentic-rag-module/app/server.py" \
  "$ROOT/enrichers/docling-processor/enricher/external/file_manager.py" \
  "$ROOT/enrichers/docling-processor/enricher/external/file_manager2.py"; do
  [[ -f "$f" ]] || continue
  sed -i -E "s/(version[[:space:]]*=[[:space:]]*\")${OLD_RE}(\")/\1$NEW\2/g" "$f"
  echo "    $f"
done

# ---------------------------------------------------------------------------
# 7) Java OpenAPI `@Info(version = "...")` — the version surfaced by each
#    service's generated OpenAPI document. Restricted to the current product
#    version so unrelated version literals are left untouched.
# ---------------------------------------------------------------------------
echo "==> [7/9] Java OpenAPI @Info version"
while IFS= read -r -d '' f; do
  if grep -qE "version[[:space:]]*=[[:space:]]*\"${OLD_RE}\"" "$f"; then
    sed -i -E "s/(version[[:space:]]*=[[:space:]]*\")${OLD_RE}(\")/\1$NEW\2/g" "$f"
    echo "    $f"
  fi
done < <(find "$ROOT/core/app" -name '*Application.java' -print0 2>/dev/null)

# ---------------------------------------------------------------------------
# 8) Version config file — `OPENK9_VERSION=...` in python_modules_config.txt.
# ---------------------------------------------------------------------------
echo "==> [8/9] Version config file"
if [[ -f "$ROOT/python_modules_config.txt" ]]; then
  sed -i -E "s/^(OPENK9_VERSION=)${OLD_RE}$/\1$NEW/" "$ROOT/python_modules_config.txt"
  echo "    $ROOT/python_modules_config.txt"
fi

# ---------------------------------------------------------------------------
# 9) Docker Compose — both `${IMAGE_TAG:-<OLD>}` defaults and hardcoded
#    `smclab/openk9-*:<OLD>` image tags. A trailing tag suffix on the default
#    (e.g. `<OLD>-chatbot` for search-frontend) is preserved.
# ---------------------------------------------------------------------------
echo "==> [9/9] Docker Compose"
shopt -s nullglob
for f in "$ROOT"/compose*.yaml; do
  sed -i -E "s|(\\\$\\{IMAGE_TAG:-)${OLD_RE}([-A-Za-z0-9]*\\})|\1$NEW\2|g" "$f"
  sed -i -E "s|(smclab/openk9-[a-z0-9-]+:)${OLD_RE}([[:space:]]*$)|\1$NEW\2|g" "$f"
  echo "    $f"
done
shopt -u nullglob

echo
echo "Done. Review changes with: git -C '$ROOT' diff"
