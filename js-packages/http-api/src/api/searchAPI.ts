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

import { GenericResultItem, SearchRequest } from "../types";
import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";

export type SearchResults<E> = {
  result: GenericResultItem<E>[];
  total: number;
  last: boolean;
};

export async function doSearch<E>(
  searchRequest: SearchRequest,
  loginInfo: LoginInfo | null,
): Promise<SearchResults<E>> {
  const fixedSearch = {
    ...searchRequest,
    range: [searchRequest.range[0], searchRequest.range[1] * 2],
    searchQuery: searchRequest.searchQuery.map((i) =>
      i.tokenType === "TEXT-TOKEN" ? { ...i, tokenType: "TEXT" } : i,
    ),
  };
  const request = await authFetch(`/api/searcher/v1/search`, loginInfo, {
    method: "POST",
    body: JSON.stringify(fixedSearch),
  });
  const response: SearchResults<E> = await request.json();
  return response;
}
