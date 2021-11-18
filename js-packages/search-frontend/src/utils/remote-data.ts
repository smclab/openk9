import { useInfiniteQuery, useQuery } from "react-query";

export function useInfiniteResults(text: string) {
  const pageSize = 10;
  return useInfiniteQuery(
    ["results", text],
    async ({ queryKey: [, text], pageParam = 0 }) => {
      return fetchResults(text, [
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

async function fetchResults(text: string, range: [number, number]) {
  const response = await fetch("/api/searcher/v1/search", {
    method: "POST",
    body: JSON.stringify({
      range,
      searchQuery: [{ tokenType: "TEXT", values: [text] }],
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
    documentTypes: Array<"istat" | "web" | "file" | "document">;
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
    file: {
      path: string;
      lastModifiedDate: string;
    };
    document: {
      title: string;
      relativeUrl: Array<string>;
      contentType: string;
      url: string;
      content: string;
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
    ({ queryKey: [, request] }) => fetchQueryAnalysisMock(request),
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
  return data;
}

async function fetchQueryAnalysisMock(
  request: AnalysisRequestDTO,
): Promise<AnalysisResponseDTO> {
  const words = request.searchText.split(/\s/).filter((w) => w !== "");
  return {
    searchText: request.searchText,
    analysis: words
      .filter((word) => word.length > 3)
      .map((word) => {
        const start = request.searchText.indexOf(word);
        const end = start + word.length;
        return {
          start,
          end,
          text: word,
          tokens: [
            {
              tokenType: "ENTITY",
              entityName: word.toUpperCase(),
              entityType: "person",
              value: Math.random().toString(),
              score: 0.8,
            },
            {
              tokenType: "ENTITY",
              entityName: word.toUpperCase() + "Alternative",
              entityType: "loc",
              value: Math.random().toString(),
              score: 0.3,
            },
          ],
        };
      }),
  };
}
type AnalysisRequestDTO = {
  searchText: string;
  tokens: Array<AnalysisRequestEntryDTO>;
};
type AnalysisRequestEntryDTO = {
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
type TokenDTO = {
  tokenType: "ENTITY";
  entityType: string;
  entityName: string;
  value: string;
};
export type AnalysisTokenDTO = TokenDTO & {
  score: number; // 0 - 1
};
