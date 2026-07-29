## Title and Short Description
A microservice that splits text into chunks using multiple strategies, and generates embeddings using OpenAI, Ollama, IBM Watsonx, Google Vertex AI.

## Description

The OpenK9 Embedding Module is the service responsible for transforming text into vector embeddings.

It is an essential component of OpenK9’s ingestion pipeline, where documents are:

1. Chunked according to configured strategies

2. Embedded using pluggable vector models

3. Returned as structured embedding chunks to be indexed into OpenK9

This module provides:

- Multiple chunking strategies, such as:
    - `Recursive`
    - `Sentence-based`
    - `Token-based`
    - `Semantic`
    - `Table-aware`
    - `Neural chunking`

- Provider-agnostic embedding generation, supporting:
    - `OpenAI`
    - `Ollama local models`
    - `IBM Watsonx`
    - `Google Vertex AI`
    - `Hugging Face`
    - `AWS Bedrock`

- Multimodal indexing — text and images embedded into the same vector space via the `EmbedContent` streaming RPC

- Cross-modal query — a single query vector from text and/or an inline image via the `EmbedQuery` unary RPC

- gRPC interface used internally by OpenK9 services

- Health checks and reflection for easier integration


## Quickstart

### OpenK9 Setup
```bash
rag-module:
    image: smclab/openk9-rag-module:2026.1.0-SNAPSHOT
    container_name: rag-module
    environment:
        ORIGINS: '*'
        OPENSEARCH_USERNAME: 'opensearch'
        OPENSEARCH_HOST: 'opensearch:9200'
        GRPC_DATASOURCE_HOST: 'datasource:9000'
        GRPC_TENANT_MANAGER_HOST: 'tenant-manager:9000'
        GRPC_EMBEDDING_MODULE_HOST: 'embedding-module:5000'
        KEYCLOAK_URL: 'http://keycloak.openk9.localhost:8081'
        UPLOAD_DIR: 'uploads'
        UPLOAD_FILE_EXTENSIONS: '[".pdf",".md",".docx",".xlsx",".pptx",".csv"]'
        MAX_UPLOAD_FILE_SIZE: '10'
        MAX_UPLOAD_FILES_NUMBER: '5'

embedding-module:
    image: smclab/openk9-embedding-module-base:2026.1.0-SNAPSHOT
    container_name: embedding-module
    environment:
        ORIGINS: '*'
```

### Local Setup
To run the embedding model in local you have to:
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
    python server.py
    ```
5. Modify the client.py file and then run:
    ```bash
    python client.py
    ```

## API Reference

The service exposes three RPCs: `GetMessages` (v1, text-only),
`EmbedContent` (v2, server-streaming — text and/or media in one vector space)
and `EmbedQuery` (v2, unary — a single query vector from text and/or an inline
image, in the same vector space as the index).

### GetMessages (v1)

#### Request: EmbeddingRequest

The request contains:

- Chunking configuration

- Embedding model configuration

- Input text

#### Fields:

| Field            | Type             | Description                           |
| ---------------- | ---------------- | ------------------------------------- |
| `chunk`          | `ChunkConfig`    | Configuration for text splitting      |
| `embeddingModel` | `EmbeddingModel` | Embedding provider and model settings |
| `text`           | `string`         | Input text to be processed            |

#### Client Example:
Imports
```python
import embedding_pb2
import embedding_pb2_grpc
import grpc
from google.protobuf.struct_pb2 import Struct
```
Chunk configs:
```python
jsonConfig = Struct()
jsonConfig.update(
    {
        "separator": ".",
        "size": 100,
        "overlap": 20,
        "model_name": "gpt-4",
        "encoding": "cl100k_base",
    }
)
chunk = {"type": 1, "jsonConfig": jsonConfig}
```
Embedding model configs:
```python
jsonConfig = Struct()
jsonConfig.update(
    {
        "api_url": "api_url",
        "watsonx_project_id": "watsonx_project_id",
        "chat_vertex_ai_model_garden": "chat_vertex_ai_model_garden",
    }
)
providerModel = {"provider": "openai", "model": "text-embedding-3-small"}
embeddingModel = {
    "apiKey": "apikey",
    "providerModel": providerModel,
    "jsonConfig": jsonConfig,
}
```
Test config:
```python
text = "Nel mezzo del cammin di nostra vita ..."
```
gRPC call:
```python
with grpc.insecure_channel("localhost:5000") as channel:
        stub = embedding_pb2_grpc.EmbeddingStub(channel)
        response = stub.GetMessages(
            embedding_pb2.EmbeddingRequest(
                chunk=chunk, embeddingModel=embeddingModel, text=text
            )
        )
    print(f"Chunks: {response.chunks}")

