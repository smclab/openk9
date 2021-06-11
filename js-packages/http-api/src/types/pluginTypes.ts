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

import { GenericResultItem } from "./searchResultsType";
import { InputSuggestionToken } from "./suggestionTypes";

export type PluginInfo = {
  pluginId: string;
  bundleInfo: {
    id: number;
    lastModified: number;
    state: string;
    symbolicName: string;
    version: string;
  };
};

export type PluginService<E> =
  | DataSourcePlugin
  | EnrichPlugin
  | SuggestionsPlugin
  | ResultRendererPlugin<E>;

export type Plugin<E> = {
  pluginId: string;
  displayName: string;
  pluginServices: PluginService<E>[];
};

export type DataSourcePlugin = {
  type: "DATASOURCE";
  displayName: string;
  driverServiceName: string;
  iconRenderer?: React.FC<{ size?: number } & any>;
  initialSettings: string;
  settingsRenderer?: React.FC<{
    currentSettings: string;
    setCurrentSettings(a: string): void;
  }>;
};

export type EnrichPlugin = {
  type: "ENRICH";
  displayName: string;
  serviceName: string;
  iconRenderer?: React.FC<{ size?: number } & any>;
  initialSettings: string;
  settingsRenderer?: React.FC<{
    currentSettings: string;
    setCurrentSettings(a: string): void;
  }>;
};

export type ResultRendererProps<E> = {
  data: GenericResultItem<E>;
  onSelect(): void;
};

export type SidebarRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type ResultRendererPlugin<E> = {
  type: "RESULT_RENDERER";
  priority?: number;
  resultType: string;
  resultRenderer: React.FC<ResultRendererProps<E>>;
  sidebarRenderer: React.FC<SidebarRendererProps<E>>;
};

export type SuggestionsPlugin = {
  type: "SUGGESTIONS";
  renderSuggestionIcons?: React.FC<{ suggestion: InputSuggestionToken }>;
};
