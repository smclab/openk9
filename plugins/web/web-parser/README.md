# Web Parser

This is a parser to crawl and extract web pages from specific sites. \
Run container from built image and configure appropriate plugin to call it.\
This parser is built with the Scrapy library. Inside Scrapy project 
two different spiders/crawlers are defined:

- a crawler that extracts content starting from a list of URLs and following links by defining a set of rules.
- a crawler that allows you to crawl a site by discovering the URLs using Sitemaps.

You can access the Scrapyd server console via port 6800. By accessing it you can, for each job, monitor its status and view its logs.\
The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.\

## Web Parser Api

This Rest service exposes three different endpoints:

### Execute Generic Web Crawler enpoint

Call this endpoint to execute a crawler that extract web pages starting from list of Urls

This endpoint takes different arguments in JSON raw body:

- **startUrls**: list of URLs to start from
- **allowedDomains**: list of allowed domains
- **allowedPaths**: list of allowed paths, specified by regular expressions
- **excludedPaths**: list of excluded paths, specified by regular expressions
- **datasourceId**: id of datasource
- **timestamp**: timestamp to check data to be extracted
- **depth**: depth to search contents
- **follow**: boolean to set if follow links from crawled pages; default is *True*
- **bodyTag**: html tag for main content to extract from page
- **titleTag**: html tag for title to assign to extracted page
- **pageCount**: count of page limit to crawl

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/execute-generic' \
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

### Execute Sitemap Web Crawler enpoint

Call this endpoint to execute a crawler that extract web pages starting from sitemap or from robots.txt file

This endpoint takes different arguments in JSON raw body:

- **sitemapUrls**: robots.txt file or sitemap list
- **allowedDomains**: username of specific liferay account
- **datasourceId**: id of datasource in Openk9
- **timestamp**: timestamp to check data to be extracted
- **bodyTag**: html tag for main content to extract from page
- **titleTag**: html tag for title to assign to extracted page (optional parameter, if not specified title from title tag is taken)
- **maxLength**: max length in characters for extracted content. If content length exceeds maxLength is truncated.
- **replaceRule**: rule expressed as list of two elements, where first is element to replace and second string with which to replace (optional parameter, if not specified, no replacement is done)

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/execute-sitemap' \
--header 'Content-Type: application/json' \
--data-raw '{
    "sitemapUrls": ["https://www.smc.it/sitemap.xml"],
    "allowedDomains": ["smc.it"],
    "bodyTag": "div#main-content",
    "titleTag": "title::text",
    "datasourceId": 1,
    "timestamp": 0,
    "maxLength": 10000
}'
```

### Cancel Job endpoint

Call this endpoint to cancel running job before it ends 

This endpoint takes the job_id as url parameter:

- **job_id**: id of running job

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/cancel-job/{job_id}'
```