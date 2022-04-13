import { useInfiniteQuery, useQuery } from "react-query";
import { isOverlapping } from "../components/useSelections";
import {
  SearchToken,
  AnalysisRequest,
  AnalysisResponse,
  AnalysisToken,
} from "@openk9/rest-api";
import { client } from "./client";

export function useInfiniteResults<E>(searchQuery: Array<SearchToken>) {
  const pageSize = 25;
  return useInfiniteQuery(
    ["results", searchQuery] as const,
    async ({ queryKey: [, searchQuery], pageParam = 0 }) => {
      return client.doSearch<E>({
        range: [pageParam * pageSize, pageParam * pageSize + pageSize],
        searchQuery,
      });
    },
    {
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        const totalDownloaded = pages.reduce(
          (total, page) => total + page.result.length,
          0,
        );
        if (totalDownloaded < lastPage.total) {
          return pages.length;
        }
      },
    },
  );
}

export function useQueryAnalysis(request: AnalysisRequest) {
  return useQuery(
    ["query-anaylis", request] as const,
    async ({ queryKey: [, request] }) =>
      fixQueryAnalysisResult(await client.fetchQueryAnalysis(request)),
  );
}

// TODO: togliere una volta implementata gestione sugestion sovrapposte
function fixQueryAnalysisResult(data: AnalysisResponse) {
  return {
    ...data,
    analysis: data.analysis
      .reverse()
      .filter((entry, index, array) =>
        array
          .slice(0, index)
          .every((previous) => !isOverlapping(previous, entry)),
      )
      .reverse(),
  };
}

async function fetchQueryAnalysisMock(
  request: AnalysisRequest,
): Promise<AnalysisResponse> {
  const words = request.searchText.split(/\s/).filter((w) => w !== "");
  return {
    searchText: request.searchText,
    analysis: words
      .filter((word) => word.length > 2)
      .map((word) => {
        const start = request.searchText.indexOf(word);
        const end = start + word.length;
        return {
          start,
          end,
          text: word,
          tokens: ((): Array<
            AnalysisToken & {
              score: number; // 0 - 1
            }
          > => {
            if (word === "pdf" || word === "pof") {
              return [{ tokenType: "DOCTYPE", value: "pdf", score: 0.3 }];
            }
            return [
              {
                tokenType: "ENTITY",
                entityName: word.toUpperCase(),
                entityType: "person",
                value: word.toLowerCase() + "___",
                score: 0.8,
              },
              {
                tokenType: "ENTITY",
                entityName: word.toUpperCase() + "Alternative",
                entityType: "loc",
                value: word.toLowerCase() + "____",
                score: 0.3,
              },
            ];
          })(),
        };
      }),
    // .concat(
    //   request.searchText.length > 0
    //     ? [
    //         {
    //           text: request.searchText,
    //           start: 0,
    //           end: request.searchText.length,
    //           tokens: [{ tokenType: "DATASOURCE", value: "tutto", score: 0 }],
    //         },
    //       ]
    //     : [],
    // )
  };
}
