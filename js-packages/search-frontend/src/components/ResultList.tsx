import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { ResultMemo } from "./Result";
import { GenericResultItem, SearchToken, SortField } from "./client";
import { Logo } from "./Logo";
import { Renderers, useRenderers } from "./useRenderers";
import { CustomVirtualScrollbar } from "./CustomScrollbar";
import { useOpenK9Client } from "./client";
import { useInfiniteQuery, useQuery } from "react-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { ResultSvg } from "../svgElement/ResultSvg";
import { SortResultList } from "./SortResultList";
import { useTranslation } from "react-i18next";
const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

export type ResultsDisplayMode =
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" };

type ResultsProps<E> = {
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E>): void;
  displayMode: ResultsDisplayMode;
  sort: SortField[];
  setSortResult: (sortResultNew: SortField) => void;
  setDetailMobile(result: GenericResultItem<E>): void;
  isMobile: boolean;
};
function Results<E>({
  displayMode,
  onDetail,
  searchQuery,
  sort,
  setSortResult,
  isMobile,
  setDetailMobile,
}: ResultsProps<E>) {
  const renderers = useRenderers();

  if (!renderers) return null;
  switch (displayMode.type) {
    case "finite":
      return (
        <FiniteResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
        />
      );
    case "infinite":
      return (
        <InfiniteResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
        />
      );
    case "virtual":
      return (
        <VirtualResults
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
        />
      );
  }
}

export const ResultsMemo = React.memo(Results);

type ResultCountProps = {
  children: number | undefined;
  setSortResult: (sortResultNew: SortField) => void;
  isMobile: boolean;
};

function ResultCount({ children, setSortResult, isMobile }: ResultCountProps) {
  const client = useOpenK9Client();
  const { t } = useTranslation();

  return (
    <React.Fragment>
      <div
        className="openk9-result-list-container-title box-title"
        css={css`
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 20.7px;
          padding-bottom: 12.7px;
          display: flex;
          margin-bottom: 8px;
          gap: 5px;
        `}
      >
        <span>
          <ResultSvg />
        </span>
        <span className="openk9-result-list-title title">
          <h2
            css={css`
              font-style: normal;
              font-weight: 700;
              font-size: 18px;
              height: 18px;
              line-height: 22px;
              align-items: center;
              color: #3f3f46;
              margin: 0;
            `}
          >
            {t("result")}
          </h2>
        </span>
      </div>
      <div
        className="openk9-number-result-list-container-wrapper "
        css={css`
          padding: 8px 16px;
          font-weight: Helvetica;
          font-weight: 700;
        `}
      >
        <div
          className="openk9-number-result-list-container more-detail-content"
          css={css`
            padding: 8px 5px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            display: flex;
            justify-content: space-between;
            border-radius: 8px;
            align-items: center;
          `}
        >
          <span
            className="openk9-number-result-list-number-of-results "
            css={css`
              color: var(--openk9-embeddable-search--active-color);
              margin-left: 5px;
            `}
          >
            {children?.toLocaleString("it")}
          </span>
          <SortResultList setSortResult={setSortResult} />
        </div>
      </div>
    </React.Fragment>
  );
}

type ResulListProps<E> = {
  renderers: Renderers;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E> | null): void;
  setDetailMobile(result: GenericResultItem<E> | null): void;
  sort: SortField[];
  setSortResult: (sortResultNew: SortField) => void;
  isMobile: boolean;
};

type FiniteResultsProps<E> = ResulListProps<E> & {};
export function FiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
}: FiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery, sort);
  return (
    <div
      className="openk9-finite-result-container"
      style={{ height: "100%", overflowY: "auto", position: "relative" }}
    >
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          className="openk9-finite-result"
          css={css`
            position: absolute;
            width: 100%;
          `}
        >
          <ResultCount setSortResult={setSortResult} isMobile={isMobile}>
            {results.data?.pages[0].total}
          </ResultCount>
          {results.data?.pages[0].result.map((result, index) => {
            return (
              <ResultMemo<E>
                renderers={renderers}
                key={index}
                result={result}
                onDetail={onDetail}
                setDetailMobile={setDetailMobile}
                isMobile={isMobile}
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
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
}: InfiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery, sort);
  const { t } = useTranslation();
  return (
    <OverlayScrollbarsComponentDockerFix
      className="openk9-infinite-results-overlay-scrollbars"
      style={{
        height: "100%",
        overflowY: "auto",
        position: "relative",
      }}
      options={{
        overflowBehavior: {
          x: "hidden",
        },
      }}
    >
      {results?.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          className="openk9-infinite-results-container-wrapper"
          css={css`
            position: absolute;
            width: 100%;
            padding-bottom: 16px;
          `}
        >
          <ResultCount setSortResult={setSortResult} isMobile={isMobile}>
            {results.data?.pages[0].total}
          </ResultCount>
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
                      setDetailMobile={setDetailMobile}
                      isMobile={isMobile}
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
                width: calc(100% - 32px);
                display: block;
              `}
            >
              {results.isFetching ? "Loading more results..." : t("load-more")}
            </button>
          )}
        </div>
      ) : (
        <React.Fragment>
          <div className="openk9-container-no-results">
            <div
              className="openk9-result-list-container-title box-title"
              css={css`
                padding: 0px 16px;
                width: 100%;
                background: #fafafa;
                padding-top: 20.7px;
                padding-bottom: 12.7px;
                display: flex;
                margin-bottom: 8px;
              `}
            >
              <span>
                <ResultSvg />
              </span>
              <span
                className="openk9-result-list-title title"
                css={css`
                  margin-left: 5px;
                  font-style: normal;
                  font-weight: 700;
                  font-size: 18px;
                  height: 18px;
                  line-height: 22px;
                  align-items: center;
                  color: #3f3f46;
                  margin-left: 8px;
                `}
              >
                {t("result")}
              </span>
            </div>
          </div>
          <NoResults />
        </React.Fragment>
      )}
    </OverlayScrollbarsComponentDockerFix>
  );
}

type VirtualResultsProps<E> = ResulListProps<E> & {};
export function VirtualResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
}: VirtualResultsProps<E>) {
  const results = useInfiniteResults<E>(searchQuery, sort);
  const resultsFlat = results.data?.pages.flatMap((page) => page.result);
  const thereAreResults = Boolean(
    results.data?.pages[0].total && results.data.pages[0].total > 0,
  );
  return (
    <div
      className="openk9-virtual-results-container"
      css={css`
        display: flex;
        flex-direction: column;
        height: 100%;
      `}
    >
      {thereAreResults && (
        <ResultCount setSortResult={setSortResult} isMobile={isMobile}>
          {results.data?.pages[0].total}
        </ResultCount>
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
                setDetailMobile={setDetailMobile}
                isMobile={isMobile}
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
                className="openk9-virtual-results-footer"
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
  const { t } = useTranslation();
  return (
    <div
      className="openk9-no-results-container"
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
      <h3>{t("no-results-were-found")}</h3>
      <div>{t("try-with-another-query")}</div>
    </div>
  );
}

export function useInfiniteResults<E>(
  searchQuery: Array<SearchToken>,
  sort: SortField[],
) {
  const pageSize = 25;
  const client = useOpenK9Client();

  return useInfiniteQuery(
    ["results", searchQuery, sort] as const,
    async ({ queryKey: [, searchQuery, sort], pageParam = 0 }) => {
      return client.doSearch<E>({
        range: [pageParam * pageSize, pageParam * pageSize + pageSize],
        searchQuery,
        sort,
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
