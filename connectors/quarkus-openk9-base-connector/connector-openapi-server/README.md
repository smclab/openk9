# Connector OpenAPI Server

This project is a Quarkus application that consumes an OpenAPI document conforming to the OpenAPI Specification to generate REST API classes and modules, it is used as custom library within the [Archetype](../archetype).  
To learn how classes can be generated from an OpenAPI document in Quarkus, [see this](https://docs.quarkiverse.io/quarkus-openapi-generator/dev/server.html).

## Generated Classes

### REST API

These APIs are necessary to comunicate with OpenK9:

**FormResource**  
Exposes **_/form_** GET endpoint, it is used to know the connector configuration.

**HealthResource**  
Exposes **_/health_** GET endpoint, it is used to know the status of connector.

**InvokeResource**  
Exposes **_/invoke_** POST endpoint, it is used to start the crawling of data.

**SampleResource**  
Exposes **_/sample_** GET endpoint, it is used to get an example of IngestionDTO accepted by OpenK9.

### Modules

**IngestionDTO**  
It is the DTO that OpenK9 accept to receive documents.
- **datasourceId**: Unique ID that identifies the datasource the ingested message belongs to.
- **tenantId**: Unique string that identifies the tenant the ingested message belongs to.
- **contentId**: Unique ID by datasource that identifies the resource inside Openk9.
- **parsingDate**: Date when scheduling associated with message is started.
- **rawContent**: Message raw content. Can be used to perform some elaboration or enrichment inside Openk9 pipeline.
- **datasourcePayload**: Reference to `DatasourcePayload` class.
- **resources**: `ResourcesDTO` object to pass resources associated with message.
- **acl**: `Acl` object to pass access control list associated with message.
- **scheduleId**: Unique string that identifies the scheduling the ingested message belongs to.
- **last**: Specify if it is the last message of scheduling. (Deprecated, see `type` property)
- **type**: Reference to `PayloadType` class.

**DatasourcePayload**  
It is the class used to pass the data crawled.

**ResourcesDTO**
- **binaries**: List of `BinaryDTO` objects containing binaries.
- **splitBinaries** : Specify if multiple binaries must be split up in differente messages.

**PayloadType**  
It has an enum with 3 values:
- **DOCUMENT**
- **HALT**
- **LAST**

**BinaryDTO**
- **id**: Unique string that identifies by datasource the binary inside Openk9.
- **name**: Name associated with binary resource.
- **contentType**: Content Type of the binary resource.
- **data**: Base64 encoded string of binary resource.
- **resourceId**: Unique ID that identifies the binary inside Openk9.

**Health**  
It is the class used in `HealthResource`, has an enum with 3 values:
- **UP**
- **DOWN**
- **UNKNOWN**

**InvokeRequest**  
It is the body request sent by OpenK9 to connector.
- **datasourceId**: ID of datasource.
- **scheduleId**: ID of schedule.
- **tenantId**: ID of tenant.
- **timestamp**: Timestamp relative to last ingestion date.

**Form**  
It is the class used in `FormResource`.

**FormField**  
It is used in `Form`.
- **info**
- **label**
- **name**
- **type**
- **size**
- **required**
- **values**: `FieldValue` object.
- **validator**: `FormFieldValidator` object.

**FieldValue**
- **value**
- **isDefault**

**FormFieldValidator**
- **min**
- **max**
- **regex**
- **datasourceConfig**: Get connector configuration for a specific datasource

---

## Compile Application

The application can be compiled using:

```shell
mvn compile
```

It generates classes under `target/generated-sources/jaxrs` path.