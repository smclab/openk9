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
  titleTag: string;
  titleCount: string;
  maxLenght: string;
  pageCount: string;
  documentTypeExtension: string;
  enrichPipeline?: { id?: string | null; name?: string | null };
  jsonConfig?: string | null;
  lastIngestionDate?: any;
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
