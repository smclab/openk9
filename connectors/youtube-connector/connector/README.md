# Youtube Connector

Youtube connector is a service for extracting video data from specific channels, playlists or single videos.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Youtube Api

This Rest service exposes one endpoint:


### Execute Youtube enpoint

Call this endpoint to execute a crawler that extract videos starting from Youtube urls

This endpoint takes different arguments in JSON raw body:

- **youtubeChannelUrl**: Youtube channel to extract from (required)
- **subtitleLang**: subtitle extracted languages (required)
  - **example**: en (English), it (Italian), fr (French)
- **audioFormat**: audio format extracted (optional, if not specified extract "m4a" format)
  - **example**: m4a, mp4
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "youtubeChannelUrl": "https://www.youtube.com/@SmcIt",
    "subtitleLang": ["it", "en"],
    "audioFormat": "m4a"
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

Using the command line go in the youtube-datasource parent folder\
From this folder:
```
cd ..
```

Build the Docker file:
```
docker build -t youtube-parser -f .\connector\Dockerfile .
```

**Command parameters:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name youtube-parser-app youtube-parser 
```

Command parameters:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Youtube Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links