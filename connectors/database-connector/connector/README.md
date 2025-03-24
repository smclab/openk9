# Wordpress Connector

Wordpress connector is a service for extracting data from specific Wordpress sites.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Wordpress Api

This Rest service exposes one endpoint:


### Execute Wordpress enpoint

Call this endpoint to execute a crawler that extract data from Wordpress

This endpoint takes different arguments in JSON raw body:

- **hostName**: Wordpress host (required)
- **dataType**: Wordpress extracted data types (required)
    - **Posts**
    - **Pages**
    - **Comments**
    - **Users**
- **doAuth**: Authentication on Wordpress before extraction? (required)
- **username**: Wordpress username. (optional, if not specified is None, use if doAuth is true)
- **password**: Wordpress password. (optional, if not specified is None, use if doAuth is true)
- **itemsPerPage**: Wordpress Items extracted per call (pagination) (optional, if not specified default to 10)
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "hostName": "example.wordpress.com",
    "dataType": ["Posts", "Pages", "Comments", "Users"],
    "doAuth": true,
    "username": "admin",
    "password": "password",
    "itemsPerPage": 10,
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
curl --location --request POST 'http://localhost:500/sample'
```

# Quickstart

## How to run

## Docker

### Using Dockerfile

Using the command line go in the wordpress-datasource parent folder\
From this folder:
```
cd ..
```

Build the Docker file:
```
docker build -t wordpress-parser -f .\connector\Dockerfile .
```

**Command parameters:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name wordpress-parser-app wordpress-parser
```

Command parameters:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Wordpress Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links