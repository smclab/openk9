import React from "react";
import { useTranslation } from "react-i18next";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { IconsCustom } from "../embeddable/entry";
import { SearchToken, SortField, useOpenK9Client } from "./client";
import {
  FilterCategoryDynamicMemo,
  WhoIsDynamic,
} from "./FilterCategoryDynamic";
import { Logo } from "./Logo";
import CustomSkeleton from "./Skeleton";
import { useInfiniteResults } from "./ResultListPagination";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  sort: SortField[];
  preFilters?: React.ReactNode;
  language: string;
  sortAfterKey: string;
  numberItems?: number | null | undefined;
  numberOfResults: number;
  isDynamicElement: WhoIsDynamic[];
  noResultMessage?: string | null | undefined;
  isActiveSkeleton: boolean;
  skeletonCategoryCustom: React.ReactNode | null;
  memoryResults: boolean;
  placeholder?: string | null | undefined;
  iconCustom: IconsCustom;
  haveSearch?: boolean | null | undefined;
  state: any;
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onRemoveFilterToken,
  sort,
  preFilters,
  language,
  sortAfterKey,
  numberItems,
  numberOfResults,
  isDynamicElement,
  noResultMessage,
  isActiveSkeleton,
  skeletonCategoryCustom,
  memoryResults,
  placeholder,
  iconCustom,
  haveSearch = true,
  state,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();

  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);

  const [offset, elementForPage] = state.range;

  const { isPreviousData } = useInfiniteResults<any>(
    state,
    searchQuery,
    sort,
    language,
    sortAfterKey,
    elementForPage,
    offset,
  );

  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);

  return (
    <>
      {suggestionCategories.data?.length === 0 && !preFilters && (
        <div
          className="openk9-filters-container-internal-no-filters"
          css={css`
            color: var(--openk9-embeddable-search--secondary-text-color);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 50vh;
          `}
        >
          <Logo size={100} />
          <h4>No Filters </h4>
        </div>
      )}
      {preFilters}
      {suggestionCategories.data?.map((suggestionCategory, index) => (
        <React.Suspense
          key={index}
          fallback={
            skeletonCategoryCustom && isActiveSkeleton && skeletonCategoryCustom
          }
        >
          <FilterCategoryDynamicMemo
            key={suggestionCategory.id}
            suggestionCategoryName={translateSuggesionCategoryName({
              names: suggestionCategory.translationMap,
              language: language,
              defaultValue: suggestionCategory.name,
            })}
            suggestionCategoryId={suggestionCategory.id}
            tokens={lastSearchQueryWithResults}
            onAdd={onAddFilterToken}
            onRemove={onRemoveFilterToken}
            multiSelect={suggestionCategory?.multiSelect}
            searchQuery={searchQuery}
            language={language}
            numberItems={numberItems}
            isDynamicElement={isDynamicElement}
            noResultMessage={noResultMessage}
            placeholder={placeholder}
            iconCustom={iconCustom}
            haveSearch={haveSearch}
          />
        </React.Suspense>
      ))}
    </>
  );
}
export const FiltersMemo = React.memo(Filters);

function useSuggestionCategories() {
  const client = useOpenK9Client();
  return useQuery(
    ["suggestion-categories"],
    async ({ queryKey }) => {
      const result = await client.getSuggestionCategories();
      return result;
    },
    {
      suspense: true,
      keepPreviousData: true,
    },
  );
}

type createLabel = {
  label: string;
  idValue?: string;
  action?(): void;
  svgIcon?: React.ReactNode;
  sizeHeight?: string;
  sizeFont?: string;
  margBottom?: string;
  marginOfSvg?: string;
  marginTop?: string;
  hasBorder?: boolean;
  svgIconRight?: React.ReactNode;
  marginRigthOfSvg?: string;
  colorLabel?: string;
  align?: string;
  disabled?: boolean;
  widthLabel?: string;
  ariaLabel?: string;
  padding?: string;
  fontWeightLabel?: string;
  gap?: string;
  classN?: string;
};
export function CreateLabel({
  label,
  idValue,
  action,
  svgIcon,
  classN = "",
  gap = "4px",
  sizeHeight = "15px",
  sizeFont = "12px",
  marginOfSvg = "0px",
  marginRigthOfSvg = "0px",
  hasBorder = true,
  svgIconRight,
  padding = "12px 8px",
  fontWeightLabel = "500",
  disabled = false,
  widthLabel,
  ariaLabel = "",
  colorLabel = "var(--openk9-embeddable-search--secondary-active-color)",
}: createLabel) {
  return (
    <button
      aria-label={ariaLabel || ""}
      id={idValue || ""}
      disabled={false}
      className={`openk9-create-label-container-wrapper ${classN}`}
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: ${padding};
        gap: ${gap};
        height: ${sizeHeight};
        background: #ffffff;
        border: ${hasBorder
          ? "1px solid  var(--openk9-embeddable-search--secondary-active-color);"
          : ""};
        border-radius: 20px;
        white-space: nowrap;
        cursor: pointer;
        color: ${colorLabel};
        font-size: ${sizeFont};
        width: ${widthLabel};
      `}
      onClick={action}
    >
      {svgIcon}
      <span
        className="openk9-create-label-container-internal-space"
        css={css`
          margin-left: ${marginOfSvg};
          margin-right: ${marginRigthOfSvg};
          font-weight: ${fontWeightLabel};
        `}
      >
        {label}
      </span>
      {svgIconRight}
    </button>
  );
}

function mergeAndSortArrays(
  sortedArray: string[],
  unsortedArray: string[],
): string[] {
  const mergedArray = [...sortedArray];
  for (const element of unsortedArray) {
    if (!sortedArray.includes(element)) {
      mergedArray.push(element);
    }
  }
  mergedArray.sort((a, b) => a.localeCompare(b));
  return mergedArray;
}

function translateSuggesionCategoryName({
  names,
  language,
  defaultValue,
}: {
  names: { [key: string]: string };
  language: string;
  defaultValue: string;
}): string {
  const desiredKey = "name." + language;
  if (names && names.hasOwnProperty(desiredKey)) {
    return names[desiredKey];
  }
  return defaultValue;
}

export function SkeletonFilters() {
  return (
    <div
      className="container-openk9-skeleton"
      css={css`
        width: 100%;
      `}
    >
      <div
        className="container-openk9-skeleton-wrapper"
        css={css`
          padding-inline: 16px;
        `}
      >
        <CustomSkeleton />
      </div>
      <div
        className="openk9-filters-container-internal"
        css={css`
          padding-inline: 16px;
          padding-top: 16px;
        `}
      >
        <div
          css={css`
            display: flex;
            flex-direction: column;
            gap: 10px;
          `}
        >
          <CustomSkeleton />

          {new Array(5).fill(null).map((_, index) => (
            <div
              key={index}
              css={css`
                display: flex;
                gap: 10px;
                width: 100%;
              `}
            >
              <CustomSkeleton width="20px" />
              <CustomSkeleton containerMax />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function SkeletonCategory() {
  const { t } = useTranslation();
  return (
    <div
      className="openk9-filters-container-internal"
      css={css`
        padding-inline: 16px;
        padding-top: 16px;
      `}
    >
      <div
        css={css`
          display: flex;
          flex-direction: column;
          gap: 10px;
        `}
      >
        <CustomSkeleton />

        {new Array(5).fill(null).map((_, index) => (
          <div
            key={index}
            css={css`
              display: flex;
              gap: 10px;
              width: 100%;
            `}
          >
            <CustomSkeleton width="20px" />
            <CustomSkeleton containerMax />
          </div>
        ))}
      </div>
    </div>
  );
}
