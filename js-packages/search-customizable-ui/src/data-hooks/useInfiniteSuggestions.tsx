import { getSuggestions, SearchQuery } from "@openk9/http-api";
import { useInfiniteQuery } from "react-query";

export function useInfiniteSuggestions(searchQuery: SearchQuery | null) {
  const pageSize = 8;
  return useInfiniteQuery(
    ["suggestions", searchQuery] as const,
    async ({ queryKey, pageParam }) => {
      if (!queryKey[1]) throw new Error();
      const result = await getSuggestions({
        searchQuery: queryKey[1],
        range: [0, pageSize],
        afterKey: pageParam,
        loginInfo: null,
      });
      return result
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (lastPage.result.length === 0) return undefined;
        return lastPage.afterKey
      },
    },
  );
}
