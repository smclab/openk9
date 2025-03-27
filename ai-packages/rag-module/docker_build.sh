#!/bin/bash

# Get the absolute path to the Git repository root
GIT_REPO_ROOT=$(git rev-parse --show-toplevel)

CONFIG_FILE_NAME="python_modules_info.txt"

# Read centralized python_modules_info.txt file
CONFIG_FILE="$GIT_REPO_ROOT/$CONFIG_FILE_NAME"
if ! source "$CONFIG_FILE"; then
    echo "Failed to source $CONFIG_FILE"
    exit 1
fi

# Check if required variables are set
if [[ -z "$PYTHON_BASE_DOCKER_IMAGE" || -z "$OPENK9_VERSION" ]]; then
    echo "PYTHON_BASE_DOCKER_IMAGE or OPENK9_VERSION is not set."
    exit 1
fi

# Build the Docker image
docker buildx build --build-arg PYTHON_BASE_DOCKER_IMAGE="$PYTHON_BASE_DOCKER_IMAGE" . -t openk9-rag-module:"$OPENK9_VERSION"

