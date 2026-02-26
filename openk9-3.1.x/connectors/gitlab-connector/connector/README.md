# Gitlab Connector

Gitlab connector is a service for extracting data from specific domains.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Gitlab Api

This Rest service exposes one endpoint:


### Execute Gitlab endpoint

Call this endpoint to execute a crawler that extract repos starting from gitlab domain

This endpoint takes different arguments in JSON raw body:

- **domain**: Gitlab domain to extract from (required)
- **accessToken**: access token connecting to Gitlab domain (required)
- **types**: list of data to extract (required)
  - **User**: extract users
  - **Project**: extract projects
  - **Project Issue**: extract issues (requires **Project**)
  - **Project Commit**: extract commits (requires **Project**)
  - **Project Branch**: extract branches (requires **Project**)
  - **Project Labels**: extract labels (requires **Project**)
  - **Project Milestone**: extract milestones (requires **Project**)
  - **Project Merge Request**: extract merge requests (requires **Project**)
- **itemsPerPage**: pagination items extracted per call (optional, if not specified get 100 items each call)
- **projectList**: List of project ids to be extracted (optional, if not specified get every projects)
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "domain": "https://git.smc.it",
    "accessToken": "123abc",
    "types": ["User", "Project", "Project Issue", "Project Commit"],
    "itemsPerPage": 20,
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

Using the command line go in the gitlab-datasource folder\
From this folder:
```
cd ..
```

Build the Docker file:
```
docker build -t gitlab-connector .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name gitlab-connector-app gitlab-connector 
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