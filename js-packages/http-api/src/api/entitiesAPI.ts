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

import { EntityLookupRequest, EntityLookupResponse } from "../types";
import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";

export async function doSearchEntities(
  query: EntityLookupRequest,
  loginInfo: LoginInfo | null,
): Promise<EntityLookupResponse> {
  const request = await authFetch(`/api/searcher/v1/entity`, loginInfo, {
    method: "POST",
    body: JSON.stringify(query),
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
  });
  return await request.json();
}
