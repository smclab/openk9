import { queryStringMapType } from "../embeddable/entry";

export type QueryKey = "search" | "textOnChange" | "selection" | "filters";

export type QueryStringValuesSpec =
  | QueryKey[]
  | Partial<Record<QueryKey, boolean>>;

export type QueryValueShape = Partial<
  Record<QueryKey, unknown> & {
    text: string;
    textOnChange: string;
  }
>;

function safeParse<T = unknown>(raw: string | null): T | null {
  if (raw == null) return null;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return raw as unknown as T;
  }
}

function setOrDelete(
  params: URLSearchParams,
  key: string,
  value: unknown | undefined,
) {
  if (value === undefined || value === null || value === "") {
    params.delete(key);
  } else {
    params.set(key, typeof value === "string" ? value : JSON.stringify(value));
  }
}

const ALL_KEYS: QueryKey[] = ["search", "textOnChange", "selection", "filters"];

export function loadQueryString<Value extends QueryValueShape>(
  defaultValue: Value,
  queryStringMap?: queryStringMapType,
): Value {
  const params = new URLSearchParams(window.location.search);
  const out: Record<string, unknown> = { ...defaultValue };

  if (queryStringMap) {
    const keys = Object.keys(queryStringMap).filter((k) => k !== "keyObj");

    if (queryStringMap.keyObj && params.has(queryStringMap.keyObj)) {
      const obj = safeParse<Record<string, unknown>>(
        params.get(queryStringMap.keyObj),
      );
      if (obj && typeof obj === "object") {
        for (const k of keys) {
          const mappedKey = (queryStringMap as any)[k];
          if (obj.hasOwnProperty(mappedKey)) {
            out[k] = obj[mappedKey];
          }
        }
      }
    } else {
      for (const k of keys) {
        const mappedKey = (queryStringMap as any)[k];
        if (params.has(mappedKey)) {
          out[k] = safeParse(params.get(mappedKey));
        }
      }
    }
  } else {
    const q = safeParse<Partial<Value>>(params.get("q"));
    if (q && typeof q === "object") {
      Object.assign(out, q);
    }
    for (const k of ALL_KEYS) {
      if (params.has(k)) {
        out[k] = safeParse(params.get(k));
      }
    }
  }

  if (!out["textOnChange"] && out["text"]) {
    out["textOnChange"] = out["text"];
  }
  return out as Value;
}

export function saveQueryString<Value extends QueryValueShape>(
  value: Value,
  queryStringMap?: queryStringMapType,
) {
  const params = new URLSearchParams(window.location.search);

  if (queryStringMap) {
    const keys = Object.keys(queryStringMap).filter((k) => k !== "keyObj");
    let obj: Record<string, unknown> = {};

    for (const k of keys) {
      const mappedKey = (queryStringMap as any)[k];
      let v = value[k as QueryKey];
      if (Array.isArray(v) && v.length === 0) {
        continue;
      }
      if (v !== undefined && v !== null && v !== "") {
        obj[mappedKey] = v;
      }
    }

    if (queryStringMap.keyObj) {
      if (Object.keys(obj).length > 0) {
        params.set(queryStringMap.keyObj, JSON.stringify(obj));
      } else {
        params.delete(queryStringMap.keyObj);
      }
      for (const k of keys) params.delete((queryStringMap as any)[k]);
    } else {
      for (const k of keys) {
        let v = value[k as QueryKey];
        if (Array.isArray(v) && v.length === 0) {
          setOrDelete(params, (queryStringMap as any)[k], undefined);
        } else {
          setOrDelete(params, (queryStringMap as any)[k], v);
        }
      }
    }
  }

  const query = params.toString();
  const url = query
    ? `${window.location.pathname}?${query}`
    : window.location.pathname;
  window.history.replaceState(null, "", url);
}
