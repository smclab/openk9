# Enricher OpenAPI Server

This project is a Quarkus application that consumes an OpenAPI document conforming to the OpenAPI Specification to generate REST API endpoints and the corresponding model classes, it is used as custom library within the [Archetype](../archetype).  
To learn how classes can be generated from an OpenAPI document in Quarkus, [see this](https://docs.quarkiverse.io/quarkus-openapi-generator/dev/server.html).

## Generated Classes

### REST API

These APIs are necessary to comunicate with OpenK9:

**FormResource**  
Exposes **_/form_** GET endpoint, it is used to know the enricher configuration.

**HealthResource**  
Exposes **_/health_** GET endpoint, it is used to know the status of enricher.

**ProcessResource**
Expose **_/process_** POST endpoint, it is used to start the data enrichment process.

### Model Classes

**Health**  
It is the class used in `HealthResource`, has an enum with 3 values:
- **UP**
- **DOWN**
- **UNKNOWN**

**OpenK9Input**
It is the class used in `ProcessResource`.
- **payload**: Contains a map of data to enrich.
- **enrichItemConfig**: Defines the configuration needed to use the enricher.
- **replyTo**: The token needed to callback OpenK9.

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

---

## Compile Application

The application can be compiled using:

```shell
./mvnw compile
```

It generates classes under `target/generated-sources/jaxrs` path.