import React from "react";
import { useInfiniteQuery, useQuery } from "react-query";
import { css } from "styled-components/macro";
import "./index.css";
import { Virtuoso } from "react-virtuoso";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons";

const resultDisplayMode:
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" } = { type: "virtual" };

export function App() {
  const [text, setText] = React.useState("");
  const [detail, setDetail] = React.useState<ResultDTO | null>(null);
  const textDebounced = useDebounce(text, 300);
  const queryAnalysis = useQueryAnalysis({
    searchText: textDebounced,
    tokens: [],
  });
  return (
    <PageLayout
      search={
        <div
          css={css`
            display: flex;
            border: 1px solid ${myTheme.searchBarBorderColor};
            border-radius: 4px;
            background-color: white;
            align-items: center;
          `}
        >
          <FontAwesomeIcon icon={faSearch} style={{ paddingLeft: "16px" }} />
          <input
            value={text}
            onChange={(event) => {
              setText(event.currentTarget.value);
              setDetail(null);
            }}
            css={css`
              flex-grow: 1;
              border: none;
              outline: none;
              padding: 16px;
              color: inherit;
              font-size: inherit;
              font-family: inherit;
              background-color: inherit;
            `}
          ></input>
        </div>
      }
      result={(() => {
        switch (resultDisplayMode.type) {
          case "finite":
            return <FiniteResults text={textDebounced} onDetail={setDetail} />;
          case "infinite":
            return (
              <InfiniteResults text={textDebounced} onDetail={setDetail} />
            );
          case "virtual":
            return <VirtualResults text={textDebounced} onDetail={setDetail} />;
        }
      })()}
      detail={detail && <DetailMemo result={detail} />}
    />
  );
}

const myTheme = {
  backgroundColor2: "#f7f7f7",
  separationBoxShadow: "0 1px 2px 0 rgb(0 0 0 / 10%)",
  redTextColor: "#c22525",
  dockbarTextColor: "#1e1c21",
  searchBarBorderColor: "#ced4da",
};

type PageLayoutProps = {
  search: React.ReactNode;
  result: React.ReactNode;
  detail: React.ReactNode;
};
function PageLayout({ search, result, detail }: PageLayoutProps) {
  return (
    <div
      css={css`
        width: 100vw;
        height: 100vh;
        display: grid;
        grid-template-columns: 50% 50%;
        grid-template-rows: auto auto 1fr;
        grid-template-areas:
          "dockbar dockbar"
          "search search"
          "result detail";
      `}
    >
      <div
        css={css`
          grid-area: search;
          background-color: ${myTheme.backgroundColor2};
          padding: 16px;
        `}
      >
        {search}
      </div>
      <div
        css={css`
          grid-area: dockbar;
          padding: 8px 16px;
          box-shadow: ${myTheme.separationBoxShadow};
          display: flex;
          align-items: center;
        `}
      >
        <div
          css={css`
            font-size: 20;
            color: ${myTheme.dockbarTextColor};
            display: flex;
            align-items: center;
          `}
        >
          <span
            css={css`
              color: ${myTheme.redTextColor};
              margin-right: 8px;
            `}
          >
            <Logo size={32} />
          </span>
          <span>Open</span>
          <span
            css={css`
              font-weight: 700;
            `}
          >
            K9
          </span>
        </div>
      </div>
      <div
        css={css`
          grid-area: result;
          overflow-y: auto;
        `}
      >
        {result}
      </div>
      <div
        css={css`
          grid-area: detail;
          overflow-y: auto;
          background-color: ${myTheme.backgroundColor2};
        `}
      >
        {detail}
      </div>
    </div>
  );
}

type ResultCountProps = {
  children: number | undefined;
};
function ResultCount({ children }: ResultCountProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {children} results
    </div>
  );
}

type ResulListProps = {
  text: string;
  onDetail(result: ResultDTO | null): void;
};

type FiniteResultsProps = ResulListProps & {};
function FiniteResults({ text, onDetail }: FiniteResultsProps) {
  const results = useInfiniteResults(text);
  return (
    <div>
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      {results.data?.pages[0].result.map((result, index) => {
        return <ResultMemo key={index} result={result} onDetail={onDetail} />;
      })}
    </div>
  );
}

type InfiniteResultsProps = ResulListProps & {};
function InfiniteResults({ text, onDetail }: InfiniteResultsProps) {
  const results = useInfiniteResults(text);
  return (
    <div>
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      {results.data?.pages.map((page, pageIndex) => {
        return (
          <React.Fragment key={pageIndex}>
            {page.result.map((result, resultIndex) => {
              return (
                <ResultMemo
                  key={resultIndex}
                  result={result}
                  onDetail={onDetail}
                />
              );
            })}
          </React.Fragment>
        );
      })}
      {results.hasNextPage && (
        <button
          onClick={() => {
            results.fetchNextPage();
          }}
        >
          load more
        </button>
      )}
    </div>
  );
}

