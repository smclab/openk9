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
import { queryStringMapType } from "../embeddable/entry";
import type { SearchToken } from "./client";

export type QueryKey =
  | "search"
  | "text"
  | "textOnChange"
  | "selection"
  | "filters";

export type QueryStringValuesSpec =
  | QueryKey[]
  | Partial<Record<QueryKey, boolean>>;

export type QueryValueShape = Partial<
  Record<QueryKey, unknown> & {
    text: string;
    textOnChange: string;
  }
>;

function safeParse<T = unknown>(rawValue: string | null): T | null {
  if (rawValue == null) return null;
  try {
    return JSON.parse(rawValue) as T;
  } catch {
    return rawValue as unknown as T;
  }
}

function setOrDelete(
  urlParams: URLSearchParams,
  paramKey: string,
  paramValue: unknown | undefined,
) {
  if (paramValue === undefined || paramValue === null || paramValue === "") {
    urlParams.delete(paramKey);
  } else {
    urlParams.set(
      paramKey,
      typeof paramValue === "string" ? paramValue : JSON.stringify(paramValue),
    );
  }
}

const ALL_QUERY_KEYS: QueryKey[] = [
  "search",
  "text",
  "textOnChange",
  "selection",
  "filters",
];

export function loadQueryString<Value extends QueryValueShape>(
  defaultValue: Value,
  queryStringMap?: queryStringMapType,
): Value {
  const searchParams = new URLSearchParams(window.location.search);
  const mergedValueRecord: Record<string, unknown> = { ...defaultValue };

  if (queryStringMap) {
    const mappingKeys = Object.keys(queryStringMap).filter(
      (k) => k !== "keyObj",
    );
    if (queryStringMap.keyObj && searchParams.has(queryStringMap.keyObj)) {
      const mappedObject = safeParse<Record<string, unknown>>(
        searchParams.get(queryStringMap.keyObj),
      );
      if (mappedObject && typeof mappedObject === "object") {
        for (const key of mappingKeys) {
          const mappedKey = (queryStringMap as any)[key];
          if (Object.prototype.hasOwnProperty.call(mappedObject, mappedKey)) {
            mergedValueRecord[key] = (mappedObject as any)[mappedKey];
          }
        }
      }
    } else {
      for (const key of mappingKeys) {
        const mappedKey = (queryStringMap as any)[key];
        if (searchParams.has(mappedKey)) {
          if (key === "filters") {
            const parsedValue = safeParse(searchParams.get(mappedKey));
            mergedValueRecord[key] = deserializeFiltersGrouped(parsedValue);
          } else {
            mergedValueRecord[key] = safeParse(searchParams.get(mappedKey));
          }
        }
      }
    }
  } else {
    const parsedQueryObject = safeParse<Partial<Value>>(searchParams.get("q"));
    if (parsedQueryObject && typeof parsedQueryObject === "object") {
      Object.assign(mergedValueRecord, parsedQueryObject);
    }
    for (const key of ALL_QUERY_KEYS) {
      if (searchParams.has(key)) {
        if (key === "filters") {
          const parsedValue = safeParse(searchParams.get(key));
          mergedValueRecord[key] = deserializeFiltersGrouped(parsedValue);
        } else {
          mergedValueRecord[key] = safeParse(searchParams.get(key));
        }
      }
    }
  }

  if (!mergedValueRecord["textOnChange"] && mergedValueRecord["text"]) {
    mergedValueRecord["textOnChange"] = mergedValueRecord["text"];
  }
  return mergedValueRecord as Value;
}

export function saveQueryString<Value extends QueryValueShape>(
  value: Value,
  queryStringMap?: queryStringMapType,
) {
  const searchParams = new URLSearchParams(window.location.search);

  if (queryStringMap) {
    const mappingKeys = Object.keys(queryStringMap).filter(
      (k) => k !== "keyObj",
    );
    const payloadObject: Record<string, unknown> = {};
    for (const key of mappingKeys) {
      const mappedKey = (queryStringMap as any)[key];
      let valueForKey = (value as any)[key];
      if (key === "filters") {
        valueForKey = serializeFiltersGrouped(valueForKey);
      }
      if (Array.isArray(valueForKey) && valueForKey.length === 0) continue;
      if (
        valueForKey !== undefined &&
        valueForKey !== null &&
        valueForKey !== ""
      ) {
        payloadObject[mappedKey] = valueForKey;
      }
    }
    if (queryStringMap.keyObj) {
      if (Object.keys(payloadObject).length > 0) {
        searchParams.set(queryStringMap.keyObj, JSON.stringify(payloadObject));
      } else {
        searchParams.delete(queryStringMap.keyObj);
      }
      for (const key of mappingKeys)
        searchParams.delete((queryStringMap as any)[key]);
    } else {
      for (const key of mappingKeys) {
        let valueForKey = (value as any)[key];
        if (key === "filters") {
          valueForKey = serializeFiltersGrouped(valueForKey);
        }
        if (Array.isArray(valueForKey) && valueForKey.length === 0) {
          setOrDelete(searchParams, (queryStringMap as any)[key], undefined);
        } else {
          setOrDelete(searchParams, (queryStringMap as any)[key], valueForKey);
        }
      }
    }
  } else {
    for (const key of ALL_QUERY_KEYS) {
      let valueForKey = (value as any)[key];
      if (key === "filters") {
        valueForKey =
          Array.isArray(valueForKey) && valueForKey.length === 0
            ? undefined
            : serializeFiltersGrouped(valueForKey);
      }
      if (searchParams.has(key) || valueForKey !== undefined) {
        setOrDelete(searchParams, key, valueForKey);
      }
    }
  }

  const queryString = searchParams.toString();
  const nextUrl = queryString
    ? `${window.location.pathname}?${queryString}`
    : window.location.pathname;
  window.history.replaceState(null, "", nextUrl);
}

