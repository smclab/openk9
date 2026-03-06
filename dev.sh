#!/bin/bash
set -e

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
    echo "Usage: $0 <command> [services...] [--profile=PROFILE]... [--tag=TAG] [--build]"
    echo ""
    echo "Commands:"
    echo "  build   [services...] [--tag=TAG] Build images (default tag: $TAG)"
    echo "  start   [services...] [--build]   Start containers (rebuilds initializer)"
    echo "  stop    [services...]             Stop containers"
    echo "  down    [services...]             Stop and remove containers (with volumes)"
    echo "  restart [services...] [--build]   Restart containers (build first with --build)"
    echo "  logs    [services...]             Follow container logs"
    echo ""
    echo "Profiles: $VALID_PROFILES (default: core)"
    echo "  core      Base services only (compose.yaml)"
    echo "  files     Core + file management (minio, tika, file-manager)"
    echo "  ai        Core + AI services (rag, embedding, talk-to)"
    echo "  keycloak  Core + Keycloak IdP"
    echo "  all       Core + files + AI"
    echo ""
    echo "Multiple profiles can be combined:"
    echo "  --profile=keycloak --profile=ai   Core + Keycloak + AI"
    echo ""
    echo "Valid services: ${VALID_SERVICES[*]}"
    echo ""
    echo "Examples:"
    echo "  $0 build                            Build all images"
    echo "  $0 build tenant-ui --tag=my-tag     Build tenant-ui with custom tag"
    echo "  $0 start                            Start core services"
    echo "  $0 start --profile=all              Start all services (core + files + AI)"
    echo "  $0 start --profile=keycloak         Start core + Keycloak"
    echo "  $0 start --profile=keycloak --profile=ai  Start core + Keycloak + AI"
    echo "  $0 start --profile=ai --build       Build and start core + AI"
    echo "  $0 restart tenant-ui --build        Rebuild and restart tenant-ui"
    echo "  $0 stop --profile=all               Stop all services"
    echo "  $0 logs tenant-ui                   Follow tenant-ui logs"
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
