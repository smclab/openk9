import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { useInfiniteResults } from "./remote-data";
import { ResultMemo } from "./Result";
import { GenericResultItem, LoginInfo, SearchToken } from "@openk9/rest-api";
import { Logo } from "./Logo";
import { myTheme } from "./myTheme";

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
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <>
          <ResultCount>{results.data?.pages[0].total}</ResultCount>
          {results.data?.pages[0].result.map((result, index) => {
            return (
              <ResultMemo key={index} result={result} onDetail={onDetail} />
            );
          })}
        </>
      ) : (
        <NoResults />
      )}
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
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <>
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
        </>
      ) : (
        <NoResults />
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
  const thereAreResults = Boolean(
    results.data?.pages[0].total && results.data.pages[0].total > 0,
  );
  return (
    <div
      css={css`
        display: flex;
        flex-direction: column;
        height: 100%;
      `}
    >
      {thereAreResults && (
        <ResultCount>{results.data?.pages[0].total}</ResultCount>
      )}
      <Virtuoso
        hidden={!thereAreResults}
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
        components={{
          Footer() {
            return (
              <div
                css={css`
                  padding: 0 16px 16px 16px;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                `}
              >
                {results.hasNextPage
                  ? "loading more results..."
                  : "all results loaded"}
              </div>
            );
          },
        }}
      />
      {!thereAreResults && <NoResults />}
    </div>
  );
}

function NoResults() {
  return (
    <div
      css={css`
        color: ${myTheme.grayTexColor};
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
      `}
    >
      <Logo size={128} />
      <h3>No results were found.</h3>
      <div>Try with another query</div>
    </div>
  );
}