export function loadLocalStorage<Value extends QueryValueShape>(
  defaultValue: Value,
  storageKey: string,
  queryStringMap?: queryStringMapType,
): Value {
  const rawStored =
    typeof window !== "undefined" && typeof window.localStorage !== "undefined"
      ? window.localStorage.getItem(storageKey)
      : null;

  const mergedValueRecord: Record<string, unknown> = { ...defaultValue };

  if (!rawStored) {
    if (
      mergedValueRecord["textOnChange"] == null &&
      mergedValueRecord["text"] != null
    ) {
      mergedValueRecord["textOnChange"] = mergedValueRecord["text"];
    }
    return mergedValueRecord as Value;
  }

  const parsedStoredData = safeParse<Record<string, unknown>>(rawStored);

  if (!parsedStoredData || typeof parsedStoredData !== "object") {
    if (
      mergedValueRecord["textOnChange"] == null &&
      mergedValueRecord["text"] != null
    ) {
      mergedValueRecord["textOnChange"] = mergedValueRecord["text"];
    }
    return mergedValueRecord as Value;
  }

  if (queryStringMap) {
    const mappingKeys = Object.keys(queryStringMap).filter(
      (k) => k !== "keyObj",
    );

    for (const key of mappingKeys) {
      const mappedKey = (queryStringMap as Record<string, string | undefined>)[
        key
      ];
      if (
        mappedKey &&
        Object.prototype.hasOwnProperty.call(parsedStoredData, mappedKey)
      ) {
        mergedValueRecord[key] = parsedStoredData[mappedKey];
      }
    }
  } else {
    Object.assign(mergedValueRecord, parsedStoredData);
  }

  if (
    mergedValueRecord["textOnChange"] == null &&
    mergedValueRecord["text"] != null
  ) {
    mergedValueRecord["textOnChange"] = mergedValueRecord["text"];
  }

  return mergedValueRecord as Value;
}

export function saveLocalStorage<Value extends QueryValueShape>(
  value: Value,
  storageKey: string,
  queryStringMap?: queryStringMapType,
) {
  let payloadObject: Record<string, unknown> = {};

  if (queryStringMap) {
    const mappingKeys = Object.keys(queryStringMap).filter(
      (k) => k !== "keyObj",
    );
    for (const key of mappingKeys) {
      const mappedKey = (queryStringMap as any)[key];
      const valueForKey = (value as any)[key];
      if (Array.isArray(valueForKey) && valueForKey.length === 0) continue;
      if (
        valueForKey !== undefined &&
        valueForKey !== null &&
        valueForKey !== ""
      ) {
        payloadObject[mappedKey] = valueForKey;
      }
    }
    if (queryStringMap.keyObj) {
      if (Object.keys(payloadObject).length > 0) {
        localStorage.setItem(storageKey, JSON.stringify(payloadObject));
      } else {
        localStorage.removeItem(storageKey);
      }
    } else {
      if (Object.keys(payloadObject).length > 0) {
        localStorage.setItem(storageKey, JSON.stringify(payloadObject));
      } else {
        localStorage.removeItem(storageKey);
      }
    }
  } else {
    if (value && Object.keys(value).length > 0) {
      localStorage.setItem(storageKey, JSON.stringify(value));
    } else {
      localStorage.removeItem(storageKey);
    }
  }
}

export function serializeFilters(filters: SearchToken[]): any[] {
  return (filters || []).map((token) => ({
    value: token.values?.[0] ?? "",
    suggestionCategoryId: token.suggestionCategoryId,
    tokenType: token.tokenType,
    keywordKey: token.keywordKey,
  }));
}

export function deserializeFilters(serializedArray: any[]): SearchToken[] {
  if (!Array.isArray(serializedArray)) return [];
  return serializedArray.map((item) => ({
    values: [item.value],
    suggestionCategoryId: item.suggestionCategoryId,
    tokenType: item.tokenType,
    keywordKey: item.keywordKey,
    filter: true,
    isFilter: true,
  }));
}

export function serializeFiltersGrouped(filters: SearchToken[]): any {
  if (!filters || filters.length === 0) return undefined;
  return {
    values: filters.map((token) => token.values?.[0] ?? ""),
    suggestionCategoryId: filters.map((token) => token.suggestionCategoryId),
    tokenType: filters.map((token) => token.tokenType),
    keywordKey: filters.map((token) => token.keywordKey),
  };
}

export function deserializeFiltersGrouped(serialized: any): SearchToken[] {
  if (!serialized || !Array.isArray(serialized.values)) return [];
  return serialized.values.map((valueItem: any, index: number) => ({
    values: [valueItem],
    suggestionCategoryId: serialized.suggestionCategoryId?.[index],
    tokenType: serialized.tokenType?.[index],
    keywordKey: serialized.keywordKey?.[index],
    filter: true,
    isFilter: true,
  }));
}

