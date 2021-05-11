#!/bin/bash

docker-compose -f docker-compose-elasticsearch.yml -f docker-compose-datasource.yml -f docker-compose-rabbitmq.yml up -d
docker-compose -f docker-compose-keycloak.yml up -d
docker-compose -f docker-compose-plugin-driver-manager.yml up -d
docker-compose -f docker-compose-ingestion.yml -f docker-compose-index-writer.yml -f docker-compose-entity-manager.yml -f docker-compose-searcher.yml up -d
docker-compose -f docker-compose-parser.yml -f docker-compose-ml.yml up -d 
docker-compose -f docker-compose-frontend.yml up -d