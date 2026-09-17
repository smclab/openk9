## Description

The **Agentic RAG Module** is a Retrieval-Augmented Generation (RAG) system built on FastAPI that provides intelligent search, conversational AI, and document analysis capabilities. It combines traditional search techniques with modern LLM-powered generation in a modular, tenant-aware architecture.

## Features

### Multi-Mode RAG Operations
- **Search Generation**: Complex search queries with faceted filtering, vector retrieval, and real-time streaming
- **Conversational RAG**: Chat-based interactions with conversation history and context awareness
- **Conversational RAG As Tool**: Chat-based interactions with conversation history and context awareness that implements a conditional RAG approach where the RAG system is used as a tool only when needed

### Tenant & User Management
- Multi-tenant architecture with tenant isolation via ACL headers
- JWT-based authentication and user session management
- Tenant-specific configuration loading via gRPC services

### Persistent Storage & History
- OpenSearch integration for chat history and document storage
- Complete CRUD operations for chat conversations
- Document upload and management capabilities

### Observability & Evaluation
- Arize Phoenix integration for tracing and monitoring
- Real-time evaluation of RAG performance
- Configurable evaluation metrics for router, retriever, and response

### Modular Architecture
- gRPC-based service discovery for configurations
- Pluggable embedding models and data sources
- Environment-based configuration

## API Endpoints

### Primary RAG Operations

#### 1. **`POST /api/rag/generate`**
- **Purpose**: Process complex search queries with RAG
- **Features**:
  - Faceted search with range filters
  - Vector-based retrieval
  - Real-time streaming via Server-Sent Events (SSE)
  - Multi-language support
- **Input**: `SearchQuery` model with search parameters
- **Output**: SSE stream of results

#### 2. **`POST /api/rag/chat`**
- **Purpose**: Conversational RAG with chat history
- **Features**:
  - Conversation context preservation
  - User-specific chat storage
  - Real-time response streaming
- **Input**: `SearchQueryChat` with chat context
- **Output**: SSE stream of chat responses

#### 3. **`POST /api/rag/chat-tool`**
- **Purpose**: Conversational RAG AS TOOL with chat history
- **Features**:
  - conditional RAG approach where the RAG system is used as a tool only when needed 
  - Conversation context preservation
  - User-specific chat storage
  - Real-time response streaming
- **Input**: `SearchQueryChat` with chat context
- **Output**: SSE stream of chat responses

### Chat Management

#### 4. **`POST /api/rag/user-chats`**
- Retrieve paginated chat history for a user
- **Parameters**: `chatSequenceNumber`, pagination controls

#### 5. **`GET /api/rag/chat/{chat_id}`**
- Fetch complete conversation for a specific chat
- Returns messages, sources, and metadata

#### 6. **`DELETE /api/rag/chat/{chat_id}`**
- Permanently delete a chat conversation
- Also removes associated uploaded documents

#### 7. **`PATCH /api/rag/chat/{chat_id}`**
- Rename a chat conversation by updating its title

### Evaluation & Monitoring

#### 8. **`POST /api/rag/evaluate`**
- Evaluate RAG performance using trace data
- **Metrics**: Router accuracy, retriever relevance, response quality
- **Data Source**: Arize Phoenix traces

#### 9. **`GET /health`**
- Health check endpoint for monitoring

## Configuration

### Environment Variables
```bash
# CORS Configuration
ORIGINS=*

# Service Endpoints
# Single host, or a comma-separated list for a multi-node cluster
# (e.g. opensearch-node-1:9200,opensearch-node-2:9200)
# Each entry accepts an http:// or https:// scheme; without one, http is assumed
OPENSEARCH_HOST=opensearch_host:port
GRPC_DATASOURCE_HOST=grpc_datasource_host:port
GRPC_EMBEDDING_MODULE_HOST=grpc_embedding_module_host:port

# OpenSearch basic authentication, applied only when both are set
OPENSEARCH_USERNAME=opensearch
OPENSEARCH_PASSWORD=opensearch_password

# OpenSearch TLS, honoured only when at least one host uses https://
OPENSEARCH_VERIFY_CERTS=true
OPENSEARCH_CA_CERTS=/etc/opensearch-certs/ca.pem

# Observability
ARIZE_PHOENIX_ENABLED=true
ARIZE_PHOENIX_PROJECT_NAME=arize_phoenix_project_name
ARIZE_PHOENIX_ENDPOINT=arize_phoenix_endpoint

# Logging level: INFO (default) or DEBUG
LOGGING_LEVEL=INFO

# Security
OPENK9_ACL_HEADER=OPENK9_ACL
```

