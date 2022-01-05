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
import { Plugin } from "@openk9/rest-api";
import { SettingsIcon } from "@openk9/search-ui-components";

import AceEditor from "react-ace";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/theme-github";

export const plugin: Plugin<unknown> = {
  pluginId: "js-enrich-script",
  displayName: "JS Enrich Script",
  pluginServices: [
    {
      type: "ENRICH",
      displayName: "JS Script",
      serviceName: "io.openk9.plugins.js.enrichprocessor.JsEnrichProcessor",
      iconRenderer,
      settingsRenderer,
      initialSettings: `{"code": ""}`,
    },
  ],
};

function iconRenderer(props: any) {
  return <SettingsIcon {...props} />;
}

function settingsRenderer({
  currentSettings,
  setCurrentSettings,
}: {
  currentSettings: string;
  setCurrentSettings(a: string): void;
}) {
  return (
    <AceEditor
      mode="javascript"
      theme="github"
      width="100%"
      height="550px"
      value={JSON.parse(currentSettings).code}
      onChange={(code) => setCurrentSettings(JSON.stringify({ code }))}
      name="JS_EDITOR"
      editorProps={{ $blockScrolling: true }}
    />
  );
}
