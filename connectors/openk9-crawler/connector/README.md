# Web Connector

This is a connector to crawl and extract web pages from specific sites. \
Run container from built image and configure appropriate plugin to call it.\
This connector is built with the Scrapy library.

You can access the Scrapyd server console via port 6800. By accessing it you can, for each job, monitor its status and view its logs.\
The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.\

## Web connector Api

This Rest service exposes two different endpoints:

### Execute Generic Web Crawler endpoint

Call this endpoint to execute a crawler that extract web pages starting from list of Urls

This endpoint takes different arguments in JSON raw body:

- **startUrls**: list of URLs to start from (required)
- **allowedDomains**: list of allowed domains (optional, if not specified allow all)
- **allowedPaths**: list of allowed paths (optional, if not specified allow all)
- **excludedPaths**: list of excluded paths (optional, if not specified exclude nothing)
- **depth**: depth to search contents (optional, if not specified )
- **follow**: boolean to set if follow links from crawled pages (optional, if not specified )
- **bodyTag**: html tag for main content to extract from page (optional, if not specified )
- **excludedBodyTags**:list of excluded tags (css selector) (optional, if not specified exclude nothing)
- **titleTag**: html tag for title to assign to extracted page (optional, if not specified )
- **pageCount**: count of page limit to crawl (optional, if not specified )
- **maxLength**: maximum length of extracted content (optional, if not specified )
- **maxSizeBytes**: maximum size in bytes of files that can be processed (required)
- **certVerification**: SSL/TLS certificate verification while processing documents (optional, if not specified is True)
- **customMetadata**: map key-value where key is the metadata to extract and value is xpath expression to get element/s to extract from html
- **documentFileExtensions**: extensions of files to allowed during extraction
- **doUseDefaultMimetypeMap**: should use default mimetype map (optional, if not specified is True)
  - *used with `mimetypeMap` updates the default map with mimetypeMap*
  - <details>
    <summary>Default mimetype map</summary>
    
    ```json
    {
      "image/jpeg":"jpg",
      "image/png":"png",
      "image/gif":"gif",
      "application/pdf":"pdf",
      "text/plain":"txt",
      "application/msword":"doc",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document":"docx",
      "application/vnd.ms-excel":"xls",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":"xlsx",
      "application/zip":"zip",
      "application/json":"json",
      "application/vnd.ms-powerpoint":"ppt",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation":"pptx",
      "application/epub+zip":"epub",
      "application/xml":"xml",
      "application/pkcs7-mime":"p7m",
      "audio/mpeg":"mp3"
    }
    ```
    </details>
