SHELL := /bin/bash
.PHONY: check_commands load_config \
        build_openai build_sentence_transformer \
        start_openai start_sentence_transformer \
        stop_openai stop_sentence_transformer

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

# Build Docker images
build_openai: load_config
	@source "$(CONFIG_PATH)"; \
	if [ -z "$$PYTHON_BASE_DOCKER_IMAGE" ]; then \
		echo "Error: Required variable PYTHON_BASE_DOCKER_IMAGE is not set in $(CONFIG_PATH)"; \
		exit 1; \
	fi; \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		-t openk9-embedding-module-openai:"$$OPENK9_VERSION" \
		-f Dockerfile.openai \
		.

build_sentence_transformer: load_config
	@source "$(CONFIG_PATH)"; \
	if [ -z "$$PYTHON_BASE_DOCKER_IMAGE" ]; then \
		echo "Error: Required variable PYTHON_BASE_DOCKER_IMAGE is not set in $(CONFIG_PATH)"; \
		exit 1; \
	fi; \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		-t openk9-embedding-module-sentence-transformer:"$$OPENK9_VERSION" \
		-f Dockerfile.sentence_transformer \
		.

# Start services
start_openai: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose -f docker-compose.openai.yml up -d; \
	unset OPENK9_VERSION

start_sentence_transformer: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose -f docker-compose.sentence_transformer.yml up -d; \
	unset OPENK9_VERSION

# Stop services
stop_openai: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose -f docker-compose.openai.yml stop; \
	unset OPENK9_VERSION

stop_sentence_transformer: load_config
	@source "$(CONFIG_PATH)"; \
	export OPENK9_VERSION="$$OPENK9_VERSION"; \
	docker compose -f docker-compose.sentence_transformer.yml stop; \
	unset OPENK9_VERSION
