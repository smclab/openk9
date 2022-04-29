import React from "react";
import { css } from "styled-components/macro";
import { SearchToken } from "@openk9/rest-api";
import { FilterCategoryMemo } from "./FilterCategory";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onRemoveFilterToken,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
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
          position: absolute;
          width: calc(100% - 32px);
          padding: 16px 16px 0px 16px;
        `}
      >
        {suggestionCategories.data?.map((suggestionCategory) => {
          return (
            <FilterCategoryMemo
              key={suggestionCategory.suggestionCategoryId}
              suggestionCategoryName={suggestionCategory.name}
              suggestionCategoryId={suggestionCategory.suggestionCategoryId}
              tokens={searchQuery}
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
  return useQuery(["suggestion-categories"], async ({ queryKey }) => {
    const result = await client.getSuggestionCategories();
    return result;
  }, {
    suspense: true
  });
}
