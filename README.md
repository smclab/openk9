<p align="center">
  <a href="https://www.openk9.io/" rel="noopener" target="_blank"><img width="200" src="media/logo.svg" alt="OpenK9 logo"></a></p>
</p>

<h1 align="center">OpenK9</h1>

<div align="center">

OpenK9 is a new Cognitive Search Engine that allows you to build next generation search experiences. It employs a scalable architecture and machine learning to enrich unstructured data and give the best user experience possible.

[![license](https://img.shields.io/badge/license-AGPL-blue.svg)](https://github.com/smclab/OpenK9/blob/master/LICENSE)
[![license](https://img.shields.io/github/v/release/smclab/openk9)](https://github.com/smclab/OpenK9/releases)
[![Follow on Twitter](https://img.shields.io/twitter/follow/K9Open.svg?label=follow+K9Open)](https://twitter.com/K9Open)

</div>

## Quickstart

To run OpenK9 on your machine with the latest stable release, you just need [Docker](https://docs.docker.com/get-started/get-docker/) installed.

```bash
docker compose up -d
```

Once started, OpenK9 is running at **https://demo.openk9.localhost**.

### Access

| Panel | URL | Credentials |
|---|---|---|
| Admin UI | [https://demo.openk9.localhost/admin](https://demo.openk9.localhost/admin) | `admin` / `admin` |
| Tenant UI | [https://demo.openk9.localhost/tenant](https://demo.openk9.localhost/tenant) | `admin` / `admin` |
| Search | [https://demo.openk9.localhost](https://demo.openk9.localhost) | — |

All administration panels use **HTTP Basic Authentication** with `admin` / `admin`.

### Compose profiles

The default `compose.yaml` starts the core stack (PostgreSQL, OpenSearch, RabbitMQ, API Gateway, backend services, frontends, and Caddy reverse proxy). Additional capabilities are available as compose overlays:

| Profile | Compose file | What it adds |
|---|---|---|
| File handling | `compose-with-file-handling.yaml` | MinIO, File Manager, Tika, MinIO Connector |
| Gen AI | `compose-with-gen-ai.yaml` | RAG module, Embedding module, Talk-To chat |
| OAuth2 server | `compose-with-oauth2-server.yaml` | Keycloak identity provider (optional) |

To add overlays, pass extra `-f` flags:

```bash
# Core + File Handling + Gen AI
docker compose -f compose.yaml -f compose-with-file-handling.yaml -f compose-with-gen-ai.yaml up -d

# Everything (core + all overlays)
docker compose -f compose.yaml -f compose-with-file-handling.yaml -f compose-with-gen-ai.yaml -f compose-with-oauth2-server.yaml up -d
```

With the Gen AI profile, conversational search is available at [https://demo.openk9.localhost/chat](https://demo.openk9.localhost/chat).

## Development with k9.sh

For developers building from source, the `k9.sh` script wraps Maven, Docker, and Docker Compose into a single CLI.

### Prerequisites

- Docker (with Compose v2)
- Java 21+ and Maven (via bundled `mvnw`)
- Node.js / Yarn (for frontend builds)

### Quick start

```bash
./k9.sh start                                    # Start core services (pulls images)
./k9.sh start --build                            # Build from source, then start
./k9.sh start --profile=with-gen-ai --build      # Build and start with AI services
./k9.sh start --profile=all                      # Start everything
```

### Common workflows

```bash
./k9.sh build datasource                         # Build a single service
./k9.sh build datasource --skip-mvn-shared-deps  # Skip shared deps if unchanged
./k9.sh restart datasource --build               # Rebuild and restart one service
./k9.sh logs tenant-manager                      # Follow logs for a service
./k9.sh down                                     # Tear down (removes volumes)
```

### Profiles

Profiles are additive. Core services are always included.

| Profile | Services added |
|---|---|
| `core` (default) | PostgreSQL, OpenSearch, RabbitMQ, API Gateway, Datasource, Tenant Manager, Ingestion, Searcher, frontends, Caddy |
| `with-file-handling` | MinIO, File Manager, Tika, MinIO Connector |
| `with-gen-ai` | RAG module, Embedding module, Talk-To |
| `with-oauth2-server` | Keycloak OAuth2/OIDC identity provider |
| `all` | All of the above |

Run `./k9.sh` without arguments for full usage information.

## Installation for production

To install Openk9 in production is advisable to deploy it in Kubernetes or Openshift environments.

You can find a complete guide to do it [here](./helm-charts/README.md) using Helm Charts.

## CI/CD Architecture

OpenK9 uses a **Modularized GitLab CI/CD** system designed for scalability, maintainability, and rapid development. The architecture follows a **Parent-Child Pipeline** pattern to optimize resource usage and isolate component builds.

### 🧩 Modular Structure

| Component | Logic File | Description |
|-----------|------------|-------------|
| **Orchestrator** | `.gitlab/.gitlab-ci.yaml` | Main entry point. Loads shared templates and triggers domain pipelines. |
| **Common Logic** | `.gitlab/.gitlab-templates.yaml` | Centralized build scripts (Maven/Yarn), rules, and variables. |
| **Backend** | `.gitlab/ci/backend.yaml` | Triggers for Java/Quarkus modules (Datasource, Ingestion, Searcher...). |
| **Frontend** | `.gitlab/ci/frontend.yaml` | Triggers for React/JS modules (Admin UI, Search Frontend...). |
| **AI Modules** | `.gitlab/ci/ai.yaml` | Triggers for AI/ML python components (RAG, Embeddings...). |
| **Helm & Conn.** | `.gitlab/ci/common.yaml` | Triggers for Helm Charts and Connectors (Main branch only). |

### 🔄 Pipeline Flow

The pipeline automatically detects changes in specific directories and triggers only the relevant child pipelines.

```mermaid
graph TD
    A[Push / Merge Request] --> B{Global Rules}
    B -- Valid --> C[Parent Pipeline Orchestrator]
    
    C --> D{Change Detection}
    
    D -- "core/app/..." --> E[Backend Trigger]
    D -- "js-packages/..." --> F[Frontend Trigger]
    D -- "ai-packages/..." --> G[AI Trigger]
    D -- "helm-charts/..." --> H[Helm Trigger]
    
    E --> E1[Child Pipeline: Backend]
    F --> F1[Child Pipeline: Frontend]
    G --> G1[Child Pipeline: AI]
    H --> H1[Child Pipeline: Helm Package]
    
    style C fill:#f9f,stroke:#333
    style E fill:#bbf,stroke:#333
    style F fill:#bfb,stroke:#333
    style G fill:#fbf,stroke:#333
```

### 🚀 Build & Restart Strategy

We use a sophisticated build strategy that adapts to the branch type:

- **Main / Tag**: Builds **OFFICIAL** images and pushes them. Triggers deployment on **Production/Staging**.
- **Feature Branch**: Builds **SNAPSHOT** images (e.g., `999-SNAPSHOT`) for developer testing. Triggers restart on personal developer namespaces.
- **Merge Request**: Runs builds and tests **WITHOUT** pushing images, ensuring code quality before merge.

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant GL as GitLab CI
    participant Reg as Container Registry
    participant Argo as ArgoCD (External)
    
    Dev->>GL: Push Code (Feature Branch)
    GL->>GL: Detect Changes & Trigger Child
    
    rect rgb(240, 248, 255)
    Note over GL: Child Pipeline Execution
    GL->>GL: Extract Version
    GL->>GL: Calculate Snapshot Tag (e.g. 999-SNAPSHOT)
    GL->>Reg: Build & Push Docker Image
    end
    
    rect rgb(255, 240, 245)
    Note over GL, Argo: Restart Trigger
    GL->>Argo: CURL Trigger (Token + User Info)
    Argo->>Argo: Validate User & Determine Namespace
    Argo->>Argo: Restart Pods in Target Namespace
    end
    
    Argo-->>GL: 200 OK (Deployment Started)
```

## Docs and Resources

- [Official Documentation](https://www.openk9.io/)


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
along with this program. If not, see <http://www.gnu.org/licenses/>.
