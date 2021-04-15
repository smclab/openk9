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

import { PluginInfo, Plugin } from "../types";
import { LoginInfo } from "./authAPI";
import { apiBaseUrl, apiBaseUrlStatic, authFetch } from "./common";

export async function getPlugins(
  loginInfo: LoginInfo | null,
): Promise<PluginInfo[]> {
  const request = await authFetch(`${apiBaseUrl}/plugin`, loginInfo);
  const response: PluginInfo[] = await request.json();
  return response;
}

export async function loadPlugin<E>(id: string): Promise<Plugin<E>> {
  const defaultPlugin: Plugin<any> = {
    pluginId: id,
    displayName: id,
    pluginType: [],
  };

  try {
    const jsURL = `${apiBaseUrlStatic}/plugins/${id}/static/build/index.js`;
    // @ts-ignore
    const code = await import(/* webpackIgnore: true */ jsURL);
    const plugin = code.plugin as Plugin<E>;
    return plugin;
  } catch (err) {
    console.warn(err);
    return defaultPlugin;
  }
}
