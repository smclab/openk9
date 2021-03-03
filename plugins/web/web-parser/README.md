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

### Execute Simple Web Crawler enpoint

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

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/execute-web-crawler' \
--header 'Content-Type: application/json' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US' \
--data-raw '{
    "startUrls": ["https://smc.it"],
    "allowedDomains": ["smc.it"],
    "allowedPaths": [],
    "excludedPaths": [".php"],
    "datasourceId": 1,
    "timestamp": 0,-
    "depth": 1,
    "follow": true
}'
```

### Execute Sitemap Web Crawler enpoint

Call this endpoint to execute a crawler that extract web pages starting from robots file or from sitemap

This endpoint takes different arguments in JSON raw body:

- **sitemapUrls**: robots.txt file or sitemap list
- **allowedDomains**: username of specific liferay account
- **datasourceId**: id of datasource
- **timestamp**: timestamp to check data to be extracted
- **depth**: companyId for which to extract the data
- **follow**: boolean to set if follow links from crawled pages; default is *False*

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/execute-sitemap-crawler' \
--header 'Content-Type: application/json' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US' \
--data-raw '{
    "sitemapUrls": ["https://www.smc.it/robots.txt"],
    "allowedDomains": [],
    "datasourceId": 1,
    "timestamp": 0,
    "depth": 3,
    "follow": true
}'
```

### Cancel Job endpoint

Call this endpoint to cancel running job before it ends 

This endpoint takes different arguments in JSON raw body:

- **job**: id of running job

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5008/cancel-job' \
--header 'Content-Type: application/json' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US' \
--data-raw '{
    "job": "01befe1e781d11eb8d7a0242ac180002"
}'
```