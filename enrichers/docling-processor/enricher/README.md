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

## Quickstart

The prerequisites for this enricher are:
- A running File Manager service
- An Openk9 pipeline callback endpoint

Optional: for local testing, the callback endpoint can be mocked using any HTTP listener.

Note: this Quickstart assumes the external services are already available.

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

### **GET /form :**

Returns the configuration form schema required by Openk9 connectors.

Current implementation:

```json
{"fields": []}
```

---

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