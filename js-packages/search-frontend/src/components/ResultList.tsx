import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import {
  ResultDTO,
  SearchTokenDTO,
  useInfiniteResults,
} from "../utils/remote-data";
import { ResultMemo } from "../renderers/Result";

type ResultsProps = {
  searchQuery: Array<SearchTokenDTO>;
  onDetail(result: ResultDTO): void;
  displayMode: { type: "finite" } | { type: "infinite" } | { type: "virtual" };
};
export function Results({ displayMode, onDetail, searchQuery }: ResultsProps) {
  switch (displayMode.type) {
    case "finite":
      return <FiniteResults searchQuery={searchQuery} onDetail={onDetail} />;
    case "infinite":
      return <InfiniteResults searchQuery={searchQuery} onDetail={onDetail} />;
    case "virtual":
      return <VirtualResults searchQuery={searchQuery} onDetail={onDetail} />;
  }
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
  searchQuery: Array<SearchTokenDTO>;
  onDetail(result: ResultDTO | null): void;
};
type FiniteResultsProps = ResulListProps & {};
export function FiniteResults({ searchQuery, onDetail }: FiniteResultsProps) {
  const results = useInfiniteResults(searchQuery);
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
export function InfiniteResults({
  searchQuery,
  onDetail,
}: InfiniteResultsProps) {
  const results = useInfiniteResults(searchQuery);
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
export function VirtualResults({ searchQuery, onDetail }: VirtualResultsProps) {
  const results = useInfiniteResults(searchQuery);
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
            return <ResultMemo result={result} onDetail={onDetail} />;
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
