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
import { useTranslation } from "react-i18next";
import { MenuItem } from "./types";

export const menuItems: MenuItem[] = [
  { labelKey: "nav.dashboard", path: "/", IsChildren: false, value: "dashboard" },
  { labelKey: "nav.buckets", path: "/buckets", IsChildren: false, value: "buckets" },
  {
    labelKey: "nav.datasource-group",
    isGroup: true,
    IsChildren: false,
    value: "datasource-group",
    children: [
      { labelKey: "nav.data-sources", path: "/data-sources", IsChildren: true, value: "data-sources" },
      { labelKey: "nav.connectors", path: "/plugin-drivers", IsChildren: true, value: "plugin-drivers" },
      { labelKey: "nav.pipelines", path: "/pipelines", IsChildren: true, value: "pipelines" },
      { labelKey: "nav.enrich-items", path: "/enrich-items", IsChildren: true, value: "enrich-items" },
    ],
  },
  {
    labelKey: "nav.mappings-group",
    isGroup: true,
    IsChildren: false,
    value: "mappings-group",
    children: [
      { labelKey: "nav.data-indices", path: "/dataindices", IsChildren: true, value: "dataindices" },
      { labelKey: "nav.document-types", path: "/document-types", IsChildren: true, value: "document-types" },
      {
        labelKey: "nav.document-type-templates",
        path: "/document-type-templates",
        IsChildren: true,
        value: "document-type-templates",
      },
      {
        labelKey: "nav.analysis-group",
        isGroup: true,
        IsChildren: true,
        value: "analysis-group",
        children: [
          { labelKey: "nav.analyzers", path: "/analyzers", IsChildren: true, value: "analyzers" },
          { labelKey: "nav.tokenizers", path: "/tokenizers", IsChildren: true, value: "tokenizers" },
          { labelKey: "nav.token-filters", path: "/token-filters", IsChildren: true, value: "token-filters" },
          { labelKey: "nav.char-filters", path: "/char-filters", IsChildren: true, value: "char-filters" },
        ],
      },
    ],
  },
  {
    labelKey: "nav.search-config-group",
    isGroup: true,
    IsChildren: false,
    value: "search-config-group",
    children: [
      { labelKey: "nav.search-config", path: "/search-configs", IsChildren: true, value: "search-configs" },
      { labelKey: "nav.filters", path: "/suggestion-categories", IsChildren: true, value: "suggestion-categories" },
      { labelKey: "nav.highlights", path: "/highlights", IsChildren: true, value: "highlights" },
      { labelKey: "nav.tabs", path: "/tabs", IsChildren: true, value: "tabs" },
      { labelKey: "nav.token-tabs", path: "/token-tabs", IsChildren: true, value: "token-tabs" },
      { labelKey: "nav.sortings", path: "/sortings", IsChildren: true, value: "sortings" },
      { labelKey: "nav.autocorrections", path: "/autocorrections", IsChildren: true, value: "autocorrections" },
      { labelKey: "nav.autocompletes", path: "/autocompletes", IsChildren: true, value: "autocompletes" },
    ],
  },
  {
    labelKey: "nav.ai-tools-group",
    isGroup: true,
    IsChildren: false,
    value: "ai-tools-group",
    children: [
      {
        labelKey: "nav.generative-ai-group",
        isGroup: true,
        IsChildren: true,
        value: "generative-ai-group",
        children: [
          {
            labelKey: "nav.large-language-models",
            path: "/large-languages-model",
            IsChildren: true,
            value: "large-languages-model",
          },
          { labelKey: "nav.embedding-models", path: "/embedding-models", IsChildren: true, value: "embedding-models" },
          {
            labelKey: "nav.rag-configuration",
            path: "/rag-configurations",
            IsChildren: true,
            value: "rag-configurations",
          },
        ],
      },
      {
        labelKey: "nav.query-analysis-group",
        isGroup: true,
        IsChildren: true,
        value: "query-analysis-group",
        children: [
          { labelKey: "nav.query-analysis", path: "/query-analyses", IsChildren: true, value: "query-analyses" },
          { labelKey: "nav.rules", path: "/rules", IsChildren: true, value: "rules" },
          { labelKey: "nav.annotators", path: "/annotators", IsChildren: true, value: "annotators" },
        ],
      },
    ],
  },
  {
    labelKey: "nav.admin-settings-group",
    isGroup: true,
    IsChildren: false,
    value: "admin-settings-group",
    children: [
      { labelKey: "nav.admin-settings-general", path: "/admin-settings", IsChildren: true, value: "admin-settings" },
      // Deliberately not nested under /admin-settings: the active item is resolved
      // by `startsWith` over `namePath`, so a child path would also match its parent.
      { labelKey: "nav.import-export", path: "/import-export", IsChildren: true, value: "import-export" },
    ],
  },
];

export const useFilteredMenuItems = (searchTerm: string) => {
  const { t } = useTranslation();

  const flattenItems = (items: MenuItem[]): MenuItem[] => {
    return items.reduce((acc: MenuItem[], item) => {
      if (item.children) {
        return [...acc, ...flattenItems(item.children)];
      }
      return [...acc, item];
    }, []);
  };

  const filterItems = (items: MenuItem[]): MenuItem[] => {
    if (!searchTerm) return items;

    const flattenedItems = flattenItems(items);
    return flattenedItems.filter((item) => t(item.labelKey).toLowerCase().includes(searchTerm.toLowerCase()));
  };

  return filterItems(menuItems);
};

