---
id: create-plugin-using-json
title: Create a plugin using Json
---

In OpenK9 you can create custom plugin using Json.

## Prerequisites

Prerequisites are described in [`OSGi project requirements`](/docs/osgi-requirements).

### Create Plugin

A complete example is present at the link https://github.com/smclab/openk9-example-json-datasource

To create a new plugin with Java you need to have a gradle project with this structure:

```
openk9-example-json-datasource
├── bnd.bnd
├── build.gradle
├── settings.gradle
└── src
    └── main
        └── resources
            └── plugin-driver-config.json
```

Your project needs to contains following files:

- `bnd.bnd` identifies plugin as bundle
```aidl
Bundle-Name: [OpenK9 - Plugin] Example
Bundle-SymbolicName: io.openk9.plugins.example

Bundle-Version: 0.0.1

-noimportjava: true
```
- `build.gradle` defines java dependencies and adds bndtools plugin
```aidl
buildscript {
	repositories {
		maven {
			url "https://plugins.gradle.org/m2/"
		}
	}
	dependencies {
		classpath "biz.aQute.bnd:biz.aQute.bnd.gradle:5.2.0"
	}
}

apply plugin: 'java'
apply plugin: 'biz.aQute.bnd.builder'

sourceCompatibility = 11
targetCompatibility = 11

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	compile 'io.openk9:io.openk9.release.api:OPENK9_VERSION'
}
```

### Plugin Definition using Json

- `plugin-driver-config.json` contains json object where driver, mapping of different document types and enrich processor
are defined.

```json
{
  "name": "example",
  "schedulerEnabled": true,
  "type": "HTTP",
  "options": {
    "url": "http://example-parser/",
    "path": "/execute",
    "method": "POST",
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonKeys": [
      "timestamp",
      "startUrls",
      "allowedDomains",
      "allowedPaths",
      "datasourceId",
      "excludedPaths",
      "bodyTag"
    ]
  },
  "documentTypes": [
    {
      "name": "web",
      "icon": "",
      "defaultDocumentType": true,
      "searchKeywords": [
        {
          "type": "TEXT",
          "keyword": "web.title",
          "options": {
            "boost": 10.0
          }
        },
        {
          "type": "TEXT",
          "keyword": "web.content"
        }
      ],
      "mappings": {
        "properties": {
          "web": {
            "properties": {
              "title": {
                "type": "text",
                "analyzer": "standard_lowercase_italian_stop_words_filter"
              },
              "content": {
                "type": "text",
                "analyzer": "standard_lowercase_italian_stop_words_filter"
              },
              "url": {
                "type": "text"
              },
              "favicon": {
                "type": "text"
              }
            }
          }
        }
      }
    }
  ],
  "enrichProcessors": [
    {
      "name": "example-ner",
      "type": "ASYNC",
      "options": {
        "destinationName": "io.openk9.ner"
      }
    }
  ]
}

```


