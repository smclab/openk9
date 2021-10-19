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
  afterKey?: string, // for pagination
  loginInfo: LoginInfo | null;
}): Promise<{ result: SuggestionResult[]; afterKey: string }> {
  const request = await authFetch(`/api/searcher/v1/suggestions`, loginInfo, {
    method: "POST",
    body: JSON.stringify({ searchQuery, range, afterKey }),
  });
  const response = await request.json();
  return response;
}

type SuggestionsCategoriesResult = {
  categories: Array<{
    categoryId: number;
    name: string;
    parentCategoryId: number; // 0 means root category
  }>;
};

export async function getSuggestionCategories(): Promise<SuggestionsCategoriesResult> {
  // TODO implement
  return {
    categories: [
      { categoryId: 0, name: "ALL", parentCategoryId: 0 },
      { categoryId: 1, name: "DATASOURCE", parentCategoryId: 0 },
      { categoryId: 2, name: "DOCTYPE", parentCategoryId: 0 },
      { categoryId: 3, name: "ENTITY", parentCategoryId: 0 },
      { categoryId: 4, name: "TEXT", parentCategoryId: 0 },
    ],
  };
}
