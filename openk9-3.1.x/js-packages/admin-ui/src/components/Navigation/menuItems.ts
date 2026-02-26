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
import { MenuItem } from "./types";

export const menuItems: MenuItem[] = [
  { label: "Dashboard", path: "/", IsChildren: false, value: "dashboard" },
  { label: "Buckets", path: "/buckets", IsChildren: false, value: "buckets" },
  {
    label: "Datasource and Data Enrichments",
    isGroup: true,
    IsChildren: false,
    value: "datasource-group",
    children: [
      { label: "Data Sources", path: "/data-sources", IsChildren: true, value: "data-sources" },
      { label: "Connectors", path: "/plugin-drivers", IsChildren: true, value: "plugin-drivers" },
      { label: "Pipelines", path: "/pipelines", IsChildren: true, value: "pipelines" },
      { label: "Enrich Items", path: "/enrich-items", IsChildren: true, value: "enrich-items" },
    ],
  },
  {
    label: "Mappings",
    isGroup: true,
    IsChildren: false,
    value: "mappings-group",
    children: [
      { label: "Data Indices", path: "/dataindices", IsChildren: true, value: "dataindices" },
      { label: "Document Types", path: "/document-types", IsChildren: true, value: "document-types" },
      {
        label: "Document Type Templates",
        path: "/document-type-templates",
        IsChildren: true,
        value: "document-type-templates",
      },
      {
        label: "Analysis",
        isGroup: true,
        IsChildren: true,
        value: "analysis-group",
        children: [
          { label: "Analyzers", path: "/analyzers", IsChildren: true, value: "analyzers" },
          { label: "Tokenizers", path: "/tokenizers", IsChildren: true, value: "tokenizers" },
          { label: "Token Filters", path: "/token-filters", IsChildren: true, value: "token-filters" },
          { label: "Char Filters", path: "/char-filters", IsChildren: true, value: "char-filters" },
        ],
      },
    ],
  },
  {
    label: "Search Configuration",
    isGroup: true,
    IsChildren: false,
    value: "search-config-group",
    children: [
      { label: "Search Config", path: "/search-configs", IsChildren: true, value: "search-configs" },
      { label: "Filters", path: "/suggestion-categories", IsChildren: true, value: "suggestion-categories" },
      { label: "Tabs", path: "/tabs", IsChildren: true, value: "tabs" },
      { label: "Token Tabs", path: "/token-tabs", IsChildren: true, value: "token-tabs" },
      { label: "Autocorrections", path: "/autocorrections", IsChildren: true, value: "autocorrections" },
      { label: "Autocompletes", path: "/autocompletes", IsChildren: true, value: "autocompletes" },
    ],
  },
  {
    label: "AI Tools Configuration",
    isGroup: true,
    IsChildren: false,
    value: "ai-tools-group",
    children: [
      {
        label: "Generative AI Configuration",
        isGroup: true,
        IsChildren: true,
        value: "generative-ai-group",
        children: [
          {
            label: "Large Language Models",
            path: "/large-languages-model",
            IsChildren: true,
            value: "large-languages-model",
          },
          { label: "Embedding Models", path: "/embedding-models", IsChildren: true, value: "embedding-models" },
          { label: "RAG Configuration", path: "/rag-configurations", IsChildren: true, value: "rag-configurations" },
        ],
      },
      {
        label: "Query Analysis Configuration",
        isGroup: true,
        IsChildren: true,
        value: "query-analysis-group",
        children: [
          { label: "Query Analysis", path: "/query-analyses", IsChildren: true, value: "query-analyses" },
          { label: "Rules", path: "/rules", IsChildren: true, value: "rules" },
          { label: "Annotators", path: "/annotators", IsChildren: true, value: "annotators" },
        ],
      },
    ],
  },
];

export const useFilteredMenuItems = (searchTerm: string) => {
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
    return flattenedItems.filter((item) => item.label.toLowerCase().includes(searchTerm.toLowerCase()));
  };

  return filterItems(menuItems);
};

