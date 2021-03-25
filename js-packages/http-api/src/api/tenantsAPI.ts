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

import { apiBaseUrlV2 } from "./common";

export type Tenant = {
  tenantId: number;
  name: string;
  virtualHost: string;
  jsonConfig: String;
};

export async function getTenants(): Promise<Tenant[]> {
  const request = await fetch(`${apiBaseUrlV2}/tenant`);
  const response: Tenant[] = await request.json();
  return response;
}

export async function getTenant(tenantId: number): Promise<Tenant> {
  const request = await fetch(`${apiBaseUrlV2}/tenant/${tenantId}`);
  const response: Tenant = await request.json();
  return response;
}

export async function postTenant(data: {
  name: string;
  virtualHost: string;
  jsonConfig: string;
}) {
  await fetch(`${apiBaseUrlV2}/tenant`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

export async function putTenant(data: Tenant) {
  console.log(data);
  data.jsonConfig = "{}";
  await fetch(`${apiBaseUrlV2}/tenant`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}
