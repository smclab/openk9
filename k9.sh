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
#   ./k9.sh start --profile=ai --build   # build and start with AI services
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
        files)    echo "compose-files.yaml" ;;
        ai)       echo "compose-ai.yaml" ;;
        keycloak) echo "compose-keycloak.yaml" ;;
        *)        return 1 ;;
    esac
}

VALID_PROFILES="core files ai keycloak all"

# --- Valid services for build ---

VALID_SERVICES=(
    api-gateway tenant-manager datasource ingestion searcher
    search-frontend admin-ui tenant-ui web-connector
)

# --- Argument parsing ---

parse_args() {
    CMD="${1:-}"
    shift 2>/dev/null || true

    BUILD=false
    SERVICES=()
    OTHER_ARGS=()

    for arg in "$@"; do
        if [ "$arg" == "--build" ]; then
            BUILD=true
        elif [[ "$arg" == --tag=* ]]; then
            TAG="${arg#--tag=}"
        elif [[ "$arg" == --profile=* ]]; then
            local p="${arg#--profile=}"
            if [ "$p" == "all" ]; then
                PROFILES+=(files ai)
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

build_all() {
    echo "Building all local images with tag: $TAG"

    echo "--- Building Core Java Services ---"
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

        echo "Building Spring Boot services..."
        ./mvnw package -DskipTests jib:dockerBuild \
            -Djib.to.image=smclab/openk9-api-gateway:$TAG \
            -f app/api-gateway/pom.xml

        echo "Building Quarkus services..."
        QUARKUS_SERVICES=("tenant-manager" "datasource" "ingestion" "searcher")
        for SVC in "${QUARKUS_SERVICES[@]}"; do
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

    echo "All images built successfully with tag: $TAG"
}

build_single() {
    local service=$1
    echo "--- Building: $service (tag: $TAG) ---"
    case "$service" in
        api-gateway)
            (cd core && ./mvnw package -DskipTests jib:dockerBuild \
                -Djib.to.image=smclab/openk9-api-gateway:$TAG \
                -f app/api-gateway/pom.xml)
            ;;
        tenant-manager|datasource|ingestion|searcher)
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
    esac
}

do_build() {
    if [ ${#SERVICES[@]} -gt 0 ]; then
        for svc in "${SERVICES[@]}"; do
            build_single "$svc"
        done
    else
        build_all
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

Profiles:
  core       Base services: PostgreSQL, OpenSearch, RabbitMQ,
             API Gateway, Datasource, Tenant Manager, Ingestion,
             Searcher, frontends, Caddy reverse proxy (default)
  files      Core + file handling: MinIO, Tika, File Manager
  ai         Core + AI services: RAG module, Embedding, Talk-To
  keycloak   Core + Keycloak identity provider
  all        Shorthand for: files + ai

  Profiles are additive. Combine multiple --profile flags to
  compose the stack you need.

Services (for targeted build/restart):
  api-gateway  tenant-manager  datasource  ingestion  searcher
  search-frontend  admin-ui  tenant-ui  web-connector

Build details:
  A full build (./k9.sh build) performs these steps in order:
    1. Clean all Maven modules
    2. Install root POM into local Maven repository
    3. Install vendored hibernate-rx-multitenancy
    4. Install shared modules (common, client, tenant-events)
    5. Build api-gateway (Spring Boot + Jib)
    6. Build Quarkus services (tenant-manager, datasource,
       ingestion, searcher) as container images
    7. Build frontend Docker images (search-frontend, admin-ui,
       tenant-ui)
    8. Build web-connector Docker image

  A single-service build (./k9.sh build datasource) skips steps
  1-4 and only builds the specified service. Make sure shared
  modules are already installed (run a full build first).

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
    Admin UI:    https://demo.openk9.localhost/admin
    Search:      https://demo.openk9.localhost
    API Gateway: https://demo.openk9.localhost/api

Examples:
  ./k9.sh start                            Start core services
  ./k9.sh start --build                    Build everything, then start
  ./k9.sh start --profile=ai              Start core + AI services
  ./k9.sh start --profile=keycloak --profile=ai --build
                                           Build and start with Keycloak + AI
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
            # Use up -d to force recreate and pick up the new images
            compose up -d "${SERVICES[@]}"
        else
            compose restart "${SERVICES[@]}"
        fi
        ;;
    logs)
        compose logs -f "${SERVICES[@]}"
        ;;
    *)
        usage
        exit 1
        ;;
esac
