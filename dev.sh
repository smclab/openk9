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

        # We build and install ALL common dependencies first.
        # Explicitly targeting client submodules ensures gRPC classes are generated 
        # and Jandex indexes are created and available in local repo.
        echo "Installing all shared modules..."
        ./mvnw install -DskipTests \
            -pl common,client,client/grpc,client/grpc/common,client/grpc/searcher-client-grpc,client/grpc/tenant-manager-client-grpc,client/grpc/file-manager-client-grpc,client/grpc/datasource-client-grpc,client/grpc/app-manager,client/grpc/text-embedding,tenant-events,../vendor/hibernate-rx-multitenancy \
            -am

        # Spring Boot: api-gateway (uses jib-maven-plugin)
        echo "Building api-gateway (Spring Boot)..."
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
    # echo "--- Building Frontend Services ---"
    # docker build --build-arg BUILD_ENV=chatbot -t smclab/openk9-search-frontend:$TAG -f js-packages/search-frontend/Dockerfile .
    # docker build -t smclab/openk9-admin-ui:$TAG -f js-packages/admin-ui/Dockerfile .

    # 3. Build Connectors
    # echo "--- Building Connectors ---"
    # docker build -t smclab/openk9-web-connector:$TAG -f connectors/openk9-crawler/connector/Dockerfile connectors/openk9-crawler/connector

    echo "All images built successfully with tag: $TAG"
}

run_compose() {
    echo "Starting Docker Compose with local images..."
    # Execute the command with provided arguments, or default to 'up -d' if none
    if [ $# -eq 0 ]; then
        $DOCKER_COMPOSE -f $COMPOSE_FILE up -d
    else
        $DOCKER_COMPOSE -f $COMPOSE_FILE "$@"
    fi
}

usage() {
    echo "Usage: $0 [build|run|up|stop|down|restart]"
    echo ""
    echo "Commands:"
    echo "  build   Build all local images with tag $TAG"
    echo "  run/up  Run docker compose with local images (defaults to 'up -d')"
    echo "          Use --build to build before running: $0 up --build"
    echo "  stop    Stop containers"
    echo "  down    Stop and remove containers"
    echo "  restart Restart containers"
}

case "$1" in
    build)
        build_images
        ;;
    up|run)
        shift # remove 'up' or 'run' from arguments
        if [ "$1" == "--build" ]; then
            build_images
            shift # remove '--build' from arguments
        fi
        run_compose up -d "$@"
        ;;
    stop)
        $DOCKER_COMPOSE -f $COMPOSE_FILE stop
        ;;
    down)
        $DOCKER_COMPOSE -f $COMPOSE_FILE down --volumes
        ;;
    restart)
        $DOCKER_COMPOSE -f $COMPOSE_FILE restart
        ;;
    *)
        usage
        exit 1
        ;;
esac
