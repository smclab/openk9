#!/bin/bash
set -e

# Configuration
TAG="local-dev"
COMPOSE_FILE="compose-dev.yaml"

# Detect docker compose command
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Change to the openk9 directory
cd "$(dirname "$0")"

# --- Argument parsing ---
# Parses: COMMAND [services...] [--build] [other flags...]
# Results are stored in: CMD, SERVICES, BUILD, OTHER_ARGS

VALID_SERVICES=(
    api-gateway tenant-manager datasource ingestion searcher
    search-frontend admin-ui tenant-ui web-connector
)

parse_args() {
    CMD="${1:-}"
    shift 2>/dev/null || true

    BUILD=false
    SERVICES=()
    OTHER_ARGS=()

    for arg in "$@"; do
        if [ "$arg" == "--build" ]; then
            BUILD=true
        elif [[ "$arg" == -* ]]; then
            OTHER_ARGS+=("$arg")
        else
            validate_service "$arg"
            SERVICES+=("$arg")
        fi
    done
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
    echo "Starting build of all local images with tag: $TAG"

    # 1. Build Core Java Services
    echo "--- Building Core Java Services ---"
    (
        cd core
        chmod +x mvnw

        echo "Clean everything..."
        ./mvnw clean

        echo "Installing hibernate-rx-multitenancy..."
        ./mvnw install -P '!validate,!format' -DskipTests \
            -f "../vendor/hibernate-rx-multitenancy/pom.xml"

        # We build and install all common dependencies first.
        echo "Installing all shared modules..."
        for module in common client tenant-events; do
            echo "Installing $module..."
            ./mvnw install -DskipTests -f "$module/pom.xml"
        done

        # Spring Boot: api-gateway (uses jib-maven-plugin)
        echo "Building Spring Boot services..."
        ./mvnw package -DskipTests jib:dockerBuild \
            -Djib.to.image=smclab/openk9-api-gateway:$TAG \
            -f app/api-gateway/pom.xml

        # Quarkus Services (use quarkus-container-image-jib)
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

    # 2. Build Frontend Services (Docker context is root)
    echo "--- Building Frontend Services ---"
    docker build -t smclab/openk9-search-frontend:$TAG -f js-packages/search-frontend/Dockerfile .
    docker build -t smclab/openk9-admin-ui:$TAG -f js-packages/admin-ui/Dockerfile .
    docker build -t smclab/openk9-tenant-ui:$TAG -f js-packages/tenant-ui/Dockerfile .

    # 3. Build Connectors
    echo "--- Building Connectors ---"
    docker build -t smclab/openk9-web-connector:$TAG -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector

    echo "All images built successfully with tag: $TAG"
}

build_single() {
    local service=$1
    echo "--- Building: $service ---"
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

# --- Compose helpers ---

run_compose() {
    $DOCKER_COMPOSE -f $COMPOSE_FILE "$@"
}

# --- Usage ---

usage() {
    echo "Usage: $0 <command> [services...] [--build] [--tag=TAG]"
    echo ""
    echo "Commands:"
    echo "  build   [services...] [--tag=TAG] Build images (default tag: $TAG)"
    echo "  start   [services...] [--build] Start containers (rebuilds initializer, build first with --build)"
    echo "  stop    [services...]          Stop containers"
    echo "  down    [services...]          Stop and remove containers (with volumes)"
    echo "  restart [services...] [--build] Restart containers (build first with --build)"
    echo "  logs    [services...]          Follow container logs"
    echo ""
    echo "Valid services: ${VALID_SERVICES[*]}"
    echo ""
    echo "Examples:"
    echo "  $0 build                       Build all images"
    echo "  $0 build tenant-ui admin-ui    Build only tenant-ui and admin-ui"
    echo "  $0 start --build               Build all and start"
    echo "  $0 restart tenant-ui --build   Rebuild and restart tenant-ui"
    echo "  $0 build --tag=2026.1-SNAPSHOT Build all with a custom tag"
    echo "  $0 stop datasource             Stop only datasource"
    echo "  $0 logs tenant-ui              Follow tenant-ui logs"
}

# --- Main ---

parse_args "$@"

case "$CMD" in
    build)
        # Parse --tag from OTHER_ARGS (only valid for build command)
        for arg in "${OTHER_ARGS[@]}"; do
            if [[ "$arg" == --tag=* ]]; then
                TAG="${arg#--tag=}"
            else
                echo "Error: unknown option '$arg' for build command"
                exit 1
            fi
        done
        do_build
        ;;
    start)
        if [ "$BUILD" = true ]; then
            do_build
        fi
        # Always rebuild the initializer image before starting, as it
        # contains the seed.js script that provisions tenants and
        # configures connectors on first boot.
        run_compose build initializer
        run_compose up -d "${OTHER_ARGS[@]}" "${SERVICES[@]}"
        ;;
    stop)
        run_compose stop "${SERVICES[@]}"
        ;;
    down)
        run_compose down --volumes "${SERVICES[@]}"
        ;;
    restart)
        if [ "$BUILD" = true ]; then
            do_build
            # Use up -d to force recreate and pick up the new images
            run_compose up -d "${SERVICES[@]}"
        else
            run_compose restart "${SERVICES[@]}"
        fi
        ;;
    logs)
        run_compose logs -f "${SERVICES[@]}"
        ;;
    *)
        usage
        exit 1
        ;;
esac
