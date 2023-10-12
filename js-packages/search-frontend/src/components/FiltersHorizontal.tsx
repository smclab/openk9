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
import {
  FilterCategoryDynamicMemo,
  WhoIsDynamic,
  createSuggestion,
  mergeAndSortObjects,
} from "./FilterCategoryDynamic";
import { useTranslation } from "react-i18next";
import { mapSuggestionToSearchToken } from "./FilterCategory";
import { capitalize } from "lodash";
import { FilterHorizontalSvg } from "../svgElement/FilterHorizontalSvg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { TrashSvg } from "../svgElement/TrashSvg";
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";
import { Language } from "@mui/icons-material";
import { ArrowDownSvg } from "../svgElement/ArrowDownSvg";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  onConfigurationChangeExt: () => void | null;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
  language: string;
  sortAfterKey: string;
  numberOfResults: number;
  isDynamicElement: WhoIsDynamic[];
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
  language,
  sortAfterKey,
  numberOfResults,
  isDynamicElement,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
  );
  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);

  const [filterSelect, setFilterSelect] =
    React.useState<Array<SearchToken>>(searchQuery);
  const [haveValue, setHaveValue] = React.useState(false);

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
  const { t } = useTranslation();
  return (
    <React.Fragment>
      <OverlayScrollbarsComponent
        className="openk9-filter-overlay-scrollbars"
        style={{
          overflowY: "auto",
          position: "relative",
          borderRadius: "8px",
        }}
      >
        {suggestionCategories.data?.map((suggestion, index) => {
          const suggestions = useInfiniteSuggestions(
            isDynamicElement,
            lastSearchQueryWithResults,
            dynamicFilters,
            suggestion.id,
            language,
          );
          return CreateSuggestion(
            index,
            suggestion,
            suggestions,
            haveValue,
            setHaveValue,
          );
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
      {!haveValue && <NoFilters />}
      {haveValue && (
        <div
          className="openk9-filter-horizontal-container-submit"
          css={css`
            display: flex;
            justify-content: flex-end;
            @media (max-width: 480px) {
              padding-inline: 20px;
              flex-direction: column;
              gap: 15px;
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
            <div>{t("remove-filters")}</div>
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
            <div>{t("add-filters") || "Add Filters"}</div>
            <div>
              <AddFiltersSvg size="22px" />
            </div>
          </button>
        </div>
      )}
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
    haveValue: boolean,
    setHaveValue: any,
  ): JSX.Element {
    const [isOpen, setIsOpen] = React.useState(true);
    const resultValue = suggestions.data?.pages || [];

    const filters = mergeAndSortObjects(
      resultValue,
      searchQuery,
      suggestion.id,
    );
    if (filters.length === 0) return <div></div>;
    React.useEffect(() => {
      setHaveValue(true);
    }, []);
    return (
      <React.Fragment key={index}>
        {filters.length !== 0 && (
          <React.Fragment>
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
                    color:
                      "var(--openk9-embeddable-search--secondary-text-color)",
                    marginRight: "6px",
                  }}
                />
              </button>
            </div>
            <GridContainer>
              {isOpen &&
                filters.map((token: any, index: number) => {
                  const asSearchToken = mapSuggestionToSearchToken(token, true);
                  const checked = filterSelect.some((element) => {
                    return (
                      element.values &&
                      element.values[0] === token.value &&
                      "goToSuggestion" in element &&
                      element.suggestionCategoryId ===
                        token.suggestionCategoryId
                    );
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
                            checked
                              ? "checked-checkbox"
                              : "not-checked-checkbox"
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
                })}
            </GridContainer>
            {suggestions.hasNextPage && (
              <div
                className="openk9-container-load-more"
                css={css`
                  text-align: center;
                  width: 100%;
                  display: flex;
                  margin-left: 12px;
                  margin-top: 10px;
                  justify-content: center;
                  @media (max-width: 480px) {
                    margin-top: 15px;
                  }
                `}
              >
                <button
                  className="openk9-load-more-button horizontal-filter-load-more"
                  aria-label={t("load-more-filter") || "load more filters"}
                  css={css`
                    background: inherit;
                    color: var(--openk9-embeddable-search--primary-color);
                    font-size: 14px;
                    font-style: normal;
                    font-weight: 700;
                    line-height: normal;
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    cursor: pointer;
                    padding: 8px 16px;
                    border: 1px solid
                      var(--openk9-embeddable-search--primary-color);
                    border-radius: 20px;
                  `}
                  onClick={() => {
                    suggestions.fetchNextPage();
                  }}
                >
                  {t("load-more") || "Load More"}
                  <ArrowDownSvg size="18px" />
                </button>
              </div>
            )}
          </React.Fragment>
        )}
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
  isDynamicElement: WhoIsDynamic[],
  searchQueryParams: SearchToken[] | null,
  dynamicFilters: boolean,
  suggestionCategoryId: number,
  language: string,
) {
  const pageSize = 8;
  const client = useOpenK9Client();

  const searchQuery = false
    ? searchQueryParams
    : createSuggestion(searchQueryParams, isDynamicElement);

  const suggestionCategories = useInfiniteQuery(
    ["suggestions", searchQuery, suggestionCategoryId, false] as const,
    async ({ queryKey: [_, searchQuery, suggestionCategoryId], pageParam }) => {
      if (!searchQuery) throw new Error();
      const result = await client.getSuggestions({
        searchQuery,
        range: [0, pageSize + 1],
        afterKey: pageParam,
        suggestionCategoryId: suggestionCategoryId,
        order: "desc",
        language: language,
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

function NoFilters() {
  const { t } = useTranslation();

  return (
    <div>
      <div
        className="openk9-filter-category-no-results-is-open"
        css={css`
          color: var(--openk9-embeddable-search--secondary-text-color);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100%;
          margin-top: 18px;
          margin-left: 10px;
        `}
      >
        <Logo size={100} />
        <h4>{t("no-filters")} </h4>
      </div>
    </div>
  );
}
