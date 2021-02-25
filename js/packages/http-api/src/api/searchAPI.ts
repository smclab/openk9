import { GenericResultItem, SearchRequest } from "../types";
import { apiBaseUrl } from "./common";

export type SearchResults<E> = {
  result: GenericResultItem<E>[];
  total: number;
  last: boolean;
};

export async function doSearch<E>(
  searchRequest: SearchRequest,
): Promise<SearchResults<E>> {
  const fixedSearch = {
    ...searchRequest,
    range: [searchRequest.range[0], searchRequest.range[1] * 2],
    searchQuery: searchRequest.searchQuery.map((i) =>
      i.tokenType === "TEXT-TOKEN" ? { ...i, tokenType: "TEXT" } : i,
    ),
  };
  const request = await fetch(`${apiBaseUrl}/search`, {
    method: "POST",
    body: JSON.stringify(fixedSearch),
  });
  const response: SearchResults<E> = await request.json();
  return response;
}