### Headers
| Header | Purpose | Required |
|--------|---------|----------|
| `Authorization` | JWT Bearer token | Optional |
| `X-Forwarded-Host` | Tenant identification | Optional |
| `OPENK9_ACL` | Access control list | Optional |
| `X-Tenant-ID` | Tenant override | Optional |

## Logging

`LOGGING_LEVEL` sets the level of the whole module and defaults to `INFO`.
Records are written as `%(asctime)s - %(levelname)s - %(name)s - %(message)s`,
where `%(name)s` is the module that produced them (`app.rag.agentic_rag`,
`app.server`, ...).

### What each level reports

| Level | What it reports |
|-------|-----------------|
| `INFO` | Start and end of each request with its outcome and duration, routing decisions, number of retrieved documents, and every non-blocking guardrail decision |
| `WARNING` | Every block: input guardrail, output guardrail, scope gate, encoded blob rejected before the pipeline |
| `ERROR` | Failures of the pipeline and of the guardrail providers |
| `DEBUG` | Everything above, plus the per-document scores and the user query and the answer in clear |

A request always ends with one record carrying its outcome:

| Outcome | Meaning |
|---------|---------|
| `COMPLETED` | The answer was streamed to the end |
| `BLOCKED_INPUT` | The input guardrail classified the query into a category |
| `BLOCKED_OUTPUT` | The output guardrail, or the provider's own content filter, stopped the answer |
| `OFF_SCOPE` | The scope gate judged the answer outside the allowed domain |
| `ERROR` | The pipeline failed, rate limit included |

A blocked input conversation, at the default level:

```
2026-09-01 10:12:03,114 - INFO - app.rag.agentic_rag - [request] start rag_type=CHAT_RAG query_chars=61 query_hash=3f2a9c1b tenant_id=litwick chat_id=abc123
2026-09-01 10:12:03,540 - INFO - app.rag.agentic_rag - [input_guardrail] enabled provider=openai_moderation threshold=0.7 query_chars=61 query_hash=3f2a9c1b tenant_id=litwick chat_id=abc123
2026-09-01 10:12:04,002 - WARNING - app.rag.agentic_rag - [input_guardrail] BLOCKED category=SYSTEM_PROMPT_LEAKAGE score=0.82 document_id=42 provider=openai_moderation tenant_id=litwick chat_id=abc123
2026-09-01 10:12:04,010 - INFO - app.rag.agentic_rag - [request] end outcome=BLOCKED_INPUT duration_ms=896 chain=agentic_rag tenant_id=litwick chat_id=abc123
```

### Privacy of what is logged

No user query and no answer is written at `INFO` or `WARNING`. Where the
content has to be identified, the records carry its length and a truncated
sha256 (`query_chars=61 query_hash=3f2a9c1b`), which is enough to recognise
the same query across requests without disclosing it.

With `LOGGING_LEVEL=DEBUG` those same records gain the content in clear:
an event always produces a single record, whose level states its importance
and whose fields depend on the level in force. Because of that, the query
rides on a record whose level is `WARNING` or `INFO`, not `DEBUG`: a
collection pipeline that forwards only `WARNING` and above would receive it.
`DEBUG` is a diagnostic level and must not be left on in production.

## Local Development
```bash
# 1. Clone and setup
git clone <repo>
cd agentic-rag-module
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Generate gRPC Python code (run after installing requirements)
python -m grpc_tools.protoc -I. \
  --python_out=. \
  --grpc_python_out=. \
  app/external_services/grpc/searcher/searcher.proto \
  app/external_services/grpc/embedding/embedding.proto

# 2. Start development server
uvicorn app.server:app --host 0.0.0.0 --port 5000 --reload
```

## Deployment with Docker

### Using Makefile Commands

The module includes a comprehensive Makefile for easy Docker management:

```bash
# Verify required tools (git, docker) and environment
make check_commands

# Load and validate configurations
make load_config

# Build Docker image with configured base image and version
make build

# Start Docker Compose services in detached mode
make start

# Stop running Docker Compose services
make stop

# Show help
make help
```
