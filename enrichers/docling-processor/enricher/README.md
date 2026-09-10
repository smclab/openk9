## Title and Short Description
Docling processor is an enricher that asynchronously processes documents (using Docling), extracts their Markdown content, and sends the result back to an Openk9 pipeline callback.

## Description

This enricher asynchronously process documents with a pipeline built on FastAPI.
Given a payload by Openk9, it retrieves binary resources from the pre-signed URL carried in the payload, converts supported documents through Docling into Markdown format, and post the processed result back to a callback URL (`replyTo`).

The transformation is executed in a background thread so the API returns immediately while the processing continues.

Main features:

- Fetches binary files from the pre-signed URL carried in the payload

- Converts documents using Docling (see [Supported formats](#supported-formats))

- Sends results to an Openk9 callback endpoint

- Health check endpoint

- Configuration schema endpoint for Openk9 UI

## Supported formats

The format of each binary is detected from its bytes, falling back to the
extension of the binary's `name` for the text-based formats that are
indistinguishable as bytes (Markdown, e-mail, LaTeX, WebVTT, AsciiDoc).

Converted formats:

| Family | Formats |
| --- | --- |
| Office | `docx`, `doc`, `pptx`, `ppt`, `xlsx`, `xls` |
| PDF and images | `pdf`, `image` (jpg, png, tiff, bmp, webp) |
| Text and markup | `md`, `asciidoc`, `html`, `latex`, `csv`, `email`, `epub`, `boxnote`, `ebcdic` |
| XML | `xml_jats`, `xml_doclang`, `dclx` |
| Docling and subtitles | `json_docling`, `vtt` |
| Apple | `iwork_pages` |

Docling also knows OpenDocument (`odt`, `ods`, `odp`), XBRL (`xml_xbrl`),
audio and video, but their backends need install extras this image does not
ship (`odfdo`, `arelle-release`, `whisper`/`librosa`). Adding one means adding
the matching extra to the three `requirements*.in` and to `SUPPORTED_FORMATS`
in `app/utils/pipeline_options.py`.

USPTO patents (`xml_uspto`) and METS-GBS archives (`mets_gbs`) are left out for
a different reason: Docling converts them from a file but not from a stream,
because its format detection and those two backends read the stream without
rewinding it, and this enricher only ever holds a stream.

All of these are rejected up front rather than failing deep inside the
backend.

**OCR engine.** Docling defaults the PDF pipeline to `OcrAutoOptions`, which
picks an engine by probing the environment and does not forward the configured
language to it. The processor therefore pins EasyOCR, so the enrich item's
`pipeline_options.ocr_options.lang` stays effective. `force_full_page_ocr` was
superseded by `pipeline_options.ocr_options.mode`; Docling still accepts the
old name as an alias for `mode=full_page`.

## Quickstart

### OpenK9 Setup

The enricher runs as part of the OpenK9 stack. Bring it up with the
`file-handling` profile, which provides the full pre-signed-URL chain
(MinIO, ingestion, datasource, Docling, and Tika):

```bash
./k9.sh up --with=file-handling
```

The prerequisite for this enricher is:
- An Openk9 pipeline callback endpoint

> **Build platform note:** the `cpu` mode pins `torch`/`torchvision` `+cpu`
> wheels that are published only for `linux/amd64` (it runs under emulation on
> Apple Silicon, so build and startup are slower).
 
### Local Setup
To run the enricher in local you have to:
1. Create a virtual environment from the ernicher folder project, run:
    ```bash
    python -m venv venv
    ```
2. Activate the virtual environment:
    ```bash
    #On macOS / Linux
    source venv/bin/activate

    #On Windows (PowerShell)
    venv\Scripts\Activate

    #On Windows (cmd)
    venv\Scripts\activate.bat
    ```
3. Install the requirements:
    ```bash 
    #CPU only mode
    pip install -r requirements_cpu.txt

    #GPU mode
    pip install -r requirements.txt
    ```
4. Start the two services, each in its own terminal:
    ```bash
    #Docling Processor
    uvicorn app.server:app --host 0.0.0.0 --port 8002

    #OpenK9 datasource/callback mock (receives the result on DATASOURCE_HOST)
    uvicorn external.doc_server:app --host 0.0.0.0 --port 8001
    ```
5. Serve a sample binary over HTTP so Docling can fetch it. Drop a document
   (for example `sample.pdf`) into a directory and start Python's built-in
   static server from that directory:
    ```bash
    cd /path/to/sample-dir
    python -m http.server 8003
    ```
   The file is now reachable at `http://localhost:8003/sample.pdf`.
6. Trigger a task on the processor. Open the Docling Swagger UI at
   http://localhost:8002/docs, call `POST /start-task/`, and set each binary's
   `url` to the static-server URL from the previous step:
    ```json
    {
      "payload": {
        "tenantId": "mrossi",
        "resources": {
          "binaries": [
            {"resourceId": "doc_1", "url": "http://localhost:8003/sample.pdf"}
          ]
        }
      },
      "enrichItemConfig": {},
      "replyTo": "fake-token"
    }
    ```
   Docling fetches each binary with a plain GET on its `url`, converts it, and
   posts the Markdown result to
   `{DATASOURCE_HOST}/api/datasource/pipeline/callback/{replyTo}` on :8001,
   where the mock prints it. Any HTTP source works, so the static server can be
   swapped for any host reachable from the processor.

## API Reference

### **POST /start-task/ :**

Starts the background document-processing task.

**Request Body**

```json
{
  "payload": 
    {
        "tenantId": "TENANT ID",
        "resources": {
            "binaries": [
                {"resourceId": "RESOURCE_ID", "metadata_vari": "METADATA"},
                {"resourceId": "RESOURCE_ID", "metadata_vari": "METADATA"},
                {"resourceId": "RESOURCE_ID", "metadata_vari": "METADATA"},
            ]
        },
    },
  "enrichItemConfig": { ... },
  "replyTo": "callbackToken"
}
```

**Behavior**

* Extracts binary resources from the Openk9 payload
* Downloads each binary from the pre-signed `url` in the payload
* Converts them into Markdown with Docling
* Sends results to:

    `POST {DATASOURCE_HOST}/api/datasource/pipeline/callback/{replyTo}`

- Returns immediately :

  ```json
  {"status": "ok", "message": "Process started"}
  ```

---

### **GET /health :**

Simple health-check endpoint.
Returns:

```json
{"status": "UP"}
```

---

## Enrich Item Configuration

The processor does not expose a configuration form endpoint: the `jsonConfig`
of the enrich item is written as raw JSON in the Openk9 admin UI and reaches
the processor unchanged in the `enrichItemConfig` field of `POST /start-task/`.

**Top-level keys**

| Key | Applied to |
|---|---|
| `pipeline_options` | the pipeline options of the Docling format option selected for the detected extension |
| `backend_options` | reserved, currently inert: no Docling format option exposes it (they expose `pipeline_options`, `backend`, `pipeline_cls`), so the key is silently ignored |
| `error_strategy` | read by the processor itself, not forwarded to Docling |

Keys that the target options object does not expose are skipped with a debug
log, so an unknown or misspelled option never fails the conversion.

**Notations**

Both a nested object and flat dot-separated keys are accepted; the flat form is
expanded before being applied. These two configurations are equivalent:

```json
{
  "pipeline_options": {
    "do_ocr": true,
    "ocr_options": { "lang": ["it", "en"] }
  }
}
```

```json
{
  "pipeline_options.do_ocr": true,
  "pipeline_options.ocr_options.lang": ["it", "en"]
}
```

Do not mix the two notations on the same path: writing both
`"pipeline_options"` as an object and `"pipeline_options.do_ocr"` raises
`Conflict at key` and the conversion fails.

**Error strategy**

`error_strategy` only affects payloads carrying more than one binary:

| Value | Behavior |
|---|---|
| `fail-soft` | a failing binary is marked with an `error` field, the others are still processed |
| `fail-fast` | the first failure invalidates the whole payload (default) |

**Examples**

PDF, with OCR and table structure:

```json
{
  "error_strategy": "fail-soft",
  "pipeline_options": {
    "do_ocr": true,
    "do_table_structure": true,
    "document_timeout": 120,
    "accelerator_options": { "num_threads": 4, "device": "cpu" },
    "ocr_options": { "lang": ["it", "en"], "force_full_page_ocr": false }
  }
}
```

Images, with picture description (images use the PDF pipeline):

```json
{
  "pipeline_options": {
    "do_ocr": true,
    "do_picture_description": true,
    "generate_page_images": true,
    "images_scale": 2,
    "picture_description_options": { "picture_area_threshold": 0.001 }
  }
}
```

Audio, transcribed through the ASR pipeline:

```json
{
  "pipeline_options": {
    "asr_options": {
      "repo_id": "openai/whisper-small",
      "timestamps": true,
      "temperature": 0
    }
  }
}
```

HTML and PPTX go through the simple pipeline, which accepts only the generic
options:

```json
{
  "pipeline_options": { "document_timeout": 60 }
}
```

The option names are those of the Docling pipeline options; see the upstream
documentation for the full set and for the `DocumentConverter` behavior:
<https://docling-project.github.io/docling/>

## Configuration

Environment variables expected:

```
DATASOURCE_HOST=http://localhost:8001   # Openk9 datasource/callback host
```
## License

Copyright (c) the respective contributors, as shown by the AUTHORS file.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program. If not, see http://www.gnu.org/licenses/.