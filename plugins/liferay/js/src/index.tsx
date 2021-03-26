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

import Editor from "react-simple-code-editor";
import { highlight, languages } from "prismjs/components/prism-core";
import "prismjs/components/prism-clike";
import "prismjs/components/prism-javascript";

export const plugin: Plugin<LiferayResultType> = {
  pluginId: "liferay-datasource",
  displayName: "Liferay DataSource",
  pluginType: ["DATASOURCE", "ENRICH"],
  dataSourceAdminInterfacePath: {
    iconRenderer,
    settingsRenderer,
  },
  dataSourceRenderingInterface: {
    resultRenderers: {
      file: DocumentResultCard as any,
      user: ContactResultCard as any,
      calendar: CalendarResultCard as any,
    },
    sidebarRenderers: {
      file: DocumentSidebar as any,
      user: ContactSidebar as any,
      calendar: CalendarSidebar as any,
    },
  },
};

function iconRenderer(props: any) {
  return <DXPLogo {...props} />;
}

function settingsRenderer(props: any) {
  console.log("settingsRenderer", props);

  const json = props.setter;
  const setJson = props.setSetter;

  return (
    <>
      <h5>Settings Panel</h5>
      <Editor
        value={JSON.stringify(JSON.parse(json), null, 4)}
        onValueChange={(json) => setJson(json)}
        highlight={(json) => highlight(json, languages.js)}
        padding={10}
        style={{
          border: "1px solid rgb(206, 212, 218)",
          borderRadius: "4px",
          fontFamily:
            'SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
        }}
      />
    </>
  );
}
