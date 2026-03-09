#!/bin/bash
set -e

#
# k9.sh — OpenK9 local development CLI
#
# Builds Docker images from source and manages the local Docker Compose
# stack. Wraps Maven, Docker, and Docker Compose into a single entry
# point so you don't have to remember individual build commands.
#
# Prerequisites:
#   - Docker (with Compose v2 plugin or standalone docker-compose)
#   - Java 21+ and Maven (via the bundled mvnw wrapper)
#   - Node.js / Yarn (only if building frontend images)
#
# Quick start:
#   ./k9.sh start                        # start core services (pulls images)
#   ./k9.sh start --build                # build from source, then start
#   ./k9.sh start --profile=with-gen-ai --build  # build and start with AI services
#
# Run ./k9.sh without arguments for full usage information.
#

# Configuration defaults
TAG="local-dev"
PROFILES=()

# Detect docker compose command
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Change to the openk9 directory
cd "$(dirname "$0")"

# --- Profile to compose file mapping ---

profile_to_file() {
    case "$1" in
        core)     echo "" ;;
        with-file-handling) echo "compose-with-file-handling.yaml" ;;
        with-gen-ai)       echo "compose-with-gen-ai.yaml" ;;
        with-oauth2-server) echo "compose-with-oauth2-server.yaml" ;;
        *)        return 1 ;;
    esac
}

VALID_PROFILES="core with-file-handling with-gen-ai with-oauth2-server all"

# --- Profile to services mapping ---

CORE_SERVICES=(
    api-gateway tenant-manager datasource ingestion searcher
    search-frontend admin-ui tenant-ui web-connector
)
GEN_AI_SERVICES=(rag-module embedding-module talk-to)
FILE_HANDLING_SERVICES=(file-manager tika minio-connector)

VALID_SERVICES=(
    "${CORE_SERVICES[@]}" "${GEN_AI_SERVICES[@]}" "${FILE_HANDLING_SERVICES[@]}"
)

services_for_profile() {
    case "$1" in
        core)               echo "${CORE_SERVICES[*]}" ;;
        with-gen-ai)        echo "${GEN_AI_SERVICES[*]}" ;;
        with-file-handling) echo "${FILE_HANDLING_SERVICES[*]}" ;;
    esac
}

# --- Argument parsing ---

parse_args() {
    CMD="${1:-}"
    shift 2>/dev/null || true

    BUILD=false
    SKIP_MVN_SHARED_DEPS=false
    SERVICES=()
    OTHER_ARGS=()

    for arg in "$@"; do
        if [ "$arg" == "--build" ]; then
            BUILD=true
        elif [ "$arg" == "--skip-mvn-shared-deps" ]; then
            SKIP_MVN_SHARED_DEPS=true
        elif [[ "$arg" == --tag=* ]]; then
            TAG="${arg#--tag=}"
        elif [[ "$arg" == --profile=* ]]; then
            local p="${arg#--profile=}"
            if [ "$p" == "all" ]; then
                PROFILES+=(with-file-handling with-gen-ai with-oauth2-server)
            elif profile_to_file "$p" >/dev/null; then
                PROFILES+=("$p")
            else
                echo "Error: unknown profile '$p'"
                echo "Valid profiles: $VALID_PROFILES"
                exit 1
            fi
        elif [[ "$arg" == -* ]]; then
            OTHER_ARGS+=("$arg")
        else
            validate_service "$arg"
            SERVICES+=("$arg")
        fi
    done

    # Deduplicate profiles
    local unique=()
    for p in "${PROFILES[@]}"; do
        local dup=false
        for u in "${unique[@]}"; do
            if [ "$p" == "$u" ]; then
                dup=true
                break
            fi
        done
        if [ "$dup" = false ]; then
            unique+=("$p")
        fi
    done
    PROFILES=("${unique[@]}")
}

