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

import { GenericResultItem } from "./searchResults";

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

export type Plugin<E> = {
  pluginId: string;
  displayName: string;
  pluginType: ("DATASOURCE" | "SUGGESTION" | "ENRICH")[];
  dataSourceAdminInterfacePath?: DataSourceAdminInterface;
  dataSourceRenderingInterface?: DataSourceRenderingInterface<E>;
};

export type DataSourceAdminInterface = {
  iconRenderer: React.FC<{ size?: number } & any>;
  settingsRenderer: React.FC<{
    setter: any;
    setSetter(a: any): void;
  }>;
};

export type ResultRendererProps<E> = {
  data: GenericResultItem<E>;
  onSelect(): void;
};

export type ResultRenderersType<E> = {
  [key: string]: React.FC<ResultRendererProps<E>>;
};

export type SidebarRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type SidebarRenderersType<E> = {
  [key: string]: React.FC<SidebarRendererProps<E>>;
};

export type DataSourceRenderingInterface<E> = {
  resultRenderers: ResultRenderersType<E>;
  sidebarRenderers: SidebarRenderersType<E>;
};
