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
  const { t } = useTranslation();
  const suggestionCategories = useSuggestionCategories();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
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
  const suggestions = useInfiniteSuggestions(
    lastSearchQueryWithResults,
    dynamicFilters,
  );
  const [selectedCheckboxes, setSelectedCheckboxes] =
    React.useState<Array<SearchToken>>(searchQuery);
  const handleCheckboxChange = (event: any, token: SearchToken) => {
    const isChecked = event.target.checked;
    const aggiunto = {
      ...token,
      attributeName: [token.values],
    };

    if (isChecked) {
      setSelectedCheckboxes([...selectedCheckboxes, aggiunto]);
    } else {
      setSelectedCheckboxes(
        selectedCheckboxes.filter(
          (t) =>
            t.values && aggiunto.values && t.values[0] !== aggiunto.values[0],
        ),
      );
    }
  };

  return (
    <React.Fragment>
      {suggestionCategories.data?.map((suggestion, index) => {
        return (
          <React.Fragment key={index}>
            <div style={{ marginBottom: "20px" }}>{suggestion.name}</div>
            <GridContainer>
              {suggestions.data?.pages[0].result.map((token, index) => {
                const asSearchToken = mapSuggestionToSearchToken(token, true);

                return (
                  <React.Fragment key={index}>
                    {asSearchToken.suggestionCategoryId === suggestion.id ? (
                      <div
                        style={{
                          width: "207px",
                          height: "87px",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={selectedCheckboxes.some((element) => {
                            return (
                              element.values &&
                              element.values[0] === token.value
                            );
                          })}
                          onChange={(event) =>
                            handleCheckboxChange(event, asSearchToken)
                          }
                        />

                        {asSearchToken?.values}
                      </div>
                    ) : null}
                  </React.Fragment>
                );
              })}
            </GridContainer>
          </React.Fragment>
        );
      })}
      <div style={{ display: "flex" }}>
        <button
          onClick={() => {
            onConfigurationChange({ filterTokens: selectedCheckboxes });
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
    style={{
      display: "grid",
      gridTemplateColumns: "repeat(4, 1fr)",
      gridGap: "5px",
    }}
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

export function useInfiniteSuggestions(
  searchQueryParams: SearchToken[] | null,
  dynamicFilters: boolean,
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
    ["suggestions", searchQuery] as const,
    async ({ queryKey: [_, searchQuery], pageParam }) => {
      if (!searchQuery) throw new Error();
      const result = await client.getSuggestions({
        searchQuery,
        afterKey: pageParam,
        order: "desc",
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
