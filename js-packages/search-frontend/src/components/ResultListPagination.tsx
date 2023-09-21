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
}: ResultsProps<E>) {
  const renderers = useRenderers();
  const [currentPage, setCurrentPage] = React.useState<number>(1);
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

type FiniteResultsProps<E> = ResulListProps<E> & { sortAfterKey: string };
// export function FiniteResults<E>({
//   renderers,
//   searchQuery,
//   onDetail,
//   setDetailMobile,
//   sort,
//   setSortResult,
//   isMobile,
//   overChangeCard = false,
//   language,
//   sortAfterKey,
//   setTotalResult,
// }: FiniteResultsProps<E>) {
//   const results = useInfiniteResults<E>(
//     searchQuery,
//     sort,
//     language,
//     sortAfterKey,
//   );

//   setTotalResult(results.data?.pages[0].total ?? null);

//   return (
//     <div
//       className="openk9-finite-result-container"
//       style={{ height: "100%", overflowY: "auto", position: "relative" }}
//     >
//       {results.data?.pages[0].total && results.data.pages[0].total > 0 ? (
//         <div
//           className="openk9-finite-result"
//           css={css`
//             position: absolute;
//             width: 100%;
//           `}
//         >
//           <ResultCount setSortResult={setSortResult} isMobile={isMobile}>
//             {results.data?.pages[0].total}
//           </ResultCount>
//           {results.data?.pages[0].result.map((result, index) => {
//             return (
//               <ResultMemo<E>
//                 renderers={renderers}
//                 key={index}
//                 result={result}
//                 onDetail={onDetail}
//                 setDetailMobile={setDetailMobile}
//                 isMobile={isMobile}
//                 overChangeCard={overChangeCard}
//               />
//             );
//           })}
//         </div>
//       ) : (
//         <NoResults />
//       )}
//     </div>
//   );
// }

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
  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    currentPage,
    numberOfResults,
    elementForPage,
    currentPage,
  );

  const itemsPerPage = 3; // Numero di elementi per pagina
  const pagesToShow = 3; // Numero di pagine da mostrare per volta

  const [viewButton, setViewButton] = React.useState({
    start: 0,
    end: itemsPerPage,
  });

  const numberOfPage = Math.ceil(numberOfResults / itemsPerPage);

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
  const { t } = useTranslation();
  setTotalResult(results.data?.pages[0].total ?? null);
  const numberOfPageT = Math.ceil(numberOfResults / elementForPage);
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
                      overChangeCard={overChangeCard}
                    />
                  );
                })}
              </React.Fragment>
            );
          })}
          <React.Fragment>
            <button disabled={viewButton.start === 0} onClick={resetClick}>
              {"<<"}
            </button>
            <button disabled={viewButton.start === 0} onClick={handlePrevClick}>
              {"<"}
            </button>
            {Array.from({ length: numberOfPage }).map(
              (_, index) =>
                index >= viewButton.start &&
                index < viewButton.end && (
                  <button
                    onClick={() => {
                      results.fetchNextPage();
                      setCurrentPage(index);
                    }}
                    key={index}
                  >
                    {index + 1}
                  </button>
                ),
            )}

            <button
              disabled={viewButton.end >= numberOfPage}
              onClick={handleNextClick}
            >
              {">"}
            </button>
            <button
              disabled={viewButton.end >= numberOfPage}
              onClick={() => resetEndClick(numberOfPage)}
            >
              {">>"}
            </button>
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
    </OverlayScrollbarsComponentDockerFix>
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
  pagination: number,
  numberOfResults: number,
  elementForPage: number,
  currentPage: number,
) {
  const pageSize = elementForPage;
  const client = useOpenK9Client();

  const result = elementForPage * (currentPage + 1);

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
