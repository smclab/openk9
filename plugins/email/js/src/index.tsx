import React from "react";
import { Plugin } from "@openk9/rest-api";
import { EmailResultItem } from "./EmailItem";
import { EmailResult } from "./EmailResult";
import { EmailDetail } from "./EmailDetail";

export const plugin: Plugin<EmailResultItem> = {
  pluginId: "email-datasource",
  displayName: "Email DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Email DataSource",
      driverServiceName: "io.openk9.plugins.email.driver.EmailPluginDriver",
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
          }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Async Email NER",
      serviceName:
        "io.openk9.plugins.email.enrichprocessor.AsyncEmailNerEnrichProcessor",
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
    {
      type: "RESULT_RENDERER",
      resultType: "email",
      resultRenderer: EmailResult,
      sidebarRenderer: EmailDetail,
    },
  ],
};
