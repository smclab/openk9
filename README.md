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

OpenK9 ships a local development CLI, `k9.sh`, that wraps Maven,
Docker, and Docker Compose into a single entry point. You need
[Docker](https://docs.docker.com/get-started/get-docker/),
Docker Compose v2, Java 21, Node 20+ and Yarn installed.

Run the prerequisite check first:

```bash
./k9.sh doctor
```

Then start the core stack:

```bash
./k9.sh up
```

This brings up the base services — PostgreSQL, OpenSearch, RabbitMQ,
**Keycloak**, the OpenK9 backend services (tenant-manager,
datasource, ingestion, searcher), the frontends, the web connector
and Caddy as the reverse proxy. The PostgreSQL init script under
`compose-utilities/postgresql/` only creates the empty `openk9`,
`tenantmanager`, and `keycloak` databases. The schema migrations
are run by each service on first start (tenant-manager via
Liquibase, datasource via Liquibase, Keycloak via its built-in
migrator).

A Node-based **initializer** container then runs `seed.js`, which:

1. Creates the demo tenant via the tenant-manager REST API
   (`POST /tenant-manager/tenant`) — this triggers the actor system
   to provision the per-tenant Keycloak realm and the openk9 schema.
2. Registers the default plugin drivers (Sitemap Crawler, Minio
   Connector) on the datasource GraphQL endpoint.
3. Creates the SMC Website datasource and links it to the Default
   Bucket.

The initializer is fire-once per `postgres-data` volume: on
subsequent `up`s it sees the tenant already exists and exits
cleanly. Run `./k9.sh down -v` and `./k9.sh up` to re-seed from a
clean state.

Access points (with the demo tenant):
- Admin UI:   <https://demo.openk9.localhost/admin>
- Tenant UI:  <https://demo.openk9.localhost/tenant>
- Search UI:  <https://demo.openk9.localhost>
- Chat UI:    <https://demo.openk9.localhost/chat> (requires `--with=gen-ai`)

Admin credentials: username *k9admin*, password *openk9*.

Opt-in profiles add services on top of the core:

```bash
./k9.sh up --with=file-handling     # adds MinIO, Tika, File Manager, Minio Connector
./k9.sh up --with=gen-ai            # adds RAG module, Embedding module, Talk-To
```

To use the published images instead of locally-built ones:

```bash
IMAGE_GROUP=smclab IMAGE_TAG=3.0.0 ./k9.sh up
```

Common commands:

```bash
./k9.sh build datasource            # rebuild a single service from source
./k9.sh restart datasource          # restart a service after code changes
./k9.sh logs datasource             # follow a service's logs
./k9.sh down                        # stop containers, keep volumes
./k9.sh down -v                     # stop containers and wipe volumes (Keycloak, OpenSearch, PostgreSQL)
./k9.sh push datasource --tag=...   # push a built image to OPENK9_REGISTRY
```

Run `./k9.sh` without arguments for the full reference.

A `.env` file at the repo root (gitignored) lets you set defaults
such as `TAG`, `OPENK9_REGISTRY`, `IMAGE_GROUP`, `IMAGE_TAG`. The
shell environment always wins, then `.env`, then the hardcoded
defaults inside `k9.sh`.

### Differences from `main`

The `3.0.x` branch is the maintenance line and intentionally exposes
a smaller surface than `main`:

- **Keycloak is a core dependency.** There is no `--with=oauth2`
  profile here — Keycloak is part of `compose.yaml` and always
  starts.
- **No `api-gateway` service.** The Spring Boot API gateway does
  not exist on `3.0.x`; Caddy proxies `/api/...` requests directly
  to the backend services.
- **Smaller AI surface.** The `agentic-rag-module` and
  `chunk-evaluation-module` shipped on `main` are not available
  here. The `--with=gen-ai` profile only enables `rag-module`,
  `embedding-module`, and `talk-to`.
- **`./k9.sh down` is non-destructive by default.** Volumes are
  preserved across restarts. Pass `-v` (or `--volumes`) to wipe
  them when you really want a clean slate.

## Installation for production

To install Openk9 in production is advisable to deploy it in Kubernetes or Openshift environments.

You can find a complete guide to do it [here](./helm-charts/README.md) using Helm Charts.

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
