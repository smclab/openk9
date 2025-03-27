#!/bin/bash
set -euo pipefail

# Check if required commands exist
command -v git >/dev/null 2>&1 || { echo "Error: 'git' is not installed. Aborting."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Error: 'docker' is not installed. Aborting."; exit 1; }

# Get the absolute path to the Git repository root
GIT_REPO_ROOT=$(git rev-parse --show-toplevel)

# Define the configuration file name and full path
CONFIG_FILE_NAME="python_modules_info.txt"
CONFIG_FILE="$GIT_REPO_ROOT/$CONFIG_FILE_NAME"

# Ensure the configuration file exists
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file '$CONFIG_FILE' not found."
    exit 1
fi

# Source the configuration file to load environment variables
if ! source "$CONFIG_FILE"; then
    echo "Error: Failed to source '$CONFIG_FILE'."
    exit 1
fi

# Validate required variables are set
if [[ -z "${PYTHON_BASE_DOCKER_IMAGE:-}" || -z "${OPENK9_VERSION:-}" ]]; then
    echo "Error: Required variables PYTHON_BASE_DOCKER_IMAGE or OPENK9_VERSION are not set in $CONFIG_FILE."
    exit 1
fi

# Build the Docker image using buildx
docker buildx build \
    --build-arg PYTHON_BASE_DOCKER_IMAGE="$PYTHON_BASE_DOCKER_IMAGE" \
    -t openk9-rag-module:"$OPENK9_VERSION" \
    .
