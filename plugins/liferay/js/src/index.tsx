import React from "react";
import ClayIcon from "@clayui/icon";
import { Plugin } from "@openk9/http-api";

import { WebResultItem } from "./types";
import { WebResultCard } from "./WebResultCard";
import { WebSidebar } from "./WebSidebar";

export const plugin: Plugin<WebResultItem> = {
  pluginId: "web-datasource",
  displayName: "Web DataSource",
  pluginType: ["DATASOURCE", "ENRICH"],
  dataSourceAdminInterfacePath: {
    iconRenderer,
    settingsRenderer,
  },
  dataSourceRenderingInterface: {
    resultRenderers: {
      web: WebResultCard as any,
    },
    sidebarRenderers: {
      web: WebSidebar as any,
    },
  },
};

function iconRenderer(props: any) {
  console.log("iconRenderer", props);
  return <ClayIcon symbol="document" />;
}

function settingsRenderer(props: any) {
  console.log("settingsRenderer", props);
  return (
    <>
      <h1>Settings Panel</h1>
    </>
  );
}
