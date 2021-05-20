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
import { Plugin } from "@openk9/http-api";

import { LiferayResultType } from "./types";
import { CalendarResultCard } from "./CalendarResultCard";
import { CalendarSidebar } from "./CalendarSidebar";
import { DocumentResultCard } from "./DocumentResultCard";
import { DocumentSidebar } from "./DocumentSidebar";
import { ContactResultCard } from "./ContactResultCard";
import { ContactSidebar } from "./ContactSidebar";
import { DXPLogo } from "@openk9/search-ui-components";

export const plugin: Plugin<LiferayResultType> = {
  pluginId: "liferay-datasource",
  displayName: "Liferay DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Liferay DataSource",
      driverServiceName: "io.openk9.plugins.liferay.driver.LiferayPluginDriver",
      iconRenderer,
      initialSettings: `
      {
        "domain": "http://liferay-portal:8080",
        "username": "test@liferay.com",
        "password": "test",
        "timestamp": ,
        "companyId": 20097,
        "datasourceId": 99
      }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Liferay NER",
      serviceName:
        "io.openk9.plugins.liferay.enrichprocessor.LiferayNerEnrichProcessor",
      iconRenderer,
      initialSettings: `{"entities": ["person", "organization", "loc"], "confidence": 0.50}`,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "file",
      resultRenderer: DocumentResultCard as any,
      sidebarRenderer: DocumentSidebar as any,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "user",
      resultRenderer: ContactResultCard as any,
      sidebarRenderer: ContactSidebar as any,
    },
    {
      type: "RESULT_RENDERER",
      resultType: "calendar",
      resultRenderer: CalendarResultCard as any,
      sidebarRenderer: CalendarSidebar as any,
    },
  ],
};

function iconRenderer(props: any) {
  return <DXPLogo {...props} />;
}
