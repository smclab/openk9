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
- **Output**: SSE stream with tool execution results

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
OPENSEARCH_HOST=localhost:9200
GRPC_DATASOURCE_HOST=localhost:50051
GRPC_TENANT_MANAGER_HOST=localhost:50052
GRPC_EMBEDDING_MODULE_HOST=localhost:50053

# Observability
ARIZE_PHOENIX_ENABLED=true
ARIZE_PHOENIX_PROJECT_NAME=agentic_rag
ARIZE_PHOENIX_ENDPOINT=https://phoenix.openk9.io/v1/traces

# Security
OPENK9_ACL_HEADER=OPENK9_ACL
```

### Headers
| Header | Purpose | Required |
|--------|---------|----------|
| `Authorization` | JWT Bearer token | Conditional |
| `X-Forwarded-Host` | Tenant identification | Yes |
| `OPENK9_ACL` | Access control list | Conditional |
| `X-Tenant-ID` | Tenant override | Optional |

## Local Development
```bash
# 1. Clone and setup
git clone <repo>
cd agentic-rag-module
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# 2. Start development server
uvicorn app.server:app --host 0.0.0.0 --port 5000 --reload
```

## Deployment with Docker

### Using Makefile Commands

The module includes a comprehensive Makefile for easy Docker management:

```bash
# Check environment and dependencies
make check_commands

# Load and validate configuration
make load_config

# Build Docker image with configured version
make build

# Start all services (detached mode)
make start

# Stop services
make stop

# Show help
make help
```
