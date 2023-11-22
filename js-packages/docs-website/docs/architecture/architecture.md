---
id: architecture
title: Architecture Overview
slug: /architecture

---

Openk9's architecture is composed by multiple components. Components can be splitted in third-party components and
custom components.

### Custom components

- [**Ingestion**](ingestion): is the component that handles data ingestion logic.
- [**Datasource**](datasource): is the component that handles data source management logic.
- [**Tenant Manager**](tenant-manager): is the component that handles entities and ontologies extracted from data.
- [**Entity Manager**](entity-manager): is the component that handles entities and ontologies extracted from data.
- [**Searcher**](searcher): is the component that defines search logic.
- [**File Manager**](file-manager): is the component that handles entities and ontologies extracted from data.


### Third-party components

- [**Elasticsearch**](https://www.elastic.co/): open source search and analytics engine.
- [**Neo4j**](https://neo4j.com/): open source graph database.
- [**RabbitMQ**](https://www.rabbitmq.com/): open source message broker.
- [**Keycloak**](https://www.keycloak.org/): open source identity and access management.
- [**MinIo**](https://min.io/): high-performance, S3 compatible object storage.

<br />
<br />

![img](../../static/img/architecture.png)
