import React from "react";
import { useTranslation } from "react-i18next";
import { useInfiniteQuery } from "react-query";
import { Virtuoso } from "react-virtuoso";
import { css } from "styled-components/macro";
import { TemplatesProps } from "../embeddable/entry";
import { CustomVirtualScrollbar } from "./CustomScrollbar";
import { Logo } from "./Logo";
import { ResultMemo } from "./Result";
import { Options, setSortResultsType } from "./SortResults";
import {
  GenericResultItem,
  SearchToken,
  SortField,
  useOpenK9Client,
} from "./client";
import { useRange } from "./useRange";
import { Renderers, useRenderers } from "./useRenderers";

export type ResultsDisplayMode =
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" };

type ResultsProps<E> = {
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E>): void;
  displayMode: ResultsDisplayMode;
  sort: SortField[];
  setSortResult: setSortResultsType;
  setDetailMobile(result: GenericResultItem<E>): void;
  isMobile: boolean;
  overChangeCard?: boolean;
  memoryResults: boolean;
  language: string;
  setSortAfterKey: React.Dispatch<React.SetStateAction<string>>;
  sortAfterKey: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  numberOfResults: number;
  counterIsVisible?: boolean;
  selectOptions: Options;
  viewButton: boolean;
  templateCustom: TemplatesProps | null;
  NoResultsCustom?: any | undefined | null;
  setViewButtonDetail: React.Dispatch<React.SetStateAction<boolean>>;
  setSelectedSort: setSortResultsType;
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
  counterIsVisible = false,
  selectOptions,
  setIdPreview,
  memoryResults,
  viewButton,
  NoResultsCustom,
  setSelectedSort,
  setViewButtonDetail,
  templateCustom,
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
          counterIsVisible={counterIsVisible}
          setIdPreview={setIdPreview}
          selectOptions={selectOptions}
          memoryResults={memoryResults}
          viewButton={viewButton}
          setViewButtonDetail={setViewButtonDetail}
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
          setIdPreview={setIdPreview}
          memoryResults={memoryResults}
          viewButton={viewButton}
          setViewButtonDetail={setViewButtonDetail}
          noResultsCustom={NoResultsCustom}
          setSelectedSort={setSelectedSort}
          templateCustom={templateCustom}
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
          setIdPreview={setIdPreview}
          memoryResults={memoryResults}
          viewButton={viewButton}
          setViewButtonDetail={setViewButtonDetail}
        />
      );
  }
}

export const ResultsMemo = React.memo(Results);

type ResulListProps<E> = {
  renderers: Renderers;
  searchQuery: Array<SearchToken>;
  onDetail(result: GenericResultItem<E> | null): void;
  setDetailMobile(result: GenericResultItem<E> | null): void;
  sort: SortField[];
  setSortResult: setSortResultsType;
  isMobile: boolean;
  overChangeCard?: boolean;
  language: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  numberOfResults: number;
  setIdPreview: React.Dispatch<React.SetStateAction<string>> | undefined | null;
};

