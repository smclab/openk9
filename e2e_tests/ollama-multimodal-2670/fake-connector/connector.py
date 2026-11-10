"""
Connettore attivo dello scenario "E2E embedding multimodale Ollama 2670".

Il datasource lo invoca su POST /invoke all'avvio di uno scheduling: risponde
200 subito e poi, in un thread, manda all'ingestion i messaggi che un
connettore reale produrrebbe: due pagine di manuale con la loro immagine
JPEG, un estratto di tre pagine come PDF, e il LAST. I binari viaggiano
inline (base64) nei resources.binaries del messaggio.

Non contiene asserzioni: le verifiche le fa e2e.py su OpenSearch e sul
searcher a scheduling finito.
"""
import base64
import json
import os
import sys
import threading
import time
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer

CORPUS = os.environ.get("CORPUS", "/app/corpus")
INGESTION_URL = os.environ.get(
    "INGESTION_URL",
    "http://openk9-ingestion:8080/api/ingestion/v1/ingestion/",
)

# (contentId, testo, file del binario, content type, fileId)
DOCUMENTS = [
    ("pagina-sentina", "Manuale First 40.7, pagina 19", "sentina.jpg", "image/jpeg", "2670001"),
    ("pagina-altra", "Manuale First 40.7, pagina 6", "altra.jpg", "image/jpeg", "2670002"),
    ("estratto-pdf", "Manuale First 40.7, pagine 17, 19 e 20", "estratto.pdf", "application/pdf", "2670003"),
]

_driven = set()
_driven_lock = threading.Lock()


def log(msg):
    sys.stdout.write("[fake-connector] %s\n" % msg)
    sys.stdout.flush()


def send(name, message):
    req = urllib.request.Request(
        INGESTION_URL,
        data=json.dumps(message).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            log("sent %s -> HTTP %s" % (name, resp.status))
    except urllib.error.HTTPError as e:
        log("sent %s -> HTTP %s (error body: %s)" % (name, e.code, e.read()[:300]))
    except Exception as e:  # noqa: BLE001
        log("sent %s -> FAILED: %r" % (name, e))


def drive(tenant_id, schedule_id, datasource_id):
    log("driving ingestion for tenant=%s schedule=%s datasource=%s"
        % (tenant_id, schedule_id, datasource_id))
    now = int(time.time() * 1000)
    # lascia partire l'attore dello scheduling
    time.sleep(3)
    for content_id, text, filename, content_type, file_id in DOCUMENTS:
        with open(os.path.join(CORPUS, filename), "rb") as fh:
            data = base64.b64encode(fh.read()).decode()
        send(content_id, {
            "datasourceId": datasource_id, "tenantId": tenant_id,
            "scheduleId": schedule_id, "contentId": content_id,
            "parsingDate": now, "rawContent": text, "type": "DOCUMENT", "last": False,
            "datasourcePayload": {"document": {"title": text, "content": text}},
            "resources": {"binaries": [{"id": file_id, "name": filename,
                                        "contentType": content_type, "data": data}]},
        })
        time.sleep(1)
    time.sleep(5)
    send("LAST", {
        "datasourceId": datasource_id, "tenantId": tenant_id, "scheduleId": schedule_id,
        "parsingDate": now + 1, "datasourcePayload": {}, "type": "LAST", "last": True,
    })
    log("ingestion sequence complete for schedule=%s" % schedule_id)


class Handler(BaseHTTPRequestHandler):
    def _reply(self, code, obj):
        payload = json.dumps(obj).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def do_GET(self):
        # /sample: i doc type del datasource, fra cui document.content, il
        # campo del testo del data index knn
        if self.path.rstrip("/").endswith("/sample"):
            self._reply(200, {"datasourcePayload": {"document": {"title": "t", "content": "c"}}})
        else:
            self._reply(200, {})

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0") or 0)
        raw = self.rfile.read(length) if length else b""
        # risponde subito, prima di fare altro
        self._reply(200, {})

        if not self.path.rstrip("/").endswith("/invoke"):
            return
        try:
            ctx = json.loads(raw or b"{}")
        except Exception:  # noqa: BLE001
            log("could not parse /invoke body: %r" % raw[:300])
            return

        tenant_id = ctx.get("tenantId")
        schedule_id = ctx.get("scheduleId")
        datasource_id = ctx.get("datasourceId")
        if not (tenant_id and schedule_id and datasource_id is not None):
            log("/invoke missing fields, ignoring: %r" % ctx)
            return

        with _driven_lock:
            if schedule_id in _driven:
                log("already driven schedule=%s, skipping" % schedule_id)
                return
            _driven.add(schedule_id)

        threading.Thread(
            target=drive, args=(tenant_id, schedule_id, datasource_id), daemon=True
        ).start()

    def log_message(self, *args):
        pass


def main():
    log("listening on 0.0.0.0:8080, ingestion=%s" % INGESTION_URL)
    HTTPServer(("0.0.0.0", 8080), Handler).serve_forever()


if __name__ == "__main__":
    main()
