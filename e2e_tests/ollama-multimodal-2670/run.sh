#!/usr/bin/env bash
#
# E2E embedding multimodale Ollama 2670 — esecuzione dello scenario.
#
# Presuppone lo stack k9.sh GIA' costruito e avviato dal checkout sotto test,
# con --with=file-handling --with=gen-ai, e un server Ollama con il modello
# di config.env. Lo script AGGIUNGE solo il connettore finto, crea un tenant
# usa e getta, indicizza due immagini e un PDF di tre pagine e verifica chunk
# e ricerche. Esito sulla console; exit 0 se PASS, diverso da 0 se FAIL.
#
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=config.env
source "$SCRIPT_DIR/config.env"

COMPOSE_FILE="$SCRIPT_DIR/docker-compose.fake-connector.yml"
AUTH_HDR="Authorization: Basic YWRtaW46YWRtaW4="

if docker compose version >/dev/null 2>&1; then DC=(docker compose); else DC=(docker-compose); fi

say()  { printf '\n\033[1m== %s ==\033[0m\n' "$1"; }
info() { printf '   %s\n' "$1"; }
die()  { printf '\n\033[31mABORT: %s\033[0m\n' "$1" >&2; exit 2; }

# --------------------------------------------------------------------------- #
say "Precondizioni"
for c in "$DATASOURCE_CONTAINER" "$INGESTION_CONTAINER" "$TENANTMANAGER_CONTAINER" "$OPENSEARCH_CONTAINER"; do
    st="$(docker inspect -f '{{.State.Health.Status}}' "$c" 2>/dev/null || echo absent)"
    [ "$st" = "healthy" ] || die "container '$c' non healthy (stato=$st).
   Avvia prima lo stack dal checkout da testare:
     cd <openk9-repo> && ./k9.sh build embedding-module && ./k9.sh up --with=file-handling --with=gen-ai"
    info "$c: healthy"
done
[ "$(docker inspect -f '{{.State.Running}}' "$EMBEDDING_CONTAINER" 2>/dev/null)" = "true" ] \
    || die "container '$EMBEDDING_CONTAINER' non in esecuzione (serve --with=gen-ai)"
proj="$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$DATASOURCE_CONTAINER" 2>/dev/null)"
[ "$proj" = "openk9" ] || die "lo stack in esecuzione appartiene al progetto compose '$proj', non 'openk9'."
info "progetto compose: openk9"

caps="$(curl -sf "$OLLAMA_HOST_URL/api/show" -d "{\"model\":\"$OLLAMA_MODEL\"}" \
    | python3 -c 'import json,sys; print(",".join(json.load(sys.stdin).get("capabilities") or []))')" \
    || die "Ollama non risponde su $OLLAMA_HOST_URL o il modello $OLLAMA_MODEL non c'e' (ollama pull $OLLAMA_MODEL)"
case ",$caps," in *,vision,*) ;; *) die "il modello $OLLAMA_MODEL non ha la capability vision ($caps)";; esac
info "Ollama $(curl -sf "$OLLAMA_HOST_URL/api/version"), $OLLAMA_MODEL: $caps"

# --------------------------------------------------------------------------- #
say "Corpus (due immagini e un PDF di tre pagine)"
[ -n "$CORPUS_DIR" ] || die "CORPUS_DIR non impostata: la cartella con le immagini di prova (vedi config.env)"
mkdir -p "$SCRIPT_DIR/corpus"
cp "$CORPUS_DIR/$PAGE_SENTINA" "$SCRIPT_DIR/corpus/sentina.jpg" || die "immagine assente: $CORPUS_DIR/$PAGE_SENTINA"
cp "$CORPUS_DIR/$PAGE_OTHER" "$SCRIPT_DIR/corpus/altra.jpg" || die "immagine assente: $CORPUS_DIR/$PAGE_OTHER"
[ -f "$CORPUS_DIR/$QUERY_IMAGE" ] || die "immagine di query assente: $CORPUS_DIR/$QUERY_IMAGE"
# Il PDF lo fa Pillow del modulo di embedding: le JPEG entrano in base64 da
# stdin, il PDF esce in base64 da stdout
pages=()
for p in $PDF_PAGES; do pages+=("$CORPUS_DIR/$p"); done
python3 -c 'import base64,json,sys; print(json.dumps([base64.b64encode(open(p,"rb").read()).decode() for p in sys.argv[1:]]))' "${pages[@]}" \
  | docker exec -i "$EMBEDDING_CONTAINER" python -c '
import base64, io, json, sys
from PIL import Image
images = [Image.open(io.BytesIO(base64.b64decode(d))).convert("RGB") for d in json.load(sys.stdin)]
out = io.BytesIO()
images[0].save(out, "PDF", save_all=True, append_images=images[1:], resolution=150)
print(base64.b64encode(out.getvalue()).decode())' \
  | python3 -c 'import base64,sys; open(sys.argv[1],"wb").write(base64.b64decode(sys.stdin.read()))' "$SCRIPT_DIR/corpus/estratto.pdf" \
  || die "costruzione del PDF fallita"
