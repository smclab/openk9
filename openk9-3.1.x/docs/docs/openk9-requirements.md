---
id: openk9-requirements
title: OpenK9 Requirements
---

# OpenK9 Requirements

# Compatibility matrix

This table outlines the tested and supported versions of third-party components for **Openk9** across multiple releases.

| Component   | Openk9 1.7.x |  Openk9 2.0.x   | Openk9 3.0.x               | 
|-------------|-----------------------------|-----------------------------|-----------------------------|
| Elasticsearch  | 7.x.x                        | N/A                        | N/A
| OpenSearch  | N/A | 2.x.x                        | 2.x.x                        | 
| RabbitMQ    | 3.12.x, 3.13.x | 3.12.x, 3.13.x                       | 4.x.x                       | 
| Keycloak    | 21.x, 22.x                   | 21.x, 22.x                   | 25.x                         |
| PostgreSQL  | 14.x, 15.x  | 14.x, 15.x                   | 15.x, 16.x                   | 
| MinIO       | RELEASE.2023-x.x | RELEASE.2023-x.x, 2024-x.x | RELEASE.2025-x.x             | 


## Minimum hardware requirements

- **CPU Cores:** 8
- **Memory:** 16 GB
- **Storage:** Variable, based on data ingested

## Software requirements

Openk9 needs to run of one of following sofware environments:

- Docker compose
- Kubernetes
- OpenShift

## Integration for Large Language Models to enable generative ai features

Openk9 supports the following integrations to enable the generative part features:

- Openai APIs
- Google VertxAI integration
- IBM Watsonx integration
- LLMs on premise through Ollama and Vllm

## Tips for configuring cpu and memory limits

Resources and CPU Limits are based on workload requirements.

In any case it is recommended for core services, based on the use of JVM, to setup:

- cpu limit >= 1000m (with lower values startup problems there could be)
- memory limit >= 1024Mi (with lower values startup problems there could be)
- heap size >= 60% of memory limit

For third party services follows owners guidelines.

## Storage sizing guidelines

### OpenSearch/Elasticsearch

To estimate the storage required for OpenSearch, consider a **medium document size** of approximately **10 KB per indexed document**. Additionally, factor in the **monthly growth rate** of the database size.

The storage needed for a single OpenSearch node can be calculated as follows:

**Total storage for an OpenSearch node:**  
`(Average document size) × (Initial number of indexed documents) + (Average document size) × (Monthly growth factor of total documents)`

### MinIO

To size the storage for MinIO, consider the **full size of the original data source**. This is particularly important during the initial data ingestion phase when all data is being processed at once.

### RabbitMQ

RabbitMQ requires storage to temporarily persist messages in queues. Once a message is processed, the storage is freed. The exact storage needs depend on factors such as:

- **Message ingestion rate:** Number of messages ingested per second
- **Processing rate:** Number of messages processed per second (this can be configured)
- **Average message size**

Under high back-pressure conditions (e.g., during the initial data ingestion when all documents are processed at once), higher storage capacity is required.

A general storage estimation can be made using the formula:

**Total storage for RabbitMQ:**  
`(Average message size) × (Initial number of indexed documents)`

Considering an average document size of **10 KB**, you can use this value for estimation.

### PostgreSQL

PostgreSQL stores only configuration data, so it requires minimal storage. Typically, **100 MB** is sufficient for most deployments.
