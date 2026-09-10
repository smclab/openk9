## Title and Short Description
Docling processor is an enricher that asynchronously processes documents (using Docling), extracts their Markdown content, and sends the result back to an Openk9 pipeline callback.

## Description

This enricher asynchronously process documents with a pipeline built on FastAPI.
Given a payload by Openk9, it retrieves binary resources from a file-manager service, converts supported documents through Docling into Markdown format, and post the processed result back to a callback URL (`replyTo`).

The transformation is executed in a background thread so the API returns immediately while the processing continues.

Main features:

- Fetches binary files from an external File Manager

- Converts documents using Docling (`.docx`, `.pdf`, and other supported formats)

- Sends results to an Openk9 callback endpoint

- Health check endpoint

- Configuration schema endpoint for Openk9 UI

## OCR and pipeline options

Docling defaults the PDF pipeline's OCR to `OcrAutoOptions`, which picks an
engine by probing the environment and forwards only `mode` to it, dropping the
configured language. The processor therefore pins EasyOCR, so the enrich item's
`pipeline_options.ocr_options.lang` stays effective.

Two OCR fields changed with Docling 2.126:

| before | now |
| --- | --- |
| `ocr_options.force_full_page_ocr` | `ocr_options.mode` (`default`, `full_page`, `layout_regions`, `pdf_aware_layout_regions`) |
| `ocr_options.bitmap_area_threshold` | `ocr_options.scale` (default 3.0) |

Docling still accepts `force_full_page_ocr` as a deprecated alias of
`mode=full_page`, so existing enrich items keep working.

Formats whose backend needs an install extra this image does not ship
(OpenDocument, XBRL, audio, video) and the two Docling can only read from a
file rather than a stream (USPTO patents, METS-GBS archives) are rejected up
front by `SUPPORTED_FORMATS` in `app/utils/pipeline_options.py`, rather than
failing deep inside the backend.

## Quickstart

### OpenK9 Setup
<!-- Include this in the Docker Compose:

```bash
docling-processor:
        build:
            context: .
            args:
                MODE: cpu
        env_file: .env
        ports:
            - "5000:5000"
``` -->

The prerequisites for this enricher are:
- A running File Manager service
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
4. Run the following commands:
    ```bash
    #Docling Processor
    uvicorn app.server:app --host 0.0.0.0 --port 8002

    #File Manager mockup
    uvicorn external.file_manager:app --host 0.0.0.0 --port 8000

    #OpenK9 mockup
    uvicorn external.doc_server:app --host 0.0.0.0 --port 8001
    ```
5. Go to http://localhost:8001/docs and send a payload with your file

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
* Downloads them from `FM_HOST`
* Converts them into Markdown with Docling
* Sends results to:

    `POST {S_HOST}/api/datasource/pipeline/callback/{replyTo}`

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
FM_HOST=http://localhost:8000     # File Manager host
S_HOST=http://localhost:8001      # Openk9 callback host
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