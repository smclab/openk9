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
import { ChunkType, PluginDriverType, Provisioning } from "../../graphql-generated";

export interface FormValues {
  connectorName: string;
  connectorDescription: string;
  host: string;
  port: string;
  secure: boolean;
  method: string;
  allowedDomains: [];
  bodytag: string;
  titleTag: string;
  maxLenght: string;
  pageCount: string;
  documentFileExtension: string;
  scheduling: string;
}

export type KeyValue = {
  [key: string]: any;
};

export interface ConnectionData {
  datasourceId: string;
  name?: string | null;
  description?: string | null;
  dataIndices?: Array<{ id?: string | null; name?: string | null }>;
  dataIndex?: {
    id?: string | null;
    name?: string | null;
    description?: string | null;
  };
  bodyTag: string;
  optionDataindex: Array<{ id: string; name: string }>;
  titleTag: string;
  titleCount: string;
  maxLenght: string;
  pageCount: string;
  documentTypeExtension: string;
  enrichPipeline?: { id?: string | null; name?: string | null };
  jsonConfig?: string | null;
  lastIngestionDate?: any;
  enrichPipelineCustom:
    | { id: string | null | undefined; name: string | null | undefined; linkedEnrichItems: any[] | null }
    | undefined
    | null;
  scheduling?: string | null;
  purging?: string | null;
  isCronSectionscheduling?: boolean | null;
  isCronSectionreindex?: boolean | null;
  isCronSectionpurge?: boolean | null;
  reindex?: string | null;
  schedulingPreset?: { label?: string | null; value?: string | null };
  schedulingMinute?: string | null;
  schedulingHour?: string | null;
  schedulingDayOfMonth?: string | null;
  schedulingDayOfWeek?: string | null;
  schedulingMonth?: string | null;
  purgePreset?: { label?: string | null; value?: string | null };
  purgeMinute?: string | null;
  purgeHour?: string | null;
  purgeDayOfMonth?: string | null;
  purgeDayOfWeek?: string | null;
  purgeMonth?: string | null;
  reindexMinute?: string | null;
  reindexHour?: string | null;
  reindexMonth?: string | null;
  reindexDayOfMonth?: string | null;
  reindexDayOfWeek?: string | null;
  startAtCreation?: boolean;
  cronExpression?: string | null;
  linkedEnrichItems?: any[] | null;
  pipeline?: {
    id?: string | null;
    name?: string | null;
  };
  pluginDriverSelect?: {
    id?: string | null;
    nameConnectors?: string | null;
    description?: string | null;
    host?: string | null;
    path?: string | null;
    port?: string | null;
    secure?: boolean | null;
    method?: string | null;
    provisioning?: Provisioning;
    pluginDriverType?: PluginDriverType;
    json?: string | null;
  };
  vectorIndex?: {
    urlField?: string;
    titleField?: string;
    textEmbeddingField?: string;
    chunkWindowSize?: number;
    embeddingDocTypeFieldId: { id: string; name: string } | null;
    embeddingJsonConfig: string | null;
    chunkType?: ChunkType | null;
    knnIndex?: boolean | null | undefined;
    docTypeIds?: number[] | null;
  } | null;
  [key: string]: any;
}

export interface CustomForm {
  name: string;
  values: string | string[];
}

export interface FormValues {
  connectorName: string;
  connectorDescription: string;
  host: string;
  port: string;
  secure: boolean;
  method: string;
  allowedDomains: [];
  bodytag: string;
  titleTag: string;
  maxLenght: string;
  pageCount: string;
  documentFileExtension: string;
  scheduling: string;
}

