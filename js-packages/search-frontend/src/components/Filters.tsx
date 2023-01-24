import React from "react";
import { css } from "styled-components/macro";
import { SearchToken } from "./client";
import { buttonStyle, FilterCategoryMemo } from "./FilterCategory";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";

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
          margin-bottom: 16px;
          border-bottom: 1px solid var(--openk9-embeddable-search--border-color);
          display: flex;
        `}
      >
        <strong
          css={css`
            margin-left: 16px;
            font-family: "Helvetica";
            font-style: normal;
            font-weight: 700;
            font-size: 16px;
            line-height: 44px;
            display: flex;
            align-items: center;
          `}
        >
          Filters
        </strong>
        <span
          css={css`
            margin-left: auto;
            margin-right: 20px;
            font-family: "Helvetica";
            font-style: normal;
            font-weight: 400;
            font-size: 14px;
            line-height: 44px;
            display: flex;
            align-items: center;
            color: #000000;
            cursor: pointer;
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: [] });
          }}
        >
          Cancel
          <span
            css={css`
              opacity: 0.5;
              margin-left: 5px;
            `}
          >
            X
          </span>
        </span>
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
