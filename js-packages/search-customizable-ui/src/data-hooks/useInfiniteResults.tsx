import { doSearch, SearchQuery } from "@openk9/http-api";
import { useInfiniteQuery } from "react-query";

export function useInfiniteResults(searchQuery: SearchQuery | null) {
  const pageSize = 4;
  return useInfiniteQuery(
    ["search", searchQuery] as const,
    async ({ queryKey, pageParam = 0 }) => {
      if (!queryKey[1])
        throw new Error();
      const result = await doSearch(
        {
          searchQuery: queryKey[1],
          range: [pageParam * pageSize, pageParam * pageSize + pageSize],
        },
        null
      );
      return {
        ...result,
        page: pageParam,
        last: pageParam * pageSize + pageSize > result.total,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (lastPage.last)
          return undefined;
        return lastPage.page + 1;
      },
    }
  );
}
