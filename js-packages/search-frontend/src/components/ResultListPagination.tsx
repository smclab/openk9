import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import React from "react";
import { useTranslation } from "react-i18next";
import { useInfiniteQuery } from "react-query";
import { css } from "styled-components/macro";
import { ResultTitleTwo } from "../renderer-components";
import { Logo } from "./Logo";
import { ResultMemo } from "./Result";
import CustomSkeleton from "./Skeleton";
import { setSortResultsType } from "./SortResults";
import {
  GenericResultItem,
  SearchToken,
  SortField,
  useOpenK9Client,
} from "./client";
import styled from "styled-components";
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
  language: string;
  sortAfterKey: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  numberOfResults: number;
  pagination: number;
  currentPage: number;
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  callback?: () => void;
};
function ResultsPagination<E>({
  displayMode,
  onDetail,
  searchQuery,
  sort,
  setSortResult,
  isMobile,
  setDetailMobile,
  overChangeCard = false,
  language,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  pagination,
  currentPage,
  setCurrentPage,
  callback,
}: ResultsProps<E>) {
  const renderers = useRenderers();

  if (!renderers) return null;
  switch (displayMode.type) {
    case "finite":
    case "virtual":
    case "infinite":
      return (
        <React.Suspense>
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
            sortAfterKey={sortAfterKey}
            numberOfResults={numberOfResults}
            currentPage={currentPage}
            elementForPage={pagination}
            setCurrentPage={setCurrentPage}
            callback={callback}
          />
        </React.Suspense>
      );
  }
}

export const ResultsPaginationMemo = React.memo(ResultsPagination);

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
};

type InfiniteResultsProps<E> = ResulListProps<E> & {
  sortAfterKey: string;
  numberOfResults: number;
  currentPage: number;
  elementForPage: number;
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  callback?: () => void;
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
  sortAfterKey,
  setTotalResult,
  currentPage,
  numberOfResults,
  elementForPage,
  callback,
  setCurrentPage,
}: InfiniteResultsProps<E>) {
  const result = currentPage * elementForPage;
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    elementForPage,
    result,
  );

  const itemsPerPage = 3; // Numero di elementi per pagina
  const pagesToShow = 3; // Numero di pagine da mostrare per volta
  const partialResult = numberOfResults / elementForPage;
  const numberOfPage = Math.ceil(partialResult);
  const [viewButton, setViewButton] = React.useState({
    start: currentPage,
    end: itemsPerPage,
  });

  React.useEffect(() => {
    if (currentPage === 0) {
      resetClick();
    }
  }, [currentPage]);

  const handlePrevClick = () => {
    setViewButton((view) => ({
      ...view,
      start: Math.max(view.start - pagesToShow, 0),
      end: Math.max(view.end - pagesToShow, itemsPerPage),
    }));
  };

  const resetClick = () => {
    setViewButton((view) => ({
      ...view,
      start: 0,
      end: pagesToShow,
    }));
  };

  const resetEndClick = (paginationMax: number) => {
    setViewButton((view) => ({
      ...view,
      start: paginationMax - pagesToShow,
      end: paginationMax,
    }));
  };

  const handleNextClick = () => {
    setViewButton((view) => ({
      ...view,
      start: Math.min(view.start + pagesToShow, numberOfPage - pagesToShow),
      end: Math.min(view.end + pagesToShow, numberOfPage),
    }));
  };

  setTotalResult(results.data?.pages[0].total ?? null);

  return (
    <React.Fragment>
      <div style={{ overflowX: "hidden" }} className="scroll">
        {results?.data?.pages[0].total && results.data.pages[0].total > 0 ? (
          <div
            className="openk9-infinite-results-container-wrapper"
            css={css`
              padding-bottom: 16px;
              ::-webkit-scrollbar {
                width: 10px;
              }

              /* Track */
              ::-webkit-scrollbar-track {
                box-shadow: inset 0 0 5px grey;
                border-radius: 10px;
              }

              /* Handle */
              ::-webkit-scrollbar-thumb {
                background: gray;
                border-radius: 10px;
                height: 5px;
              }

              /* Handle on hover */
              ::-webkit-scrollbar-thumb:hover {
                background: black;
                height: 5px;
              }
            `}
          >
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
                      />
                    );
                  })}
                </React.Fragment>
              );
            })}
            <React.Fragment>
              <div
                className="openk9-container-button-for-pagination"
                css={css`
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  margin-top: 10px;
                  gap: 10px;
                `}
              >
                <PaginationsButton
                  onClick={resetClick}
                  disabled={viewButton.start === 0}
                  aria-label="bottone per mostrare le prime pagine"
                >
                  {"<<"}
                </PaginationsButton>
                <PaginationsButton
                  onClick={handlePrevClick}
                  disabled={viewButton.start === 0}
                >
                  {"<"}
                </PaginationsButton>
                {Array.from({ length: numberOfPage }).map(
                  (_, index) =>
                    ((index === viewButton.start && index === viewButton.end) ||
                      (index >= viewButton.start &&
                        index < viewButton.end)) && (
                      <PaginationsButton
                        key={index}
                        onClick={() => {
                          results.fetchNextPage();
                          setCurrentPage(index);
                          callback && callback();
                        }}
                        isActive={currentPage === index}
                        aria-label={
                          "clicca per vedere la " + (index + 1) + " pagina"
                        }
                      >
                        {"" + (index + 1)}
                      </PaginationsButton>
                    ),
                )}
                <PaginationsButton
                  onClick={handleNextClick}
                  disabled={viewButton.end >= numberOfPage}
                  aria-label="bottone per mostrare le tre pagine successive"
                >
                  {">"}
                </PaginationsButton>
                <PaginationsButton
                  onClick={() => resetEndClick(numberOfPage)}
                  disabled={viewButton.end >= numberOfPage}
                  aria-label="bottone per mostrare le ultime tre pagine"
                >
                  {">>"}
                </PaginationsButton>
              </div>
            </React.Fragment>
          </div>
        ) : (
          <React.Fragment>
            <NoResults />
          </React.Fragment>
        )}
      </div>
    </React.Fragment>
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

