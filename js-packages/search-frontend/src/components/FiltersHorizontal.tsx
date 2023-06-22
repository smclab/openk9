import React from "react";
import { css } from "styled-components/macro";
import { SearchToken, SortField, SuggestionResult } from "./client";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useInfiniteQuery, useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { Logo } from "./Logo";
import { PlusSvg } from "../svgElement/PlusSvg";
import { FilterCategoryDynamicMemo } from "./FilterCategoryDynamic";
import { useTranslation } from "react-i18next";
import { mapSuggestionToSearchToken } from "./FilterCategory";
import { capitalize } from "lodash";

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
  const { data, isPreviousData } = useInfiniteResults(searchQuery, sort);
  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);

  const [filterSelect, setFilterSelect] =
    React.useState<Array<SearchToken>>(searchQuery);
  const handleCheckboxChange = (event: any, token: SearchToken) => {
    const isChecked = event.target.checked;
    const newFilter = {
      ...token,
      attributeName: [token.values],
    };

    if (isChecked) {
      setFilterSelect([...filterSelect, newFilter]);
    } else {
      setFilterSelect(
        filterSelect.filter(
          (t) =>
            t.values && newFilter.values && t.values[0] !== newFilter.values[0],
        ),
      );
    }
  };

  return (
    <React.Fragment>
      {suggestionCategories.data?.map((suggestion, index) => {
        const suggestions = useInfiniteSuggestions(
          lastSearchQueryWithResults,
          dynamicFilters,
          suggestion.id,
        );
        return (
          <React.Fragment key={index}>
            <div
              className="openk9-filters-horizontal-category"
              css={css`
                margin-top: 20px;
                margin-bottom: 20px;
                color: #525258;
                font-weight: 600;
                ::first-letter {
                  text-transform: capitalize;
                }
              `}
            >
              {suggestion.name}
            </div>
            <GridContainer>
              {suggestions.data?.pages[0].result.map((token, index) => {
                const asSearchToken = mapSuggestionToSearchToken(token, true);
                const checked = filterSelect.some((element) => {
                  return element.values && element.values[0] === token.value;
                });
                return (
                  <React.Fragment key={index}>
                    <div
                      className="openk9-filter-horizontal-container-input-value"
                      css={css`
                        overflow: hidden;
                        text-overflow: ellipsis;
                        color: ${checked ? "#d6012e" : "black"};
                        display: flex;
                        align-items: flex-start;
                      `}
                    >
                      <input
                        className="custom-checkbox openk9-filter-horizontal-input"
                        type="checkbox"
                        checked={checked}
                        onChange={(event) =>
                          handleCheckboxChange(event, asSearchToken)
                        }
                        css={css`
                          width: 14px;
                          appearance: none;
                          min-width: 15px;
                          min-height: 15px;
                          border-radius: 4px;
                          border: 2px solid #ccc;
                          background-color: ${checked
                            ? "var(--openk9-embeddable-search--secondary-active-color)"
                            : "#fff"};
                          background-size: 100%;
                          background-position: center;
                          background-repeat: no-repeat;
                          cursor: pointer;
                          margin-right: 10px;
                        `}
                      />

                      {asSearchToken?.values &&
                        capitalize(asSearchToken?.values[0])}
                    </div>
                  </React.Fragment>
                );
              })}
            </GridContainer>
          </React.Fragment>
        );
      })}
      <div
        className="openk9-filter-horizontal-container-submit"
        css={css`
          display: flex;
          justify-content: flex-end;
        `}
      >
        <button
          className="openk9-filter-horizontal-submit"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: filterSelect });
          }}
        >
          Applica i Filtri
        </button>
      </div>
    </React.Fragment>
  );
}

export const FiltersHorizontalMemo = React.memo(FiltersHorizontal);

const GridContainer = ({ children }: { children: any }) => (
  <div
    className="openk9-filters-horizontal-container"
    css={css`
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      grid-gap: 15px;
      grid-auto-rows: auto;
      margin-bottom: 50px;
    `}
  >
    {children}
  </div>
);

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

export function useInfiniteSuggestions(
  searchQueryParams: SearchToken[] | null,
  dynamicFilters: boolean,
  suggestionCategoryId: number,
) {
  const pageSize = 30;
  const client = useOpenK9Client();
  let searchQuery: SearchToken[] | null = [];
  if (searchQueryParams && searchQueryParams?.length > 0) {
    searchQueryParams.forEach((singleSearchQuery) => {
      if (dynamicFilters) searchQuery?.push(singleSearchQuery);
    });
  } else {
    searchQuery = searchQueryParams;
  }

  const suggestionCategories = useInfiniteQuery(
    ["suggestions", searchQuery, suggestionCategoryId] as const,
    async ({ queryKey: [_, searchQuery], pageParam }) => {
      if (!searchQuery) throw new Error();
      const result = await client.getSuggestions({
        searchQuery,
        afterKey: pageParam,
        order: "desc",
        suggestionCategoryId: suggestionCategoryId,
      });
      return {
        result: result.result,
        afterKey: result.afterKey,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (!lastPage.afterKey) return undefined;
        if (pages[pages.length - 1].result.length < pageSize) return undefined;
        return lastPage.afterKey;
      },
      suspense: true,
    },
  );

  return suggestionCategories;
}
