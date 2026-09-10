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