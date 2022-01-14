import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { useInfiniteResults } from "./remote-data";
import { ResultMemo } from "./Result";
import { GenericResultItem, LoginInfo, SearchToken } from "@openk9/rest-api";

type ResultsProps<E> = {
  loginInfo: LoginInfo | null;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E>): void;
  displayMode: { type: "finite" } | { type: "infinite" } | { type: "virtual" };
};
export function Results<E>({
  displayMode,
  onDetail,
  searchQuery,
  loginInfo,
}: ResultsProps<E>) {
  switch (displayMode.type) {
    case "finite":
      return (
        <FiniteResults
          loginInfo={loginInfo}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
    case "infinite":
      return (
        <InfiniteResults
          loginInfo={loginInfo}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
    case "virtual":
      return (
        <VirtualResults
          loginInfo={loginInfo}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
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
type ResulListProps<E> = {
  loginInfo: LoginInfo | null;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E> | null): void;
};
type FiniteResultsProps<E> = ResulListProps<E> & {};
export function FiniteResults<E>({
  searchQuery,
  onDetail,
  loginInfo,
}: FiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(loginInfo, searchQuery);
  return (
    <div>
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      {results.data?.pages[0].result.map((result, index) => {
        return <ResultMemo key={index} result={result} onDetail={onDetail} />;
      })}
    </div>
  );
}
type InfiniteResultsProps<E> = ResulListProps<E> & {};
export function InfiniteResults<E>({
  searchQuery,
  onDetail,
  loginInfo,
}: InfiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(loginInfo, searchQuery);
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
type VirtualResultsProps<E> = ResulListProps<E> & {};
export function VirtualResults<E>({
  searchQuery,
  onDetail,
  loginInfo,
}: VirtualResultsProps<E>) {
  const results = useInfiniteResults<E>(loginInfo, searchQuery);
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
