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
  - **ends with**: /@channel, /@channel/videos, /@channel/shorts, /@channel/streams
- **subtitleLang**: subtitle extracted languages (required)
  - **example**: en (English), it (Italian), fr (French)
- **doExtractAudio**: Check if audio will be extracted [bool] (optional, if not specified do not extract)
- **audioFormat**: audio format extracted (optional, if not specified extract "m4a" format)
  - **example**: m4a, mp4
- **socketTimeout**: Time to wait for unresponsive hosts, in seconds (optional, if not specified waits 20 seconds)
- **sleepIntervalSubtitles**: Number of seconds to sleep between subtitles request (optional, if not specified waits 60 seconds)
  - **optimal**: 60 seconds (can be lowered)
- **sleepIntervalRequests**: Number of seconds to sleep between requests during extraction (optional, if not specified waits 5 seconds)
  - **optimal**: Between 5 and 10 seconds (more if needed)
- **sleepInterval**: Number of seconds to sleep between downloads during extraction (optional, if not specified waits 5 seconds)
- **doUseRandomWaitTime**: True uses random sleep time between sleepInterval and maxSleepInterval, false sleep time is sleepInterval (optional, default True)
- **maxSleepInterval**: Maximum number of seconds to sleep between downloads during extraction (optional, used if 'Use Random Wait Time' is set to True)
- **retriesCount**: Times to retry during extraction, value used for generic errors and download errors (optional, if not specified do retry 10 times)
- **maxReadBytesSize**: Download speed limit, in bytes/sec (optional, if not specified downloads 2048 bytes/sec)
- **doExtractComments**: Check if comments will be extracted [bool] (optional, if not specified do not extract)
- **maxTotalComments**: Maximum total number of comments to extract (optional, if not specified extract all)
  - **values**: [null] extract all, [int] extract until the specified number
- **maxRootComments**: Maximum number of root comments to extract (optional, if not specified extract all)
  - **values**: [null] extract all, [int] extract until the specified number
- **maxTotalReplies**: Maximum total number of replies comments to extract (optional, if not specified extract all)
  - **values**: [null] extract all, [int] extract until the specified number
- **maxRootCommentsReplies**: Maximum number of replies per root comment to extract (optional, if not specified extract all)
  - **values**: [null] extract all, [int] extract until the specified number
- **verbose**: Do use verbose logs yt-dlp (optional, default True)
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
    "doExtractAudio": true,
    "audioFormat": "m4a",
    "socketTimeout": 20,
    "sleepIntervalSubtitles": 60,
    "sleepIntervalRequests": 5,
    "sleepInterval": 5,
    "doUseRandomWaitTime": true,
    "maxSleepInterval": 30,
    "retriesCount": 10,
    "maxReadBytesSize": 2048,
    "doExtractComments": true,
    "maxTotalComments": 1000,
    "maxRootComments": null,
    "maxTotalReplies": 500,
    "maxRootCommentsReplies": 100,
    "verbose": true,
    "datasourceId": 1,
    "tenantId": "1",
    "scheduleId": "1",
    "timestamp": 0
}'
```
```
"maxTotalComments": 1000,
"maxRootComments": null,
"maxTotalReplies": 500,
"maxRootCommentsReplies": 100,

It will extract:
A maximum of 1000 comments in total ("maxTotalComments": 1000), 
All the root(base) comments ("maxRootComments": null), 
A maximum of 500 replies in total ("maxTotalReplies": 500),
A maximum of 100 replies under each root(base) comment ("maxRootCommentsReplies": 100)
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

### Using Docker Compose

Run the following command to build the connector
```
docker compose -f docker-compose.yml build
```

To run the connector use one of:
- This command runs the connector using youtube clients that don't need a PO Token
```
docker compose -f docker-compose.yml up -d
```
or
- This command runs the connector and also a server that provides a PO Token when extracting
```
docker compose -f docker-compose.yml -f compose-all.yml up -d
```

Using the version with the PO Token is less prone to have errors or missing content while extracting videos 

### Using Dockerfile

Build the Docker file:
```
docker build -t youtube-connector .
```

**Command parameters:
- **-t**: Set built image name

Run the built Docker image:
```
docker run -p 5000:5000 --name youtube-connector youtube-connector 
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
