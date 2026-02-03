#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

SHELL := /bin/bash
.PHONY: check_commands load_config \
        build_base build_sentence_transformer \
        start_base start_sentence_transformer \
        stop_base stop_sentence_transformer
.DEFAULT_GOAL := help

# Define variables
CONFIG_FILE := python_modules_config.txt
GIT_REPO_ROOT := $(shell git rev-parse --show-toplevel 2>/dev/null)
CONFIG_PATH := $(if $(GIT_REPO_ROOT),$(GIT_REPO_ROOT)/$(CONFIG_FILE),)

# Show help message
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "OpenK9 Embedding Modules Management"
	@echo ""
	@echo "Targets:"
	@echo "  check_commands               Verify required tools (git, docker) and environment"
	@echo "  load_config                  Load and validate configuration from $(CONFIG_FILE)"
	@echo "  build_base                   Build Docker image for base module with configured settings"
	@echo "  build_sentence_transformer   Build Docker image for Sentence Transformer module"
	@echo "  start_base                   Start base module service in detached mode"
	@echo "  start_sentence_transformer   Start Sentence Transformer service in detached mode"
	@echo "  stop_base                    Stop running base module service"
	@echo "  stop_sentence_transformer    Stop running Sentence Transformer service"
	@echo "  help                         Show this help message"
	@echo ""
	@echo "Typical workflow for a module (e.g., base):"
	@echo "  make build_base              # Build the Docker image"
	@echo "  make start_base              # Start the service"
	@echo "  make stop_base               # Stop the service"
	@echo ""
	@echo "For Sentence Transformer module, replace 'base' with 'sentence_transformer' in the targets."

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

# Build Docker images
build_base: load_config
	@source "$(CONFIG_PATH)" && \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		--pull \
		-t openk9-embedding-module-base:"$$OPENK9_VERSION" \
		-f Dockerfile.base \
		.

build_sentence_transformer: load_config
	@source "$(CONFIG_PATH)" && \
	docker buildx build \
		--build-arg PYTHON_BASE_DOCKER_IMAGE="$$PYTHON_BASE_DOCKER_IMAGE" \
		--pull \
		-t openk9-embedding-module-sentence-transformer:"$$OPENK9_VERSION" \
		-f Dockerfile.sentence_transformer \
		.

# Start services
start_base: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose -f docker-compose.base.yml up -d && \
	unset OPENK9_VERSION

start_sentence_transformer: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose -f docker-compose.sentence_transformer.yml up -d && \
	unset OPENK9_VERSION

# Stop services
stop_base: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose -f docker-compose.base.yml stop && \
	unset OPENK9_VERSION

stop_sentence_transformer: load_config
	@source "$(CONFIG_PATH)" && \
	export OPENK9_VERSION="$$OPENK9_VERSION" && \
	docker compose -f docker-compose.sentence_transformer.yml stop && \
	unset OPENK9_VERSION
