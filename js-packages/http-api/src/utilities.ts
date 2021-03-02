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

import { SearchQuery, SearchToken } from "./types";

export function escapeString(str: string) {
  if (str.includes(" ")) {
    return `"${str}"`;
  } else {
    return str;
  }
}

export function despaceString(str: string) {
  return str.replaceAll(" ", "+");
}

export function undespaceString(str: string) {
  return str.replaceAll("+", " ");
}

export function isSearchQueryEmpty(query: SearchQuery) {
  return query.length === 0;
}

export function readQueryParamToken(
  query: SearchQuery,
  keywordKey: string,
): string[] | null {
  const item = query.find((i) => i.keywordKey === keywordKey);
  if (item) {
    return item.values;
  } else {
    return null;
  }
}

export function setQueryParamToken({
  query,
  keywordKey,
  values,
  entityType,
  tokenType,
}: {
  query: SearchQuery;
  keywordKey: string;
  values: string[] | string[] | null;
  tokenType: SearchToken["tokenType"];
  entityType?: string;
}): SearchQuery {
  if (values === null) {
    return query.filter((i) => i.keywordKey !== keywordKey);
  } else if (query.find((i) => i.keywordKey === keywordKey)) {
    return query.map((i) =>
      i.keywordKey === keywordKey &&
      (i.tokenType !== "ENTITY" || i.entityType === entityType) &&
      i.tokenType === tokenType
        ? { ...i, values: values as any }
        : i,
    );
  } else {
    const item: any = {
      tokenType,
      keywordKey,
      values,
      entityType,
    };
    return [...query, item];
  }
}
