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
