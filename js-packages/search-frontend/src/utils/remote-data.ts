import { useInfiniteQuery, useQuery } from "react-query";
import { isOverlapping } from "../logic/useSelections";

export function useInfiniteResults(searchQuery: Array<SearchTokenDTO>) {
  const pageSize = 10;
  return useInfiniteQuery(
    ["results", searchQuery] as const,
    async ({ queryKey: [, searchQuery], pageParam = 0 }) => {
      return fetchResults(searchQuery, [
        pageParam * pageSize,
        pageParam * pageSize + pageSize,
      ]);
    },
    {
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        const totalDownloaded = pages.reduce(
          (total, page) => total + page.result.length,
          0,
        );
        if (totalDownloaded < lastPage.total) {
          return pages.length + 1;
        }
      },
    },
  );
}

async function fetchResults(
  searchQuery: Array<SearchTokenDTO>,
  range: [number, number],
) {
  const response = await fetch("/api/searcher/v1/search", {
    method: "POST",
    body: JSON.stringify({
      range,
      searchQuery,
    }),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  });
  const data = (await response.json()) as ResultResponseDTO;
  return data;
}
type ResultResponseDTO = {
  total: number;
  result: ResultDTO[];
};

export type ResultDTO = {
  source: {
    datasourceId: number;
    id: string;
    documentTypes: Array<
      | "istat"
      | "web"
      | "file"
      | "document"
      | "notizie"
      | "pubblicazioni"
      | "pdf"
    >;
    istat?: {
      topic: Array<string>;
      category: string;
    };
    web?: {
      favicon: string;
      title: string;
      url: string;
      content: string;
    };
    file?: {
      path: string;
      lastModifiedDate: string;
    };
    document?: {
      title: string;
      relativeUrl: Array<string>;
      contentType: string;
      url: string;
      content: string;
    };
    notizie?: {
      category: string;
      imgUrl: string;
      pubDate: string;
      topic: string;
    };
    pubblicazioni?: {
      category: string;
      imgUrl: string;
      pubDate: string;
      topic: string;
      authors: string;
    };
    resources: {
      binaries: {
        id: string;
        name: string;
        contentType: string;
      }[];
    };
  };
  highlight: {
    "web.title"?: string[];
    "web.content"?: string[];
    "web.url"?: string[];
    "file.path"?: string[];
    "document.url"?: string[];
    "document.title"?: string[];
    "document.content"?: string[];
  };
};

export function useQueryAnalysis(request: AnalysisRequestDTO) {
  return useQuery(
    ["query-anaylis", request] as const,
    ({ queryKey: [, request] }) => fetchQueryAnalysis(request),
  );
}

async function fetchQueryAnalysis(request: AnalysisRequestDTO) {
  const response = await fetch("/api/searcher/v1/query-analysis", {
    method: "POST",
    body: JSON.stringify(request),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  });
  const data = (await response.json()) as AnalysisResponseDTO;
  data.analysis = data.analysis
    .reverse()
    .filter((entry, index, array) =>
      array
        .slice(0, index)
        .every((previous) => !isOverlapping(previous, entry)),
    )
    .reverse(); // TODO togliere una volta implementata gestione sugestion sovrapposte
  return data;
}

async function fetchQueryAnalysisMock(
  request: AnalysisRequestDTO,
): Promise<AnalysisResponseDTO> {
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
          tokens: ((): Array<AnalysisTokenDTO> => {
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

type AnalysisRequestDTO = {
  searchText: string;
  tokens: Array<AnalysisRequestEntryDTO>;
};

export type AnalysisRequestEntryDTO = {
  text: string;
  start: number;
  end: number;
  token: TokenDTO;
};

type AnalysisResponseDTO = {
  searchText: string;
  analysis: Array<AnalysisResponseEntryDTO>;
};

export type AnalysisResponseEntryDTO = {
  text: string;
  start: number;
  end: number;
  tokens: Array<AnalysisTokenDTO>;
};

export type TokenDTO =
  | {
      tokenType: "DOCTYPE";
      value: string;
    }
  | {
      tokenType: "DATASOURCE";
      value: string;
    }
  | {
      tokenType: "ENTITY";
      entityType: string;
      entityName: string;
      keywordKey?: string;
      value: string;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      value: string;
    };

export type AnalysisTokenDTO = TokenDTO & {
  score: number; // 0 - 1
};

export type SearchTokenDTO =
  | {
      tokenType: "DATASOURCE";
      values: string[];
    }
  | {
      tokenType: "DOCTYPE";
      values: string[];
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      values: string[];
    }
  | {
      tokenType: "ENTITY";
      keywordKey?: string;
      entityType: string;
      values: string[];
    };