- **mimetypeMap**: map key-value where key is the mimetype and value is file extension (optional, if not specified must use doUseDefaultMimetypeMap)
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl -X 'POST' \
  'http://localhost:5000/startUrlsCrawling' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "startUrls": [
    "https://www.smc.it"
  ],
  "allowedDomains": ["www.smc.it"],
  "allowedPaths": [],
  "excludedPaths": [],
  "bodyTag": "body",
  "excludedBodyTags": [],
  "titleTag": "title::text",
  "pageCount": 0,
  "depth": 0,
  "datasourceId": 0,
  "scheduleId": "string",
  "timestamp": 0,
  "replaceRule": [
    "",
    ""
  ],
  "tenantId": "",
  "follow": true,
  "maxLength": 1000,
  "maxSizeBytes": 20971520,
  "certVerification": False,
  "excluded_paths": [],
  "document_file_extensions": [],
  "doUseDefaultMimetypeMap": False,
    "mimetypeMap": {
      "image/jpeg":"jpg",
      "image/png":"png",
      "image/gif":"gif",
      "application/pdf":"pdf"
    },
  "close_spider_page_count": 0
}'
```

### Execute Sitemap Web Crawler endpoint

Call this endpoint to execute a crawler that extract web pages starting from sitemap's urls

This endpoint takes different arguments in JSON raw body:

- **sitemapUrls**: list of URLs to start from (required)
- **allowedDomains**: list of allowed domains (optional, if not specified allow all)
- **allowedPaths**: list of allowed paths, specified by regular expressions
- **excludedPaths**: list of excluded paths, specified by regular expressions
- **depth**: depth to search contents
- **follow**: boolean to set if follow links from crawled pages; default is *True*
- **bodyTag**: html tag for main content to extract from page (optional, if not specified get all body page)
- **linksToFollow**: list of href xpath to follow during extraction (optional, if not specified is ignored)
- **usePlaywright**: boolean to set if the crawler should use playwright (optional, default is *False*)
- **playwrightSelector**: css/xpath selector to be waited for by playwright (optional, required if *usePlaywright* is *True*)
- **playwrightTimeout**: timeout waited by selector if not found (optional, default is *5000*)
- **excludedBodyTags**:list of excluded tags (css selector) (optional, if not specified exclude nothing)
- **titleTag**: html tag for title to assign to extracted page  (optional, if not specified get head title tag)
- **pageCount**: count of page limit to crawl
- **maxLength**: maximum length of extracted content (optional, if not specified)
- **maxSizeBytes**: maximum size in bytes of files that can be processed (required)
- **certVerification**: SSL/TLS certificate verification while processing documents (optional, if not specified is True)
- **customMetadata**: map key-value where key is the metadata added to Openk9 payload and value is xpath expression to get element/s to extract from html and ling to metadata
- **doExtractDocs**: if follows links when connector link from sitemap
- **documentFileExtensions**: extensions of files to allowed during extraction
- **doUseDefaultMimetypeMap**: should use default mimetype map (optional, if not specified is True)
  - *used with `mimetypeMap` updates the default map with mimetypeMap*
  - <details>
    <summary>Default mimetype map</summary>
    
    ```json
    {
      "image/jpeg":"jpg",
      "image/png":"png",
      "image/gif":"gif",
      "application/pdf":"pdf",
      "text/plain":"txt",
      "application/msword":"doc",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document":"docx",
      "application/vnd.ms-excel":"xls",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":"xlsx",
      "application/zip":"zip",
      "application/json":"json",
      "application/vnd.ms-powerpoint":"ppt",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation":"pptx",
      "application/epub+zip":"epub",
      "application/xml":"xml",
      "application/pkcs7-mime":"p7m",
      "audio/mpeg":"mp3"
    }
    ```
    </details>
- **mimetypeMap**: map key-value where key is the mimetype and value is file extension (optional, if not specified must use doUseDefaultMimetypeMap)
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted


Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/startSitemapCrawling' \
--header 'Content-Type: application/json' \
--data-raw '{
    "sitemapUrls": ["https://www.smc.it/sitemap.xml"],
    "allowedDomains": ["www.smc.it"],
    "allowedPaths": [],
    "excludedPaths": [".pdf"],
    "linksToFollow": ["//a[@class="class-name"]/@href"],
    "usePlaywright": true,
    "playwrightSelector": "div.card.card-simple.border.p-2",
    "playwrightTimeout": 5000,
    "datasourceId": 1,
    "timestamp": 0,
    "bodyTag": "div#main-content",
    "excludedBodyTags": [],
    "titleTag": "title::text",
    "depth": 0,
    "pageCount": 0
    "follow": true,
    "maxSizeBytes": 20971520,
    "certVerification": False,
    "doExtractDocs": true,
    "documentFileExtensions": [".pdf"],
    "doUseDefaultMimetypeMap": False,
    "mimetypeMap": {
      "image/jpeg":"jpg",
      "image/png":"png",
      "image/gif":"gif",
      "application/pdf":"pdf"
    },
    "customMetadata": {
      "metadataName1": "//span/text",
      "metadataName2": "//div[@id="images"]/a/text()"
    }
}'
```

### Cancel job endpoint

Call this endpoint to cancel running job before it ends 