type FiniteResultsProps<E> = ResulListProps<E> & {
  sortAfterKey: string;
  counterIsVisible: boolean;
  selectOptions: Options;
  language: string;
  setSortResult: setSortResultsType;
  memoryResults: boolean;
  viewButton: boolean;
  setViewButtonDetail: React.Dispatch<React.SetStateAction<boolean>>;
};
export function FiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  isMobile,
  overChangeCard = false,
  language,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  setIdPreview,
  memoryResults,
  viewButton,
  setViewButtonDetail,
}: FiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
    memoryResults,
  );
  React.useEffect(() => {
    if (results.data && results.data.pages[0].total) {
      setTotalResult(results.data.pages[0].total);
    }
  }, [results.data, setTotalResult]);

  return (
    <div
      className="openk9-finite-result-container"
      style={{ height: "100%", overflowY: "auto", overflowX: "hidden" }}
    >
      {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          className="openk9-finite-result"
          css={css`
            width: 100%;
          `}
        >
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
                viewButton={viewButton}
                setViewButtonDetail={setViewButtonDetail}
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
  setIdPreview: React.Dispatch<React.SetStateAction<string>> | undefined | null;
  language: string;
  setViewButtonDetail: React.Dispatch<React.SetStateAction<boolean>>;
  memoryResults: boolean;
  viewButton: boolean;
  noResultsCustom: React.ReactNode;
  setSelectedSort: setSortResultsType;
  templateCustom?: TemplatesProps | null;
};
export function InfiniteResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  isMobile,
  overChangeCard = false,
  language,
  setSortAfterKey,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  setIdPreview,
  memoryResults,
  noResultsCustom,
  viewButton,
  templateCustom,
  setViewButtonDetail,
}: InfiniteResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
    memoryResults,
  );
  const { setNumberOfResults, setCorrection } = useRange();
  React.useEffect(() => {
    if (results.data && results.data.pages[0].total) {
      setTotalResult(results.data.pages[0].total);
      setNumberOfResults(results.data.pages[0].total);
    } else {
      setNumberOfResults(0);
    }

    setCorrection(results?.data?.pages[0]?.autocorrection);
  }, [results.data, setTotalResult, setNumberOfResults, setCorrection]);
  React.useEffect(() => {
    const sak =
      results.data?.pages[results.data.pages.length - 1].result[
        results.data?.pages[0].result.length - 1
      ]?.sortAfterKey;
    if (sort && sak && sortAfterKey !== sak) {
      setSortAfterKey(sak);
    }
  }, [results]);
  const { t } = useTranslation();
  return (
    <div
      className="openk9-infinite-results-overlay-scrollbars"
      css={css`
        height: 100%;
        overflow-y: auto;
        overflow-x: hidden;
        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }

        ::-webkit-scrollbar-track {
          background-color: transparent;
        }

        ::-webkit-scrollbar-thumb {
          background: rgba(0, 0, 0, 0.4);
          border-radius: 10px;
          height: 5px;
        }

        ::-webkit-scrollbar-thumb:hover {
          background: rgba(0, 0, 0, 0.55);
          height: 5px;
        }
      `}
    >
      {results?.data?.pages[0].total && results.data.pages[0].total > 0 ? (
        <div
          className="openk9-infinite-results-container-wrapper"
          css={css`
            padding-bottom: 16px;
          `}
        >
          <ul
            role="list"
            css={css`
              list-style-type: none;
              padding: 0;
              margin: 0;
              gap: 10px;
              display: flex;
              flex-direction: column;
            `}
          >
            {results.data?.pages.map((page, pageIndex) => {
              return (
                <React.Fragment key={`page-${pageIndex}`}>
                  {page.result.map((result, resultIndex) => {
                    return (
                      <li
                        role="listitem"
                        aria-labelledby="resultid"
                        key={resultIndex}
                        css={css`
                          background: white;
                          border: 2px solid transparent;
                          border-radius: 8px;
                          :hover {
                            border: 2px solid gray;
                          }
                        `}
                      >
                        <ResultMemo<E>
                          renderers={renderers}
                          key={resultIndex}
                          result={result}
                          onDetail={onDetail}
                          setDetailMobile={setDetailMobile}
                          isMobile={isMobile}
                          overChangeCard={overChangeCard}
                          setIdPreview={setIdPreview}
                          viewButton={viewButton}
                          setViewButtonDetail={setViewButtonDetail}
                          templateCustom={templateCustom}
                        />
                      </li>
                    );
                  })}
                </React.Fragment>
              );
            })}
          </ul>
          {results.hasNextPage && (
            <div
              className="openk9-container-embeddable-result-button"
              css={css`
                padding-inline: 16px;
                margin-top: 10px;
                padding-bottom: 16px;
              `}
            >
              <button
                className="openk9-embeddable-result-list-button-load-more"
                onClick={() => {
                  if (!results.isFetching) {
                    results.fetchNextPage();
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
          {noResultsCustom ? noResultsCustom : <NoResults />}
        </React.Fragment>
      )}
    </div>
  );
}

type VirtualResultsProps<E> = ResulListProps<E> & {
  sortAfterKey: string;
  language: string;
  memoryResults: boolean;
  viewButton: boolean;
  setViewButtonDetail: React.Dispatch<React.SetStateAction<boolean>>;
};
export function VirtualResults<E>({
  renderers,
  searchQuery,
  onDetail,
  setDetailMobile,
  sort,
  isMobile,
  overChangeCard = false,
  language,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  memoryResults,
  viewButton,
  setViewButtonDetail,
}: VirtualResultsProps<E>) {
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
    memoryResults,
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
                viewButton={viewButton}
                setViewButtonDetail={setViewButtonDetail}
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
        padding: 30px;
        background: white;
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
  memoryResults: boolean,
) {
  const pageSize = numberOfResults;
  const client = useOpenK9Client();
  const { searchQueryData, sortData } = recoverySearchQueryAndSort(searchQuery);
  const { setRange } = useRange();

  return useInfiniteQuery(
    ["results", searchQueryData, sortData, language] as const,
    async ({ queryKey: [, searchQuery, sort], pageParam = 0 }) => {
      const RangePage: [number, number] = !(sortData && sortAfterKey)
        ? [pageParam * pageSize, pageSize]
        : [0, pageSize];

      setRange(RangePage);

      return client.doSearch<E>({
        range: RangePage,
        language,
        searchQuery: searchQueryData,
        sort: sortData && [sortData],
        sortAfterKey: (sortData && pageParam > 0 && sortAfterKey) || "",
      });
    },
    {
      keepPreviousData: true,
      cacheTime: memoryResults ? Infinity : 0,
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

export function recoverySearchQueryAndSort(searchQuery: SearchToken[]) {
  const searchQueryData = searchQuery.filter(
    (info) => !info.hasOwnProperty("isSort"),
  );
  const sortData = searchQuery.find((info) => info.hasOwnProperty("isSort"));
  const sort =
    sortData && sortData.hasOwnProperty("sort")
      ? (sortData as any).sort
      : undefined;

  return {
    searchQueryData,
    sortData: sort ? sort : undefined,
  };
}