type VirtualResultsProps = ResulListProps & {};
function VirtualResults({ text, onDetail }: VirtualResultsProps) {
  const results = useInfiniteResults(text);
  const resultsFlat = results.data?.pages.flatMap((page) => page.result);
  return (
    <div
      css={css`
        display: flex;
        flex-direction: column;
        height: 100%;
      `}
    >
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      <Virtuoso
        style={{ flexGrow: 1 }}
        totalCount={resultsFlat?.length ?? 0}
        itemContent={(index) => {
          const result = resultsFlat?.[index];
          if (result) {
            return <Result result={result} onDetail={onDetail} />;
          }
          return null;
        }}
        endReached={() => {
          if (results.hasNextPage) {
            results.fetchNextPage();
          }
        }}
      />
    </div>
  );
}

type ResultProps = {
  result: ResultDTO;
  onDetail(result: ResultDTO | null): void;
};
function Result({ result, onDetail }: ResultProps) {
  return (
    <div
      css={css`
        padding: 0px 16px 16px 16px;
      `}
    >
      <div
        css={css`
          border: 1px solid ${myTheme.searchBarBorderColor};
          border-radius: 4px;
        `}
        onMouseEnter={() => onDetail(result)}
      >
        {(() => {
          if (result.source.documentTypes.includes("web")) {
            return <WebResult result={result} />;
          }
          return (
            <pre
              css={css`
                height: 100px;
                overflow: hidden;
              `}
            >
              {JSON.stringify(result, null, 2)}
            </pre>
          );
        })()}
      </div>
    </div>
  );
}
const ResultMemo = React.memo(Result);

type DetailProps = {
  result: ResultDTO;
};
function Detail({ result }: DetailProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {(() => {
        if (result.source.documentTypes.includes("web")) {
          return <WebDetail result={result} />;
        }
        return <pre css={css``}>{JSON.stringify(result, null, 2)}</pre>;
      })()}
    </div>
  );
}
const DetailMemo = React.memo(Detail);

