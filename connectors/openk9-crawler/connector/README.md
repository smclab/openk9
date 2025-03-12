# Web Parser

This is a parser to crawl and extract web pages from specific sites. \
Run container from built image and configure appropriate plugin to call it.\
This parser is built with the Scrapy library.

You can access the Scrapyd server console via port 6800. By accessing it you can, for each job, monitor its status and view its logs.\
The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.\

## Web Parser Api

This Rest service exposes two different endpoints:

### Execute Generic Web Crawler enpoint

Call this endpoint to execute a crawler that extract web pages starting from list of Urls

This endpoint takes different arguments in JSON raw body:

- **startUrls**: list of URLs to start from (required)
- **allowedDomains**: list of allowed domains (optional, if not specified allow all)
- **allowedPaths**: list of allowed paths (optional, if not specified allow all)
- **excludedPaths**: list of excluded paths (optional, if not specified exclude nothing)
- **depth**: depth to search contents (optional, if not specified )
- **follow**: boolean to set if follow links from crawled pages (optional, if not specified )
- **bodyTag**: html tag for main content to extract from page (optional, if not specified )
- **titleTag**: html tag for title to assign to extracted page (optional, if not specified )
- **pageCount**: count of page limit to crawl (optional, if not specified )
- **maxLength**: maximum length of extracted content (optional, if not specified )
- **specificTags**:
- **documentFileExtensions**: 
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
    "https://www.openk9.io"
  ],
  "allowedDomains": [],
  "allowedPaths": [],
  "excludedPaths": [],
  "bodyTag": "body",
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
  "excluded_paths": [],
  "document_file_extensions": [],
  "specific_tags": [],
  "close_spider_page_count": 0
}'
```

### Execute Sitemap Web Crawler enpoint

Call this endpoint to execute a crawler that extract web pages starting from sitemap's urls

This endpoint takes different arguments in JSON raw body:

- **sitemapUrls**: list of URLs to start from (required)
- **allowedDomains**: list of allowed domains (optional, if not specified allow all)
- **allowedPaths**: list of allowed paths, specified by regular expressions
- **excludedPaths**: list of excluded paths, specified by regular expressions
- **depth**: depth to search contents
- **follow**: boolean to set if follow links from crawled pages; default is *True*
- **bodyTag**: html tag for main content to extract from page (optional, if not specified get all body page)
- **titleTag**: html tag for title to assign to extracted page  (optional, if not specified get head title tag)
- **pageCount**: count of page limit to crawl
- **maxLength**: maximum length of extracted content (optional, if not specified )
- **specificTags**:
- **documentFileExtensions**: 
- **datasourceId**: id of datasource
- **tenantId**: id of tenant
- **scheduleId**: id of schedulation
- **timestamp**: timestamp to check data to be extracted


Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/startSitemapCrawling' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startUrls": ["https://smc.it"],
    "allowedDomains": ["smc.it"],
    "allowedPaths": [],
    "excludedPaths": [".php"],
    "datasourceId": 1,
    "timestamp": 0,
    "bodyTag": "div#main-content",
    "titleTag": "title::text",
    "depth": 0,
    "pageCount": 0
    "follow": true
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
curl --location --request POST 'http://localhost:500/sample'
```