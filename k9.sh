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
#   ./k9.sh up                            # start core services (pulls images)
#   ./k9.sh up --build                    # build from source, then start
#   ./k9.sh up --with=gen-ai --build      # build and start with AI services
#
# Run ./k9.sh without arguments for full usage information.
#

# Change to the repo root so relative paths and .env loading work correctly.
cd "$(dirname "$0")"

# --- .env loader ---
# Reads key=value pairs from openk9/.env (gitignored). Shell environment
# variables always take precedence; .env values are only applied when the
# variable is not already set.
_load_env_file() {
    [ -f ".env" ] || return 0
    while IFS= read -r _env_line || [ -n "$_env_line" ]; do
        # Skip blank lines and comments.
        [[ -z "${_env_line// }" || "$_env_line" =~ ^[[:space:]]*# ]] && continue
        _env_key="${_env_line%%=*}"
        _env_val="${_env_line#*=}"
        # Strip leading/trailing whitespace from the key.
        _env_key="${_env_key#"${_env_key%%[![:space:]]*}"}"
        _env_key="${_env_key%"${_env_key##*[![:space:]]}"}"
        # Only export if not already set in the shell environment.
        [ -z "${!_env_key+x}" ] && export "${_env_key}=${_env_val}"
    done < ".env"
    unset _env_line _env_key _env_val
}
_load_env_file

# Configuration defaults — use ${VAR:-default} so .env-set values survive.
TAG="${TAG:-local-dev}"
OPENK9_REGISTRY="${OPENK9_REGISTRY:-}"
PROFILES=()

# Detect host architecture for container image builds.
HOST_ARCH=$(uname -m)
case "$HOST_ARCH" in
    aarch64|arm64) JIB_PLATFORM="linux/arm64"; JIB_ARCH="arm64" ;;
    *)             JIB_PLATFORM="linux/amd64"; JIB_ARCH="amd64" ;;
esac

# Detect docker compose command. Use an array so the two-word form
# "docker compose" is passed as two arguments, not one quoted string.
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD=(docker compose)
else
    DOCKER_COMPOSE_CMD=(docker-compose)
fi

# --- Logging helpers ---
_info()  { printf '%s\n'         "$*"; }
_ok()    { printf '\033[0;32m✓\033[0m %s\n' "$*"; }
_warn()  { printf '\033[0;33m!\033[0m %s\n' "$*" >&2; }
_error() { printf '\033[0;31m✗\033[0m %s\n' "$*" >&2; }

# --- Profile to compose file mapping ---

profile_to_file() {
    case "$1" in
        core)           echo "" ;;
        file-handling)  echo "compose-with-file-handling.yaml" ;;
        gen-ai)         echo "compose-with-gen-ai.yaml" ;;
        oauth2)         echo "compose-with-oauth2-server.yaml" ;;
        *)              return 1 ;;
    esac
}

VALID_PROFILES="core file-handling gen-ai oauth2 all"

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
        core)           echo "${CORE_SERVICES[*]}" ;;
        gen-ai)         echo "${GEN_AI_SERVICES[*]}" ;;
        file-handling)  echo "${FILE_HANDLING_SERVICES[*]}" ;;
    esac
}

# --- Argument parsing ---

