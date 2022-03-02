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
      iconRenderer,
      initialSettings: `
        {
          "entities": [
              "person",
              "email",
              "organization"
          ],
          "confidence": 0.9,
          "mailServer": "172.20.20.1",
          "port": "993",
          "username": "test",
          "password": "test",
          "datasourceId": 99,
          "folder": "INBOX"
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Async Email NER",
      serviceName:
        "io.openk9.plugins.email.enrichprocessor.AsyncEmailNerEnrichProcessor",
      iconRenderer,
      initialSettings: `{
                            "entityConfiguration": {
                                "person": 0.7,
                                "organization": 0.7,
                                "loc": 0.7,
                                "email": 0.9
                            },
                            "defaultConfidence": 0.8,
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
