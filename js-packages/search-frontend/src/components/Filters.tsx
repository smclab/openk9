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
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onRemoveFilterToken,
  filtersSelect,
  sort,
  dynamicFilters,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const { t } = useTranslation();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(searchQuery, sort);
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
        `}
      >
        <div
          className="openk9-filter-list-container-internal-title "
          css={css`
            display: flex;
          `}
        >
          <span>
            <FilterSvg />
          </span>
          <span
            className="openk9-filters-list-title title"
            css={css`
              margin-left: 10px;
              font-style: normal;
              font-weight: 700;
              font-size: 18px;
              height: 18px;
              line-height: 22px;
              display: flex;
              align-items: center;
              color: #3f3f46;
            `}
          >
            {t("filters")}
          </span>
        </div>
      </div>
      <div
        className="openk9-number-filter-list-container-wrapper"
        css={css`
          padding: 8px 24px;
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
};
export function CreateLabel({
  label,
  action,
  svgIcon,
  sizeHeight = "15px",
  sizeFont = "12px",
  margBottom = "13px",
  marginOfSvg = "0px",
  marginTop = "0px",
  marginRigthOfSvg = "0px",
  hasBorder = true,
  svgIconRight,
  disabled = false,
  widthLabel,
  colorLabel = "var(--openk9-embeddable-search--secondary-active-color)",
  align = "baseline",
  ariaLabel,
}: createLabel) {
  return (
    <div
      className="openk9-create-label-container-wrapper"
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 4px 8px;
        gap: 4px;
        height: ${sizeHeight};
        background: #ffffff;
        border: ${
          hasBorder
            ? "1px solid  var(--openk9-embeddable-search--secondary-active-color);"
            : ""
        };
        border-radius: 20px;
        margin-left: 10px;
        margin-top: ${marginTop};
        cursor: pointer;
        color: ${colorLabel};
        font-size: ${sizeFont};
        width: ${widthLabel};
        white-space: nowrap;
        aria-label=${ariaLabel || ""};
      `}
      onClick={action}
    >
      <div
        className="openk9-create-label-container-text-style"
        css={css`
          color: ${colorLabel};
          margin-bottom: ${margBottom};
          font-size: ${sizeFont};
          font-weight: 700;
          display: block;
          margin-block-start: 1em;
          margin-block-end: 1em;
          margin-inline-start: 0px;
          margin-inline-end: 0px;
        `}
      >
        <div
          className="openk9-create-label-container-internal-create"
          css={css`
            display: flex;
            align-items: ${align};
          `}
        >
          {svgIcon}
          <span
            className="openk9-create-label-container-internal-space"
            css={css`
              margin-left: ${marginOfSvg};
              margin-right: ${marginRigthOfSvg};
            `}
          >
            {label}
          </span>
          {svgIconRight}
        </div>
      </div>
    </div>
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