validate_service() {
    local svc="$1"
    for valid in "${VALID_SERVICES[@]}"; do
        if [ "$svc" == "$valid" ]; then
            return 0
        fi
    done
    echo "Error: unknown service '$svc'"
    echo "Valid services: ${VALID_SERVICES[*]}"
    exit 1
}

# --- Build functions ---

build_mvn_shared_deps() {
    echo "--- Building Maven Shared Dependencies ---"
    (
        cd core
        chmod +x mvnw

        echo "Clean everything..."
        ./mvnw clean

        echo "Installing root POM..."
        ./mvnw install -N -DskipTests

        echo "Installing hibernate-rx-multitenancy..."
        ./mvnw install -P '!validate,!format' -DskipTests \
            -f "../vendor/hibernate-rx-multitenancy/pom.xml"

        echo "Installing all shared modules..."
        for module in common client tenant-events; do
            echo "Installing $module..."
            ./mvnw install -DskipTests -f "$module/pom.xml"
        done
    )
}

build_core() {
    echo "--- Building Core Services ---"
    (
        cd core

        echo "Building Spring Boot services..."
        ./mvnw package -DskipTests jib:dockerBuild \
            -Djib.to.image=smclab/openk9-api-gateway:$TAG \
            -f app/api-gateway/pom.xml

        echo "Building Quarkus services..."
        for SVC in tenant-manager datasource ingestion searcher; do
            echo "Building $SVC..."
            ./mvnw package -DskipTests \
                -Dquarkus.profile=prod \
                -Dquarkus.container-image.build=true \
                -Dquarkus.container-image.push=false \
                -Dquarkus.container-image.group=smclab \
                -Dquarkus.container-image.name=openk9-$SVC \
                -Dquarkus.container-image.tag=$TAG \
                -pl app/$SVC
        done
    )

    echo "--- Building Frontend Services ---"
    docker build -t smclab/openk9-search-frontend:$TAG -f js-packages/search-frontend/Dockerfile .
    docker build -t smclab/openk9-admin-ui:$TAG -f js-packages/admin-ui/Dockerfile .
    docker build -t smclab/openk9-tenant-ui:$TAG -f js-packages/tenant-ui/Dockerfile .

    echo "--- Building Connectors ---"
    docker build -t smclab/openk9-web-connector:$TAG -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector
}

build_gen_ai() {
    echo "--- Building AI Services ---"
    docker build -t smclab/openk9-rag-module:$TAG -f ai-packages/rag-module/Dockerfile ai-packages/rag-module
    docker build -t smclab/openk9-embedding-module-base:$TAG -f ai-packages/embedding-modules/Dockerfile ai-packages/embedding-modules
    docker build -t smclab/openk9-talk-to:$TAG -f js-packages/talk-to/Dockerfile .
}

build_file_handling() {
    echo "--- Building File Services ---"
    docker build -t smclab/openk9-minio-connector:$TAG -f connectors/minio-connector/connector/Dockerfile connectors/minio-connector/connector
    (cd core && for SVC in file-manager tika; do
        echo "Building $SVC..."
        ./mvnw package -DskipTests \
            -Dquarkus.profile=prod \
            -Dquarkus.container-image.build=true \
            -Dquarkus.container-image.push=false \
            -Dquarkus.container-image.group=smclab \
            -Dquarkus.container-image.name=openk9-$SVC \
            -Dquarkus.container-image.tag=$TAG \
            -pl app/$SVC
    done)
}

build_by_profiles() {
    # Core is always built
    local profiles_to_build=(core)

    for p in "${PROFILES[@]}"; do
        if [ "$p" != "core" ]; then
            profiles_to_build+=("$p")
        fi
    done

    if [ "$SKIP_MVN_SHARED_DEPS" = false ]; then
        build_mvn_shared_deps
    fi

    for p in "${profiles_to_build[@]}"; do
        case "$p" in
            core)  build_core ;;
            with-gen-ai)       build_gen_ai ;;
            with-file-handling) build_file_handling ;;
        esac
    done

    echo "All images built successfully with tag: $TAG"
}

