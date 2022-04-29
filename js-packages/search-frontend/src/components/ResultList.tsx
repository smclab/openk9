import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { useInfiniteResults } from "./remote-data";
import { ResultMemo } from "./Result";
import { GenericResultItem, SearchToken } from "@openk9/rest-api";
import { Logo } from "./Logo";
import { Renderers } from "./useRenderers";
import { CustomScrollbar } from "./CustomScrollbar";

export type ResultsDisplayMode = { type: "finite" } | { type: "infinite" } | { type: "virtual" }

type ResultsProps<E> = {
  renderers: Renderers;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E>): void;
  displayMode: ResultsDisplayMode;
};
function Results<E>({
  renderers,
  displayMode,
  onDetail,
  searchQuery,
}: ResultsProps<E>) {
  switch (displayMode.type) {
    case "finite":
      return (
        <FiniteResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
    case "infinite":
      return (
        <InfiniteResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
    case "virtual":
      return (
        <VirtualResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
        />
      );
  }
}

export const ResultsMemo = React.memo(Results)

type ResultCountProps = {
  children: number | undefined;
};
function ResultCount({ children }: ResultCountProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
        border-bottom: 1px solid var(--openk9-embeddable-search--border-color);
      `}
    >
      {children} results
    </div>
  );
}

type ResulListProps<E> = {
  renderers: Renderers;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E> | null): void;
};

type FiniteResultsProps<E> = ResulListProps<E> & {};
export function FiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
}: FiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery);
  return (
    <div>
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <>
          <ResultCount>{results.data?.pages[0].total}</ResultCount>
          {results.data?.pages[0].result.map((result, index) => {
            return (
              <ResultMemo<E>
                renderers={renderers}
                key={index}
                result={result}
                onDetail={onDetail}
              />
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
  renderers,
  searchQuery,
  onDetail,
}: InfiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery);
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
                    <ResultMemo<E>
                      renderers={renderers}
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
              className="openk9-embeddable-search--result-container"
              css={css`
                background-color: inherit;
                color: inherit;
                font-family: inherit;
                font-size: inherit;
                padding: 8px 16px;
                width: calc(100% - 32px);
                margin-bottom: 16px;
                display: block;
              `}
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
  renderers,
  searchQuery,
  onDetail,
}: VirtualResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery);
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
            return (
              <ResultMemo
                renderers={renderers}
                result={result}
                onDetail={onDetail}
              />
            );
          }
          return null;
        }}
        endReached={() => {
          if (results.hasNextPage) {
            results.fetchNextPage();
          }
        }}
        components={{
          Scroller: CustomScrollbar as any,
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
        color: var(--openk9-embeddable-search--secondary-text-color);
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
