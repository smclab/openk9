import React from "react";
import { css } from "styled-components/macro";
import { SearchToken, SortField, SuggestionResult } from "./client";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import {
  UseInfiniteQueryResult,
  useInfiniteQuery,
  useQuery,
} from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { Logo } from "./Logo";
import { PlusSvg } from "../svgElement/PlusSvg";
import { FilterCategoryDynamicMemo } from "./FilterCategoryDynamic";
import { useTranslation } from "react-i18next";
import { mapSuggestionToSearchToken } from "./FilterCategory";
import { capitalize } from "lodash";
import { FilterHorizontalSvg } from "../svgElement/FilterHorizontalSvg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { TrashSvg } from "../svgElement/TrashSvg";
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  onConfigurationChangeExt: () => void | null;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
};
function FiltersHorizontal({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onConfigurationChangeExt,
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
      <OverlayScrollbarsComponent
        className="openk9-filter-overlay-scrollbars"
        style={{
          overflowY: "auto",
          position: "relative",
          height: "70%",
          borderRadius: "8px",
        }}
      >
        {suggestionCategories.data?.map((suggestion, index) => {
          const suggestions = useInfiniteSuggestions(
            lastSearchQueryWithResults,
            dynamicFilters,
            suggestion.id,
          );
          return CreateSuggestion(index, suggestion, suggestions);
        })}
      </OverlayScrollbarsComponent>
      <div
        css={css`
          @media (max-width: 480px) {
            margin-top: 5px;
            border: 0.5px solid rgba(128, 128, 128, 0.48);
          }
        `}
      ></div>
      <div
        className="openk9-filter-horizontal-container-submit"
        css={css`
          display: flex;
          justify-content: flex-end;
          @media (max-width: 480px) {
            padding-inline: 20px;
            flex-direction: column;
          }
        `}
      >
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="rimuovi filtri"
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
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: white;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: black;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              color: var(--red-tones-500, #c0272b);
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
              align-items: center;
            }
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: [] });
            onConfigurationChangeExt && onConfigurationChangeExt();
          }}
        >
          <div>Rimuovi filtri</div>
          <TrashSvg size="18px" />
        </button>
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="applica filtri"
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
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: #d6012e;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: white;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
            }
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: filterSelect });
            onConfigurationChangeExt && onConfigurationChangeExt();
          }}
        >
          <div>Applica i Filtri</div>
          <div>
            <AddFiltersSvg size="22px" />
          </div>
        </button>
      </div>
    </React.Fragment>
  );

  function CreateSuggestion(
    index: number,
    suggestion: { name: string; id: number; multiSelect: boolean },
    suggestions: UseInfiniteQueryResult<
      {
        result: SuggestionResult[];
        afterKey: string;
      },
      unknown
    >,
  ): JSX.Element {
    const [isOpen, setIsOpen] = React.useState(true);
    return (
      <React.Fragment key={index}>
        {index !== 0 && (
          <div
            style={{
              border: "0.5px solid #8080807a",
              marginTop: "0px",
              marginBottom: "20px",
              marginInline: "16px",
            }}
          ></div>
        )}
        <div
          css={css`
            margin-top: 20px;
            margin-bottom: 20px;
            @media (max-width: 480px) {
              display: flex;
              justify-content: space-between;
              margin-left: 16px;
              margin-bottom: 20px;
            }
          `}
        >
          <div
            className="openk9-filters-horizontal-category"
            css={css`
              color: #525258;
              font-weight: 600;
              ::first-letter {
                text-transform: capitalize;
              }
              @media (max-width: 480px) {
                color: var(--openk9-embeddable-tabs--primary-color);
                font-weight: 700;
              }
            `}
          >
            {suggestion.name}
          </div>
          <button
            aria-label={isOpen ? "chiudi i filtri" : "apri i filtri"}
            css={css`
              margin-right: 16px;
              background: inherit;
              border: none;
              @media (min-width: 480px) {
                display: none;
              }
            `}
            onClick={() => {
              setIsOpen(!isOpen);
            }}
          >
            <FontAwesomeIcon
              icon={isOpen ? faChevronDown : faChevronUp}
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                marginRight: "6px",
              }}
            />
          </button>
        </div>
        <GridContainer>
          {isOpen &&
            suggestions.data?.pages[0].result.map(
              (token: any, index: number) => {
                const asSearchToken = mapSuggestionToSearchToken(token, true);
                const checked = filterSelect.some((element) => {
                  return element.values && element.values[0] === token.value;
                });
                return (
                  <React.Fragment key={index}>
                    <div
                      className={`openk9-filter-horizontal-container-input-value ${
                        checked ? "check-container" : "not-check-container"
                      }`}
                      css={css`
                        overflow: hidden;
                        text-overflow: ellipsis;
                        color: ${checked ? "#d6012e" : "black"};
                        display: flex;
                        align-items: flex-start;
                        @media (max-width: 480px) {
                          margin-left: 15px;
                          margin-right: 15px;
                        }
                      `}
                    >
                      <input
                        id={
                          "filter-horizontal " + index + " " + suggestion.name
                        }
                        className={`custom-checkbox openk9-filter-horizontal-input  ${
                          checked ? "checked-checkbox" : "not-checked-checkbox"
                        }`}
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
                      <label
                        htmlFor={
                          "filter-horizontal " + index + " " + suggestion.name
                        }
                      >
                        {asSearchToken?.values &&
                          capitalize(asSearchToken?.values[0])}
                      </label>
                    </div>
                  </React.Fragment>
                );
              },
            )}
        </GridContainer>
      </React.Fragment>
    );
  }
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
      @media (max-width: 480px) {
        grid-template-columns: repeat(1, 1fr);
        overflow: auto;
        margin-bottom: 20px;
      }
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
        range: [0, 50],
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
