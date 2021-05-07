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

import { Tenant } from "../types";
import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";

export async function getTenants(
  loginInfo: LoginInfo | null,
): Promise<Tenant[]> {
  const request = await authFetch(`/api/datasource/v2/tenant`, loginInfo);
  const response: Tenant[] = await request.json();
  return response;
}

export async function getTenant(
  tenantId: number,
  loginInfo: LoginInfo | null,
): Promise<Tenant> {
  const request = await authFetch(
    `/api/datasource/v2/tenant/${tenantId}`,
    loginInfo,
  );
  const response: Tenant = await request.json();
  return response;
}

export async function postTenant(
  data: {
    name: string;
    virtualHost: string;
    jsonConfig: string;
  },
  loginInfo: LoginInfo | null,
) {
  await authFetch(`/api/datasource/v2/tenant`, loginInfo, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

export async function putTenant(data: Tenant, loginInfo: LoginInfo | null) {
  if (!data.jsonConfig) {
    data.jsonConfig = "{}";
  }

  await authFetch(`/api/datasource/v2/tenant`, loginInfo, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

export async function deleteTenant(
  tenantId: number,
  loginInfo: LoginInfo | null,
) {
  await authFetch(`/api/datasource/v2/tenant/${tenantId}`, loginInfo, {
    method: "DELETE",
  });
}
