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

/** Chiavi di `queryStringMapType` che rimappano un valore (tutte tranne `keyObj`). */
const QUERY_STRING_MAP_KEYS = [
  "text",
  "textOnChange",
  "filters",
  "selection",
] as const;
type QueryStringMapKey = (typeof QUERY_STRING_MAP_KEYS)[number];

/** Le chiavi effettivamente presenti nella mappa passata dal consumatore. */
function mappedKeysOf(
  queryStringMap: NonNullable<queryStringMapType>,
): QueryStringMapKey[] {
  return QUERY_STRING_MAP_KEYS.filter((key) => queryStringMap[key] !== undefined);
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
    const mappingKeys = mappedKeysOf(queryStringMap);
    if (queryStringMap.keyObj && searchParams.has(queryStringMap.keyObj)) {
      const mappedObject = safeParse<Record<string, unknown>>(
        searchParams.get(queryStringMap.keyObj),
      );
      if (mappedObject && typeof mappedObject === "object") {
        for (const key of mappingKeys) {
          const mappedKey = queryStringMap[key] as string;
          if (Object.prototype.hasOwnProperty.call(mappedObject, mappedKey)) {
            mergedValueRecord[key] = mappedObject[mappedKey];
          }
        }
      }
    } else {
      for (const key of mappingKeys) {
        const mappedKey = queryStringMap[key] as string;
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
    const mappingKeys = mappedKeysOf(queryStringMap);
    const payloadObject: Record<string, unknown> = {};
    for (const key of mappingKeys) {
      const mappedKey = queryStringMap[key] as string;
      let valueForKey: unknown = value[key];
      if (key === "filters") {
        valueForKey = serializeFiltersGrouped(toSearchTokens(valueForKey));
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
        searchParams.delete(queryStringMap[key] as string);
    } else {
      for (const key of mappingKeys) {
        let valueForKey: unknown = value[key];
        if (key === "filters") {
          valueForKey = serializeFiltersGrouped(toSearchTokens(valueForKey));
        }
        if (Array.isArray(valueForKey) && valueForKey.length === 0) {
          setOrDelete(searchParams, queryStringMap[key] as string, undefined);
        } else {
          setOrDelete(searchParams, queryStringMap[key] as string, valueForKey);
        }
      }
    }
  } else {
    for (const key of ALL_QUERY_KEYS) {
      let valueForKey: unknown = value[key];
      if (key === "filters") {
        valueForKey =
          Array.isArray(valueForKey) && valueForKey.length === 0
            ? undefined
            : serializeFiltersGrouped(toSearchTokens(valueForKey));
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
    const mappingKeys = mappedKeysOf(queryStringMap);
    for (const key of mappingKeys) {
      const mappedKey = queryStringMap[key] as string;
      const valueForKey: unknown = value[key];
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

/** Forma piatta dei filtri: un oggetto per token. */
export type SerializedFilter = {
  value: string;
  suggestionCategoryId: number | undefined;
  tokenType: SearchToken["tokenType"];
  keywordKey: string | undefined;
};

export function serializeFilters(filters: SearchToken[]): SerializedFilter[] {
  return (filters || []).map((token) => ({
    value: token.values?.[0] ?? "",
    suggestionCategoryId: token.suggestionCategoryId,
    tokenType: token.tokenType,
    keywordKey: token.keywordKey,
  }));
}

export function deserializeFilters(serializedArray: unknown): SearchToken[] {
  if (!Array.isArray(serializedArray)) return [];
  return serializedArray.map((item) => ({
    values: [item.value],
    suggestionCategoryId: item.suggestionCategoryId,
    tokenType: item.tokenType,
    keywordKey: item.keywordKey,
    filter: true,
    isFilter: true,
  })) as SearchToken[];
}

/** Forma compatta dei filtri in query string: liste parallele indicizzate. */
export type GroupedFilters = {
  values: string[];
  suggestionCategoryId: Array<number | undefined>;
  tokenType: Array<SearchToken["tokenType"]>;
  keywordKey: Array<string | undefined>;
};

export function serializeFiltersGrouped(
  filters: SearchToken[],
): GroupedFilters | undefined {
  if (!filters || filters.length === 0) return undefined;
  return {
    values: filters.map((token) => token.values?.[0] ?? ""),
    suggestionCategoryId: filters.map((token) => token.suggestionCategoryId),
    tokenType: filters.map((token) => token.tokenType),
    keywordKey: filters.map((token) => token.keywordKey),
  };
}

/** I filtri arrivano dalla query string: si accetta solo cio' che e' un array. */
function toSearchTokens(value: unknown): SearchToken[] {
  return Array.isArray(value) ? (value as SearchToken[]) : [];
}

function isGroupedFilters(value: unknown): value is GroupedFilters {
  return (
    typeof value === "object" &&
    value !== null &&
    Array.isArray((value as { values?: unknown }).values)
  );
}

export function deserializeFiltersGrouped(serialized: unknown): SearchToken[] {
  if (!isGroupedFilters(serialized)) return [];
  // I token arrivano dalla query string: la union discriminata non e'
  // verificabile staticamente, si asserisce una volta sola qui.
  return serialized.values.map((valueItem, index) => ({
    values: [valueItem],
    suggestionCategoryId: serialized.suggestionCategoryId?.[index],
    tokenType: serialized.tokenType?.[index],
    keywordKey: serialized.keywordKey?.[index],
    filter: true,
    isFilter: true,
  })) as SearchToken[];
}

