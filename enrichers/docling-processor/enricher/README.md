## Title and Short Description
An enricher that asynchronously processes documents (using Docling), extracts their Markdown content, and sends the result back to an Openk9 pipeline callback.

## Description

This enricher asynchronously proces documents with a pipeline built on FastAPI.
Given a payload by Openk9, it retrieves binary resources from a file-manager service, converts supported documents through Docling into Markdown format, and posts the processed result back to a callback URL (`replyTo`).

The transformation is executed in a background thread so the API returns immediately while the processing continues.

Main features:

- Fetches binary files from an external File Manager

- Converts documents using Docling (`.docx`, `.pdf`, and other supported formats)

- Sends results to an Openk9 callback endpoint

- Health check endpoint

- Configuration schema endpoint for Openk9 UI

## Quickstart

ALTRO?

Environment variables expected:

```
FM_HOST=http://localhost:8000     # File Manager host
S_HOST=http://localhost:8001      # Openk9 callback host
```

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

- **Returns immediately :**

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

???

## License

???