parse_args() {
    CMD="${1:-}"
    shift 2>/dev/null || true

    BUILD=false
    SKIP_MVN_SHARED_DEPS=false
    DOWN_REMOVE_VOLUMES=false
    SERVICES=()
    OTHER_ARGS=()
    PLATFORM_OVERRIDE=""

    for arg in "$@"; do
        if [ "$arg" == "--build" ] || [ "$arg" == "-b" ]; then
            BUILD=true
        elif [ "$arg" == "--skip-shared-core" ]; then
            SKIP_MVN_SHARED_DEPS=true
        elif [[ "$arg" == --tag=* ]]; then
            TAG="${arg#--tag=}"
        elif [[ "$arg" == --platform=* ]]; then
            local plat="${arg#--platform=}"
            case "$plat" in
                amd64) PLATFORM_OVERRIDE="linux/amd64" ;;
                arm64) PLATFORM_OVERRIDE="linux/arm64" ;;
                linux/amd64|linux/arm64) PLATFORM_OVERRIDE="$plat" ;;
                *)
                    echo "Error: unknown platform '$plat'"
                    echo "Valid values: amd64, arm64"
                    exit 1
                    ;;
            esac
        elif [ "$arg" == "--all" ] || [ "$arg" == "-a" ]; then
            PROFILES+=(file-handling gen-ai oauth2)
        elif [ "$arg" == "--volumes" ] || [ "$arg" == "-v" ]; then
            DOWN_REMOVE_VOLUMES=true
        elif [[ "$arg" == --with=* ]]; then
            local p="${arg#--with=}"
            if profile_to_file "$p" >/dev/null; then
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

    # Apply platform override after all args are parsed, so it wins
    # over the host-architecture auto-detection at the top of the script.
    if [ -n "$PLATFORM_OVERRIDE" ]; then
        JIB_PLATFORM="$PLATFORM_OVERRIDE"
        case "$PLATFORM_OVERRIDE" in
            linux/amd64) JIB_ARCH="amd64" ;;
            linux/arm64) JIB_ARCH="arm64" ;;
        esac
    fi

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
            "-Djib.to.image=smclab/openk9-api-gateway:$TAG" \
            "-Djib.platform.architecture=$JIB_ARCH" \
            -f app/api-gateway/pom.xml

        echo "Building Quarkus services..."
        for SVC in tenant-manager datasource ingestion searcher; do
            echo "Building $SVC..."
            ./mvnw package -DskipTests \
                -Dquarkus.profile=prod \
                "-Dquarkus.jib.platforms=$JIB_PLATFORM" \
                -Dquarkus.container-image.build=true \
                -Dquarkus.container-image.push=false \
                -Dquarkus.container-image.group=smclab \
                "-Dquarkus.container-image.name=openk9-$SVC" \
                "-Dquarkus.container-image.tag=$TAG" \
                "-pl" "app/$SVC"
        done
    )

    echo "--- Building Frontend Services ---"
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-search-frontend:$TAG" -f js-packages/search-frontend/Dockerfile .
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-admin-ui:$TAG" -f js-packages/admin-ui/Dockerfile .
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-tenant-ui:$TAG" -f js-packages/tenant-ui/Dockerfile .

    echo "--- Building Connectors ---"
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-web-connector:$TAG" -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector
}

build_gen_ai() {
    echo "--- Building AI Services ---"
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-rag-module:$TAG" -f ai-packages/rag-module/Dockerfile ai-packages/rag-module
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-embedding-module-base:$TAG" -f ai-packages/embedding-modules/Dockerfile ai-packages/embedding-modules
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-talk-to:$TAG" -f js-packages/talk-to/Dockerfile .
}

