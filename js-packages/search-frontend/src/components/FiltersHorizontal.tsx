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
import { PlusSvg } from "../svgElement/PlusSvg";
import { useDebounce } from "./useDebounce";
import { FilterCategoryDynamicMemo } from "./FilterCategoryDynamic";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
};
function FiltersHorizontal({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onRemoveFilterToken,
  filtersSelect,
  sort,
  dynamicFilters,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const [hasMoreSuggestionsCategories, setHasMoreSuggestionsCategories] =
    React.useState(false);
  const [loadAll, setLoadAll] = React.useState(false);
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
        className="openk9-filters-container-internal"
        css={css`
          padding: 16px 16px 0px 0px;
          width: 100%;
          box-sizing: border-box;
          display: flex;
          flex-wrap: wrap;
          @media (min-width: 320px) and (max-width: 768px) {
            flex-direction: column;
            padding: 0px 0px 16px 16px;
          }
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
              isCollapsable={false}
              isUniqueLoadMore={true}
              loadAll={loadAll}
              setHasMoreSuggestionsCategories={setHasMoreSuggestionsCategories}
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
              isCollapsable={false}
              isUniqueLoadMore={true}
              loadAll={loadAll}
              setHasMoreSuggestionsCategories={setHasMoreSuggestionsCategories}
            />
          );
        })}
      </div>
      {hasMoreSuggestionsCategories && (
        <div style={{ textAlign: "center", width: "100%" }}>
          <CreateLabel
            label=" Load More"
            action={() => {
              setLoadAll(true);
              setHasMoreSuggestionsCategories(false);
            }}
            svgIcon={<PlusSvg size={12} />}
            marginOfSvg="5px"
            hasBorder={false}
          />
        </div>
      )}
    </OverlayScrollbarsComponent>
  );
}
export const FiltersHorizontalMemo = React.memo(FiltersHorizontal);

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
  colorLabel = "var(--openk9-embeddable-search--secondary-active-color)",
  align = "baseline",
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
        border: ${hasBorder
          ? "1px solid  var(--openk9-embeddable-search--secondary-active-color);"
          : ""};
        border-radius: 20px;
        margin-left: 10px;
        margin-top: ${marginTop};
        cursor: pointer;
        white-space: nowrap;
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
