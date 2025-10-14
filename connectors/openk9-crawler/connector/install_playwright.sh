#!/bin/bash
# install_playwright.sh
# Conditionally install Playwright WebKit and dependencies

set -e

BROWSER="${1:-webkit}"  # Default to webkit
QUIET=0

echoerr() { if [[ $QUIET -ne 1 ]]; then echo "$@" 1>&2; fi }

# Parse options (if needed; simplify if always called with arg)
while [[ $# -gt 0 ]]; do
  case "$1" in
    -q|--quiet) QUIET=1; shift ;;
    *) BROWSER="$1"; shift ;;
  esac
done

# Check cache dir for this browser
CACHE_DIR="/root/.cache/ms-playwright/${BROWSER}-"*  # Wildcard for version
if ls $CACHE_DIR >/dev/null 2>&1; then
  echoerr "$BROWSER already installed at $CACHE_DIR"
else
  echoerr "Installing $BROWSER with deps"
  playwright install --with-deps "$BROWSER"
fi