```
**Behavior**
- Input text is cleaned

- Text is split using the selected chunking strategy (See Configuration)

- Each chunk is embedded using the configured embedding model

- Embeddings are returned in order with metadata

### EmbedContent (v2)

Indexes text and/or binaries into the same vector space and streams one
`EmbeddedChunk` per piece. `text` and `refs` are combinable.

#### Request: EmbedContentRequest

| Field            | Type                | Description                                                          |
| ---------------- | ------------------- | ------------------------------------------------------------------- |
| `tenantId`       | `string`            | Tenant that owns the request (used in the structured logs)          |
| `chunk`          | `RequestChunk`      | Text chunking configuration (see Configuration)                     |
| `embeddingModel` | `EmbeddingModel`    | Provider/model settings; `multimodal=true` enables image embedding  |
| `vectorDataType` | `VectorDataType`    | Output encoding: `FLOAT32` (0), `BYTE` (1), `BINARY` (2)            |
| `text`           | `string` (optional) | Inline text to embed                                                |
| `refs`           | `MediaRef` repeated | Binaries to fetch and embed                                         |

At least one of `text` / `refs` must be set, otherwise the RPC fails with
`INVALID_ARGUMENT`.

`MediaRef` fields: `url` (short-lived pre-signed GET URL — the module only does
a plain GET, no storage credentials), `fileId` (copied onto every chunk derived
from it), `contentType` (drives the modality; the HTTP `Content-Type` is used as
a fallback when absent).

#### Response: stream of EmbeddedChunk

| Field                | Type                | Description                                          |
| -------------------- | ------------------- | ---------------------------------------------------- |
| `number`             | `int32`             | 1-based, progressive over the whole stream           |
| `total`              | `int32` (optional)  | Total chunk count; set only for a text-only request  |
| `text`               | `string`            | Chunk text (empty for image chunks)                  |
| `fileId`             | `string` (optional) | Source binary; absent for inline-text chunks         |
| `vectorDataType`     | `VectorDataType`    | Same as requested                                    |
| `dimension`          | `int32`             | Vector dimension                                     |
| `f32` / `i8` / `bits`| `oneof vector`      | The vector, encoded according to `vectorDataType`    |

**Behavior**
- Text is chunked and embedded first, then each ref in list order; `number` is
  progressive over the whole stream.

- Vectors are L2-normalized, then quantized according to `vectorDataType`.

- A ref whose modality has no handler (audio/video/unknown) or that the model
  cannot embed (image on a text-only model, undecodable binary, provider limits)
  is skipped: no chunk for that `fileId`, a structured log line, the stream
  continues with the other refs.

- An error while embedding the inline `text` is fail-fast: the RPC ends with the
  gRPC `INTERNAL` status (same as `GetMessages`).

- Image embedding requires a multimodal model (`embeddingModel.multimodal=true`)
  on a provider with a multimodal embedder (AWS Bedrock, Google Vertex AI); other
  models keep the text-only path and skip image refs.

#### Client Example

See `app/client_multimodal.py` for a runnable smoke client (text + image/audio
refs, Vertex or Bedrock selected via `MM_PROVIDER`). Minimal call:

```python
request = embedding_pb2.EmbedContentRequest(
    tenantId="mew",
    chunk=embedding_pb2.RequestChunk(type=1, jsonConfig=chunk_json_config),
    embeddingModel=embedding_pb2.EmbeddingModel(
        multimodal=True,
        providerModel=embedding_pb2.ProviderModel(
            provider="chat_vertex_ai", model="multimodalembedding@001"
        ),
        jsonConfig=model_json_config,
    ),
    vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_FLOAT32,
    text="OpenK9 is an open source search platform.",
    refs=[
        embedding_pb2.MediaRef(
            url="https://.../signed-get-url", fileId="img-1", contentType="image/jpeg"
        )
    ],
)

