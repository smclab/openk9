# Database Connector

Database connector is a service for extracting data from specific databases.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Database Api

This Rest service exposes one endpoint:


### Execute Database enpoint

Call this endpoint to execute a crawler that extract tables from Database

This endpoint takes different arguments in JSON raw body:

- **dialect**: Database name such as 'mysql', 'oracle', 'postgresql', etc. (required)
- **driver**: Database driver (e.g., psycopg2, pymysql). (required)
- **username**: Database username. (required)
- **password**: Database password. (required)
- **host**: Database server address (IP or domain). (required)
- **port**: Port number for database connection. (required)
- **db**: Name of the database to connect to. (required)
- **schema**: Name of the schema to extract table from. (optional, default None)
- **table**: Name of the table to extract (required)
- **columns**: Name of the columns to extract (optional, if not specified extract all columns)
- **where**: Where condition used for extraction (optional, if not specified extract without conditions)
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "dialect": "mysql",
    "driver": pymysql,
    "username": "admin",
    "password": "password",
    "host": "localhost",
    "port": "8080",
    "db": "mydb",
    "table": "test_table",
    "columns": [],
    "where": "",
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

Using the command line go in the Database-datasource parent folder\
From this folder:
```
cd ..
```

Build the Docker file:
```
docker build -t database-connector .
```

**Command parameters:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name database-parser-app Database-parser 
```

Command parameters:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Database Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links