---
id: create-external-data-parser
title: Create an External Custom Data Parser
---

To ingest data from your data source you need to realize new external data parser

## Prerequisites

No prerequisites are required; you can realize it using your favorite programming language.
The only constraint lies in respecting the interface defined by the ingestion APIs. You can learn about it in
[apposite Api section](/docs/api/ingestion-api)

### Create External Data Parser using Python

A complete example is present at the link https://github.com/smclab/openk9-example-python-datasource

External data parser must expose endpoints to be called by Openk9 when it triggers new data ingestion.

When openk9 calls the parser, it passes any configuration parameters in the request.

``python
kubectl config set-context --current --namespace = openk9
``

Parser need to respect ingestion Api interface and pass in request body following parameters:

- **contentId**:

- **datasourceId**:

- **parsingDate**:

- **rawContent**:

- **datasourcePayload**:

- **resources**:
