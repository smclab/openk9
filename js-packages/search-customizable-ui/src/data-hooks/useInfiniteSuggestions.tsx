import { getSuggestions, SearchToken } from "@openk9/rest-api";
import {
  filterSuggestionByActiveSuggestionCategory,
  filterSuggestionBySearchQuery,
} from "@openk9/search-ui-components";
import { useInfiniteQuery } from "react-query";

const ENABLED = true;

export function useInfiniteSuggestions(
  searchQuery: SearchToken[] | null,
  activeSuggestionCategory: number,
) {
  const pageSize = ENABLED ? 8 : 100;
  return useInfiniteQuery(
    ["suggestions", searchQuery, activeSuggestionCategory] as const,
    async ({
      queryKey: [_, searchQuery, activeSuggestionCategory],
      pageParam,
    }) => {
      if (!searchQuery) throw new Error();
      const result = await getSuggestions({
        searchQuery,
        range: [0, pageSize],
        afterKey: pageParam,
        loginInfo: null,
      });
      return {
        result: result.result
          .filter(
            filterSuggestionByActiveSuggestionCategory(
              activeSuggestionCategory,
            ),
          )
          .filter(filterSuggestionBySearchQuery(searchQuery ?? [])),
        afterKey: result.afterKey,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (ENABLED) {
          // console.table(pages.map(page => ({afterKey: page.afterKey})))
          // console.log(getDuplicates(pages.map(page => page.afterKey)))
          if (!lastPage.afterKey) return undefined;
          return lastPage.afterKey;
        } else {
          return undefined;
        }
      },
    },
  );
}

// function getDuplicates(array: Array<string>) {
//   const found:Record<string, true> = {}
//   return array.filter(item => {
//     if (item in found) return true;
//     found[item] =true
//     return false
//   })
// }
