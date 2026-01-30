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
    
- gRPC interface used internally by OpenK9 services

- Health checks and reflection for easier integration


## Quickstart

### OpenK9 Setup
```bash
rag-module:
    image: smclab/openk9-rag-module:3.1.0
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
    image: smclab/openk9-embedding-module-base:3.1.0
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