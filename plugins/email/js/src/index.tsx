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

import { EmailResultItem } from "./types";
import { EmailResultCard } from "./EmailResultCard";
import { EmailSidebar } from "./EmailSidebar";
import { EmailIcon } from "@openk9/search-ui-components";

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
      type: "RESULT_RENDERER",
      resultType: "application",
      resultRenderer: EmailResultCard as any,
      sidebarRenderer: EmailSidebar as any,
    },
  ],
};

function iconRenderer(props: any) {
  return <EmailIcon {...props} />;
}
