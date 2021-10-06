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
import ReactDOM from "react-dom";
import * as ok9Components from "@openk9/search-ui-components";
import * as ok9API from "@openk9/http-api";
import * as reactJSS from "react-jss";
import clayIcon from "@clayui/icon";

import { createAsset } from "use-asset";
import {
  loadPlugin,
  PluginInfo,
  ResultRendererPlugin,
  ResultRendererProps,
  SidebarRendererProps,
} from "@openk9/http-api";

/**
 * This object is to be used when loading plugins in a React Suspense environment.
 * Remember to add a <Suspense /> wrapper, or it will crash.
 *
 * @example
 * const plugin = pluginLoader.read("plugin-id");
 * console.log(plugin.displayName);
 */
export const pluginLoader = createAsset(async (id) => {
  const plugin = await loadPlugin(id);
  return plugin;
});

/**
 * Since OpenK9 plugins rely on global objects, to avoid repeating dependencies, it's crucial to call this method BEFORE loading any plugin.
 * This will load shared dependencies in window.OpenK9.deps.
 *
 * @param compatMode - set to true to load dependencies also in window object directly, to allow loading older plugins.
 */
export function loadPluginDepsIntoGlobal(compatMode = false): any /* TODO fix exporting external lib typings */ {
  if (typeof window !== "undefined") {
    if (typeof (window as any).OpenK9 === "undefined")
      (window as any).OpenK9 = { deps: {} };

    (window as any).OpenK9.deps.React = React;
    (window as any).OpenK9.deps.ReactDOM = ReactDOM;
    (window as any).OpenK9.deps.ok9API = ok9API;
    (window as any).OpenK9.deps.ok9Components = ok9Components;
    (window as any).OpenK9.deps.clayIcon = clayIcon;
    (window as any).OpenK9.deps.reactJSS = reactJSS;

    if (compatMode) {
      (window as any).React = React;
      (window as any).ReactDOM = ReactDOM;
      (window as any).ok9API = ok9API;
      (window as any).ok9Components = ok9Components;
      (window as any).clayIcon = clayIcon;
      (window as any).reactJSS = reactJSS;
    }
  }

  return { React, ReactDOM, ok9API, ok9Components, clayIcon, reactJSS };
}

/**
 * A key-value map with renderer functions for each result type, for the result cards.
 */
export type ResultRenderersType<E> = {
  [key: string]: React.FC<ResultRendererProps<E>>;
};

/**
 * A key-value map with renderer functions for each result type, for the preview sidebar.
 */
export type SidebarRenderersType<E> = {
  [key: string]: React.FC<SidebarRendererProps<E>>;
};

/**
 * Given a list of available plugins, it loads them and returns a map with all the available result renderers.
 */
export function getPluginResultRenderers(pluginInfos: PluginInfo[]) {
  const plugins = pluginInfos
    .map((pI) => pluginLoader.read(pI.pluginId))
    .filter(Boolean);

  const resultRendererPluginServices = plugins
    .flatMap((p) => p.pluginServices)
    .filter(
      (ps) => ps.type === "RESULT_RENDERER",
    ) as ResultRendererPlugin<unknown>[];
  const resultTypes = [
    ...new Set(resultRendererPluginServices.map((ps) => ps.resultType)),
  ];

  const resultRenderers: ResultRenderersType<any> = {};
  const sidebarRenderers: SidebarRenderersType<any> = {};
  resultTypes.forEach((type) => {
    resultRenderers[type] = resultRendererPluginServices
      .filter((ps) => ps.resultType === type)
      .sort(
        (a, b) => (a.priority || -1) - (b.priority || -1),
      )[0].resultRenderer;
    sidebarRenderers[type] = resultRendererPluginServices
      .filter((ps) => ps.resultType === type)
      .sort(
        (a, b) => (a.priority || -1) - (b.priority || -1),
      )[0].sidebarRenderer;
  });

  return { resultRenderers, sidebarRenderers };
}
