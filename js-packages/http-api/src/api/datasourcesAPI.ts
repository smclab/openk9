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

import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";

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

export async function getSupportedDataSources(
  loginInfo: LoginInfo | null,
): Promise<SupportedDataSource[]> {
  const request = await authFetch(
    `/api/datasource/v1/supported-datasources`,
    loginInfo,
  );
  const response: SupportedDataSource[] = await request.json();
  return response;
}

export async function getDataSources(
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo[]> {
  const request = await authFetch(`/api/datasource/v2/datasource`, loginInfo);
  const response: DataSourceInfo[] = await request.json();
  return response;
}

export async function getDataSourceInfo(
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const request = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
  );
  const response: DataSourceInfo = await request.json();
  return response;
}

export async function changeDataSourceInfo(
  datasourceId: number,
  datasource: Partial<DataSourceInfo>,
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const request = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
    {
      method: "PATCH",
      headers: {
        ContentType: "application/json",
      },
      body: JSON.stringify(datasource),
    },
  );
  const response: DataSourceInfo = await request.json();
  return response;
}

export async function postDataSource(
  data: {
    active: boolean;
    description: string;
    jsonConfig: string;
    lastIngestionDate: number;
    name: string;
    tenantId: number;
    scheduling: string;
    driverServiceName: string;
  },
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const request = await authFetch(`/api/datasource/v2/datasource`, loginInfo, {
    method: "POST",
    headers: { ContentType: "application/json" },
    body: JSON.stringify(data),
  });
  const response: DataSourceInfo = await request.json();
  return response;
}

export async function deleteDataSource(
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<string> {
  const request = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
    {
      method: "DELETE",
    },
  );
  const response: string = await request.text();
  return response;
}

export async function toggleDataSource(
  datasourceId: number,
  loginInfo: LoginInfo | null,
  active: boolean,
): Promise<DataSourceInfo> {
  return changeDataSourceInfo(datasourceId, { active }, loginInfo);
}

export async function getSchedulerItems(
  loginInfo: LoginInfo | null,
): Promise<SchedulerItem[]> {
  const request = await authFetch(`/api/datasource/v1/scheduler`, loginInfo);
  const response: SchedulerItem[] = await request.json();
  return response;
}

export async function triggerScheduler(
  schedulerJobs: string[],
  loginInfo: LoginInfo | null,
): Promise<SchedulerRequestReturn> {
  const request = await authFetch(
    `/api/datasource/v1/scheduler/trigger`,
    loginInfo,
    {
      method: "POST",
      headers: { ContentType: "application/json" },
      body: JSON.stringify(schedulerJobs),
    },
  );
  const response: SchedulerRequestReturn = await request.json();
  return response;
}

export async function triggerReindex(
  ids: number[],
  loginInfo: LoginInfo | null,
): Promise<string> {
  const request = await authFetch(
    `/api/datasource/v1/index/reindex`,
    loginInfo,
    {
      method: "POST",
      headers: { ContentType: "application/json" },
      body: JSON.stringify({ datasourceIds: ids }),
    },
  );
  const response: string = await request.text();
  return response;
}

export async function getDriverServiceNames(
  loginInfo: LoginInfo | null,
): Promise<string[]> {
  const request = await authFetch(
    `/api/datasource/v1/driver-service-names`,
    loginInfo,
  );
  const response: string[] = await request.json();
  return response;
}

export async function getEnrichItem(
  loginInfo: LoginInfo | null,
): Promise<EnrichItem[]> {
  const request = await authFetch(`/api/datasource/v2/enrichItem`, loginInfo);
  const response: EnrichItem[] = await request.json();
  return response;
}

export async function postEnrichItem(
  enrichItem: Partial<EnrichItem>,
  loginInfo: LoginInfo | null,
): Promise<EnrichItem[]> {
  const request = await authFetch(`/api/datasource/v2/enrichItem/`, loginInfo, {
    method: "POST",
    headers: {
      ContentType: "application/json",
    },
    body: JSON.stringify(enrichItem),
  });
  const response: EnrichItem[] = await request.json();
  return response;
}

export async function changeEnrichItem(
  enrichItemId: number,
  enrichItem: Omit<EnrichItem, "enrichItemId">,
  loginInfo: LoginInfo | null,
): Promise<EnrichItem[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichItem/${enrichItemId}`,
    loginInfo,
    {
      method: "PATCH",
      headers: {
        ContentType: "application/json",
      },
      body: JSON.stringify(enrichItem),
    },
  );
  const response: EnrichItem[] = await request.json();
  return response;
}

export async function deleteEnrichItem(
  enrichItemId: number,
  loginInfo: LoginInfo | null,
): Promise<EnrichItem[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichItem/${enrichItemId}`,
    loginInfo,
    {
      method: "DELETE",
    },
  );
  const response: EnrichItem[] = await request.json();
  return response;
}

export async function getEnrichPipeline(
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichPipeline`,
    loginInfo,
  );
  const response: EnrichPipeline[] = await request.json();
  return response;
}

export async function postEnrichPipeline(
  enrichPipeline: Omit<EnrichPipeline, "enrichPipelineId">,
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichPipeline/`,
    loginInfo,
    {
      method: "POST",
      headers: {
        ContentType: "application/json",
      },
      body: JSON.stringify(enrichPipeline),
    },
  );
  const response: EnrichPipeline[] = await request.json();
  return response;
}

export async function changeEnrichPipeline(
  enrichPipelineId: number,
  enrichPipeline: Partial<EnrichPipeline>,
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
    loginInfo,
    {
      method: "PATCH",
      headers: {
        ContentType: "application/json",
      },
      body: JSON.stringify(enrichPipeline),
    },
  );
  const response: EnrichPipeline[] = await request.json();
  return response;
}

export async function deleteEnrichPipeline(
  enrichPipelineId: number,
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const request = await authFetch(
    `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
    loginInfo,
    {
      method: "DELETE",
    },
  );
  const response: EnrichPipeline[] = await request.json();
  return response;
}