function useInfiniteResults(text: string) {
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

type ResultDTO = {
  source: {
    documentTypes: Array<"istat" | "web">;
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
  };
  highlight: {
    "web.title"?: string[];
    "web.content"?: string[];
    "web.url"?: string[];
  };
};

function useQueryAnalysis(request: AnalysisRequestDTO) {
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
  const words = request.searchText.split("");
  return {
    searchText: request.searchText,
    analysis: words.map((word) => {
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

type AnalysisResponseEntryDTO = {
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

type AnalysisTokenDTO = TokenDTO & {
  score: number; // 0 - 1
};

type WebResultProps = { result: ResultDTO };
function WebResult({ result }: WebResultProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-template-columns: 30px auto;
        grid-template-rows: auto auto auto;
        padding: 8px 16px;
        grid-column-gap: 16px;
        grid-row-gap: 8px;
      `}
    >
      <div
        css={css`
          grid-column: 1;
          grid-row: 1;
          align-self: center;
          width: 30px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        `}
      >
        <img
          src={result.source.web?.favicon}
          alt=""
          css={css`
            max-height: 30px;
            max-width: 30px;
          `}
        />
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 1;
          font-size: 1.5em;
          font-weight: 500;
          ${truncatedLineStyle}
        `}
      >
        {result.highlight["web.title"] ? (
          <HighlightedText text={result.highlight["web.title"][0]} />
        ) : (
          result.source.web?.title
        )}
      </div>
      <a
        href={result.source.web?.url}
        css={css`
          grid-column: 2;
          grid-row: 2;
          font-size: 0.8em;
          ${truncatedLineStyle}
        `}
      >
        {result.highlight["web.url"] ? (
          <HighlightedText text={result.highlight["web.url"][0]} />
        ) : (
          result.source.web?.url
        )}
      </a>
      <div
        css={css`
          grid-column: 2;
          grid-row: 3;
          ${truncatedLineStyle};
        `}
      >
        {result.highlight["web.content"] ? (
          result.highlight["web.content"].map((text, index) => (
            <div key={index} css={truncatedLineStyle}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div
            css={css`
              max-height: 100px;
              overflow-y: hidden;
            `}
          >
            {result.source.web?.content}
          </div>
        )}
      </div>
    </div>
  );
}

type WebDetailProps = {
  result: ResultDTO;
};
function WebDetail({ result }: WebDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img src={result.source.web?.favicon} alt="" />
      <div
        css={css`
          font-size: 1.5em;
          font-weight: 500;
        `}
      >
        {result.highlight["web.title"] ? (
          <HighlightedText text={result.highlight["web.title"][0]} />
        ) : (
          result.source.web?.title
        )}
      </div>
      <div
        css={css`
          font-size: 0.8em;
        `}
      >
        {result.highlight["web.url"] ? (
          <HighlightedText text={result.highlight["web.url"][0]} />
        ) : (
          result.source.web?.url
        )}
      </div>
      <div>
        {result.highlight["web.content"] ? (
          result.highlight["web.content"].map((text, index) => (
            <div key={index}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div>{result.source.web?.content}</div>
        )}
      </div>
    </div>
  );
}

const truncatedLineStyle = css`
  overflow: hidden;
  max-width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
`;

function HighlightedText({
  text,
  Highlight = HighLight,
}: {
  text: string;
  Highlight?: React.FC<{}>;
}) {
  return (
    <>
      {Array.from(
        new DOMParser().parseFromString(text, "text/html").body.childNodes,
      ).map((child, index) => {
        if (child instanceof Text) return child.textContent;
        if (child instanceof Element && child.tagName === "EM")
          return <Highlight key={index}>{child.textContent}</Highlight>;
        return null;
      })}
    </>
  );
}

function HighLight({ children }: { children?: React.ReactNode }) {
  return (
    <span
      css={css`
        color: ${myTheme.redTextColor};
      `}
    >
      {children}
    </span>
  );
}

function useDebounce<T>(value: T, delay: number) {
  const [debouncedValue, setDebouncedValue] = React.useState(value);

  React.useEffect(() => {
    const timeout = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return function cleanup() {
      clearTimeout(timeout);
    };
  }, [value, delay]);

  return debouncedValue;
}

export function Logo({ size = 26 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 97 94">
      <g transform="translate(-273 -246)">
        <path
          d="M326.332,285.609a2.026,2.026,0,0,0,1.539-.645,2.111,2.111,0,0,0,.645-1.539,2.023,2.023,0,0,0-.645-1.535,2.115,2.115,0,0,0-1.539-.648,2.057,2.057,0,0,0-1.535.648,2.091,2.091,0,0,0-.648,1.535,2.057,2.057,0,0,0,.648,1.539,2.017,2.017,0,0,0,1.535.645"
          fill="currentColor"
        />
        <path
          d="M317.676-311.331a81.97,81.97,0,0,0-2.844-11.426,2.424,2.424,0,0,0-2.676-1.637,17.919,17.919,0,0,0-9.605,4.961c-2.953,2.9-5.062,7.414-6.75,11.48a4.319,4.319,0,0,0,.141,3.512,18.853,18.853,0,0,0,7.082,7.48,18.491,18.491,0,0,0,5.227,2.422c.535.156,1.086.289,1.645.4a23.015,23.015,0,0,0,3.711.379c.949.027,2.715.074,3.582-1.582a3.384,3.384,0,0,0,.277-.723C319-301.506,318.383-307.081,317.676-311.331Zm0,0"
          transform="translate(0 595.276)"
          fill="none"
          stroke="currentColor"
          strokeMiterlimit="10"
          strokeWidth="6"
        />
        <path
          d="M313.578-324.174c1.414.34,8.094,1.82,9.469,2.324,1.09.4,2.184.781,3.262,1.211,2.23.887,4.57,1.867,5.938,3.949.289.438.531.9.852,1.32.742.961,1.711,1,2.84,1.125l3.559.41c2.516.285,5.027.578,7.539.863.379.043.762.09,1.141.129a3.729,3.729,0,0,1,2.273,1.457,3.258,3.258,0,0,1,.82,2.461,19.486,19.486,0,0,1-8.984,16.031c-3.691,2.461-6.922,1.152-12.066,1.859a3.886,3.886,0,0,0-2.312,1.2,3.726,3.726,0,0,0-.785.969,3.133,3.133,0,0,0-.406,1.3v.109l.742,15.645a18.156,18.156,0,0,0,3.316,8.016,38.047,38.047,0,0,1,3.246,5.555,45.1,45.1,0,0,0,33-43.469,45.143,45.143,0,0,0-45.141-45.039,45.459,45.459,0,0,0-6.883.52,45.14,45.14,0,0,0-38.238,44.594c0,9.918,4.2,17.492,9.609,24.949-.18-.246,3.793-2.684,4.133-2.9a34.678,34.678,0,0,0,5.977-4.6,10.516,10.516,0,0,0,3.418-5.395c.09-.453.152-.91.2-1.367.082-.824.105-1.656.109-2.484"
          transform="translate(0 595.276)"
          fill="none"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeMiterlimit="10"
          strokeWidth="6"
        />
      </g>
    </svg>
  );
}