This endpoint takes the job_id as url parameter:

- **job_id**: id of running job

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/cancel-job/{job_id}'
```


### Get results job endpoint

Call this endpoint to cancel running job before it ends 

This endpoint takes the job_id as url parameter:

- **job_id**: id of running job

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/cancel-job/{job_id}'
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

### Using Dockerfile (Single container)

Use the following commands from this folder (**openk9-crawler/connector/**)

Build the Docker file:
```
docker build --build-arg WITH_SCRAPYDWEB=1 -t web-connector .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the **Dockerfile**
- **--build-arg**: Docker build arguments
  - **WITH_SCRAPYDWEB**: _[1==Ture]_: Should scrapydweb be installed in this container (optional, default False)

Run the built Docker image:
```
docker run -p 5000:5000 -p 6800:6800 -p 5001:5001 --name web-connector-app web-connector 
```

**Command parameters**:
- **-p**: Exposed port to make api calls
  - **Port 5000**: exposes api server (required)
  - **Port 6800**: exposes scrapyd server (optional)
  - **Port 5001**: exposes scrapydweb web ui (optional, use when --build-arg WITH_SCRAPYDWEB=1)
- **-name**: Set docker container name


### Using Dockerfile (Dedicated scrapydweb container)

Use the following commands from this folder (**openk9-crawler/connector/**)

Build the Docker files:\
- **Connector**:
```
docker build -t web-connector .
```
- **Scrapydweb**: 
```
docker build -t scrapyd-web -f ./scrapydweb.Dockerfile .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the **Dockerfile**
- **--build-arg**: Docker build arguments
  - **WITH_SCRAPYDWEB**: Omitted since Scrapydweb will have its own container
    
\
Create the network (Handle communication between container):
```
docker network create -d bridge scrapyd-network
```

**Command parameters**:
- **-d**: Set the driver to manage the Network (default: bridge)

\
Run the built Docker images:
- **Connector**:
```
docker run -p 5000:5000 -p 6800:6800 --name web-connector-app --network scrapyd-network web-connector
```
- **Scrapydweb**: 
```
docker run -p 5001:5001 -e SCRAPYDWEB_PORT=5001 -e SCRAPYD_SERVERS=web-connector-app:6800 --name scrapyd-web-app scrapyd-web
```

**Command parameters**:
- **-p**: Exposed port to make api calls
  - **Port 5000**: exposes api server (required)
  - **Port 6800**: exposes scrapyd server (required)
- **-name**: Set docker container name
- **--network**: Set the communication network (required, set as previously created)
  - **-e**: Environment variable
    - **Scrapydweb**:
      - **SCRAPYDWEB_BIND**: ScrapydWeb server visible externally (optional, default 127.0.0.1)
      - **SCRAPYDWEB_PORT**: Accept connections on the specified port (optional, default 5001)
      - **ENABLE_AUTH**: Enables basic auth for the web UI (optional, default False)
        - _In order to enable basic auth, both USERNAME and PASSWORD should be non-empty strings_
        - **USERNAME**: Authentication username (required with ENABLE_AUTH)
        - **PASSWORD**: Authentication password (required with ENABLE_AUTH)
      - **SCRAPYD_SERVERS**: Scrapyd server host (required, use {connector-container-name}:6800)
        - the string format: username:password@ip:port#group
          - The default port would be 6800 if not provided,
          - Both basic auth and group are optional.
          - e.g. '127.0.0.1:6800' or 'username:password@localhost:6801#group'
        - the tuple format: (username, password, ip, port, group)
          - When the username, password, or group is too complicated (e.g. contains ':@#'),
          - or if ScrapydWeb fails to parse the string format passed in,
          - it's recommended to pass in a tuple of 5 elements.
          - e.g. ('', '', '127.0.0.1', '6800', '') or ('username', 'password', 'localhost', '6801', 'group')
      
## Kubernetes/Openshift

To run Web Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links