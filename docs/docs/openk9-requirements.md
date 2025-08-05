---
id: openk9-requirements
title: OpenK9 Requirements
---

## Minimum Hardware Requirements

- **CPU Cores:** 8
- **Memory:** 32 GB
- **Storage:** Variable based on data ingested

## Software Requirements

- **Kubernetes:** v1.x
- **OpenShift:** v1.x

## Tips for Configuring Resources and CPU Limits

Resources and CPU Limits are based on workload requirements.

In any case it is recommended for core services, based on the use of JVM, to setup:

- cpu limit >= 1000m (with lower values startup problems there could be)
- memory limit >= 1024Mi (with lower values startup problems there could be)
- heap size >= 60% of memory limit

For third party services follows owners guidelines.

## Storage Sizing Guidelines

### OpenSearch

To estimate the storage required for OpenSearch, consider a **medium document size** of approximately **10 KB per indexed document**. Additionally, factor in the **monthly growth rate** of the database size.

The storage needed for a single OpenSearch node can be calculated as follows:

**Total Storage for an OpenSearch Node:**  
`(Average document size) × (Initial number of indexed documents) + (Average document size) × (Monthly growth factor of total documents)`

### MinIO

To size the storage for MinIO, consider the **full size of the original data source**. This is particularly important during the initial data ingestion phase when all data is being processed at once.

_(Further details to be confirmed.)_

### RabbitMQ

RabbitMQ requires storage to temporarily persist messages in queues. Once a message is processed, the storage is freed. The exact storage needs depend on factors such as:

- **Message ingestion rate:** Number of messages ingested per second
- **Processing rate:** Number of messages processed per second (this can be configured)
- **Average message size**

Under high back-pressure conditions (e.g., during the initial data ingestion when all documents are processed at once), higher storage capacity is required.

A general storage estimation can be made using the formula:

**Total Storage for RabbitMQ:**  
`(Average message size) × (Initial number of indexed documents)`

Considering an average document size of **10 KB**, you can use this value for estimation.

### PostgreSQL

PostgreSQL stores only configuration data, so it requires minimal storage. Typically, **100 MB** is sufficient for most deployments.
