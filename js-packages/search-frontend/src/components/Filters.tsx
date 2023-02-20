import React from "react";
import { css } from "styled-components/macro";
import { SearchToken } from "./client";
import { buttonStyle, FilterCategoryMemo } from "./FilterCategory";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onRemoveFilterToken,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(searchQuery);
  React.useEffect(() => {
    if (!isPreviousData && data?.pages[0].result.length) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [data?.pages, isPreviousData, searchQuery]);
  return (
    <OverlayScrollbarsComponent
      style={{
        overflowY: "auto",
        position: "relative",
        height: "100%",
      }}
    >
      <div
        css={css`
          display: flex;
          flex-direction: row;
          align-items: center;
          padding: 8px 24px;
          gap: 16px;
          background: #fafafa;
          border-radius: 8px 8px 0px 0px;
          height: 48px;
        `}
      >
        <span>
          <FilterSvg />
        </span>
        <span>Filtri</span>
      </div>
      <div
        css={css`
          padding: 8px 24px;
        `}
      >
        <div
          css={css`
            display: flex;
            padding: 8px 8px;
            justify-content: space-between;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 8px;
            align-items: flex-start;
            margin-left: -8px;
          `}
        >
          <div>
            <span
              css={css`
                color: var(--openk9-embeddable-search--active-color);
                font-weight: 700;
              `}
            >
              2
            </span>
            <span> filtri applicati</span>
          </div>
          <div>
            <CreateLabel
              label="Rimuovi filtri"
              action={() => {
                onConfigurationChange({ filterTokens: [] });
              }}
            />
          </div>
        </div>
      </div>

      <div
        css={css`
          position: absolute;
          padding: 16px 16px 0px 0px;
          width: calc(100% - 32px);
        `}
      >
        {suggestionCategories.data?.map((suggestionCategory) => {
          return (
            <FilterCategoryMemo
              key={suggestionCategory.id}
              suggestionCategoryName={suggestionCategory.name}
              suggestionCategoryId={suggestionCategory.id}
              tokens={lastSearchQueryWithResults}
              onAdd={onAddFilterToken}
              onRemove={onRemoveFilterToken}
              multiSelect={suggestionCategory?.multiSelect}
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
};
export function CreateLabel({
  label,
  action,
  svgIcon,
  sizeHeight = "15px",
  sizeFont = "12px",
  margBottom = "13px",
  marginOfSvg = "0px",
}: createLabel) {
  return (
    <div
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 4px 8px;
        gap: 4px;
        height: ${sizeHeight};
        background: #ffffff;
        border: 1px solid
          var(--openk9-embeddable-search--secondary-active-color);
        border-radius: 20px;
        margin-left: 10px;
        cursor: pointer;
      `}
      onClick={action}
    >
      <p
        css={css`
          color: var(--openk9-embeddable-search--secondary-active-color);
          margin-bottom: ${margBottom};
          font-size: ${sizeFont};
          font-weight: 700;
        `}
      >
        {svgIcon}
        <span
          css={css`
            margin-left: ${marginOfSvg};
          `}
        >
          {label}
        </span>
      </p>
    </div>
  );
}
