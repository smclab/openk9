/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from "react";
import ClayIcon from "@clayui/icon";
import { Plugin } from "@openk9/rest-api";

import { WebResultType } from "./types";
import { PageResultCard } from "./PageResultCard";
import { DocumentResultCard } from "./DocumentResultCard";
import { DocumentSidebar } from "./DocumentSidebar";
import { PageSidebar } from "./PageSidebar";

export const plugin: Plugin<WebResultType> = {
  pluginId: "sitemap-web-datasource",
  displayName: "Sitemap Web DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Sitemap Web DataSource",
      driverServiceName: "io.openk9.plugins.web.driver.sitemap.SitemapWebPluginDriver",
      iconRenderer,
      initialSettings: `
        {
          "startUrls": ["https://www.google.com/"],
          "allowedDomains": ["google.com"],
          "allowedPaths": ["https://www.google.com/"],
          "excludedPaths": [".pdf"],
          "datasourceId": 99,
          "timestamp": 0,
          "depth": 2,
          "max_length": 5000,
          "page_count": 3000
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Web NER",
      serviceName:
        "WebNerEnrichProcessor",
      iconRenderer,
      initialSettings: `{"entities": ["person", "organization"], "confidence": 0.90}`,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "document",
      resultRenderer: DocumentResultCard as any,
      sidebarRenderer: DocumentSidebar as any,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "web",
      resultRenderer: PageResultCard as any,
      sidebarRenderer: PageSidebar as any,
    },
  ],
};

function iconRenderer(props: any) {
  return <ClayIcon symbol="globe" {...props} />;
}
