SHELL := /bin/bash
.PHONY: check_commands load_config build start stop
.DEFAULT_GOAL := help

# Define variables
RAG_MODULE_DOCKER_IMAGE_NAME := openk9-rag-module
CONFIG_FILE := python_modules_config.txt
GIT_REPO_ROOT := $(shell git rev-parse --show-toplevel 2>/dev/null)
CONFIG_PATH := $(if $(GIT_REPO_ROOT),$(GIT_REPO_ROOT)/$(CONFIG_FILE),)

# Show help message
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "OpenK9 RAG Module Management"
	@echo ""
	@echo "Targets:"
	@echo "  check_commands  Verify required tools (git, docker) and environment"
	@echo "  load_config     Load and validate configuration from $(CONFIG_FILE)"
	@echo "  build           Build Docker image with configured base image and version"
	@echo "  start           Start Docker Compose services in detached mode"
	@echo "  stop            Stop running Docker Compose services"
	@echo "  help            Show this help message"
	@echo ""
	@echo "Typical workflow:"
	@echo "  make build      # Build the Docker image"
	@echo "  make start      # Start the services"
	@echo "  make stop       # Stop the services"

# Check for required commands and environment
check_commands:
	@command -v git >/dev/null 2>&1 || { echo "Error: 'git' is not installed. Aborting."; exit 1; }
	@git rev-parse --show-toplevel >/dev/null 2>&1 || { echo "Error: Not in a git repository. Aborting."; exit 1; }
	@command -v docker >/dev/null 2>&1 || { echo "Error: 'docker' is not installed. Aborting."; exit 1; }
	@test -n "$(GIT_REPO_ROOT)" || { echo "Error: Failed to get git repository root"; exit 1; }

# Load and verify environment variables
load_config: check_commands
	@test -f "$(CONFIG_PATH)" || { echo "Error: Config file '$(CONFIG_PATH)' not found"; exit 1; }
	@source "$(CONFIG_PATH)" || { echo "Error: Failed to source '$(CONFIG_PATH)'"; exit 1; }; \
	test -n "$$OPENK9_VERSION" || { echo "Error: OPENK9_VERSION not set in config"; exit 1; }; \
	test -n "$$PYTHON_BASE_DOCKER_IMAGE" || { echo "Error: PYTHON_BASE_DOCKER_IMAGE not set in config"; exit 1; }

# Build Docker image
build: load_config
	@source "$(CONFIG_PATH)" && \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		-t "$(RAG_MODULE_DOCKER_IMAGE_NAME):$$OPENK9_VERSION" \
		.

# Start Docker Compose service
start: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose up -d && \
	unset OPENK9_VERSION

# Stop Docker Compose service
stop: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose stop && \
	unset OPENK9_VERSION
