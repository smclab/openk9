## Description

The **Orchestrated RAG Module** is the successor of `agentic-rag-module`: a
Retrieval-Augmented Generation service in which an orchestrator agent delegates
the search to a Discovery agent, possibly more than once, and answers only from
the documents it receives. The agents run on PydanticAI; the HTTP API and the
chat storage are those of `agentic-rag-module`, and so is most of the RAG
configuration, so an installation uses one module or the other behind the same
gateway.

## How it answers

`/api/rag/generate`, `/api/rag/chat` and `/api/rag/chat-tool` answer through
the orchestrator: input guardrail, intent detection, then the orchestrator,
which delegates each search to a Discovery agent, possibly more than once, and
answers only from the documents delivered to it, marking each sentence with the
number of its source. The answer is streamed as it is written, through the
output guardrail. The architecture page of the documentation,
`docs/docs/architecture/orchestrated-rag-module.md`, describes the turn.

Chat management (`/api/rag/user-chats`, `/api/rag/chat/{chat_id}`), uploads,
`embed-domains`, `embed-guardrails`, `/api/rag/evaluate` and `/health` work as
in `agentic-rag-module`.

### Choosing the module

An installation uses this module or `agentic-rag-module`, never both. The API
Gateway sends the RAG requests to the host of its `openk9.rag.host` Helm value
(`io.openk9.apigw.rag-url`): the README of the
`helm-charts/03-gen-ai/openk9-orchestrated-rag-module` chart explains how to
switch and how to go back. Locally, `./k9.sh up --with=gen-ai
--with=orchestrated-rag` starts this module in place of the other one.

## SSE events

Each event is a JSON object with a `chunk` and a `type`.

| Type        | When                                  | `chunk`                                                        |
|-------------|---------------------------------------|----------------------------------------------------------------|
| `STATUS`    | before `START`, once per phase        | the phase of the turn, see below                               |
| `SOURCES`   | before `START`, when sources were delivered | every delivered source: `ordinal`, `document_id`, `title`, `url` |
| `START`     | before the first piece of the answer  | empty                                                          |
| `CHUNK`     | while the answer is written           | a piece of the answer, with the `[n]` markers of its sources   |
| `TITLE`     | after the answer, first turn of a logged user's chat | the generated title                             |
| `DOCUMENT`  | after the answer, one per cited source | `ordinal`, `citations` (quotes), `score`, `title`, `url`, `domain` |
| `GUARDRAIL` | instead of the answer, when the input guardrail blocks the question | `Guardrail violation`            |
| `CANCEL`    | instead of the rest of the answer, when the output guardrail or the scope gate blocks it; no `END` follows | `Inappropriate content`, or the redirect message of the scope gate |
| `ERROR`     | on a failure                          | an opaque message; `message` carries the reason of a refusal   |
| `END`       | at the end                            | empty                                                          |

The `STATUS` phases, in the order they can come:

| Phase         | Meaning                                                       |
|---------------|---------------------------------------------------------------|
| `planning`    | the orchestrator decides whether and what to search           |
| `discovering` | a search is delegated to the Discovery                        |
| `retrieving`  | the Discovery searches OpenSearch                             |
| `evaluating`  | the Verifier or the real-time evaluation judges the answer    |
| `generating`  | the answer is about to start                                  |

A turn that does not search does not announce `discovering` or `retrieving`.
These names differ from the ones the roadmap #2193 anticipated (`planning`,
`retrieving`, `querying_data`, `evaluating`).

## RAG configuration

The module reads the RAG configuration of the tenant (`CHAT_RAG`,
`CHAT_RAG_TOOL`, `SIMPLE_GENERATE`). The fields of its own live in the
`jsonConfig`.

Fields of this module:

| Field                   | Default | Effect |
|-------------------------|---------|--------|
| `max_iterations`        | none    | Retries of the orchestrator after a rejection of the Verifier, and the request limit of the turn derived from them; without it there is no request limit, only the six delegations per turn |
| `enable_verifier`       | `false` | The Verifier checks every claim against the delivered documents and has the orchestrator repair the answer; the answer is buffered until verified; off with `bypass_rag`, since there are no documents to check against |
| `parallel_discovery`    | `false` | The orchestrator can delegate independent sub-questions in parallel (`discover_many`) |
| `max_parallel_subtasks` | `3`     | Sub-questions searched in parallel, between 1 and 4 |
| `enable_intent_detection` | `true` | Detect the domains of the question and filter the searches on them; skipped anyway for a tenant without domain documents or with `enable_real_time_evaluation` |
| `orchestrator_prompt`   | none    | Instructions of the tenant added after the default prompt of the orchestrator; not used with `bypass_rag`, where the prompt comes from `prompt_no_rag` |
| `discovery_prompt`      | none    | Instructions of the tenant added after the default prompt of every Discovery |

Fields shared with `agentic-rag-module`, with the same meaning: `bypass_rag`
and `prompt_no_rag`, `chunk_window`, `range_values`, `metadata`,
`score_threshold`, `domain_threshold`, `enable_conversation_title`,
`enable_real_time_evaluation` (as there, the turn also skips the input
guardrail and the intent detection) and the guardrails configuration. As
there, `retrieve_citations` is a field of the LLM configuration, not of the
RAG configuration: it adds the quotes to the `DOCUMENT` events, while the
`[n]` markers are always there. `rerank` is read but does not work yet, here
as in `agentic-rag-module`: the URL of the reranker never reaches the search. One difference on the output guardrail: the answer streams in
pieces of 40 characters rather than in the tokens of the provider, so the same
`output_guardrail_chunk_interval` checks about ten times less often; lower it
to keep the frequency.

Fields of `agentic-rag-module` this module ignores: `prompt` (use
`orchestrator_prompt` and `discovery_prompt`), `reformulate`,
`rephrase_prompt` and `analyze_query_prompt` (the orchestrator resolves
follow-up questions by itself), `answer_only_with_context` (the answer always
comes from the documents). `rag_tool_description` is used only by the routing
judge of the evaluations.

For a tenant that comes from `agentic-rag-module`: its `prompt` is the whole
prompt of the answer, while `orchestrator_prompt` and `discovery_prompt` are
added to the default prompts, which carry the rules on searching and citing.
Do not copy it: move into `orchestrator_prompt` only the instructions of the
tenant on the answer (tone, language, constraints), and into
`discovery_prompt` only those on the search.

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
