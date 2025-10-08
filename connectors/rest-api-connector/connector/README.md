# Rest API Connector

Rest API connector is a service for extracting data from specific rest api urls.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Rest API Api

This Rest service exposes one endpoint:


### Execute Rest API endpoint

Call this endpoint to execute a crawler that extract data starting from Rest API urls

This endpoint takes different arguments in JSON raw body:

- **requestList**: List of Rest API request to extract from (required)
  - Accepts _string_ or _object_
    - **string**: Url to Rest API (sends a get request with the url)
    - **object**:
      - **requestMethod**: _string_: Method for the request (optional, default "GET")
        - Accepts: "GET", "POST"
      - **requestUrl**: _string_: Url to Rest API (required)
      - **requestItemList**: _string_: Key to a list of items (optional, default Null)
        - If using an empty string it expects an array response
      - **requestPagination**: _object_: (optional, default Null)
        - **nextInResponse**: _string_: Key in the response body to next request (optional, default Null)
        - **pageBasedPagination**: _object_: Handles request that use query "page=" or similar (optional, default Null)
          - **pageParam**: _object_: Parameter that acts as page (optional, default `{"paramName":"page", "paramValue":1, "paramIncrementAmount":1}`)
            - **paramName**: _string_: Name of the parameter, usually "page" (required)
            - **paramValue**: _int_: The starting page, usually 1 (required)
            - **paramIncrementAmount**: _int_: Defines by how much param value increases (optional, default Null)
              - Null == 1
          - **limitParam**: _object_: Parameter that acts as limit (optional, default Null)
            - **paramName**: _string_: Name of the parameter, usually "limit" (required)
            - **paramValue**: _int_: The item limit per request (required)
          - **maxPages**: _int_: Maximum number of pages (optional, default Null)
            - Used to stop this request extraction if `page > maxPages`
        - **offsetBasedPagination**: _object_: Handles request that use query "offset=" and "limit=" or similar (optional, default Null)
          - **offsetParam**: _object_: Parameter that acts as offset (optional, default `{"paramName":"offset", "paramValue":0, "paramIncrementAmount":null}`)
            - **paramName**: _string_: Name of the parameter, usually "offset" (required)
            - **paramValue**: _int_: The starting offset, usually 0 (required)
            - **paramIncrementAmount**: _int_: Defines by how much param value increases (optional, default Null)
              - Null == `limitParam.paramValue`
          - **limitParam**: _object_: Parameter that acts as limit (optional, default Null)
            - **paramName**: _string_: Name of the parameter, usually "limit" (required)
            - **paramValue**: _int_: The item limit per request (required)
          - **total**: _int_: Maximum number of items (optional, default Null)
            - Used to stop this request extraction if `offset > total`
      - **requestAuth**: _object_: Overloads the authentication for this request (optional, default Null)
        - **username**: _string_: Authentication username (required)
        - **password**: _string_: Authentication password (required)
- **globalAuth**: _object_: Global authentication for all requests, if not using `requestAuth` (optional, default Null)
  - **username**: _string_: Authentication username (required)
  - **password**: _string_: Authentication password (required)
- **datasourceId**: _int_: id of datasource
- **tenantId**: _string_: id of tenant
- **scheduleId**: _string_: id of schedulation
- **timestamp**: _int_: timestamp to check data to be extracted

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/getData' \
--header 'Content-Type: application/json' \
--data-raw '{
    "requestList": [
      "https://example.com",
      {
        "requestUrl": "https://example.com",
        "requestItemList": "results",
        "requestAuth": {
          "username": "example",
          "password": "example"
        }
        "requestPagination": {
          "nextInResponse": "next",
          "pageBasedPagination": {
            "pageParam": {
              "paramName": "page",
              "paramValue": 1,
              "paramIncrementAmount": 1
            },
            "pageSizeParam": {
              "paramName": "pageSize",
              "paramValue": 10
            },
            "maxPages": 100
          },
          "offsetBasedPagination": {
            "offsetParam": {
              "paramName": "offset",
              "paramValue": 0,
              "paramIncrementAmount": 10
            },
            "limitParam": {
              "paramName": "limit",
              "paramValue": 10
            },
            "total": 100
          }
        }
      }
    ],
    "globalAuth": {
      "username": "global_example",
      "password": "global_example"
    },
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

Start form the parent folder (`rest-api-connector`)

Build the Docker file:
```
docker build -t rest-api-parser -f .\connector\Dockerfile .
```

**Command parameters**:
  - **-t**: Set built image name
  - **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name rest-api-parser-app rest-api-parser 
```

**Command parameters**:
  - **-p**: Exposed port to make api calls
  - **-name**: Set docker container name

## Kubernetes/Openshift

To run Rest API Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links