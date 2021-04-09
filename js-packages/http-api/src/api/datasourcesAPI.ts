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

import { apiBaseUrl, apiBaseUrlV2 } from "./common";

export interface SearchKeyword {
  keyword: string;
  fieldBoost: { [key: string]: number };
}

export type DocumentType = {
  name: string;
  icon: string;
  searchKeywords: SearchKeyword[];
};

export type SupportedDataSource = {
  name: string;
  active: boolean;
  documentTypes: DocumentType[];
  defaultDocumentType: DocumentType;
};

export type DataSourceInfo = {
  datasourceId: number;
  active: boolean;
  description: string;
  jsonConfig: string;
  lastIngestionDate: number;
  name: string;
  tenantId: number;
  scheduling: string;
  driverServiceName: string;
};

export type SchedulerItem = {
  jobName: string;
  datasourceId: number;
  scheduling: string;
  datasourceName: string;
};

export type EnrichPipeline = {
  active: boolean;
  datasourceId: number;
  enrichPipelineId: number;
  name: string;
};

export type EnrichItem = {
  active: boolean;
  enrichItemId: number;
  enrichPipelineId: number;
  jsonConfig: string;
  name: string;
  serviceName: string;
  _position: number;
};

type SchedulerRequestReturn = {
  errors: string[];
} & { [key: string]: boolean };

export async function getSupportedDataSources(): Promise<
  SupportedDataSource[]
> {
  const request = await fetch(`${apiBaseUrl}/supported-datasources`);
  const response: SupportedDataSource[] = await request.json();
  return response;
}

export async function getDataSources(): Promise<DataSourceInfo[]> {
  const request = await fetch(`${apiBaseUrlV2}/datasource`);
  const response: DataSourceInfo[] = await request.json();
  return response;
}

export async function getDataSourceInfo(
  datasourceId: number,
): Promise<DataSourceInfo> {
  const request = await fetch(`${apiBaseUrlV2}/datasource/${datasourceId}`);
  const response: DataSourceInfo = await request.json();
  return response;
}

export async function changeDataSourceInfo(
  datasourceId: number,
  datasource: Partial<DataSourceInfo>,
): Promise<DataSourceInfo> {
  const request = await fetch(`${apiBaseUrlV2}/datasource/${datasourceId}`, {
    method: "PATCH",
    headers: {
      ContentType: "application/json",
    },
    body: JSON.stringify(datasource),
  });
  const response: DataSourceInfo = await request.json();
  return response;
}

export async function getSchedulerItems(): Promise<SchedulerItem[]> {
  const request = await fetch(`${apiBaseUrl}/scheduler`);
  const response: SchedulerItem[] = await request.json();
  return response;
}

export async function triggerScheduler(
  schedulerJobs: string[],
): Promise<SchedulerRequestReturn> {
  const request = await fetch(`${apiBaseUrl}/scheduler/trigger`, {
    method: "POST",
    headers: { ContentType: "application/json" },
    body: JSON.stringify(schedulerJobs),
  });
  const response: SchedulerRequestReturn = await request.json();
  return response;
}

export async function triggerReindex(ids: number[]): Promise<string> {
  const request = await fetch(`${apiBaseUrl}/index/reindex`, {
    method: "POST",
    headers: { ContentType: "application/json" },
    body: JSON.stringify({ datasourceIds: ids }),
  });
  const response: string = await request.text();
  return response;
}

export async function getDriverServiceNames(): Promise<string[]> {
  const request = await fetch(`${apiBaseUrl}/driver-service-names`);
  const response: string[] = await request.json();
  return response;
}

export async function getEnrichItem(): Promise<EnrichItem[]> {
  const request = await fetch(`${apiBaseUrlV2}/enrichItem`);
  const response: EnrichItem[] = await request.json();
  return response;
}

export async function getEnrichPipeline(): Promise<EnrichPipeline[]> {
  const request = await fetch(`${apiBaseUrlV2}/enrichPipeline`);
  const response: EnrichPipeline[] = await request.json();
  return response;
}

export async function postDataSource(data: {
  active: boolean;
  description: string;
  jsonConfig: string;
  lastIngestionDate: number;
  name: string;
  tenantId: number;
  scheduling: string;
  driverServiceName: string;
}): Promise<DataSourceInfo> {
  const request = await fetch(`${apiBaseUrlV2}/datasource`, {
    method: "POST",
    headers: { ContentType: "application/json" },
    body: JSON.stringify(data),
  });
  const response: DataSourceInfo = await request.json();
  return response;
}
