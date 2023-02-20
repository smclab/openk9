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
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 20px;
          padding-bottom: 12px;
          display: flex;
        `}
      >
        <div
          css={css`
            display: flex;
          `}
        >
          <span>
            <FilterSvg />
          </span>
          <span
            css={css`
              margin-left: 10px;
              font-family: "Nunito Sans";
              font-style: normal;
              font-weight: 700;
              font-size: 20px;
              height: 18px;
              line-height: 22px;
              display: flex;
              align-items: center;
              color: #3f3f46;
            `}
          >
            Filtri
          </span>
        </div>
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
            width: 100%;
            margin-top: 8px;
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
  marginTop?: string;
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
        margin-top: ${marginTop};
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
