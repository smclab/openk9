import { queryStringMapType } from "../embeddable/entry";

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

const ALL_KEYS: QueryKey[] = [
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
          if (Object.prototype.hasOwnProperty.call(obj, mappedKey)) {
            out[k] = (obj as any)[mappedKey];
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
    if (q && typeof q === "object") Object.assign(out, q);
    for (const k of ALL_KEYS) {
      if (params.has(k)) out[k] = safeParse(params.get(k));
    }
  }

  if (!out["textOnChange"] && out["text"]) out["textOnChange"] = out["text"];
  return out as Value;
}

export function saveQueryString<Value extends QueryValueShape>(
  value: Value,
  queryStringMap?: queryStringMapType,
) {
  const params = new URLSearchParams(window.location.search);

  if (queryStringMap) {
    const keys = Object.keys(queryStringMap).filter((k) => k !== "keyObj");
    const obj: Record<string, unknown> = {};
    for (const k of keys) {
      const mappedKey = (queryStringMap as any)[k];
      const v = (value as any)[k];
      if (Array.isArray(v) && v.length === 0) continue;
      if (v !== undefined && v !== null && v !== "") obj[mappedKey] = v;
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
        const v = (value as any)[k];
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

export function loadLocalStorage<Value extends QueryValueShape>(
  defaultValue: Value,
  storageKey: string,
  queryStringMap?: queryStringMapType,
): Value {
  const raw =
    typeof window !== "undefined" && typeof window.localStorage !== "undefined"
      ? window.localStorage.getItem(storageKey)
      : null;

  const result: Record<string, unknown> = { ...defaultValue };

  if (!raw) {
    if (result["textOnChange"] == null && result["text"] != null) {
      result["textOnChange"] = result["text"];
    }
    return result as Value;
  }

  const parsedData = safeParse<Record<string, unknown>>(raw);

  if (!parsedData || typeof parsedData !== "object") {
    if (result["textOnChange"] == null && result["text"] != null) {
      result["textOnChange"] = result["text"];
    }
    return result as Value;
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
        Object.prototype.hasOwnProperty.call(parsedData, mappedKey)
      ) {
        result[key] = parsedData[mappedKey];
      }
    }
  } else {
    Object.assign(result, parsedData);
  }

  if (result["textOnChange"] == null && result["text"] != null) {
    result["textOnChange"] = result["text"];
  }

  return result as Value;
}

export function saveLocalStorage<Value extends QueryValueShape>(
  value: Value,
  storageKey: string,
  queryStringMap?: queryStringMapType,
) {
  let obj: Record<string, unknown> = {};

  if (queryStringMap) {
    const keys = Object.keys(queryStringMap).filter((k) => k !== "keyObj");
    for (const k of keys) {
      const mappedKey = (queryStringMap as any)[k];
      const v = (value as any)[k];
      if (Array.isArray(v) && v.length === 0) continue;
      if (v !== undefined && v !== null && v !== "") obj[mappedKey] = v;
    }
    if (queryStringMap.keyObj) {
      if (Object.keys(obj).length > 0) {
        localStorage.setItem(storageKey, JSON.stringify(obj));
      } else {
        localStorage.removeItem(storageKey);
      }
    } else {
      if (Object.keys(obj).length > 0) {
        localStorage.setItem(storageKey, JSON.stringify(obj));
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