build_single() {
    local service=$1
    echo "--- Building: $service (tag: $TAG) ---"
    if [ "$SKIP_MVN_SHARED_DEPS" = false ]; then
        case "$service" in
            api-gateway|tenant-manager|datasource|ingestion|searcher|file-manager|tika)
                build_mvn_shared_deps
                ;;
        esac
    fi
    case "$service" in
        api-gateway)
            (cd core && ./mvnw package -DskipTests jib:dockerBuild \
                -Djib.to.image=smclab/openk9-api-gateway:$TAG \
                -f app/api-gateway/pom.xml)
            ;;
        tenant-manager|datasource|ingestion|searcher|file-manager|tika)
            (cd core && ./mvnw package -DskipTests \
                -Dquarkus.profile=prod \
                -Dquarkus.container-image.build=true \
                -Dquarkus.container-image.push=false \
                -Dquarkus.container-image.group=smclab \
                -Dquarkus.container-image.name=openk9-$service \
                -Dquarkus.container-image.tag=$TAG \
                -pl app/$service)
            ;;
        search-frontend)
            docker build -t smclab/openk9-search-frontend:$TAG -f js-packages/search-frontend/Dockerfile .
            ;;
        admin-ui)
            docker build -t smclab/openk9-admin-ui:$TAG -f js-packages/admin-ui/Dockerfile .
            ;;
        tenant-ui)
            docker build -t smclab/openk9-tenant-ui:$TAG -f js-packages/tenant-ui/Dockerfile .
            ;;
        web-connector)
            docker build -t smclab/openk9-web-connector:$TAG -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector
            ;;
        minio-connector)
            docker build -t smclab/openk9-minio-connector:$TAG -f connectors/minio-connector/connector/Dockerfile connectors/minio-connector/connector
            ;;
        rag-module)
            docker build -t smclab/openk9-rag-module:$TAG -f ai-packages/rag-module/Dockerfile ai-packages/rag-module
            ;;
        embedding-module)
            docker build -t smclab/openk9-embedding-module-base:$TAG -f ai-packages/embedding-modules/Dockerfile ai-packages/embedding-modules
            ;;
        talk-to)
            docker build -t smclab/openk9-talk-to:$TAG -f js-packages/talk-to/Dockerfile .
            ;;
    esac
}

