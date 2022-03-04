import React from "react";
import { Plugin } from "@openk9/rest-api";
import { WebResultItem } from "@openk9/search-frontend";

export const plugin: Plugin<WebResultItem> = {
  pluginId: "web-generic-datasource",
  displayName: "Web Generic DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Web Generic DataSource",
      driverServiceName: "io.openk9.plugins.web.generic.driver.GenericWebPluginDriver",
      initialSettings: `
        {
            "datasourceId": 1,
            "timestamp": 0,
            "startUrls": ["https://www.smc.it/"],
            "allowedDomains": ["smc.it"],
            "allowedPaths": [],
            "excludedPaths": ["/en"],
            "bodyTag": "body",
        	"maxLength": 50000
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Web Generic Async NER",
      serviceName:
        "io.openk9.plugins.web.generic.enrichprocessor.AsyncWebNerEnrichProcessor",
      initialSettings: `{
      "entities": ["person", "organization", "loc", "email"],
      "confidence": 0.80,
      "relations": [
            {
                "startType": "person",
                "endType": "organization",
                "name": "interacts_with"
            },
            {
                "startType": "person",
                "endType": "email",
                "name": "has_email"
            }
            ]
        }`,
    },
    {
      type: "ENRICH",
      displayName: "Web Generic Async Tika",
      serviceName:
        "io.openk9.plugins.web.generic.enrichprocessor.AsyncTikaEnrichProcessor",
      initialSettings:
      `{
            "type_mapping": {
                "application/pdf": "pdf",
                "application/msword": "word",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "word",
                "application/vnd.ms-excel": "excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": "excel",
                "application/vnd.ms-powerpoint": "powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation": "powerpoint",
                "text/plain": "plaintext",
                "application/vnd.oasis.opendocument.spreadsheet": "excel",
                "application/vnd.oasis.opendocument.text": "word",
            "application/vnd.oasis.opendocument.presentation": "powerpoint",
                "application/epub+zip": "epub"
            },
        "max_length": 10000
        }`,
    },
  ],
};