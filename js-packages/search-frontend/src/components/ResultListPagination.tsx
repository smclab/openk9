import React from "react";
import { useTranslation } from "react-i18next";
import { useInfiniteQuery } from "react-query";
import { css } from "styled-components";
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
import { Renderers, useRenderers } from "./useRenderers";
import { SelectionsAction, SelectionsState } from "./useSelections";
import {
  RangeContextProviderProps,
  setterConnection,
  useRange,
} from "./useRange";

export type ResultsDisplayMode =
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" };

type ResultsProps<E> = {
  state: SelectionsState;
  dispatch: React.Dispatch<SelectionsAction>;
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
  CustomNoResults?: React.ReactNode | null;
  backgroundSkeleton?: string | null | undefined;
};

function ResultsPagination<E>({
  state,
  dispatch,
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
  CustomNoResults,
  backgroundSkeleton,
}: ResultsProps<E>) {
  const renderers = useRenderers();

  if (!renderers) return null;

  const bootstrappedRef = React.useRef(false);
  React.useEffect(() => {
    if (bootstrappedRef.current) return;
    const size = pageSize ?? state.range[1];
    const page = initialPage ?? 0;
    dispatch({ type: "set-range", range: [page * size, size] });
    bootstrappedRef.current = true;
  }, [initialPage, pageSize, dispatch, state.range]);

  return (
    <React.Suspense
      fallback={<SkeletonResult background={backgroundSkeleton} />}
    >
      <InfiniteResults
        state={state}
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
        customNoResults={CustomNoResults}
      />
    </React.Suspense>
  );
}

export const ResultsPaginationMemo = React.memo(ResultsPagination);

type InfiniteResultsProps<E> = {
  state: SelectionsState;
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
  customNoResults?: React.ReactNode | null;
  dynamicFilters?: boolean;
};

export function InfiniteResults<E>({
  state,
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
  customNoResults,
  dynamicFilters = true,
}: InfiniteResultsProps<E>) {
  const [offset, elementForPage] = state.range;

  const {
    setNumberOfResults,
    setCorrection,
    overrideSearchWithCorrection,
    setOverrideSearchWithCorrection,
  } = useRange();
  const results = useInfiniteResults<E>(
    state,
    searchQuery,
    sort,
    language,
    sortAfterKey,
    elementForPage,
    offset,
    overrideSearchWithCorrection,
    setOverrideSearchWithCorrection,
    dynamicFilters,
  );

  React.useEffect(() => {
    const total = results?.data?.pages[0]?.total ?? 0;
    setTotalResult(total);
    setNumberOfResults(total);
    setCorrection(results?.data?.pages[0]?.autocorrection);
  }, [results?.data, setTotalResult, setNumberOfResults, setCorrection]);

  return (
    <div
      css={css`
        overflow-x: hidden;
      `}
      className="scroll"
    >
      {results?.data?.pages[0]?.total ?? 0 > 0 ? (
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
        <>{customNoResults ? customNoResults : <NoResults />}</>
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
  state: SelectionsState,
  searchQuery: Array<SearchToken>,
  sort: SortField[],
  language: string,
  sortAfterKey: string,
  elementForPage: number,
  offset: number,
  overrideSearchWithCorrection?: RangeContextProviderProps,
  setOverrideSearchWithCorrection?: setterConnection,
  dynamicFilters: boolean = true,
) {
  const client = useOpenK9Client();
  const suppressIntermediate = React.useMemo(
    () => state.text.trim().length > 0 && searchQuery.length === 0,
    [state.text, searchQuery],
  );
  if (!dynamicFilters) return undefined;
  const remappingSearchQuery =
    overrideSearchWithCorrection?.isAutocorrection === false
      ? searchQuery.map((token) =>
          token.tokenType === "TEXT" && token.search
            ? { ...token, overrideSearchWithCorrection: false }
            : token,
        )
      : searchQuery;

  const data = useInfiniteQuery(
    [
      "results",
      searchQuery,
      sort,
      language,
      sortAfterKey,
      offset,
      elementForPage,
      overrideSearchWithCorrection?.renderingCorrection,
    ] as const,
    async ({ queryKey }) => {
      const [
        ,
        qSearchQuery,
        qSort,
        qLanguage,
        qSortAfterKey,
        qOffset,
        qElementForPage,
      ] = queryKey;

      const range: [number, number] =
        qSortAfterKey === ""
          ? [qOffset, qElementForPage]
          : [0, qElementForPage];

      return client.doSearch<E>({
        range,
        language: qLanguage,
        searchQuery: remappingSearchQuery,
        sort: qSort,
        sortAfterKey: qSortAfterKey || "",
      });
    },
    {
      enabled: elementForPage > 0 && !suppressIntermediate,
      keepPreviousData: false,
      suspense: true,
      notifyOnChangeProps: ["isFetching"],
    },
  );
  if (overrideSearchWithCorrection?.isAutocorrection === false) {
    setOverrideSearchWithCorrection?.((ov) => ({
      ...ov,
      isAutocorrection: true,
    }));
  }
  return data;
}

export function SkeletonResult({
  background,
}: {
  background?: string | null | undefined;
}) {
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
            <CustomSkeleton width="80px" backgroundColor={background} />
            <CustomSkeleton width="150px" backgroundColor={background} />
          </div>
          <div
            css={css`
              padding: 16px 16px;
              overflow: hidden;
            `}
          >
            <ResultTitleTwo>
              <CustomSkeleton backgroundColor={background} />
            </ResultTitleTwo>
            <CustomSkeleton
              counter={3}
              width="100%"
              backgroundColor={background}
            />
          </div>
        </div>
      ))}
    </>
  );
}