build_file_handling() {
    echo "--- Building File Services ---"
    docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-minio-connector:$TAG" -f connectors/minio-connector/connector/Dockerfile connectors/minio-connector/connector
    (cd core && for SVC in file-manager tika; do
        echo "Building $SVC..."
        ./mvnw package -DskipTests \
            -Dquarkus.profile=prod \
            "-Dquarkus.jib.platforms=$JIB_PLATFORM" \
            -Dquarkus.container-image.build=true \
            -Dquarkus.container-image.push=false \
            -Dquarkus.container-image.group=smclab \
            "-Dquarkus.container-image.name=openk9-$SVC" \
            "-Dquarkus.container-image.tag=$TAG" \
            "-pl" "app/$SVC"
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
            core)           build_core ;;
            gen-ai)         build_gen_ai ;;
            file-handling)  build_file_handling ;;
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
                "-Djib.to.image=smclab/openk9-api-gateway:$TAG" \
                "-Djib.platform.architecture=$JIB_ARCH" \
                -f app/api-gateway/pom.xml)
            ;;
        tenant-manager|datasource|ingestion|searcher|file-manager|tika)
            (cd core && ./mvnw package -DskipTests \
                -Dquarkus.profile=prod \
                "-Dquarkus.jib.platforms=$JIB_PLATFORM" \
                -Dquarkus.container-image.build=true \
                -Dquarkus.container-image.push=false \
                -Dquarkus.container-image.group=smclab \
                "-Dquarkus.container-image.name=openk9-$service" \
                "-Dquarkus.container-image.tag=$TAG" \
                "-pl" "app/$service")
            ;;
        search-frontend)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-search-frontend:$TAG" -f js-packages/search-frontend/Dockerfile .
            ;;
        admin-ui)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-admin-ui:$TAG" -f js-packages/admin-ui/Dockerfile .
            ;;
        tenant-ui)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-tenant-ui:$TAG" -f js-packages/tenant-ui/Dockerfile .
            ;;
        web-connector)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-web-connector:$TAG" -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector
            ;;
        minio-connector)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-minio-connector:$TAG" -f connectors/minio-connector/connector/Dockerfile connectors/minio-connector/connector
            ;;
        rag-module)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-rag-module:$TAG" -f ai-packages/rag-module/Dockerfile ai-packages/rag-module
            ;;
        embedding-module)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-embedding-module-base:$TAG" -f ai-packages/embedding-modules/Dockerfile ai-packages/embedding-modules
            ;;
        talk-to)
            docker build --pull --platform "$JIB_PLATFORM" -t "smclab/openk9-talk-to:$TAG" -f js-packages/talk-to/Dockerfile .
            ;;
    esac
}

_has_java_service() {
    local java_services="api-gateway tenant-manager datasource ingestion searcher file-manager tika"
    for svc in "$@"; do
        for jsvc in $java_services; do
            [ "$svc" = "$jsvc" ] && return 0
        done
    done
    return 1
}