info "$(ls "$SCRIPT_DIR/corpus" | tr '\n' ' ')"

START_TS="$(date -u +%Y-%m-%dT%H:%M:%S)"

# --------------------------------------------------------------------------- #
say "Clean-slate (drop tenant + indici)"
python3 "$SCRIPT_DIR/e2e.py" clean

# --------------------------------------------------------------------------- #
say "Avvio fake-connector (overlay additivo)"
"${DC[@]}" -f "$COMPOSE_FILE" -p "$COMPOSE_PROJECT" up -d --build --force-recreate || die "impossibile avviare il fake-connector"
reachable=false
for _ in $(seq 1 15); do
    if docker exec "$DATASOURCE_CONTAINER" curl -sf "http://$FAKE_HOST:8080/sample" >/dev/null 2>&1; then
        reachable=true; break
    fi
    sleep 1
done
[ "$reachable" = true ] || die "fake-connector non raggiungibile da $DATASOURCE_CONTAINER"
info "fake-connector raggiungibile su http://$FAKE_HOST:8080"

# --------------------------------------------------------------------------- #
say "Provisioning tenant/modello/datasource/data index"
DS_ID="$(python3 "$SCRIPT_DIR/e2e.py" provision | tee /dev/stderr | tail -n1)"
case "$DS_ID" in
    ''|*[!0-9]*) die "provisioning fallito: DS_ID non valido ('$DS_ID')";;
esac
info "datasource id = $DS_ID"

# --------------------------------------------------------------------------- #
say "Trigger scheduling"
http="$(curl -s -o /dev/null -w '%{http_code}' -X POST "$DS_URL/v2/trigger" \
    -H "$AUTH_HDR" -H "X-TENANT-ID: $SCHEMA" -H 'Content-Type: application/json' \
    -d "{\"datasourceId\":$DS_ID}")"
[ "$http" = "200" ] || die "trigger HTTP=$http"
info "trigger HTTP=200 (il fake-connector manda 3 DOCUMENT -> LAST)"

# --------------------------------------------------------------------------- #
say "Attesa completamento scheduling"
final=""
for _ in $(seq 1 100); do
    st="$(docker exec "$POSTGRES_CONTAINER" psql -U openk9 -d openk9 -tA \
        -c "SELECT status FROM \"$SCHEMA\".scheduler ORDER BY id DESC LIMIT 1;" 2>/dev/null | tr -d '[:space:]')"
    case "$st" in
        FINISHED)               final="$st"; break;;
        CANCELLED|ERROR|FAILURE) final="$st"; break;;
    esac
    sleep 3
done
info "stato scheduler finale: ${final:-<timeout>}"
[ "$final" = "FINISHED" ] || info "ATTENZIONE: scheduling non FINISHED — le assert potrebbero fallire"

# --------------------------------------------------------------------------- #
say "Raccolta evidenze"
if docker logs --since "$START_TS" "$EMBEDDING_CONTAINER" 2>&1 \
        | grep "EmbedContent served" | grep -F "tenantId=$SCHEMA" | grep -q "client=ollama-multimodal"; then
    LOG_CLIENT=true
else
    LOG_CLIENT=false
fi
info "EmbedContent servito da client=ollama-multimodal: $LOG_CLIENT"
docker logs --since "$START_TS" "$EMBEDDING_CONTAINER" 2>&1 | grep -F "Ollama model" | tail -n1 | sed 's/^/   /'

# --------------------------------------------------------------------------- #
say "Verifica (asserzioni)"
python3 "$SCRIPT_DIR/e2e.py" check --ds-id "$DS_ID" --log-client "$LOG_CLIENT"
RESULT=$?

# --------------------------------------------------------------------------- #
if [ "$RESULT" -eq 0 ]; then
    say "Teardown (esito PASS)"
    "${DC[@]}" -f "$COMPOSE_FILE" -p "$COMPOSE_PROJECT" down -v --remove-orphans >/dev/null 2>&1
    python3 "$SCRIPT_DIR/e2e.py" clean >/dev/null 2>&1
    rm -rf "$SCRIPT_DIR/corpus"
    info "fake-connector, tenant, indici e corpus rimossi"
    printf '\n\033[32mSCENARIO PASS\033[0m\n'
else
    say "Esito FAIL — stato lasciato per debug"
    info "fake-connector, tenant '$SCHEMA' e indici NON rimossi"
    info "log connector: docker logs $FAKE_HOST"
    info "log embedding: docker logs $EMBEDDING_CONTAINER"
    printf '\n\033[31mSCENARIO FAIL\033[0m\n'
fi
exit "$RESULT"
