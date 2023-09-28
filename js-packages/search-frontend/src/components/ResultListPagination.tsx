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
import { result } from "lodash";
import "../components/Scrollbar.css";
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
  pagination: number;
  currentPage: number;
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
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
  setSortAfterKey,
  sortAfterKey,
  setTotalResult,
  numberOfResults,
  pagination,
  currentPage,
  setCurrentPage,
}: ResultsProps<E>) {
  const renderers = useRenderers();

  const changePage = (page: number) => {
    setCurrentPage(page);
  };
  if (!renderers) return null;
  switch (displayMode.type) {
    case "finite":
    case "virtual":
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
          currentPage={currentPage}
          changePage={changePage}
          elementForPage={pagination}
          setCurrentPage={setCurrentPage}
        />
      );
  }
}

export const ResultsPaginationMemo = React.memo(ResultsPagination);

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
          <span>
            <SortResultList setSortResult={setSortResult} />
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
};

type InfiniteResultsProps<E> = ResulListProps<E> & {
  setSortAfterKey: React.Dispatch<React.SetStateAction<string>>;
  sortAfterKey: string;
  numberOfResults: number;
  currentPage: number;
  elementForPage: number;
  changePage: (page: number) => void;
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
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
  currentPage,
  numberOfResults,
  changePage,
  elementForPage,
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

  const { t } = useTranslation();
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

  const overlayRef = React.useRef<HTMLDivElement | null>(null);

  function scrollToOverlay() {
    if (overlayRef.current) {
      overlayRef.current.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }
  }

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
      <div ref={overlayRef} style={{ overflowX: "hidden" }} className="scroll">
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
                <CreateButton
                  action={resetClick}
                  value="<<"
                  disable={viewButton.start === 0}
                  ariaL="bottone per mostrare le prime pagine"
                />
                <CreateButton
                  action={handlePrevClick}
                  value="<"
                  disable={viewButton.start === 0}
                  ariaL="bottone per mostrare le tre pagine precedenti"
                />
                {Array.from({ length: numberOfPage }).map(
                  (_, index) =>
                    ((index === viewButton.start && index === viewButton.end) ||
                      (index >= viewButton.start &&
                        index < viewButton.end)) && (
                      <CreateButton
                        action={() => {
                          results.fetchNextPage();
                          setCurrentPage(index);
                          scrollToOverlay();
                        }}
                        key={index}
                        value={"" + (index + 1)}
                        isCurrent={currentPage === index}
                        ariaL={
                          "clicca per vedere la " + (index + 1) + " pagina"
                        }
                      />
                    ),
                )}
                <CreateButton
                  action={handleNextClick}
                  value=">"
                  disable={viewButton.end >= numberOfPage}
                  ariaL="bottone per mostrare le tre pagine successive "
                />
                <CreateButton
                  action={() => resetEndClick(numberOfPage)}
                  value=">>"
                  disable={viewButton.end >= numberOfPage}
                  ariaL="bottone per mostrare le ultime tre pagine"
                />
              </div>
            </React.Fragment>
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
  console.log(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    elementForPage,
    result,
  );

  return useInfiniteQuery(
    ["results", searchQuery, sort, language, sortAfterKey] as const,
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

function CreateButton({
  value,
  action,
  ariaL,
  disable = false,
  isCurrent = false,
}: {
  value: string;
  action: () => void;
  ariaL: string;
  disable?: boolean;
  isCurrent?: boolean;
}) {
  return (
    <button
      className={`openk9-result-list-pagination-button ${
        isCurrent ? "select" : "not-select"
      }`}
      disabled={disable}
      aria-label={ariaL}
      onClick={action}
      css={css`
        padding: 4px 8px !important;
        background: ${isCurrent ? "#c83939" : "white"};
        border-radius: 50px;
        border: 1px solid #c83939;
        font-size: 15px;
        cursor: pointer;
        color: ${isCurrent ? "white" : "#c83939"};
      `}
    >
      {value}
    </button>
  );
}
