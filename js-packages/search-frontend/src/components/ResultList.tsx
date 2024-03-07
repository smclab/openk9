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
import { SortResultListMemo } from "./SortResultList";
import { useTranslation } from "react-i18next";
import { result } from "lodash";
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
  overChangeCard?: boolean;
  language: string;
  setSortAfterKey: React.Dispatch<React.SetStateAction<string>>;
  sortAfterKey: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  numberOfResults: number;
  label?: string | null | undefined;
  counterIsVisible?: boolean;
  setIdPreview?:
    | React.Dispatch<React.SetStateAction<string>>
    | undefined
    | null;
};
function Results<E>({
  displayMode,
  onDetail,
  searchQuery,
  sort,
  setSortResult,
  isMobile,
  setDetailMobile,
  overChangeCard = false,
  language,
  setSortAfterKey,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  label,
  counterIsVisible = false,
  setIdPreview,
}: ResultsProps<E>) {
  const renderers = useRenderers();

  if (!renderers) return null;

  switch (displayMode.type) {
    case "finite":
      return (
        <FiniteResults
          setTotalResult={setTotalResult}
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
          overChangeCard={overChangeCard}
          language={language}
          sortAfterKey={sortAfterKey}
          numberOfResults={numberOfResults}
          label={label}
          counterIsVisible={counterIsVisible}
          setIdPreview={setIdPreview}
        />
      );
    case "infinite":
      return (
        <InfiniteResults
          setTotalResult={setTotalResult}
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
          overChangeCard={overChangeCard}
          language={language}
          setSortAfterKey={setSortAfterKey}
          sortAfterKey={sortAfterKey}
          numberOfResults={numberOfResults}
          label={label}
          counterIsVisible={counterIsVisible}
          setIdPreview={setIdPreview}
        />
      );
    case "virtual":
      return (
        <VirtualResults
          setTotalResult={setTotalResult}
          renderers={renderers}
          searchQuery={searchQuery}
          onDetail={onDetail}
          setDetailMobile={setDetailMobile}
          sort={sort}
          setSortResult={setSortResult}
          isMobile={isMobile}
          overChangeCard={overChangeCard}
          language={language}
          sortAfterKey={sortAfterKey}
          numberOfResults={numberOfResults}
          label={label}
          counterIsVisible={counterIsVisible}
          setIdPreview={setIdPreview}
        />
      );
  }
}

export const ResultsMemo = React.memo(Results);

type ResultCountProps = {
  children: number | undefined;
  setSortResult: (sortResultNew: SortField) => void;
  isMobile: boolean;
  addClass?: string;
  label?: string | undefined | null;
  results: any;
  counterIsVisible: boolean;
  language?: string;
};

function ResultCount({
  children,
  setSortResult,
  isMobile,
  addClass,
  label,
  language,
  results,
  counterIsVisible,
}: ResultCountProps) {
  const client = useOpenK9Client();
  const { t } = useTranslation();

  return (
    <React.Fragment>
      <div
        className={`openk9-result-list-container-title box-title ${
          addClass || ""
        }`}
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
          <span className="openk9-result-list-title title">
            {label || t("result")}
          </span>{" "}
          {counterIsVisible && (
            <span
              className="openk9-result-list-counter-number"
              css={css`
                color: var(--openk9-embeddable-search--active-color);
                font-weight: 700;
              `}
            >
              {results.data?.pages[0].total || 0}
            </span>
          )}
        </h2>
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
          <span>
            <SortResultListMemo
              setSortResult={setSortResult}
              relevance={t("relevance") || "relevance"}
              language={language}
            />
          </span>
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
  overChangeCard?: boolean;
  language: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  numberOfResults: number;
  setIdPreview: React.Dispatch<React.SetStateAction<string>> | undefined | null;
};

type FiniteResultsProps<E> = ResulListProps<E> & {
  sortAfterKey: string;
  label: string | null | undefined;
  counterIsVisible: boolean;
};
export function FiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
  overChangeCard = false,
  language,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  label,
  counterIsVisible,
  setIdPreview,
}: FiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
  );

  setTotalResult(results.data?.pages[0].total ?? null);

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
          <ResultCount
            counterIsVisible={counterIsVisible}
            setSortResult={setSortResult}
            isMobile={isMobile}
            label={label}
            results={results}
            language={language}
          >
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
                overChangeCard={overChangeCard}
                setIdPreview={setIdPreview}
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

