# Minio Connector

Minio connector is a service for extracting data from specific minio buckets.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Minio Api

This Rest service exposes one endpoint:


### Execute Minio endpoint

Call this endpoint to execute a crawler that extract buckets starting from minio domain

This endpoint takes different arguments in JSON raw body:

- **host**: Minio domain host name to extract from (required)
- **port**: Minio domain port to extract from (required)
- **accessKey**: access key connecting to Minio domain (required)
- **secretKey**: secret key connecting to Minio domain (required)
- **bucketName**: bucket name to extract from (required)
- **datasourcePayloadKey**: key used for datasource payload (optional, default None)
- **prefix**: bucket object prefix (optional, default None)
- **columns**: list of columns to extract (optional, default [])
- **additionalMetadata**: dictionary of metadata added to datasource payload (optional, default {})
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "host": "localhost",
    "port": "9000",
    "accessKey": "my_access_key",
    "secretKey": "my_secret_key",
    "bucketName": "bucket_name",
    "datasourcePayloadKey": "key",
    "prefix": "test",
    "columns": ["column 1", column 2"],
    "additionalMetadata": {"key": "value"},
    "datasourceId": 1,
    "tenantId": "1",
    "scheduleId": "1",
    "timestamp": 0
}'
```

### Health check endpoint

Call this endpoint to perform health check for service.

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/health'
```

### Get sample endpoint

Call this endpoint to get a sample of result.

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/sample'
```

# Quickstart

## How to run

## Docker

### Using Dockerfile


From this folder:

Build the Docker file:
```
docker build -t minio-connector .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name minio-connector-app minio-connector 
```

Command parameters:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Gitlab Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links