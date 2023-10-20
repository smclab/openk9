import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { SearchToken, SuggestionResult } from "./client";
import { InfiniteData, useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";
import { useOpenK9Client } from "./client";
import { CreateLabel } from "./Filters";
import { PlusSvg } from "../svgElement/PlusSvg";
import { NoFilter, mapSuggestionToSearchToken } from "./FilterCategory";
import { useTranslation } from "react-i18next";
import { capitalize } from "lodash";
import { ArrowDownSvg } from "../svgElement/ArrowDownSvg";

type FilterCategoryDynamicallyProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
  multiSelect: boolean;
  searchQuery: SearchToken[];
  isCollapsable?: boolean;
  isUniqueLoadMore?: boolean;
  loadAll?: boolean;
  language: string;
  isDynamicElement: WhoIsDynamic[];
  numberItems?: number | null | undefined;
  setHasMoreSuggestionsCategories?: React.Dispatch<
    React.SetStateAction<boolean>
  >;
};
function FilterCategoryDynamic({
  suggestionCategoryId,
  suggestionCategoryName,
  tokens,
  onAdd,
  onRemove,
  multiSelect,
  searchQuery,
  isCollapsable = true,
  isUniqueLoadMore = false,
  loadAll = false,
  language,
  numberItems,
  isDynamicElement,
  setHasMoreSuggestionsCategories = undefined,
}: FilterCategoryDynamicallyProps) {
  const [text, setText] = React.useState("");
  const suggestions = useInfiniteSuggestions(
    isDynamicElement,
    tokens,
    suggestionCategoryId,
    useDebounce(text, 600),
    loadAll,
    language,
    numberItems,
  );

  const { t } = useTranslation();
  const resultValue = suggestions.data?.pages || [];

  const filters = mergeAndSortObjects(
    resultValue,
    searchQuery,
    suggestionCategoryId,
  );
  React.useEffect(() => {
    if (
      setHasMoreSuggestionsCategories &&
      suggestions &&
      suggestions.hasNextPage
    )
      setHasMoreSuggestionsCategories(suggestions.hasNextPage);
  }, []);

  const [isOpen, setIsOpen] = React.useState(true);
  const [singleSelect, setSingleselect] = React.useState<
    SearchToken | undefined
  >();
  const show = Boolean(text || (filters.length ?? 0) > 0);

  if (!show)
    return (
      <React.Fragment>
        <NoFilter
          isOpen={isOpen}
          setIsOpen={setIsOpen}
          suggestionCategoryName={suggestionCategoryName}
        />
      </React.Fragment>
    );

  return (
    <div
      className="openk9-filter-category-container"
      css={css`
        margin-bottom: 16px;
        ${isUniqueLoadMore ? "width: 50%" : null}
        @media  (max-width: 768px) {
          width: 100%;
          ${isUniqueLoadMore ? "height: 50%" : null}
        }
      `}
    >
      <div>
        <div
          className="openk9-filter-category-title"
          css={css`
            user-select: none;
            margin-left: 16px;
            display: flex;
            align-items: center;
            width: 100% !important;
          `}
          onClick={() => (isCollapsable ? setIsOpen(!isOpen) : null)}
        >
          <div
            css={css`
              flex-grow: 1;
              :first-letter {
                text-transform: uppercase;
              }
            `}
          >
            <strong>{suggestionCategoryName}</strong>
          </div>
          {isCollapsable && (
            <button
              aria-label={
                t("openk9-collapsable-filter") || "openk9 collapsable filter"
              }
              style={{ background: "inherit", border: "none" }}
            >
              <FontAwesomeIcon
                icon={isOpen ? faChevronDown : faChevronUp}
                style={{
                  color:
                    "var(--openk9-embeddable-search--secondary-text-color)",
                  marginRight: "8px",
                }}
              />
            </button>
          )}
        </div>
      </div>
      {isOpen && (
        <React.Fragment>
          {!isUniqueLoadMore && (
            <div
              className="openk9-filter-category-container-search"
              css={css`
                display: flex;
                align-items: center;
                margin-bottom: 10px;
              `}
            >
              <FontAwesomeIcon
                icon={faSearch}
                style={{
                  color:
                    "var(--openk9-embeddable-search--secondary-text-color)",
                  marginLeft: "25px",
                  opacity: "0.3",
                  zIndex: "3",
                  marginTop: "16px",
                  height: "15px",
                }}
              />
              <label
                htmlFor={"search-category-" + suggestionCategoryId}
                className="visually-hidden"
              >
                Search Filter
              </label>
              <input
                className="openk9-filter-category-search"
                type="text"
                id={"search-category-" + suggestionCategoryId}
                value={text}
                placeholder={t("search-filters") || "Search filters..."}
                onChange={(event) => setText(event.currentTarget.value)}
                css={css`
                  margin-top: 17px;
                  flex-grow: 1;
                  text-indent: 25px;
                  margin-left: -25px;
                  margin-right: -9px;
                  padding: 8px 16px 8px 8px;
                  border-radius: 4px;
                  border: 1px solid
                    var(--openk9-embeddable-search--border-color);
                  border-radius: 20px;
                  background: #fafafa;
                  :focus {
                    border: 1px solid
                      var(--openk9-embeddable-search--active-color);
                    outline: none;
                  }
                  ::placeholder {
                    font-style: normal;
                    font-weight: 400;
                    font-size: 15px;
                  }
                `}
              />
            </div>
          )}
          <div
            className="openk9-filter-form-check-container"
            css={css`
              display: flex;
              flex-direction: ${isUniqueLoadMore ? "row" : "column"};
              gap: ${isUniqueLoadMore ? "0" : "5px"};
              flex-wrap: ${isUniqueLoadMore ? "wrap" : "initial"};
              padding-left: 13px;
            `}
          >
            {filters.map((suggestion, index) => {
              const asSearchToken = mapSuggestionToSearchToken(
                suggestion,
                false,
              );
              const isChecked = tokens.some((searchToken) => {
                if (
                  haveSomeValue(
                    searchToken.values || [],
                    asSearchToken.values || [""],
                  ) &&
                  searchToken.suggestionCategoryId ===
                    asSearchToken.suggestionCategoryId &&
                  searchToken.keywordKey === asSearchToken.keywordKey
                )
                  return true;
                return false;
              });

              return (
                <React.Fragment key={"fragment-filter-dynamic " + index}>
                  <div
                    key={index}
                    className="form-check"
                    css={css`
                      display: flex;
                      align-items: ${multiSelect ? "baseline" : "stretch"};
                      width: ${isUniqueLoadMore ? "50%" : "auto"};
                      margin-bottom: ${isUniqueLoadMore ? "8px" : "0"};
                      @media (max-width: 768px) {
                        width: 100%;
                        height: ${isUniqueLoadMore ? "50%" : "auto"};
                      }
                    `}
                  >
                    {multiSelect ? (
                      <React.Fragment>
                        <input
                          className={`form-check-input ${
                            isChecked
                              ? "checked-checkbox filter-dynamic-check"
                              : "not-checked-checkbox filter-dynamic-not-check"
                          }`}
                          type="checkbox"
                          checked={isChecked}
                          onChange={(event) => {
                            if (event.currentTarget.checked) {
                              if (multiSelect) {
                                onAdd(asSearchToken);
                              } else {
                                tokens.some((searchToken) => {
                                  if (
                                    JSON.parse(JSON.stringify(searchToken))
                                      ?.multiSelect
                                  )
                                    onRemove(searchToken);
                                });
                                onAdd(asSearchToken);
                              }
                            } else {
                              onRemove(asSearchToken);
                            }
                          }}
                          css={css`
                            width: 14px;
                            appearance: none;
                            min-width: 15px;
                            min-height: 15px;
                            border-radius: 4px;
                            border: 2px solid #ccc;
                            background-color: ${isChecked
                              ? "var(--openk9-embeddable-search--secondary-active-color)"
                              : "#fff"};
                            background-size: 100%;
                            background-position: center;
                            background-repeat: no-repeat;
                            cursor: pointer;
                            margin-right: 10px;
                          `}
                        />
                      </React.Fragment>
                    ) : (
                      <SingleSelect
                        isChecked={isChecked}
                        multiSelect={multiSelect}
                        asSearchToken={asSearchToken}
                        onAdd={onAdd}
                        onRemove={onRemove}
                        singleSelect={singleSelect}
                        setSingleSelect={setSingleselect}
                      />
                    )}
                    <span
                      css={css`
                        margin-left: 5px;
                      `}
                    >
                      <label
                        className="form-check-label"
                        css={css`
                          text-overflow: ellipsis;
                          font-style: normal;
                          font-weight: 600;
                          line-height: 22px;
                          /* or 147% */
                          color: #000000;
                        `}
                      >
                        {suggestion.tokenType === "ENTITY" ? (
                          <>
                            <strong
                              className="openk9-filter-category-suggestion-value"
                              css={css`
                                :first-letter {
                                  text-transform: uppercase;
                                }
                                display: inline-block;
                              `}
                            >
                              {suggestion.entityType}
                            </strong>
                            : {suggestion.entityValue}
                          </>
                        ) : (
                          capitalize(suggestion.value)
                        )}
                      </label>
                    </span>
                  </div>
                </React.Fragment>
              );
            })}
          </div>
          {!isUniqueLoadMore && suggestions.hasNextPage && (
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
                className="openk9-load-more-button"
                aria-label={t("load-more-filter") || "load more filters"}
                css={css`
                  background: inherit;
                  color: var(--openk9-embeddable-search--primary-color);
                  font-size: 14px;
                  font-style: normal;
                  font-weight: 400;
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
                <ArrowDownSvg size="16px" />
              </button>
            </div>
          )}
        </React.Fragment>
      )}
    </div>
  );
}

export const FilterCategoryDynamicMemo = React.memo(FilterCategoryDynamic);

export function useInfiniteSuggestions(
  isDynamicElement: WhoIsDynamic[],
  searchQueryNotFilter: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
  loadAll: boolean,
  language: string,
  numberItems: number | null | undefined,
  allDynamic = false,
) {
  const pageSize = loadAll ? 19 : suggestKeyword === "" ? 8 : 19;
  const NPageSize = numberItems ? numberItems : pageSize;
  const client = useOpenK9Client();
  const searchQuery = allDynamic
    ? searchQueryNotFilter
    : createSuggestion(searchQueryNotFilter, isDynamicElement);
  const suggestionCategories = useInfiniteQuery(
    [
      "suggestions",
      searchQuery,
      activeSuggestionCategory,
      suggestKeyword,
      loadAll,
    ] as const,
    async ({
      queryKey: [_, searchQuery, activeSuggestionCategory, suggestKeyword],
      pageParam,
    }) => {
      if (!searchQuery) throw new Error();
      const result = await client.getSuggestions({
        searchQuery,
        range: [0, NPageSize + 1],
        afterKey: pageParam,
        suggestionCategoryId: activeSuggestionCategory,
        suggestKeyword,
        order: suggestKeyword ? "desc" : "asc",
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

function SingleSelect({
  isChecked,
  multiSelect,
  asSearchToken,
  onAdd,
  onRemove,
  singleSelect,
  setSingleSelect,
}: {
  isChecked: boolean;
  multiSelect: boolean;
  asSearchToken: SearchToken;
  onAdd: (searchToken: SearchToken) => void;
  onRemove: (searchToken: SearchToken) => void;
  singleSelect: SearchToken | undefined;
  setSingleSelect: React.Dispatch<
    React.SetStateAction<SearchToken | undefined>
  >;
}) {
  return (
    <React.Fragment>
      <div>
        <input
          id="radio-button"
          className={`radio-button ${
            isChecked
              ? "is-checked-dynamic-radio"
              : "is-not-checked-dynamic-radio"
          }`}
          type="radio"
          checked={false}
          onChange={(event) => {
            if (event.currentTarget.checked) {
              if (singleSelect) onRemove(singleSelect);
              setSingleSelect(asSearchToken);
              onAdd(asSearchToken);
            } else {
              onRemove(asSearchToken);
            }
          }}
          onClick={(event) => {
            if (isChecked) {
              onRemove(asSearchToken);
            }
          }}
          css={css`
            appearance: none !important;
            width: 17px !important;
            height: 16px !important;
            border-radius: 50% !important;
            border: 2px solid #ccc !important;
            background-color: ${isChecked
              ? "var(--openk9-embeddable-search--secondary-active-color) !important"
              : "#fff !important"};
            cursor: pointer !important;
          `}
          onMouseOver={(event) => {
            if (!isChecked) {
              const target = event.target as HTMLInputElement;
              target.style.backgroundColor = "#e6e6e6";
            }
          }}
          onMouseOut={(event) => {
            const target = event.target as HTMLInputElement;
            target.style.backgroundColor = isChecked
              ? "var(--openk9-embeddable-search--secondary-active-color)"
              : "#fff";
          }}
        />
      </div>
    </React.Fragment>
  );
}

//da modificare non appena si deciderà di mettere l'opzionalità (end/or) all'interno di una stessa categoria
export function mergeAndSortObjects(
  sortedArra: { result: SuggestionResult[]; afterKey: string }[],
  unsortedArray: SearchToken[],
  suggestionCategoryId: number,
): SuggestionResult[] {
  let sortedArray = sortedArra.flatMap((page) => page.result);
  let mergedArray = sortedArray.concat();
  if (mergedArray.length === 0) {
    mergedArray = [...unsortedArray]
      .filter(
        (element) =>
          element.tokenType === "TEXT" &&
          "goToSuggestion" in element &&
          element.suggestionCategoryId === suggestionCategoryId,
      )
      .flatMap((element) =>
        (element.values || []).map((value) => ({
          tokenType: "TEXT",
          keywordKey: element.keywordKey,
          value: value || "",
          suggestionCategoryId: element.suggestionCategoryId || 0,
          count: element.count,
        })),
      );
  }

  if (mergedArray.length === 0) {
    return [];
  }

  [...unsortedArray].filter(
    (element) =>
      element.tokenType === "TEXT" &&
      "goToSuggestion" in element &&
      element.suggestionCategoryId === suggestionCategoryId,
  );

  for (const element of unsortedArray) {
    const foundElement = mergedArray.find((obj) => {
      if (
        obj.tokenType === "TEXT" &&
        element.tokenType === "TEXT" &&
        element.values[0] !== obj.value &&
        element.suggestionCategoryId === suggestionCategoryId
      ) {
        return (
          obj.tokenType === element.tokenType &&
          (obj.value?.includes(element.values[0]) ||
            obj.value === element.values[0])
        );
      }
    });

    if (foundElement && element.tokenType === "TEXT") {
      element.values.forEach((singlevalue) => {
        const newElement: SuggestionResult = {
          tokenType: "TEXT",
          keywordKey: element.keywordKey,
          value: singlevalue,
          suggestionCategoryId: element.suggestionCategoryId || 0,
          count: element.count,
        };
        mergedArray.push(newElement);
      });
    }
  }

  mergedArray.sort((a, b) => {
    return a.value.localeCompare(b.value);
  });

  return mergedArray;
}

export type WhoIsDynamic = "tab" | "filter" | "search";

export function createSuggestion(
  searchQueryNotFilter: SearchToken[] | null,
  whoIsDynamic: WhoIsDynamic[],
): SearchToken[] | null {
  const searchQuery: SearchToken[] | null = [];
  whoIsDynamic.forEach((add) => {
    switch (add) {
      case "tab":
        searchQueryNotFilter?.forEach((searchToken) => {
          if ("isTab" in searchToken) {
            searchQuery.push(searchToken);
          }
        });
        break;
      case "filter":
        searchQueryNotFilter?.forEach((searchToken) => {
          if ("goToSuggestion" in searchToken) {
            searchQuery.push(searchToken);
          }
        });
        break;
      case "search":
        searchQueryNotFilter?.forEach((searchToken) => {
          if ("isSearch" in searchToken) {
            searchQuery.push(searchToken);
          }
        });
        break;
      default:
        return searchQueryNotFilter;
    }
  });

  return searchQuery;
}

function haveSomeValue(values: string[], value: string[]) {
  const singleValue = value[0];
  return values.includes(singleValue);
}
