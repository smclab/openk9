---
id: connector-requirements
title: Requirements for Openk9 connector
---

Check existing Openk9 connector to search for connector based on your data source. Explore [connectors section](/plugins).

If not available, to ingest data from your data source you need to realize new Openk9 connector.

Samples to start for realize new connector are available in [Python](python-base-connector.md) and [Java](python-java-connector.md)


## What is an Openk9 connector

A connector in Openk9 is a service that get data from external data source and send these to [Openk9 Ingestion component](../architecture/ingestion.md).

## Prerequisites

No prerequisites are required about programming language. You can realize it using your favorite programming language.

There are some specifications you need to respect when develop new Openk9 Connector.

### Connector Trigger Endpoint

An Openk9 connector must expose an endpoint which is then used by Openk9 to call the connector itself.

This endpoint must be have following mandatory parameters:

- **datasourceId**: Id of Datasource
- **scheduleId**: Id of Schedule
- **tenantId**: Id of Tenant
- **timestamp**: Timestamp relative to last ingestion date

The different ids coming from Openk9 then are propagated again to Openk9 Ingestion interface. They are used to properly forward data inside Openk9 processing steps.

The field **timestamp** can be used by connector to filter data and get only new or modified data.

### Openk9 Ingestion Interface

Connector when send data to Openk9 needs to respect [ingestion Api interface](/) and pass in request body following parameters:

- **contentId**: unique contentId for data sent to Openk9
- **datasourceId**: id of Datasource (received in request and propagated for)
- **scheduleId**: id of Schedule (received in request and propagated for)
- **tenantId**: id of Tenant (received in request and propagated for)
- **parsingDate**: date when data extraction starts
- **rawContent**: raw text content of data
- **datasourcePayload**: structured content of data
- **resources**: resources attached to data (Optional)
- **acl**: acl associated with data (Optional)
- **type**: used to specify the type of message. (Optional)
- **last**: boolean to indicates all data are sent to Openk9. (Optional)

#### Datasource Payload

Datasource payload consists in a structured object where you can pass metadata of the document you need to send to Openk9.

For example in casa of a web page datasource payload could be like following:

```
{
    "web": {
        "title": "Lorem ipsum Wikipendia",
        "content": "Lorem ipsum....",
        "url": "https://it.wikipedia.org/wiki/Lorem_ipsum"
    }
}
```

You can add all metadata you want, and map them to different key inside the object.

#### Resources

Using parameter *resources* is possibile to pass to Openk9 binary resources. To pass binaries to Openk9 you need to encode them to base64 string and then 
pass to parameters resources, building object like following:

```
{
    {
        "binaries": [
            {
                "data": "<base64-string>",
                "contentType": "application/pdf",
                "id": 1,
                "name": "example.pdf"
            },
            {
                "data": "<base64-string>",
                "contentType": "image/png",
                "id": 2,
                "name": "image.png"
            }
        ]
    }
}
```

When Openk9 get message, extracts binaries from message and stored them to S3 Minio storage. Then binaries are available inside Openk9 System to perform any sort of processing.

Validating this parameter is not mandatory. If you don't set it, Openk9 interprets message haven't any binaris associated.

#### Acl

Using *acl* parameter is possible to index, together with document, the related acl (Access Control List).
To pass acl you need first to be able to get them from original data. Then you need to construct an object like following:

```
{
    {
      "rolesName": ["site_admin", "site_editor", "site_viewer"],
    }
}
```

Validating this parameter is not mandatory. If you don't set it, Openk9 interprets the data like a data without any acl and consequentially it results visible to all.

#### Type

Using *type* parameter is possibile to label message for its type.
Three different types of types are supported:

- **DOCUMENT**: is default type. Identify a message with document to be indexed
- **HALT**: HALT type identify a message that report to Openk9 to stop running schedulation. This can be used when an error occurs during data extraction and the expected behaviour is to stop extraction and stop also the correspondign schedulation inside Openk9
- **LAST**: to report that data extraction is finished


