import React from "react";
import { css } from "styled-components/macro";
import { SearchToken, SortField } from "./client";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { Logo } from "./Logo";
import {
  FilterCategoryDynamicMemo,
  WhoIsDynamic,
} from "./FilterCategoryDynamic";
import { useTranslation } from "react-i18next";
import { SelectionsAction } from "./useSelections";
import CustomSkeleton from "./Skeleton";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
  preFilters?: React.ReactNode;
  language: string;
  sortAfterKey: string;
  isCollapsable?: boolean;
  numberItems?: number | null | undefined;
  numberOfResults: number;
  isDynamicElement: WhoIsDynamic[];
  noResultMessage?: string | null | undefined;
  selectionsDispatch?: React.Dispatch<SelectionsAction>;
  isActiveSkeleton: boolean;
  skeletonCategoryCustom: React.ReactNode | null;
  memoryResults: boolean;
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onRemoveFilterToken,
  filtersSelect,
  sort,
  dynamicFilters,
  preFilters,
  language,
  sortAfterKey,
  isCollapsable = true,
  numberItems,
  numberOfResults,
  isDynamicElement,
  noResultMessage,
  isActiveSkeleton,
  selectionsDispatch,
  skeletonCategoryCustom,
  memoryResults,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const { t } = useTranslation();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
    memoryResults,
  );
  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);
  const [count, setCount] = React.useState(0);
  React.useEffect(() => {
    let accumulatore = 0;
    searchQuery
      .filter((search) => "goToSuggestion" in search)
      .forEach((filter) => {
        if (filter && filter.values)
          accumulatore = accumulatore + filter.values?.length || 0;
      });
    setCount(accumulatore);
  }, [searchQuery]);

  return (
    <div
      className="openk9-filter-overlay-scrollbars"
      css={css`
        overflow-y: auto;
        position: relative;
        height: 100%;
        border-radius: 8px;
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
          height:5px;
          
        }
        
        ::-webkit-scrollbar-thumb:hover {
          background: rgba(0, 0, 0, .55);
          height:5px;
        }
      `}
    >
      <div
        className="openk9-filter-list-container-title box-title"
        css={css`
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 20px;
          padding-bottom: 13px;
          display: flex;
          @media (max-width: 480px) {
            display: none;
          }
        `}
      >
        <div
          className="openk9-filter-list-container-internal-title "
          css={css`
            display: flex;
            gap: 5px;
          `}
        >
          <span>
            <FilterSvg />
          </span>
          <span className="openk9-filters-list-title title">
            <h2
              css={css`
                font-style: normal;
                font-weight: 700;
                font-size: 18px;
                height: 18px;
                line-height: 22px;
                display: flex;
                align-items: center;
                color: #3f3f46;
                margin: 0;
              `}
            >
              {t("filters")}
            </h2>
          </span>
        </div>
      </div>
      <div
        className="openk9-number-filter-list-container-wrapper"
        css={css`
          padding: 8px 24px;
          @media (max-width: 480px) {
            display: none;
          }
        `}
      >
        <div
          className="openk9-number-filters-list-container more-detail-content"
          css={css`
            display: flex;
            padding: 8px 8px;
            justify-content: space-between;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 8px;
            align-items: center;
            margin-left: -8px;
            width: 100%;
            margin-top: 8px;
          `}
        >
          <div>
            <span
              className="openk9-number-filters-list-number-of-results"
              css={css`
                color: var(--openk9-embeddable-search--active-color);
                font-weight: 700;
              `}
            >
              {count}{" "}
            </span>
            <span>{t("active-filters")}</span>
          </div>
          <div className="openk9-active-container-number-filters-list-number-of-results">
            <CreateLabel
              label={t("remove-filters")}
              action={() => {
                onConfigurationChange({ filterTokens: [] });
                if (selectionsDispatch)
                  selectionsDispatch({ type: "reset-filters" });
              }}
              svgIconRight={
                <DeleteLogo
                  heightParam={8}
                  widthParam={8}
                  colorSvg={"#C0272B"}
                />
              }
              marginRigthOfSvg={"6px"}
            />
          </div>
        </div>
      </div>

      <div
        className="openk9-filters-container-internal"
        css={css`
          position: absolute;
          padding: 16px 16px 0px 0px;
          width: calc(100% - 32px);
        `}
      >
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
              margin-left: 30px;
            `}
          >
            <Logo size={100} />
            <h4>No Filters </h4>
          </div>
        )}
        {preFilters}
        {suggestionCategories.data?.map((suggestionCategory, index) => (
          <React.Suspense
            fallback={
              skeletonCategoryCustom
                ? isActiveSkeleton && skeletonCategoryCustom
                : isActiveSkeleton && <SkeletonCategory />
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
              isCollapsable={isCollapsable}
              numberItems={numberItems}
              isDynamicElement={isDynamicElement}
              noResultMessage={noResultMessage}
            />
          </React.Suspense>
        ))}
      </div>
    </div>
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
  const { t } = useTranslation();
  return (
    <div>
      <div className="openk9-skeleton">
        <div
          className="openk9-result-list-container-title box-title openk9-skeleton-container-result"
          css={css`
            padding: 0px 16px;
            padding-top: 20.7px;
            padding-bottom: 12.7px;
            display: flex;
            margin-bottom: 8px;
          `}
        >
          <span>
            <FilterSvg />
          </span>
          <span
            className="openk9-result-list-title title openk9-skeleton-container-title"
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
            <CustomSkeleton width="80px" />
          </span>
        </div>
      </div>
      <div
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
