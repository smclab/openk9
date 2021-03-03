# Liferay Parser

This is a parser to extract contents from Liferay portal. In particular, users, calendars and contents from the document media library are extracted.\
Build image of service, run as Docker container and configure appropriate plugin to call it.\
The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Build

To build images of service you can simply run:

```
docker build -t liferay-parser .
```

A pre-built image of liferay parser is present in Smc Docker Hub at following link: https://hub.docker.com/repository/docker/smclab/liferay-parser/general.\
Then add service in main docker-compose file in the following way:

```
liferay-parser:
    image: smclab/liferay-parser:latest
    container_name: liferay-parser
    command: gunicorn -w 1 -t 120 -b 0.0.0.0:80 main:app
    ports:
        - "5005:80"
    environment:
        INGESTION_URL: <insert here url of Ingestion Api> 
```


## Liferay Parser Api

The service exposes APIs through Swagger on root url.
This Rest service exposes two different endpoints:

### Execute enpoint

This endpoint allows you to execute and start extraction of contents from Liferay portal instance.

This endpoint takes different arguments in JSON raw body:

- **domain**: domain where is located liferay portal
- **username**: username of specific liferay account
- **password**: password of specific liferay account
- **datasourceId**: id of datasource
- **timestamp**: timestamp to check data to be extracted
- **companyId**: companyId for which to extract the data

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5007/execute' \
--header 'Content-Type: application/json' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US' \
--data-raw '{
    "domain": "http://liferay-portal:8080",
    "username": "test@liferay.com",
    "password": "test",
    "timestamp": 0,
    "companyId": 20097,
    "datasourceId": 5
}'
```

### Status endpoint

```
curl --location --request GET 'http://localhost:5007/status' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US'
```

This endpoint allows you to view the last log line to check the status of the extraction process.