for chunk in stub.EmbedContent(request):
    print(chunk.number, chunk.fileId or "-", chunk.dimension)
```

### EmbedQuery (v2)

Embeds a query into a single `EmbeddedVector`, quantized with the same
`vectorDataType` as the index so it is directly KNN-comparable. No chunking, no
storage I/O. `text` and `inline` are combinable.

#### Request: EmbedQueryRequest

| Field            | Type                  | Description                                                         |
| ---------------- | --------------------- | ------------------------------------------------------------------- |
| `tenantId`       | `string`              | Tenant that owns the request (used in the structured logs)          |
| `embeddingModel` | `EmbeddingModel`      | Provider/model settings; `multimodal=true` enables image embedding  |
| `vectorDataType` | `VectorDataType`      | Output encoding: `FLOAT32` (0), `BYTE` (1), `BINARY` (2)            |
| `text`           | `string` (optional)   | Query text                                                          |
| `inline`         | `InlineMedia` (optional) | Query image sent inline (`data` bytes + `contentType`, images only) |

At least one of `text` / `inline` must be set, otherwise the RPC fails with
`INVALID_ARGUMENT`.

#### Response: EmbeddedVector

| Field                | Type             | Description                                       |
| -------------------- | ---------------- | ------------------------------------------------- |
| `vectorDataType`     | `VectorDataType` | Same as requested                                 |
| `dimension`          | `int32`          | Vector dimension                                  |
| `f32` / `i8` / `bits`| `oneof vector`   | The vector, encoded according to `vectorDataType` |

**Behavior**
- A query returns exactly one vector, so there is no skip: a capability the
  model lacks is an error, not a degraded result.

- `text` → a query-time text embedding. `inline` → an image embedding in the
  same space as the indexed documents.

- `text + inline` is produced **only** by a model with native mixed input (text
  and image in one call, one vector — e.g. Cohere Embed v4 on AWS Bedrock); there
  is no module-side fusion of two separate vectors.

- The vector is L2-normalized, then quantized according to `vectorDataType`.

- Error mapping: empty request or non-image `inline` → `INVALID_ARGUMENT`; image
  on a text-only model or `text + inline` on a model without native mixed input
  → `FAILED_PRECONDITION`; a provider error → gRPC `INTERNAL`.

#### Client Example

See `app/client_multimodal.py` with `MM_MODE=query` for a runnable smoke client.
Minimal call:

```python
vector = stub.EmbedQuery(
    embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        embeddingModel=embedding_pb2.EmbeddingModel(
            multimodal=True,
            providerModel=embedding_pb2.ProviderModel(
                provider="aws_bedrock", model="cohere.embed-v4:0"
            ),
            jsonConfig=model_json_config,
        ),
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_FLOAT32,
        text="a cat on a rug",
        inline=embedding_pb2.InlineMedia(data=image_bytes, contentType="image/jpeg"),
    )
)

print(vector.dimension, vector.WhichOneof("vector"))
```

## Configuration
**ChunkType**:
| ChunkType           | Key |
|---------------------|--------|
| DEFAULT             | 0      |
| TEXT_SPLITTER       | 1      |
| TOKEN_TEXT_SPLITTER | 2      |
| CHARACTER_TEXT_SPLITTER | 3  |
| SEMANTIC_SPLITTER   | 4      |
| SENTENCE_SPLITTER   | 5      |
| RECURSIVE_SPLITTER  | 6      |
| TABLE_CHUNKER       | 7      |
| LATE_CHUNKER        | 8      |
| NEURAL_CHUNKER      | 9      |



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