#!/usr/bin/env python3
"""
Helper API/JSON dello scenario "E2E embedding multimodale Ollama 2670".

Sottocomandi (configurazione dall'ambiente, esportata da run.sh con
config.env):

  clean       cancella il tenant di prova (in due passi) e i suoi indici
  provision   tenant -> initTenant -> modello di embedding Ollama (abilitato)
              -> plugin driver -> datasource -> doc type dal /sample ->
              data index knn (stampa l'id del datasource sull'ultima riga)
  check       aggancia il datasource al bucket (KNN) e verifica i chunk su
              OpenSearch e le ricerche dal searcher; exit 0 se tutto passa
"""
import argparse
import base64
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request

UUID_RE = re.compile(
    r"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
)

AUTH = "Basic " + base64.b64encode(b"admin:admin").decode()


def env(name):
    v = os.environ.get(name)
    if not v:
        sys.exit("e2e.py: missing required environment variable %s" % name)
    return v


def _request(method, url, body=None, headers=None, timeout=30):
    data = json.dumps(body).encode() if body is not None else None
    h = {"Authorization": AUTH}
    if data is not None:
        h["Content-Type"] = "application/json"
    if headers:
        h.update(headers)
    req = urllib.request.Request(url, data=data, headers=h, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw.strip() else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            parsed = json.loads(raw) if raw.strip() else {}
        except ValueError:
            parsed = {"_raw": raw}
        return e.code, parsed


def gql(query):
    url = env("DS_URL") + "/graphql"
    status, body = _request(
        "POST", url, {"query": query}, {"X-TENANT-ID": env("SCHEMA")}, timeout=120
    )
    if status != 200 or "errors" in body:
        sys.exit("GraphQL failed (%s): %s" % (status, json.dumps(body)[:500]))
    return body["data"]


def _entity_id(node, mutation_name):
    fv = node.get("fieldValidators")
    if fv:
        sys.exit("%s validation error: %s" % (mutation_name, json.dumps(fv)))
    return node["entity"]["id"]


# --------------------------------------------------------------------------- #
# clean
# --------------------------------------------------------------------------- #
def cmd_clean(_args):
    tm, vhost, os_url, schema = env("TM_URL"), env("VHOST"), env("OS_URL"), env("SCHEMA")

    status, body = _request(
        "POST", tm + "/tenant-manager/tenant/delete", {"virtualHost": vhost}
    )
    message = (body or {}).get("message", "") if isinstance(body, dict) else ""
    # il messaggio e' "Deletion token: <uuid>. Send it back with a DELETE ..."
    m = UUID_RE.search(message) if message else None
    token = m.group(0) if m else ""
    if status == 200 and token:
        _request(
            "DELETE",
            tm + "/tenant-manager/tenant/delete",
            {"virtualHost": vhost, "token": token},
        )
        print("clean: tenant deletion requested for %s" % vhost)
    else:
        print("clean: no existing tenant to delete (status=%s)" % status)

    st, _ = _request("DELETE", "%s/%s-*" % (os_url, schema))
    print("clean: dropped OpenSearch indices %s-* (status=%s)" % (schema, st))


# --------------------------------------------------------------------------- #
# provision
# --------------------------------------------------------------------------- #
def _create_tenant(tm, schema, vhost):
    # riprova finche' una cancellazione precedente (asincrona) non e' finita
    for attempt in range(1, 16):
        status, body = _request(
            "POST",
            tm + "/tenant-manager/tenant",
            {
                "virtualHost": vhost,
                "securityConfiguration": "NO_GATEWAY_AUTH",
                "tenantName": schema,
            },
        )
        if status == 200 and body.get("tenantName"):
            print("provision: tenant %s created" % schema)
            return
        if status in (409, 400, 500):
            print("provision: tenant not ready (status=%s), retry %d" % (status, attempt))
            time.sleep(2)
            continue
        sys.exit("provision: unexpected tenant-create status %s: %s" % (status, body))
    sys.exit("provision: tenant could not be created (still deleting?)")


def _embedding_model():
    dto = ", ".join([
        'name:"%s"' % env("MODEL_NAME"),
        'providerModel:{provider:"ollama", model:"%s"}' % env("OLLAMA_MODEL"),
        'apiUrl:"%s"' % env("OLLAMA_URL"),
        'apiKey:""',
        "vectorSize:%s" % env("VECTOR_SIZE"),
        "vectorDataType:FLOAT32",
        'jsonConfig:"{}"',
    ])
    model = gql(
        "mutation { embeddingModel(embeddingModelDTO:{%s}){ entity{ id } "
        "fieldValidators{ field message } } }" % dto
    )
    model_id = _entity_id(model["embeddingModel"], "embeddingModel")
    gql("mutation { enableEmbeddingModel(id:%s){ id } }" % model_id)
    print("provision: embedding model %s (ollama %s) id %s enabled"
          % (env("MODEL_NAME"), env("OLLAMA_MODEL"), model_id))


def _content_field():
    """L'id di document.content: con provisioning SYSTEM i doc type nascono
    dal /sample del connettore, in modo asincrono, alla creazione del driver."""
    for _ in range(30):
        fields = gql("{ docTypeFields(first:500){ edges{ node{ id path } } } }")
        by_path = {e["node"]["path"]: e["node"]["id"] for e in fields["docTypeFields"]["edges"]}
        if "document.content" in by_path:
            return by_path["document.content"]
        time.sleep(2)
    sys.exit("provision: docTypeField document.content not found")


def cmd_provision(_args):
    tm, schema, vhost = env("TM_URL"), env("SCHEMA"), env("VHOST")
    fake_host, pd_name, ds_name = env("FAKE_HOST"), env("PD_NAME"), env("DS_NAME")

    _create_tenant(tm, schema, vhost)
    status, body = _request("POST", tm + "/provisioning/initTenant", {"tenantName": schema})
    if status != 200:
        sys.exit("provision: initTenant failed (%s): %s" % (status, body))

    _embedding_model()

    pd = gql(
        'mutation { pluginDriver(pluginDriverDTO:{'
        'name:"%s", description:"e2e ollama multimodal scenario", '
        'type:HTTP, provisioning:SYSTEM, '
        'resourceUri:{baseUri:"http://%s:8080", path:"/invoke"}'
        '}){ entity{id} fieldValidators{field message} } }' % (pd_name, fake_host)
    )
    pd_id = _entity_id(pd["pluginDriver"], "pluginDriver")

    ds = gql(
        'mutation { datasource(datasourceDTO:{'
        'name:"%s", description:"e2e ollama multimodal scenario", '
        'schedulable:false, reindexable:false, jsonConfig:"{}"'
        '}){ entity{id} fieldValidators{field message} } }' % ds_name
    )
    ds_id = _entity_id(ds["datasource"], "datasource")
    gql(
        "mutation { bindPluginDriverToDatasource(datasourceId:%s, pluginDriverId:%s)"
        "{ left{id} right{id} } }" % (ds_id, pd_id)
    )
    print("provision: plugin driver %s bound to datasource %s" % (pd_id, ds_id))

    field_id = _content_field()
    index = gql(
        "mutation { dataIndex(datasourceId:%s, dataIndexDTO:{"
        'name:"e2e-ollama-knn", knnIndex:true, '
        "chunkType:CHUNK_TYPE_CHARACTER_TEXT_SPLITTER, chunkWindowSize:0, "
        "embeddingDocTypeFieldId:%s}){ entity{ id } "
        "fieldValidators{ field message } } }" % (ds_id, field_id)
    )
    index_id = _entity_id(index["dataIndex"], "dataIndex")
    gql("mutation { bindDataIndexToDatasource(datasourceId:%s, dataIndexId:%s)"
        "{ left{ id } } }" % (ds_id, index_id))
    print("provision: knn data index %s on document.content bound" % index_id)
    # ultima riga: l'id del datasource, letto da run.sh
    print(ds_id)


# --------------------------------------------------------------------------- #
# check
# --------------------------------------------------------------------------- #
def _wire_bucket(ds_id):
    """Default Bucket del tenant: il datasource agganciato (ora che l'indice
    fisico esiste), retrieveType KNN (la query con immagine lo richiede),
    autocorrection staccata, 10 vicini per il parser KNN."""
    buckets = gql("{ buckets(first:10){ edges{ node{ id name searchConfig{ id "
                  "queryParserConfigs(first:20){ edges{ node{ id name type "
                  "description jsonConfig } } } } } } } }")["buckets"]["edges"]
    bucket = next(e["node"] for e in buckets if e["node"]["name"] == "Default Bucket")
    gql("mutation { addDatasourceToBucket(bucketId:%s, datasourceId:%s){ left{ id } } }"
        % (bucket["id"], ds_id))
    _entity_id(gql(
        "mutation { bucket(id:%s, patch:true, bucketDTO:{name:\"Default Bucket\", "
        "description:\"\", retrieveType:KNN, refreshOnDate:false, refreshOnQuery:false, "
        "refreshOnSuggestionCategory:false, refreshOnTab:false}){ entity{ id } "
        "fieldValidators{ field message } } }" % bucket["id"])["bucket"], "bucket")
    gql("mutation { unbindAutocorrectionFromBucket(bucketId:%s){ left{ id } } }" % bucket["id"])
    config = bucket.get("searchConfig") or {}
    for edge in (config.get("queryParserConfigs") or {}).get("edges", []):
        parser = edge["node"]
        if parser["type"] != "KNN":
            continue
        json_config = json.loads(parser["jsonConfig"] or "{}")
        json_config["kNeighbors"] = 10
        gql("mutation { queryParserConfig(searchConfigId:%s, queryParserConfigId:%s, "
            "patch:true, queryParserConfigDTO:{name:%s, type:\"KNN\", description:%s, "
            "jsonConfig:%s}){ entity{ id } } }"
            % (config["id"], parser["id"], json.dumps(parser["name"]),
               json.dumps(parser["description"] or ""), json.dumps(json.dumps(json_config))))


def _chunks(os_url, schema):
    st, body = _request(
        "POST", "%s/%s-*/_search" % (os_url, schema),
        {"size": 100, "query": {"match_all": {}}},
    )
    return [h["_source"] for h in body.get("hits", {}).get("hits", [])] if st == 200 else []


def _vector(source):
    for key, value in source.items():
        if isinstance(value, list) and len(value) > 16 and all(
            isinstance(x, (int, float)) for x in value
        ):
            return key, value
    return None, None


def _search(text=None, image=None):
    token = {"tokenType": "KNN", "values": [text] if text else []}
    if image:
        with open(image, "rb") as fh:
            token["media"] = {"data": base64.b64encode(fh.read()).decode(),
                              "contentType": "image/jpeg"}
    vhost = env("VHOST")
    out = subprocess.run(
        ["curl", "-sk", "--resolve", "%s:443:127.0.0.1" % vhost, "-X", "POST",
         env("SEARCH_URL"), "-H", "Content-Type: application/json", "-d", "@-"],
        input=json.dumps({"searchQuery": [token], "range": [0, 20]}),
        capture_output=True, text=True, timeout=180,
    )
    try:
        payload = json.loads(out.stdout)
    except ValueError:
        return None, "risposta non JSON: %s" % out.stdout[:200]
    if "result" not in payload:
        return None, "errore dal searcher: %s" % json.dumps(payload)[:200]
    return payload["result"], None


def _image_ranking(result):
    """(contentId, pagina) dei chunk-immagine, nell'ordine del searcher."""
    ranking = []
    for hit in result:
        source = hit.get("source") or {}
        binary = source.get("binary") or {}
        if (binary.get("contentType") or "").startswith(("image/", "application/pdf")):
            page = (binary.get("metadata") or {}).get("page")
            ranking.append((source.get("contentId"), page))
    return ranking


def cmd_check(args):
    os_url, schema = env("OS_URL"), env("SCHEMA")
    checks = []

    def add(group, name, expected, actual):
        checks.append((group, name, expected, actual, expected == actual))

    chunks = _chunks(os_url, schema)
    if not chunks:
        print("CHECK ABORT: nessun chunk in %s-*" % schema)
        return 1

    def of(content_id):
        return [c for c in chunks if c.get("contentId") == content_id]

    def media(content_id):
        return [c for c in of(content_id) if (c.get("binary") or {}).get("fileId")
                or (c.get("binary") or {}).get("contentType")]

    size = int(env("VECTOR_SIZE"))
    for content_id in (env("SENTINA_CONTENT_ID"), env("OTHER_CONTENT_ID")):
        group = "IMMAGINE %s" % content_id
        images = media(content_id)
        add(group, "un chunk per l'immagine", 1, len(images))
        add(group, "contentType del chunk", "image/jpeg",
            images[0]["binary"].get("contentType") if images else None)
        add(group, "vettore di %d dimensioni" % size, size,
            len(_vector(images[0])[1] or []) if images else None)
        add(group, "chunk di testo presente", True,
            any(not (c.get("binary") or {}) for c in of(content_id)))

    pages = media(env("PDF_CONTENT_ID"))
    add("PDF", "un chunk per pagina", 3, len(pages))
    add("PDF", "numeri di pagina", [1, 2, 3],
        sorted((p["binary"].get("metadata") or {}).get("page") for p in pages))
    add("PDF", "vettori di %d dimensioni" % size, [size] * 3,
        [len(_vector(p)[1] or []) for p in pages])

    if args.wire_bucket:
        _wire_bucket(args.ds_id)
        time.sleep(3)

    query_image = os.path.join(env("CORPUS_DIR"), env("QUERY_IMAGE"))
    sentina, other = env("SENTINA_CONTENT_ID"), env("OTHER_CONTENT_ID")

    result, error = _search(image=query_image)
    ranking = _image_ranking(result) if result else []
    print("\nquery immagine (ritaglio di p019) -> chunk-immagine: %s" % (ranking or error))
    add("RICERCA immagine", "risponde", None, error)
    add("RICERCA immagine", "il primo e' la pagina 19 (immagine o pagina 2 del PDF)", True,
        bool(ranking) and ranking[0] in ((sentina, None), (env("PDF_CONTENT_ID"), 2)))
    add("RICERCA immagine", "pagina-sentina prima di pagina-altra", True,
        (sentina, None) in ranking and (other, None) in ranking
        and ranking.index((sentina, None)) < ranking.index((other, None)))

    result, error = _search(text="schema dell'impianto di sentina")
    ranking = _image_ranking(result) if result else []
    print("query testo 'schema dell'impianto di sentina' -> chunk-immagine: %s" % (ranking or error))
    add("RICERCA testo", "risponde", None, error)
    add("RICERCA testo", "immagine pertinente prima della non correlata", True,
        (sentina, None) in ranking and (other, None) in ranking
        and ranking.index((sentina, None)) < ranking.index((other, None)))

    result, error = _search(text="schema dell'impianto di sentina", image=query_image)
    ranking = _image_ranking(result) if result else []
    print("query testo + immagine -> chunk-immagine: %s" % (ranking or error))
    add("RICERCA testo+immagine", "risponde (embed_mixed)", None, error)
    add("RICERCA testo+immagine", "il primo e' la pagina 19", True,
        bool(ranking) and ranking[0] in ((sentina, None), (env("PDF_CONTENT_ID"), 2)))

    add("LOG", "client ollama-multimodal nel modulo di embedding", True, args.log_client == "true")

    width = max(len(name) for _, name, _, _, _ in checks) + 2
    current = None
    passed = 0
    for group, name, expected, actual, ok in checks:
        if group != current:
            print("\n== %s ==" % group)
            current = group
        passed += 1 if ok else 0
        line = "  [%s] %-*s" % ("PASS" if ok else "FAIL", width, name)
        if ok:
            print(line)
        else:
            print("%s\n         atteso : %s\n         reale  : %s"
                  % (line, json.dumps(expected), json.dumps(actual)))

    print("\n---------------------------------------------")
    print("RISULTATO: %d/%d assert superate" % (passed, len(checks)))
    ok_all = passed == len(checks)
    print("ESITO: %s" % ("PASS" if ok_all else "FAIL"))
    return 0 if ok_all else 1


def main():
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("clean")
    sub.add_parser("provision")
    p_check = sub.add_parser("check")
    p_check.add_argument("--ds-id", required=True)
    p_check.add_argument("--log-client", default="false")
    p_check.add_argument("--no-wire-bucket", dest="wire_bucket", action="store_false")
    args = parser.parse_args()

    if args.cmd == "clean":
        cmd_clean(args)
    elif args.cmd == "provision":
        cmd_provision(args)
    elif args.cmd == "check":
        sys.exit(cmd_check(args))


if __name__ == "__main__":
    main()
