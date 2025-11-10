#!/usr/bin/env bash
set -e  # Ferma lo script se c'è un errore

BROWSER=${1:-webkit}  # Default: webkit, puoi passare "chromium", "firefox" o "all"

echo "🔧 [Playwright Installer] Installing Playwright and browser: ${BROWSER}"

# 1️⃣ Assicura che Playwright Python sia installato
if ! python -m playwright --version >/dev/null 2>&1; then
  echo "📦 Installing Playwright Python package..."
  pip install --no-cache-dir playwright
fi

# 2️⃣ Installa le librerie di sistema necessarie
echo "📚 Installing system dependencies for Playwright..."
apt-get update && apt-get install -y --no-install-recommends \
  libglib2.0-0 libnss3 libatk1.0-0 libatk-bridge2.0-0 \
  libdrm2 libxcomposite1 libxdamage1 libxrandr2 \
  libgbm1 libasound2 libpangocairo-1.0-0 libgtk-3-0 \
  && rm -rf /var/lib/apt/lists/*

# 3️⃣ Imposta una directory persistente per i browser (stesso path del Dockerfile)
export PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
mkdir -p "$PLAYWRIGHT_BROWSERS_PATH"
chmod -R 777 "$PLAYWRIGHT_BROWSERS_PATH"

# 4️⃣ Installa il browser specifico (o tutti)
echo "🌐 Installing browser binaries..."
case "$BROWSER" in
  webkit)
    playwright install --with-deps webkit ;;
  chromium)
    playwright install --with-deps chromium ;;
  firefox)
    playwright install --with-deps firefox ;;
  all)
    playwright install --with-deps ;;
  *)
    echo "❌ Unknown browser type: $BROWSER" && exit 1 ;;
esac

echo "✅ Playwright and ${BROWSER} installed successfully."