do_build() {
    if [ ${#SERVICES[@]} -gt 0 ]; then
        for svc in "${SERVICES[@]}"; do
            build_single "$svc"
        done
    else
        build_by_profiles
    fi
}

# --- Compose helper ---

compose() {
    local flags=(-f "compose.yaml")
    for p in "${PROFILES[@]}"; do
        local overlay
        overlay="$(profile_to_file "$p")"
        if [ -n "$overlay" ]; then
            flags+=(-f "$overlay")
        fi
    done
    IMAGE_TAG="$TAG" $DOCKER_COMPOSE "${flags[@]}" "$@"
}

# --- Usage ---

usage() {
    cat <<'USAGE'
k9.sh — OpenK9 local development CLI

Usage: ./k9.sh <command> [services...] [options]

Commands:
  build   [services...]   Build Docker images from source
  start   [services...]   Start the Docker Compose stack
  stop    [services...]   Stop running containers
  down    [services...]   Stop and remove containers and volumes
  restart [services...]   Restart containers
  logs    [services...]   Follow container logs

Options:
  --build              Build images before starting/restarting
  --tag=TAG            Docker image tag (default: local-dev)
  --profile=PROFILE    Enable a compose profile (repeatable)
  --skip-mvn-shared-deps
                       Skip Maven Shared Dependencies build (clean,
                       install root POM, vendored hibernate, shared
                       modules). Useful when dependencies haven't
                       changed and you only need to rebuild the
                       service itself.

Profiles:
  core       Base services: PostgreSQL, OpenSearch, RabbitMQ,
             API Gateway, Datasource, Tenant Manager, Ingestion,
             Searcher, frontends, Caddy reverse proxy (default)
  with-file-handling
             Core + file handling: MinIO, Tika, File Manager
  with-gen-ai
             Core + AI services: RAG module, Embedding, Talk-To
  with-oauth2-server
             Core + Keycloak OAuth2/OIDC identity provider
  all        Shorthand for: with-file-handling + with-gen-ai
             + with-oauth2-server

  Profiles are additive. Combine multiple --profile flags to
  compose the stack you need.

Services (for targeted build/restart):
  api-gateway  tenant-manager  datasource  ingestion  searcher
  search-frontend  admin-ui  tenant-ui  web-connector
  rag-module  embedding-module  talk-to
  file-manager  tika  minio-connector

Build details:
  Core images are always built. Additional profiles add extra
  services on top of core.

  ./k9.sh build                            Build core images
  ./k9.sh build --profile=with-gen-ai      Build core + AI images
  ./k9.sh build --profile=with-file-handling
                                           Build core + file images
  ./k9.sh build --profile=all             Build everything

  Maven Shared Dependencies (clean, install root POM, vendored
  hibernate, shared modules) are built automatically. Skip
  with --skip-mvn-shared-deps.

  A single-service build (./k9.sh build datasource) automatically
  builds Maven Shared Dependencies for Java services.

Startup behavior:
  On every start, the initializer container is rebuilt and runs
  seed.js, which idempotently provisions:
    - A demo tenant (demo.openk9.localhost)
    - Plugin drivers (Sitemap Crawler, Minio Connector)
    - A sample datasource (SMC Website)
    - Links the datasource to the Default Bucket

  The initializer is safe to re-run and skips already-created
  resources.

Access:
  After startup, the services are available at:
    Tenant UI:   https://demo.openk9.localhost/tenant
    Admin UI:    https://demo.openk9.localhost/admin
    Search:      https://demo.openk9.localhost

  With --profile=with-gen-ai, the Talk-To conversational
  interface is also available at:
    Talk-To:     https://demo.openk9.localhost/chat

  Talk-To is a conversational AI frontend that uses the RAG and
  Embedding modules to provide chat-based search. All three AI
  services (rag-module, embedding-module, talk-to) start together
  with the with-gen-ai profile.

Examples:
  ./k9.sh start                            Start core services
  ./k9.sh start                            Start core services
  ./k9.sh start --build                    Build core, then start
  ./k9.sh start --profile=with-gen-ai --build
                                           Build core + AI, then start
  ./k9.sh start --profile=with-oauth2-server
                                           Start core + Keycloak
  ./k9.sh build --profile=with-gen-ai      Build core + AI images
  ./k9.sh build --profile=all             Build all profiles
  ./k9.sh build datasource --tag=test     Build only datasource with custom tag
  ./k9.sh restart datasource --build      Rebuild and restart datasource
  ./k9.sh logs datasource                 Follow datasource logs
  ./k9.sh down                            Tear down everything (with volumes)
USAGE
}

# --- Main ---

parse_args "$@"

case "$CMD" in
    build)
        do_build
        ;;
    start)
        if [ "$BUILD" = true ]; then
            do_build
        fi
        # Always rebuild the initializer image before starting, as it
        # contains the seed.js script that provisions tenants and
        # configures connectors on first boot.
        compose build initializer
        compose up -d "${OTHER_ARGS[@]}" "${SERVICES[@]}"
        ;;
    stop)
        compose stop "${SERVICES[@]}"
        ;;
    down)
        compose down --volumes "${SERVICES[@]}"
        ;;
    restart)
        if [ "$BUILD" = true ]; then
            do_build
        fi
        # Always rebuild the initializer to pick up seed.js changes.
        compose build initializer
        # Always use up -d so compose file changes (env vars,
        # volumes, profiles) are picked up. Plain "restart" only
        # sends SIGHUP without re-reading the config.
        compose up -d "${SERVICES[@]}"
        ;;
    logs)
        compose logs -f "${SERVICES[@]}"
        ;;
    *)
        usage
        exit 1
        ;;
esac