type InfiniteResultsProps<E> = ResulListProps<E> & {
  setSortAfterKey: React.Dispatch<React.SetStateAction<string>>;
  sortAfterKey: string;
  label?: string | undefined | null;
  counterIsVisible: boolean;
  setIdPreview: React.Dispatch<React.SetStateAction<string>> | undefined | null;
};
export function InfiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
  overChangeCard = false,
  language,
  setSortAfterKey,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  label,
  counterIsVisible,
  setIdPreview,
}: InfiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
  );

  const { t } = useTranslation();
  setTotalResult(results.data?.pages[0].total ?? null);
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
          <ResultCount
            counterIsVisible={counterIsVisible}
            setSortResult={setSortResult}
            isMobile={isMobile}
            label={label}
            results={results}
            language={language}
          >
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
                      overChangeCard={overChangeCard}
                      setIdPreview={setIdPreview}
                    />
                  );
                })}
              </React.Fragment>
            );
          })}
          {results.hasNextPage && (
            <div
              className="openk9-container-embeddable-result-button"
              css={css`
                position: absolute;
                left: 16px;
                right: 16px;
                margin-top: 10px;
                padding-bottom: 16px;
              `}
            >
              <button
                className="openk9-embeddable-result-list-button-load-more"
                aria-label={t("load-more-results") || "load more results"}
                onClick={() => {
                  if (!results.isFetching) {
                    results.fetchNextPage();
                    setSortAfterKey(
                      results.data?.pages[0].result[result.length - 1]
                        .sortAfterKey || "",
                    );
                  }
                }}
                css={css`
                  border: 1px solid
                    var(--openk9-embeddable-search--secondary-active-color);
                  padding: 8px 16px;
                  background: inherit;
                  width: 100%;
                  padding-inline: 16px;
                  border-radius: 20px;
                  color: var(
                    --openk9-embeddable-search--secondary-active-color
                  );
                  cursor: pointer;
                `}
              >
                {results.isFetching
                  ? t("loading-more-results")
                  : t("load-more")}
              </button>
            </div>
          )}
        </div>
      ) : (
        <React.Fragment>
          <ResultCount
            setSortResult={setSortResult}
            isMobile={isMobile}
            addClass="openk9-container-no-results"
            label={label}
            results={result}
            counterIsVisible={counterIsVisible}
          >
            {results.data?.pages[0].total}
          </ResultCount>
          <NoResults />
        </React.Fragment>
      )}
    </OverlayScrollbarsComponentDockerFix>
  );
}

type VirtualResultsProps<E> = ResulListProps<E> & {
  sortAfterKey: string;
  label?: string | undefined | null;
  counterIsVisible: boolean;
};
export function VirtualResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  setSortResult,
  isMobile,
  overChangeCard = false,
  language,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  label,
  counterIsVisible,
}: VirtualResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
  );
  const resultsFlat = results.data?.pages.flatMap((page) => page.result);
  const thereAreResults = Boolean(
    results.data?.pages[0].total && results.data.pages[0].total > 0,
  );
  setTotalResult(results.data?.pages[0].total ?? null);
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
        <ResultCount
          setSortResult={setSortResult}
          isMobile={isMobile}
          label={label}
          results={results}
          counterIsVisible={counterIsVisible}
          language={language}
        >
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
                overChangeCard={overChangeCard}
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
  language: string,
  sortAfterKey: string,
  numberOfResults: number,
) {
  const pageSize = numberOfResults;
  const client = useOpenK9Client();
  return useInfiniteQuery(
    ["results", searchQuery, sort, language, sortAfterKey] as const,
    async ({ queryKey: [, searchQuery, sort], pageParam = 0 }) => {
      const RangePage: [number, number] =
        sortAfterKey === "" ? [pageParam * pageSize, pageSize] : [0, pageSize];
      return client.doSearch<E>({
        range: RangePage,
        language,
        searchQuery,
        sort,
        sortAfterKey: sortAfterKey || "",
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