#### Last

Using *type* parameter is possibile to report that data extraction is finished.
Is behaviour is the same as type parameter with value LAST.


### Asynchronous implementation of data extraction

When Openk9 calls the connector's trigger endpoint, the connector must start the extraction asynchronously on a separate thread and possibly respond to Openk9 if this started correctly or if instead there was some error (it sends an exception with a code error and related message). 
If Openk9 starts correctly at that point it disconnects, starts a schedule and waits for messages from the connector.

The connector begins data extraction and sends this data one by one to the above ingestion endpoint.

At this point two cases can occur:

1. The connector correctly extracts all documents from the source and sends them all to Openk9 wconsequentiallyithout errors.
2. The connector encounters an error during data extraction (for example, it does not correctly extract a document from the source or breaks when it sends it to ingestion)

In case number 1, the connector, after sending all the documents, sends a message with last a true.

In case number 2 the connector can:

- ignore the errors that occur and get to the end by sending the last
- in case of error, stop data extraction and signal Openk9 to stop scheduling

To signal Openk9 to interrupt a schedule, a message with type HALT must be sent to the ingestion.

### Management endpoints

Connector can expose some management endpoints needed to configure and connect in proper way these to Openk9 instance.

Every following endpoint is not mandatory to connect and use connector. But if implemented, configuration through admin ui is simpler.

#### Openk9 Health Check Endpoint

When you connect and configure your own connector in Openk9 admin Ui, you can perform health check directly from
admin interface. In this way, you can check if your connector is healthy.
To enable this feature your own connector needs to implement health check endpoint respecting specification as follows:

```
curl -X 'GET' \
'http://localhost:32805/health' \
-H 'accept: application/json'
```

The endpoint return JSON response like this:

```
{
  "status": "UP/DOWN",
  "checks": [
      {
          "name": "OpenSearch cluster health check",
          "status": "UP",
          "data": {
              "status": "Green"
          }
      },
      {
          "name": "SmallRye Reactive Messaging - startup check",
          "status": "UP"
      }
  ]
}
```

*Status* parameter is mandatory in the response. You can then add other parameters, like for example details, but they are not analyzed by Openk9 to perform health check. Openk9 uses only *status* parameter for this scope.

#### Openk9 Form Endpoint

When you connect and configure your own connector in Openk9 admin Ui, Openk9 can create a configuration form with specific
data for your own connector in automatic way.
To enable this feature your own connector needs to implement form endpoint respecting specification as follows:

```
curl -X 'GET' \
  'http://localhost:32805/form' \
  -H 'accept: application/json'
```

The endpoint return JSON response like this:

```
{
  "fields": [
    {
      "label": "Sitemap Urls",
      "name": "sitemapUrls",
      "field": "sitemapUrls",
      "type": "list",
      "size": 4,
      "required": true,
      "values": [
        {
          "value": "sitemapUrl1",
          "label": "sitemapUrl1",
          "isDefault": false
        },
        {
          "value": "sitemapUrl2",
          "label": "sitemapUrl2",
          "isDefault": false
        }
      ],
      "info": "",
      "placeholder": "string",
      "validator": {
        "min": 0,
        "max": 100,
        "regex": "/[[:alnum:]]+/"
      }
    },
    {
      "label": "Allowed Domains",
      "name": "allowedDomains",
      "type": "list",
      "size": 4,
      "required": false,
      "values": [],
      "info": "",
      "validator": {
        "min": 0,
        "max": 100,
        "regex": "/[[:alnum:]]+/"
      }
    },
    {
      "label": "Body Tag",
      "name": "bodyTag",
      "type": "text",
      "size": 4,
      "required": true,
      "values": [
        {
          "value": "body",
          "isDefault": true
        }
      ],
      "info": "",
      "validator": {
        "min": 0,
        "max": 100,
        "regex": "/[[:alnum:]]+/"
      }
    },
    {
      "label": "Title tag",
      "name": "titleTag",
      "type": "text",
      "size": 4,
      "required": true,
      "info": "",
      "values": [
        {
          "value": "title::text",
          "isDefault": true
        }
      ],
      "validator": {
        "min": 0,
        "max": 100,
        "regex": "/[[:alnum:]]+/"
      }
    },
    {
      "label": "Page Count",
      "name": "pageCount",
      "type": "number",
      "size": 4,
      "required": true,
      "info": "",
      "placeholder: "",
      "values": [
        {
          "value": "0",
          "isDefault": true
        }
      ],
      "validator": {
        "min": 0,
        "max": 100,
        "regex": "/[[:alnum:]]+/"
      }
    }
  ]
}
```