do_build() {
    if [ ${#SERVICES[@]} -gt 0 ]; then
        # For multi-service builds that include at least one Java service,
        # build shared Maven dependencies once up front to avoid redundant
        # rebuilds when build_single is called for each service.
        if [ ${#SERVICES[@]} -gt 1 ] && [ "$SKIP_MVN_SHARED_DEPS" = false ] \
                && _has_java_service "${SERVICES[@]}"; then
            build_mvn_shared_deps
            SKIP_MVN_SHARED_DEPS=true
        fi
        for svc in "${SERVICES[@]}"; do
            build_single "$svc"
        done
    else
        build_by_profiles
    fi
}

# --- doctor command ---

do_doctor() {
    local any_fail=false

    _check_tool() {
        local label="$1" cmd="$2" min_ver="$3" detected_ver="$4"
        if [ -z "$detected_ver" ]; then
            printf '  %-22s %s\n' "$label" "MISSING"
            any_fail=true
        else
            local major minor
            major=$(echo "$detected_ver" | grep -oE '[0-9]+' | head -1)
            if [ -n "$min_ver" ] && [ "${major:-0}" -lt "$min_ver" ] 2>/dev/null; then
                printf '  %-22s %s  (found %s, need >= %s)\n' \
                    "$label" "WRONG_VERSION" "$detected_ver" "$min_ver"
                any_fail=true
            else
                printf '  %-22s %s  (%s)\n' "$label" "OK" "$detected_ver"
            fi
        fi
    }

    echo "k9.sh — prerequisite check"
    echo ""

    local java_ver=""
    java_ver=$(java -version 2>&1 | grep -oE '[0-9]+\.[0-9]+|[0-9]+' | head -1) || true
    # java -version prints "1.8.x" for Java 8, "11", "17", "21" for modern releases.
    # Normalise "1.x" → "x" so the major-version comparison works uniformly.
    case "$java_ver" in 1.*) java_ver="${java_ver#1.}" ;; esac
    _check_tool "java (>= 21)" "java" "21" "$java_ver"

    local docker_ver=""
    docker_ver=$(docker --version 2>/dev/null | grep -oE '[0-9]+\.[0-9.]+' | head -1) || true
    _check_tool "docker" "docker" "" "$docker_ver"

    local compose_ver=""
    compose_ver=$("${DOCKER_COMPOSE_CMD[@]}" version 2>/dev/null | grep -oE '[0-9]+\.[0-9.]+' | head -1) || true
    _check_tool "docker compose (v2)" "docker compose" "2" "$compose_ver"

    local node_ver=""
    node_ver=$(node --version 2>/dev/null | grep -oE '[0-9]+' | head -1) || true
    _check_tool "node (>= 20)" "node" "20" "$node_ver"

    local yarn_ver=""
    yarn_ver=$(yarn --version 2>/dev/null) || true
    _check_tool "yarn" "yarn" "" "$yarn_ver"

    local python_ver=""
    python_ver=$(python3 --version 2>/dev/null | grep -oE '[0-9]+\.[0-9]+' | head -1) || true
    _check_tool "python3 (>= 3.10)" "python3" "3" "$python_ver"

    echo ""
    if [ "$any_fail" = true ]; then
        _error "Install the missing requirements to run k9.sh."
        exit 1
    else
        _ok "All prerequisites satisfied."
    fi
}

# --- push command ---

do_push() {
    if [ -z "$OPENK9_REGISTRY" ]; then
        _error "OPENK9_REGISTRY is not set."
        _info  "Define it in your shell environment or in openk9/.env:"
        _info  "  OPENK9_REGISTRY=registry.example.com/openk9"
        exit 1
    fi

    if [ ${#SERVICES[@]} -eq 0 ]; then
        _error "No services specified. Usage: ./k9.sh push <service...> [--tag=TAG]"
        exit 1
    fi

    local push_failed=false
    for svc in "${SERVICES[@]}"; do
        local src_image="smclab/openk9-${svc}:${TAG}"
        local dst_image="${OPENK9_REGISTRY}/openk9-${svc}:${TAG}"

        # Verify the source image exists locally.
        if ! docker image inspect "$src_image" >/dev/null 2>&1; then
            _error "Image not found locally: $src_image"
            _info  "Build it first: ./k9.sh build $svc --tag=$TAG --platform=amd64"
            push_failed=true
            continue
        fi

        # Refuse to push non-amd64 images; the Kubernetes target is always amd64.
        local img_arch
        img_arch=$(docker image inspect --format '{{.Architecture}}' "$src_image" 2>/dev/null) || true
        if [ "$img_arch" != "amd64" ]; then
            _error "Image $src_image is $img_arch, not amd64."
            _info  "Rebuild for the correct platform before pushing:"
            _info  "  ./k9.sh build $svc --tag=$TAG --platform=amd64"
            push_failed=true
            continue
        fi

        _info "Tagging $src_image → $dst_image"
        docker tag "$src_image" "$dst_image"
        _info "Pushing $dst_image"
        docker push "$dst_image"
        _ok "$svc pushed."
    done

    if [ "$push_failed" = true ]; then
        _error "One or more services failed to push."
        exit 1
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
    IMAGE_TAG="$TAG" "${DOCKER_COMPOSE_CMD[@]}" "${flags[@]}" "$@"
}

# --- Usage ---

usage() {
    cat <<'USAGE'
k9.sh — OpenK9 local development CLI

Usage: ./k9.sh <command> [services...] [options]

Commands:
  build   [services...]   Build Docker images from source
  up      [services...]   Start the Docker Compose stack (alias: start)
  stop    [services...]   Stop running containers
  down    [services...]   Stop containers (volumes preserved by default)
  restart [services...]   Restart containers
  logs    [services...]   Follow container logs
  doctor                  Check prerequisites (java, docker, node, ...)
  push    <services...>   Tag and push images to OPENK9_REGISTRY

Options:
  -b, --build            Build images before starting/restarting
  --tag=TAG              Docker image tag (default: local-dev)
  --platform=ARCH        Override build platform: amd64 or arm64
  --with=PROFILE         Enable a compose profile (repeatable)
  -a, --all              Shorthand for --with all profiles
  --skip-shared-core     Skip Java core shared dependencies
                         (root POM, hibernate-rx-multitenancy,
                         common/, client/, tenant-events/).
                         Useful when only the service code changed.
  -v, --volumes          (down only) Also remove Docker volumes

Profiles (--with):
  core           Base services: PostgreSQL, OpenSearch, RabbitMQ,
                 API Gateway, Datasource, Tenant Manager, Ingestion,
                 Searcher, frontends, Caddy reverse proxy (default)
  file-handling  Core + file handling: MinIO, Tika, File Manager
  gen-ai         Core + AI services: RAG module, Embedding, Talk-To
  oauth2         Core + Keycloak OAuth2/OIDC identity provider

  Profiles are additive. Combine multiple --with flags to
  compose the stack you need.

Services (for targeted build/restart):
  api-gateway  tenant-manager  datasource  ingestion  searcher
  search-frontend  admin-ui  tenant-ui  web-connector
  rag-module  embedding-module  talk-to
  file-manager  tika  minio-connector

Build details:
  Core images are always built. Additional profiles add extra
  services on top of core.

  ./k9.sh build                       Build core images
  ./k9.sh build --with=gen-ai         Build core + AI images
  ./k9.sh build --with=file-handling  Build core + file images
  ./k9.sh build --all                 Build everything

  Java core shared dependencies (root POM, hibernate-rx-
  multitenancy, common/, client/, tenant-events/) are built
  automatically. Skip with --skip-shared-core.

  A single-service build (./k9.sh build datasource) automatically
  builds shared core dependencies for Java services.

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

  With --with=gen-ai, the Talk-To conversational
  interface is also available at:
    Talk-To:     https://demo.openk9.localhost/chat

  Talk-To is a conversational AI frontend that uses the RAG and
  Embedding modules to provide chat-based search. All three AI
  services (rag-module, embedding-module, talk-to) start together
  with the with-gen-ai profile.

Custom overlays:
  After starting the stack with k9.sh, you can attach
  additional services (seeders, connectors, debug tools)
  using docker compose directly:

    docker compose -f my-overlay.yaml up -d

  Declare the openk9 network as external in your file
  so the containers join the same network as the stack:

    networks:
      openk9:
        external: true
        name: openk9

  The network must be declared external because it is
  created and owned by the k9.sh compose project. A
  separate compose invocation cannot create or manage
  it — it can only join an existing one.

Examples:
  ./k9.sh up                          Start core services
  ./k9.sh up --build                  Build core, then start
  ./k9.sh up --with=gen-ai --build    Build core + AI, then start
  ./k9.sh up --with=oauth2            Start core + Keycloak
  ./k9.sh build --with=gen-ai         Build core + AI images
  ./k9.sh build --all                 Build all profiles
  ./k9.sh build datasource --tag=test Build datasource with custom tag
  ./k9.sh restart datasource --build  Rebuild and restart datasource
  ./k9.sh logs datasource             Follow datasource logs
  ./k9.sh down                        Stop containers (keep volumes)
  ./k9.sh down -v                     Stop containers and remove volumes
  ./k9.sh doctor                      Check all prerequisites
  ./k9.sh push datasource --tag=1.0   Push datasource image to registry
USAGE
}

# --- Main ---

parse_args "$@"

case "$CMD" in
    build)
        do_build
        ;;
    up|start)
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
        if [ "$DOWN_REMOVE_VOLUMES" = true ]; then
            compose down --volumes "${SERVICES[@]}"
        else
            compose down "${SERVICES[@]}"
            _info "Volumes preserved. Use './k9.sh down -v' to remove them."
        fi
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
    doctor)
        do_doctor
        ;;
    push)
        do_push
        ;;
    *)
        usage
        exit 1
        ;;
esac
