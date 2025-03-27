SHELL := /bin/bash
.PHONY: check_commands load_config build start stop

# Define variables
CONFIG_FILE := python_modules_info.txt
GIT_REPO_ROOT := $(shell git rev-parse --show-toplevel)
CONFIG_PATH := $(GIT_REPO_ROOT)/$(CONFIG_FILE)

# Check for required commands
check_commands:
	@command -v git >/dev/null 2>&1 || { echo "Error: 'git' is not installed. Aborting."; exit 1; }
	@command -v docker >/dev/null 2>&1 || { echo "Error: 'docker' is not installed. Aborting."; exit 1; }

# Load and verify environment variables
load_config: check_commands
	@if [ ! -f "$(CONFIG_PATH)" ]; then \
		echo "Error: Configuration file '$(CONFIG_PATH)' not found."; \
		exit 1; \
	fi
	@source "$(CONFIG_PATH)" || { echo "Error: Failed to source '$(CONFIG_PATH)'"; exit 1; }; \
	if [ -z "$$OPENK9_VERSION" ]; then \
		echo "Error: Required variable OPENK9_VERSION is not set in $(CONFIG_PATH)"; \
		exit 1; \
	fi

# Build Docker image
build: load_config
	@source "$(CONFIG_PATH)"; \
	if [ -z "$$PYTHON_BASE_DOCKER_IMAGE" ]; then \
		echo "Error: Required variable PYTHON_BASE_DOCKER_IMAGE is not set in $(CONFIG_PATH)"; \
		exit 1; \
	fi; \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		-t openk9-rag-module:"$$OPENK9_VERSION" \
		.

# Start Docker Compose service
start: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose up -d; \
	unset OPENK9_VERSION

# Stop Docker Compose service
stop: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose stop; \
	unset OPENK9_VERSION
