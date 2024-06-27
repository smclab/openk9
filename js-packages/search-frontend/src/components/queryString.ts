import { queryStringValues } from "../embeddable/entry";
import { SearchToken } from "./client";

export function loadQueryString<
  Value extends { text?: string; textOnChange?: string },
>(defaultValue: Value): Value {
  const params = new URLSearchParams(window.location.search);
  const q = params.get("q");
  if (q) {
    try {
      const loadedValue = JSON.parse(q) as Partial<Value>;
      const combinedValue = { ...defaultValue, ...loadedValue };

      if (!combinedValue.textOnChange && combinedValue.text) {
        combinedValue.textOnChange = combinedValue.text;
      }

      return combinedValue;
    } catch (error) {
      console.error("Error parsing query string:", error);
    }
  }
  return defaultValue;
}

export function saveQueryString<Value>(
  value: any,
  queryStringValues: queryStringValues,
) {
  const params = new URLSearchParams(window.location.search);

  const newValue = queryStringValues?.reduce((acc, prop) => {
    if (prop in value) {
      acc[prop] = value[prop];
    }
    return acc;
  }, {} as Record<string, any>);

  params.set("q", JSON.stringify(newValue));
  const url = `${window.location.pathname}?${params.toString()}`;
  window.history.replaceState(null, "", url);
}
