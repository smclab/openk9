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
/**
 * The configuration types offered by the export tab, bundled into the groups an
 * administrator recognises.
 *
 * Grouping is not cosmetic. The exporter emits only the *owning* side of each
 * relationship, so the dependency closure of a deep export follows outgoing
 * references only: seeding it with `DOC_TYPE` alone would leave every
 * `DOC_TYPE_FIELD` out, because a field points at its doc type and not the other
 * way round. Each group therefore carries the dependent types along with the one
 * it is named after.
 */
import AccountTreeOutlinedIcon from "@mui/icons-material/AccountTreeOutlined";
import ArticleOutlinedIcon from "@mui/icons-material/ArticleOutlined";
import CloudSyncOutlinedIcon from "@mui/icons-material/CloudSyncOutlined";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import ExtensionOutlinedIcon from "@mui/icons-material/ExtensionOutlined";
import ManageSearchOutlinedIcon from "@mui/icons-material/ManageSearchOutlined";
import ScatterPlotOutlinedIcon from "@mui/icons-material/ScatterPlotOutlined";
import SmartToyOutlinedIcon from "@mui/icons-material/SmartToyOutlined";
import StorageOutlinedIcon from "@mui/icons-material/StorageOutlined";
import TextSnippetOutlinedIcon from "@mui/icons-material/TextSnippetOutlined";
import TuneOutlinedIcon from "@mui/icons-material/TuneOutlined";
import type { SvgIconProps } from "@mui/material";
import type { ConfigEntityType } from "openapi-generated";
import React from "react";

export type ExportGroupId =
  | "buckets"
  | "datasources"
  | "docTypes"
  | "templates"
  | "enrichPipelines"
  | "enrichItems"
  | "embeddingModels"
  | "llmModels"
  | "prompts"
  | "searchParameters"
  | "otherConfigurations";

/**
 * The group every exportable type belongs to. This is the single source of
 * truth, keyed the way it is on purpose: `Record<ConfigEntityType, …>` makes the
 * compiler reject a regenerated client that adds a type until it is filed under
 * a group, so no type can silently drop out of the export selector.
 */
const GROUP_OF_TYPE: Record<ConfigEntityType, ExportGroupId> = {
  BUCKET: "buckets",
  TAB: "buckets",
  TOKEN_TAB: "buckets",
  SORTING: "buckets",
  SUGGESTION_CATEGORY: "buckets",
  HIGHLIGHT: "buckets",
  AUTOCOMPLETE: "buckets",
  AUTOCORRECTION: "buckets",
  DATASOURCE: "datasources",
  PLUGIN_DRIVER: "datasources",
  ACL_MAPPING: "datasources",
  DOC_TYPE: "docTypes",
  DOC_TYPE_FIELD: "docTypes",
  DOC_TYPE_TEMPLATE: "templates",
  ENRICH_PIPELINE: "enrichPipelines",
  ENRICH_PIPELINE_ITEM: "enrichPipelines",
  ENRICH_ITEM: "enrichItems",
  EMBEDDING_MODEL: "embeddingModels",
  LARGE_LANGUAGE_MODEL: "llmModels",
  RAG_CONFIGURATION: "prompts",
  SEARCH_CONFIG: "searchParameters",
  QUERY_PARSER_CONFIG: "searchParameters",
  QUERY_ANALYSIS: "searchParameters",
  RULE: "searchParameters",
  ANNOTATOR: "searchParameters",
  ANALYZER: "otherConfigurations",
  CHAR_FILTER: "otherConfigurations",
  TOKEN_FILTER: "otherConfigurations",
  TOKENIZER: "otherConfigurations",
  LANGUAGE: "otherConfigurations",
};

/** The label and icon of each group, in the order the grid lays them out. */
const GROUP_PRESENTATION: { id: ExportGroupId; label: string; Icon: React.ComponentType<SvgIconProps> }[] = [
  { id: "buckets", label: "Buckets", Icon: StorageOutlinedIcon },
  { id: "datasources", label: "Datasources", Icon: CloudSyncOutlinedIcon },
  { id: "docTypes", label: "Doc Types", Icon: DescriptionOutlinedIcon },
  { id: "templates", label: "Templates", Icon: ArticleOutlinedIcon },
  { id: "enrichPipelines", label: "Enrich Pipelines", Icon: AccountTreeOutlinedIcon },
  { id: "enrichItems", label: "Enrich Items", Icon: ExtensionOutlinedIcon },
  { id: "embeddingModels", label: "Embedding Models", Icon: ScatterPlotOutlinedIcon },
  { id: "llmModels", label: "LLM Models", Icon: SmartToyOutlinedIcon },
  { id: "prompts", label: "Prompts", Icon: TextSnippetOutlinedIcon },
  { id: "searchParameters", label: "Search Parameters", Icon: ManageSearchOutlinedIcon },
  { id: "otherConfigurations", label: "Other Configurations", Icon: TuneOutlinedIcon },
];

export type ExportGroup = {
  id: ExportGroupId;
  label: string;
  Icon: React.ComponentType<SvgIconProps>;
  types: ConfigEntityType[];
};

// `Object.entries` widens the key back to `string`; the keys of the record above
// are exactly the members of `ConfigEntityType`, so reading them as such is sound.
const TYPE_ENTRIES = Object.entries(GROUP_OF_TYPE) as [ConfigEntityType, ExportGroupId][];

export const exportGroups: ExportGroup[] = GROUP_PRESENTATION.map((group) => ({
  ...group,
  types: TYPE_ENTRIES.filter(([, id]) => id === group.id).map(([type]) => type),
}));

/**
 * The `types` query parameter for a selection of groups: the union of their
 * types. An empty selection returns an empty list, which the backend reads as
 * "no filter" and exports the whole tenant.
 */
export function typesOfGroups(selected: ReadonlySet<ExportGroupId>): ConfigEntityType[] {
  return TYPE_ENTRIES.filter(([, id]) => selected.has(id)).map(([type]) => type);
}
