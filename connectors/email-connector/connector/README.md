# Email Parser

This is a parser to extract emails from Imap server. Build image of service, run as Docker container and configure appropriate plugin to call it.\
The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Build

To build images of service you can simply run:

```
docker build -t email-parser .
```

A pre-built image of email parser is present in Smc Docker Hub at following link: https://hub.docker.com/repository/docker/smclab/email-parser.\
Then add service in main docker-compose file in the following way:

```
email-parser:
    image: smclab/email-parser:latest
    container_name: email-parser
    command: gunicorn -w 1 -t 120 -b 0.0.0.0:80 main:app
    ports:
        - "5005:80"
    environment:
        INGESTION_URL: <insert here url of Ingestion Api> 
```

## Email Parser Api

The service exposes APIs through Swagger on root url.
This Rest service exposes two different endpoints:

### Execute enpoint

This endpoint allows you to execute and start extraction of emails from specific Imap server.

This endpoint takes different arguments in JSON raw body:

- **mailServer**: url of Imap server
- **port**: port of server; default is 993
- **username**: username of specific email account
- **password**: password of specific email account
- **datasourceId**: id of datasource
- **timestamp**: timestamp to check the emails to be extracted
- **folder**: folder to search for emails to extract; if not specified default is all folders

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5005/execute' \
--header 'Content-Type: application/json' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US' \
--data-raw '{
    "mailServer":"mail.openk9.it",
    "port":"993",
    "username":"XXXX",
    "password":"XXXX",
    "datasourceId":1,
    "timestamp": 0,
    "folder":"INBOX"
}'
```

### Status endpoint

This endpoint allows you to view the last log line to check the status of the extraction process.

```
curl --location --request GET 'http://localhost:5005/status' \
--header 'Cookie: COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US'
```
