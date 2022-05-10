import React from "react";
import { Plugin } from "@openk9/rest-api";
import { GaraResultItem } from "./garaItem";
import { GaraDetail } from "./GaraDetail";
import { NotizieResultItem } from "./NotizieItem";
import { NotizieResult } from "./NotizieResult";
import { NotizieDetail } from "./NotizieDetail";

export const plugin: Plugin<GaraResultItem> = {
  pluginId: "demanio-datasource",
  displayName: "Demanio DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Demanio DataSource",
      driverServiceName: "io.openk9.plugins.demanio.driver.DemanioPluginDriver",
      initialSettings: `
        {
            "datasourceId": 1,
            "timestamp": 0,
            "startUrls": ["https://www.agenziademanio.it/opencms/it/"],
            "allowedDomains": ["agenziademanio.it"],
            "allowedPaths": ["/agenzia/", "/salastampa/", "/progetti/",
            "/patrimoniodigitale/", "/Serviziestrumenti/", "/gare-aste/",
            "/contatti/", "/notizia/", "/amministrazionetrasparente/",
            "/strutturaorganizzativa/"],
        "excludedPaths": [],
        "bodyTag": "div#main",
        "maxLength": 10000
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Demanio NER",
      serviceName:
        "io.openk9.plugins.demanio.enrichprocessor.AsyncDemanioNerEnrichProcessor",
      initialSettings: `{
            "entityConfiguration": {
                "person": 0.80,
                "organization": 0.70,
                "loc": 0.80,
                "email": 0.90
            },
            "defaultConfidence": 0.80,
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
      displayName: "Demanio Async Tika",
      serviceName:
        "io.openk9.plugins.demanio.enrichprocessor.AsyncTikaEnrichProcessor",
      initialSettings: `
      {
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
    {
      type: "ENRICH",
      displayName: "Demanio Resources Validator",
      serviceName:
        "io.openk9.plugins.demanio.enrichprocessor.AsyncResourcesValidatorEnrichProcessor",
      initialSettings: `{}`,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "gare",
      sidebarRenderer: GaraDetail,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "notizie",
      resultRenderer: NotizieResult,
      sidebarRenderer: NotizieDetail,
    },
  ],
};
