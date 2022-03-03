import React from "react";
import { Plugin } from "@openk9/rest-api";
import { WebResultItem } from "@openk9/search-frontend";

export const plugin: Plugin<WebResultItem> = {
  pluginId: "web-sitemap-datasource",
  displayName: "Web Sitemap DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Web Sitemap DataSource",
      driverServiceName: "io.openk9.plugins.web.sitemap.driver.SitemapWebPluginDriver",
      iconRenderer,
      initialSettings: `
        {
            "datasourceId": 1,
            "timestamp": 0,
            "sitemapUrls": ["https://www.smc.it/sitemap.xml"],
            "bodyTag": "body",
            "allowedDomains": ["smc.it"],
        	"maxLength": 10000
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Web Sitemap Async NER",
      serviceName:
        "io.openk9.plugins.web.sitemap.enrichprocessor.AsyncWebNerEnrichProcessor",
      iconRenderer,
      initialSettings: `
      {
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
  ],
};
