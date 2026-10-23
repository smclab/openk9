## Description

The **Orchestrated RAG Module** is the successor of `agentic-rag-module`: a
Retrieval-Augmented Generation service in which an orchestrator agent delegates
the search to a Discovery agent, possibly more than once, and answers only from
the documents it receives. The agents run on PydanticAI; the HTTP API, the chat
storage and the configuration are those of `agentic-rag-module`, so an
installation uses one module or the other behind the same gateway.

## Status

The module is being built from the proof of concept of #2262, one issue at a
time, under the roadmap #2193. For now it serves every route that does not
generate an answer:

- `/api/rag/generate`, `/api/rag/chat` and `/api/rag/chat-tool` answer with a
  single `ERROR` event until the pipeline is wired;
- `/api/rag/evaluate` answers `501 Not Implemented`;
- chat management (`/api/rag/user-chats`, `/api/rag/chat/{chat_id}`), uploads,
  `embed-domains`, `embed-guardrails` and `/health` work as in
  `agentic-rag-module`.

No chart installs the module yet.


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

# Logging level of this application: INFO (default) or DEBUG
LOGGING_LEVEL=INFO

# Logging level of the third-party libraries: INFO (default) or DEBUG
DEPENDENCIES_LOGGING_LEVEL=INFO

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


## Local Development
Dependencies are managed with [uv](https://docs.astral.sh/uv/):
`pyproject.toml` declares the direct dependencies and `uv.lock` pins the whole
tree. PyTorch comes in two variants, the `cpu` and `gpu` dependency groups:
`uv sync` and `uv run` install `cpu` together with the `dev` group.

```bash
# 1. Clone and setup
git clone <repo>
cd orchestrated-rag-module
uv sync

# 2. Start development server
uv run uvicorn app.server:app --host 0.0.0.0 --port 5000 --reload

# 3. Run the unit tests
uv run pytest tests
```

For the CUDA variant, replace the default `cpu` group with `gpu` and pass
the same flags to `uv run`, otherwise it switches back to the CPU wheels:
`uv sync --no-default-groups --group gpu --group dev`.

`uv run` generates the gRPC stubs from the `.proto` files under
`app/external_services/grpc/`, and generates them again whenever a `.proto`
changes or a stub is missing.

To change a dependency, edit `pyproject.toml` and run `uv lock`; to upgrade a
single package, run `uv lock --upgrade-package <package>`. Commit `uv.lock`
together with `pyproject.toml`: the Docker build installs with `--locked` and
fails if the two are out of sync.

## Deployment with Docker

### Using Makefile Commands

The module includes a comprehensive Makefile for easy Docker management:

```bash
# Verify required tools (git, docker) and environment
make check_commands

# Load and validate configurations
make load_config

# Build Docker image with configured base image and version, installing pytorch-cpu
make build_pytorch_cpu

# Build Docker image with configured base image and version, installing pytorch with CUDA
make build_pytorch_gpu

# Run the unit tests in the Docker test stage (MODE=cpu by default)
make test

# Start Docker Compose services in detached mode
make start

# Stop running Docker Compose services
make stop

# Show help
make help
```