function useInfiniteResults<E>(
  searchQuery: Array<SearchToken>,
  sort: SortField[],
  language: string,
  sortAfterKey: string,
  elementForPage: number,
  result: number,
) {
  const pageSize = elementForPage;
  const client = useOpenK9Client();

  return useInfiniteQuery(
    ["results", searchQuery, sort, language, sortAfterKey, result] as const,
    async ({ queryKey: [, searchQuery, sort], pageParam = 0 }) => {
      const RangePage: [number, number] =
        sortAfterKey === "" ? [result, pageSize] : [0, pageSize];
      return client.doSearch<E>({
        range: RangePage,
        language,
        searchQuery,
        sort,
        sortAfterKey: sortAfterKey || "",
      });
    },
    {
      keepPreviousData: false,
      suspense: true,
      notifyOnChangeProps: ["isFetching"],
    },
  );
}

export function SkeletonResult() {
  return (
    <React.Fragment>
      {new Array(3).fill(null).map((_, index) => (
        <div
          className="openk9-embeddable-search--result-container openk9-skeleton-container--result-container"
          key={index}
        >
          <div
            className=""
            css={css`
              display: flex;
              justify-content: space-between;
              padding: 8px 16px;
            `}
          >
            <CustomSkeleton width="80px" />
            <CustomSkeleton width="150px" />
          </div>
          <div
            css={css`
              padding: 16px 16px;
              overflow: hidden;
            `}
          >
            <ResultTitleTwo>
              <CustomSkeleton />
            </ResultTitleTwo>
            <CustomSkeleton counter={3} width="100%" />
          </div>
        </div>
      ))}
    </React.Fragment>
  );
}

const PaginationsButton = styled.button<{
  isActive?: boolean;
}>`
  ${({ isActive = false }) => `
        padding: 4px 8px !important;
        background: ${isActive ? "#c83939" : "white"};
        border-radius: 50px;
        border: 1px solid #c83939;
        font-size: 15px;
        cursor: pointer;
        color: ${isActive ? "white" : "#c83939"};
  `}
`;
