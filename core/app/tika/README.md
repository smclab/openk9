# Tika

Apache Tika-based document parser, packaged as an OpenK9 enrich item. It extracts plain text and metadata from binary files (PDF, Office, ODF, ePub, email, images, archives, ...) fetched from the OpenK9 file-manager, and writes the result back onto the enrichment payload.

## Role in the enrichment pipeline

The service runs as a Quarkus application with a single processing endpoint, `POST /process`, consumed by the OpenK9 datasource pipeline. For each message:

1. Reads `payload.resources.binaries[0]` (the binary descriptor produced upstream by a connector / file-manager step).
2. Downloads the binary from file-manager by `resourceId`, scoped to the tenant schema taken from `payload.tenantId`.
3. Detects the MIME type via the Tika `Detector` chain configured in `src/main/resources/tika-config.xml`.
4. If the detected MIME type is present in `type_mapping`, parses the file, cleans the extracted text, and enriches the payload:
   - appends the mapped alias to `payload.documentTypes`;
   - writes `payload.document.content` (truncated by `max_length`) and `payload.rawContent`;
   - optionally sets `payload.document.contentType` (from `include_content_type`);
   - optionally sets `payload.file.lastModifiedDate` (from `include_last_modified_date`);
   - builds a summary trimmed to `summary_length`.
5. Publishes the resulting message to `replyTo` for the next pipeline step.

Files whose MIME type is not in `type_mapping` are skipped.

## admin-ui integration

When selected as an enrich item in admin-ui, the component exposes `GET /form` returning the form schema used to render its configuration UI — the same contract used by the other OpenK9 enrich items (e.g. `docling-processor`). The JSON saved through that form is passed verbatim as `enrichItemConfig` in each message to `POST /process`.

## Configuration parameters (`/form`)

All parameters are read from `enrichItemConfig` on each incoming message. Defaults shown here match the runtime defaults hard-coded in `TikaProcessor`.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type_mapping` | `stringMap` (MIME → alias) | see `src/main/resources/form/form.json` | Whitelist of MIME types the component will parse. Each value is the alias appended to `documentTypes`. |
| `retain_binaries` | `checkbox` | `false` | If true, the original binary is retained on the outgoing payload after parsing. |
| `summary_length` | `number` | `5000` | Maximum length of the document summary, in characters. `-1` disables the cap. |
| `max_length` | `number` | `100000` | Maximum length of extracted text written to `document.content`. `-1` disables truncation. |
| `include_last_modified_date` | `checkbox` | `true` | If true and present in metadata, writes `file.lastModifiedDate`. |
| `include_content_type` | `checkbox` | `true` | If true, writes the detected `document.contentType`. |

The canonical schema lives in [`src/main/resources/form/form.json`](src/main/resources/form/form.json) and is served as-is by `GET /form`.

## Runtime dependencies

- **File-manager** — binaries are downloaded through it (`fileManagerClient.download(resourceId, schemaName)`).
- **RabbitMQ / message broker** — the enrichment pipeline delivers messages and consumes the `replyTo` queue.
- **Tika parsers** — the active parser set is defined in `src/main/resources/tika-config.xml`. Anything not listed there will not be parsed even if mapped in `type_mapping`.

### Key Quarkus properties

| Property | Default | Purpose |
|---|---|---|
| `tika.pool.size` | `4` | Size of the thread pool that executes `TikaProcessor.process()` concurrently (see `ProcessEndpoint`). |

Standard Quarkus properties (`quarkus.http.port`, logging, file-manager REST client URL, etc.) are defined in `src/main/resources/application.properties`.

## Running locally

From the `openk9` repository root:

```bash
# Build just this module (shared core deps are built automatically)
./k9.sh build tika

# Start the stack (file-handling overlay brings up file-manager + MinIO)
./k9.sh up --with=file-handling

# Restart only this service after a rebuild
./k9.sh restart tika

# Follow logs
./k9.sh logs tika
```

Smoke-test the form endpoint:

```bash
curl -s http://localhost:<tika-port>/form | jq '.fields[].name'
```
