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

import { SearchQuery, SuggestionResult } from "../types";
import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";

export async function getSuggestions({
  searchQuery,
  range,
  afterKey,
  loginInfo,
}: {
  searchQuery: SearchQuery;
  range?: [number, number]; // for pagination
  afterKey?: string; // for pagination
  loginInfo: LoginInfo | null;
}): Promise<{ result: SuggestionResult[]; afterKey: string }> {
  const request = await authFetch(`/api/searcher/v1/suggestions`, loginInfo, {
    method: "POST",
    body: JSON.stringify({ searchQuery, range, afterKey }),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  });
  const response = await request.json();
  return response;
}

type SuggestionsCategoriesResult = Array<{
  name: string;
  parentCategoryId: number;
  suggestionCategoryId: number;
  tenantId: number;
}>;

export const ROOT_SUGGESTION_CATEGORY_ID = -1;
export const ALL_SUGGESTION_CATEGORY_ID = 100000;
export const DOCUMENT_TYPES_SUGGESTION_CATEGORY_ID = 99999;

export async function getSuggestionCategories(
  loginInfo: LoginInfo | null,
): Promise<SuggestionsCategoriesResult> {
  const response = await authFetch(
    `/api/searcher/suggestion-categories`,
    loginInfo,
  );
  const data = (await response.json()) as SuggestionsCategoriesResult;
  return [
    {
      name: "All",
      parentCategoryId: ROOT_SUGGESTION_CATEGORY_ID,
      suggestionCategoryId: ALL_SUGGESTION_CATEGORY_ID,
      tenantId: NaN,
    },
    {
      name: "Keywords",
      parentCategoryId: ROOT_SUGGESTION_CATEGORY_ID,
      suggestionCategoryId: DOCUMENT_TYPES_SUGGESTION_CATEGORY_ID,
      tenantId: NaN,
    },
    ...data,
  ];
}
