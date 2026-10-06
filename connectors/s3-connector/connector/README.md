# S3 Connector

S3 connector is a service for extracting data from buckets of any S3-compatible object storage
(for example SeaweedFS, MinIO or AWS S3). It connects to the endpoint over plain HTTP.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## S3 Api

This Rest service exposes one endpoint:


### Execute endpoint

Call this endpoint to execute a crawler that extracts objects from a bucket of the S3 endpoint

This endpoint takes different arguments in JSON raw body:

- **host**: host name of the S3 endpoint, without `http://` (required)
- **port**: S3 port of the endpoint, e.g. `8333` for SeaweedFS or `9000` for MinIO (required)
- **accessKey**: access key of the S3 endpoint (required)
- **secretKey**: secret key of the S3 endpoint (required)
- **bucketName**: bucket name to extract from (required)
- **datasourcePayloadKey**: key used for datasource payload (optional, default None)
- **prefix**: bucket object prefix (optional, default None)
- **columns**: list of columns to extract (optional, default [])
- **additionalMetadata**: dictionary of metadata added to datasource payload (optional, default {})
- **publicBaseUrl**: base of the links to the source objects written in `document.url` as `<base>/<bucket>/<object>` (optional, default `http://<host>:<port>`); set it when the connection endpoint is not reachable by the users
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
    "port": "8333",
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

Every object is sent with `document.url`, the public URL of the source object (no signature), and with `rawContent` set to the object text when its content type is `text/*`; for the other formats the text is extracted by the enrichers.

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

Build the Docker file:
```
docker build -t s3-connector .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name s3-connector s3-connector 
```

**Command parameters**:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Gitlab Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links