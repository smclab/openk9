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
  language: string;
  sortAfterKey: string;
  setTotalResult: React.Dispatch<React.SetStateAction<number | null>>;
  pageSize?: number;
  initialPage?: number;
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
  pageSize = 10,
  initialPage = 0,
  callback,
}: ResultsProps<E>) {
  const renderers = useRenderers();
  if (!renderers) return null;

  const { setRange, setActuallyPage } = useRange();
  const bootstrappedRef = React.useRef(false);

  React.useEffect(() => {
    if (bootstrappedRef.current) return;
    const size = pageSize;
    const page = initialPage;
    setActuallyPage(page);
    setRange([page * size, size]);
    bootstrappedRef.current = true;
  }, [initialPage, pageSize, setActuallyPage, setRange]);

  return (
    <React.Suspense fallback={<SkeletonResult />}>
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
        callback={callback}
      />
    </React.Suspense>
  );
}

export const ResultsPaginationMemo = React.memo(ResultsPagination);

type InfiniteResultsProps<E> = {
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
  sortAfterKey: string;
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
  callback,
}: InfiniteResultsProps<E>) {
  const { range, setNumberOfResults } = useRange();
  const [offset, elementForPage] = range;

  const results = useInfiniteResults<E>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    elementForPage,
    offset,
  );

  React.useEffect(() => {
    const total = results.data?.pages[0]?.total ?? 0;
    setTotalResult(total);
    setNumberOfResults(total);
  }, [results.data, setTotalResult, setNumberOfResults]);

  return (
    <div style={{ overflowX: "hidden" }} className="scroll">
      {results.data?.pages[0]?.total ?? 0 > 0 ? (
        <div
          className="openk9-infinite-results-container-wrapper"
          css={css`
            padding-bottom: 16px;
            ::-webkit-scrollbar {
              width: 10px;
            }
            ::-webkit-scrollbar-track {
              box-shadow: inset 0 0 5px grey;
              border-radius: 10px;
            }
            ::-webkit-scrollbar-thumb {
              background: gray;
              border-radius: 10px;
              height: 5px;
            }
            ::-webkit-scrollbar-thumb:hover {
              background: black;
              height: 5px;
            }
          `}
        >
          {results?.data?.pages.map((page, pageIndex) => (
            <React.Fragment key={pageIndex}>
              {page.result.map((result, resultIndex) => (
                <ResultMemo<E>
                  renderers={renderers}
                  key={resultIndex}
                  result={result}
                  onDetail={onDetail}
                  setDetailMobile={setDetailMobile}
                  isMobile={isMobile}
                  overChangeCard={overChangeCard}
                />
              ))}
            </React.Fragment>
          ))}
        </div>
      ) : (
        <NoResults />
      )}
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
  elementForPage: number,
  offset: number,
) {
  const client = useOpenK9Client();

  return useInfiniteQuery(
    [
      "results",
      searchQuery,
      sort,
      language,
      sortAfterKey,
      offset,
      elementForPage,
    ] as const,
    async ({ queryKey: [, searchQuery, sort] }) => {
      const range: [number, number] =
        sortAfterKey === "" ? [offset, elementForPage] : [0, elementForPage];
      return client.doSearch<E>({
        range,
        language,
        searchQuery,
        sort,
        sortAfterKey: sortAfterKey || "",
      });
    },
    {
      enabled: elementForPage > 0,
      keepPreviousData: false,
      suspense: true,
      notifyOnChangeProps: ["isFetching"],
    },
  );
}

export function SkeletonResult() {
  return (
    <>
      {Array.from({ length: 3 }).map((_, index) => (
        <div
          className="openk9-embeddable-search--result-container openk9-skeleton-container--result-container"
          key={index}
        >
          <div
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
    </>
  );
}
