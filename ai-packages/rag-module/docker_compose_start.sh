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
if [[ -z "${OPENK9_VERSION:-}" ]]; then
    echo "Error: Required variable OPENK9_VERSION are not set in $CONFIG_FILE."
    exit 1
fi

# Export OPENK9_VERSION so it is available to child processes (like docker compose).
export OPENK9_VERSION="$OPENK9_VERSION"

# Start the Docker Compose service.
docker compose start

# After running docker compose, unset the environment variable to clean up.
unset OPENK9_VERSION