The response must return an object within an array *fields*.

Objects inside array fields must have following parameters:

- **label**: label associated with input in form
- **name**: name associated to field inside form
- **type**: type used to render correctly field in form. Is possible to choose between: text, number, list, select, boolean
- **size**: size of input in form
- **required**: if is mandatory or not
- **info**: info to rendere in information tip in form
- **values** default values to rendere in input
- **placeholder**: placeholder to render in input in form
- **validator**: type of validation to apply in input in form

Based on response Openk9 admin Ui can build a correct form to configure your own connector when is connected.


#### Openk9 Sample Endpoint

When you connect and configure your own connector in Openk9 admin Ui, openk9 enable feature to generate index mapping
from a document sample taken from this endpoint's response.
To enable this feature your own connector needs to implement sample endpoint respecting specification as follows:

```
curl -X 'GET' \
  'http://localhost:32805/sample' \
  -H 'accept: application/json'
```

This endpoint must return a JSON response respecting Ingestion Openk9 Request. In particular mapping is generated analyzing
informations inside *datasource payload*. 

In case, for example, of a web crawler a response could be:

```
{
  "parsingDate": 1705326198961,
  "datasourceId": "1",
  "contentId": 3062252989527342,
  "rawContent": "Lorem ipsum è un testo segnaposto utilizzato da grafici, progettisti, programmatori e tipografi a modo riempitivo per bozzetti e prove grafiche.[1] È un testo privo di senso, composto da parole (o parti di parole) in lingua latina, riprese pseudocasualmente dal De finibus bonorum et malorum scritto da Cicerone del 45 a.C, a volte alterate con l'inserzione di passaggi ironici. La caratteristica principale è data dal fatto che offre una distribuzione delle lettere uniforme, apparendo come un normale blocco di testo leggibile.",
  "datasourcePayload": {
    "web": {
      "url": "https://it.wikipedia.org/wiki/Lorem_ipsum",
      "content": "Lorem ipsum è un testo segnaposto utilizzato da grafici, progettisti, programmatori e tipografi a modo riempitivo per bozzetti e prove grafiche.[1] È un testo privo di senso, composto da parole (o parti di parole) in lingua latina, riprese pseudocasualmente dal De finibus bonorum et malorum scritto da Cicerone del 45 a.C, a volte alterate con l'inserzione di passaggi ironici. La caratteristica principale è data dal fatto che offre una distribuzione delle lettere uniforme, apparendo come un normale blocco di testo leggibile.",
      "title": "Lorem ipsum - Wikipedia",
      "favicon": "https://it.wikipedia.org/favicon.ico"
    }Navigation Skip to Content Navigation Menu Esigenze Close menu Esigenze Risolviamo problemi complessi scegliendo le soluzioni piÃ¹ adatte a ciascun cliente Gestione processi e dati Migliorare i processi produttivi Gestiamo la produzione dal collegamento
  },
  "resources": {
    "binaries": []
  },
  "scheduleId": "1"
}
```

This is exactly an example of message that connector construct and send to Openk9 during data extraction. Openk9 can use it to inherits structure of data
and create correct mapping for indexes.