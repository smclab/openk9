import React from "react";
import { css } from "styled-components/macro";
import { SearchToken, SortField } from "./client";
import { buttonStyle, FilterCategoryMemo } from "./FilterCategory";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { Logo } from "./Logo";
import { FilterCategoryDynamicMemo } from "./FilterCategoryDynamic";
import { useTranslation } from "react-i18next";

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
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const { t } = useTranslation();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(
    searchQuery,
    sort,
    language,
  );
  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);
  const [count, setCount] = React.useState(0);
  React.useEffect(() => {
    const count = searchQuery.filter(
      (search) => "goToSuggestion" in search,
    ).length;
    setCount(count);
  }, [searchQuery]);

  return (
    <OverlayScrollbarsComponent
      className="openk9-filter-overlay-scrollbars"
      style={{
        overflowY: "auto",
        position: "relative",
        height: "100%",
        borderRadius: "8px",
      }}
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
        {suggestionCategories.data?.length === 0 && (
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
        {suggestionCategories.data?.map((suggestionCategory) => {
          return dynamicFilters ? (
            <FilterCategoryDynamicMemo
              key={suggestionCategory.id}
              suggestionCategoryName={suggestionCategory.name}
              suggestionCategoryId={suggestionCategory.id}
              tokens={lastSearchQueryWithResults}
              onAdd={onAddFilterToken}
              onRemove={onRemoveFilterToken}
              multiSelect={suggestionCategory?.multiSelect}
              searchQuery={searchQuery}
              language={language}
            />
          ) : (
            <FilterCategoryMemo
              key={suggestionCategory.id}
              suggestionCategoryName={suggestionCategory.name}
              suggestionCategoryId={suggestionCategory.id}
              tokens={lastSearchQueryWithResults}
              onAdd={onAddFilterToken}
              onRemove={onRemoveFilterToken}
              multiSelect={suggestionCategory?.multiSelect}
              searchQuery={searchQuery}
              dynamicFilters={dynamicFilters}
              language={language}
            />
          );
        })}
      </div>
    </OverlayScrollbarsComponent>
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
};
export function CreateLabel({
  label,
  action,
  svgIcon,
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
      disabled={false}
      className="openk9-create-label-container-wrapper"
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: ${padding};
        gap: 4px;
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
