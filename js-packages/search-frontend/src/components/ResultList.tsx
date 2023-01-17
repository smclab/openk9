import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { ResultMemo } from "./Result";
import { GenericResultItem, SearchToken } from "./client";
import { Logo } from "./Logo";
import { Renderers, useRenderers } from "./useRenderers";
import { CustomVirtualScrollbar } from "./CustomScrollbar";
import { useOpenK9Client } from "./client";
import { useInfiniteQuery } from "react-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";

const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

export type ResultsDisplayMode =
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" };

type ResultsProps<E> = {
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E>): void;
  displayMode: ResultsDisplayMode;
};
function Results<E>({ displayMode, onDetail, searchQuery }: ResultsProps<E>) {
  const renderers = useRenderers();
  if (!renderers) return null;
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

export const ResultsMemo = React.memo(Results);

type ResultCountProps = {
  children: number | undefined;
};
function ResultCount({ children }: ResultCountProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
        border-bottom: 1px solid var(--openk9-embeddable-search--border-color);
        font-weight: Helvetica;
        font-weight: 700;
      `}
    >
      Result:
      <span
        css={css`
          color: var(--openk9-embeddable-search--active-color);
          margin-left: 5px;
        `}
      >
        {children?.toLocaleString("it")}
      </span>
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
    <div style={{ height: "100%", overflowY: "auto", position: "relative" }}>
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          css={css`
            position: absolute;
            width: 100%;
          `}
        >
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
        </div>
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
    <OverlayScrollbarsComponentDockerFix
      style={{
        height: "100%",
        overflowY: "auto",
        position: "relative",
      }}
    >
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          css={css`
            position: absolute;
            width: 100%;
            padding-bottom: 16px;
          `}
        >
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
                if (!results.isFetching) {
                  results.fetchNextPage();
                }
              }}
              className="openk9-embeddable-search--result-container"
              css={css`
                background-color: inherit;
                color: inherit;
                font-family: inherit;
                font-size: inherit;
                padding: 8px 16px;
                width: calc(100% - 32px);
                display: block;
              `}
            >
              {results.isFetching
                ? "Loading more results..."
                : "Load more results"}
            </button>
          )}
        </div>
      ) : (
        <NoResults />
      )}
    </OverlayScrollbarsComponentDockerFix>
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
          Scroller: CustomVirtualScrollbar as any,
          Footer() {
            return (
              <div
                css={css`
                  padding: 16px;
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

export function useInfiniteResults<E>(searchQuery: Array<SearchToken>) {
  const pageSize = 25;
  const client = useOpenK9Client();
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
      suspense: true,
      notifyOnChangeProps: ["isFetching"],
    },
  );
}
