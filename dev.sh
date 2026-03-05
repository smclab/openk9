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

build_images() {
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

        SERVICES=("tenant-manager" "datasource" "ingestion" "searcher")
        for SVC in "${SERVICES[@]}"; do
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

build_single_image() {
    local service=$1
    echo "--- Building single service: $service ---"
    case "$service" in
        api-gateway)
            (cd core && ./mvnw package -DskipTests jib:dockerBuild -Djib.to.image=smclab/openk9-api-gateway:$TAG -f app/api-gateway/pom.xml)
            ;;
        tenant-manager|datasource|ingestion|searcher)
            (cd core && ./mvnw package -DskipTests -Dquarkus.profile=prod -Dquarkus.container-image.build=true -Dquarkus.container-image.push=false -Dquarkus.container-image.group=smclab -Dquarkus.container-image.name=openk9-$service -Dquarkus.container-image.tag=$TAG -pl app/$service)
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
        *)
            echo "Error: unknown service '$service'"
            echo "Valid services: api-gateway, tenant-manager, datasource, ingestion, searcher, search-frontend, admin-ui, tenant-ui"
            exit 1
            ;;
    esac
}

run_compose() {
    echo "Starting Docker Compose with local images..."

    # Build Initializer Image
    $DOCKER_COMPOSE -f $COMPOSE_FILE build initializer

    # Execute the command with provided arguments, or default to 'up -d' if none
    if [ $# -eq 0 ]; then
        $DOCKER_COMPOSE -f $COMPOSE_FILE up -d
    else
        $DOCKER_COMPOSE -f $COMPOSE_FILE "$@"
    fi
}

usage() {
    echo "Usage: $0 [build|start|stop|down|restart]"
    echo ""
    echo "Commands:"
    echo "  build   Build all local images with tag $TAG"
    echo "  start   Run docker compose with local images (defaults to 'up -d')"
    echo "          Use --build to build before running: $0 start --build"
    echo "  stop    Stop containers"
    echo "  down    Stop and remove containers"
    echo "  restart Restart containers or specific service(s)"
    echo "          Use --build to rebuild specific service(s) before restarting: $0 restart api-gateway --build"
}

case "$1" in
    build)
        shift
        if [ $# -eq 0 ]; then
            build_images
        else
            for svc in "$@"; do
                build_single_image "$svc"
            done
        fi
        ;;
    start)
        shift # remove 'start' from arguments
        BUILD=false
        SERVICES=""
        
        # Parse arguments to find --build and identify services
        OTHER_ARGS=""
        for arg in "$@"; do
            if [ "$arg" == "--build" ]; then
                BUILD=true
            elif [[ "$arg" == -* ]]; then
                OTHER_ARGS="$OTHER_ARGS $arg"
            else
                SERVICES="$SERVICES $arg"
            fi
        done

        # Trim whitespace
        SERVICES=$(echo "$SERVICES" | sed 's/^[ \t]*//;s/[ \t]*$//')
        OTHER_ARGS=$(echo "$OTHER_ARGS" | sed 's/^[ \t]*//;s/[ \t]*$//')

        if [ "$BUILD" = true ]; then
            if [ -n "$SERVICES" ]; then
                for svc in $SERVICES; do
                    build_single_image "$svc"
                done
            else
                build_images
            fi
        fi
        run_compose up -d $OTHER_ARGS $SERVICES
        ;;
    stop)
        $DOCKER_COMPOSE -f $COMPOSE_FILE stop
        ;;
    down)
        $DOCKER_COMPOSE -f $COMPOSE_FILE down --volumes
        ;;
    restart)
        shift # remove 'restart'
        BUILD=false
        SERVICES=""
        for arg in "$@"; do
            if [ "$arg" == "--build" ]; then
                BUILD=true
            else
                SERVICES="$SERVICES $arg"
            fi
        done
        
        # Trim leading/trailing whitespace without xargs
        SERVICES=$(echo "$SERVICES" | sed 's/^[ \t]*//;s/[ \t]*$//')

        if [ "$BUILD" = true ]; then
            if [ -n "$SERVICES" ]; then
                for svc in $SERVICES; do
                    build_single_image "$svc"
                done
            else
                build_images
            fi
            # Use up -d to force recreate and pick up the new images
            run_compose up -d $SERVICES
        else
            $DOCKER_COMPOSE -f $COMPOSE_FILE restart $SERVICES
        fi
        ;;
    *)
        usage
        exit 1
        ;;
esac
