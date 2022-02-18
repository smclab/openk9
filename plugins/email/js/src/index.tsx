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
      displayName: "Email NER",
      serviceName:
        "io.openk9.plugins.email.enrichprocessor.EmailNerEnrichProcessor",
      iconRenderer,
      initialSettings: `{"entities": ["person", "email","organization"], "confidence": 0.90}`,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "email",
      resultRenderer: EmailResult,
      sidebarRenderer: EmailDetail,
    },
  ],
};

function iconRenderer() {
  return <span>📧</span>; // TODO
}
