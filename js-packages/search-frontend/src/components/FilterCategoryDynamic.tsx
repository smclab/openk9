import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { SearchToken, SuggestionResult } from "./client";
import { useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";
import { useOpenK9Client } from "./client";
import { NoFilter, mapSuggestionToSearchToken } from "./FilterCategory";
import { useTranslation } from "react-i18next";
import { capitalize } from "lodash";
import { ArrowDownSvg } from "../svgElement/ArrowDownSvg";
import { IconsCustom } from "../embeddable/entry";

type FilterCategoryDynamicallyProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
  multiSelect: boolean;
  searchQuery: SearchToken[];
  isUniqueLoadMore?: boolean;
  loadAll?: boolean;
  language: string;
  isDynamicElement: WhoIsDynamic[];
  placeholder?: string | undefined | null;
  noResultMessage?: string | null | undefined;
  numberItems?: number | null | undefined;
  iconCustom: IconsCustom;
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
  isUniqueLoadMore = false,
  loadAll = false,
  language,
  numberItems,
  isDynamicElement,
  setHasMoreSuggestionsCategories = undefined,
  noResultMessage,
  placeholder,
  iconCustom,
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
          noResultMessage={noResultMessage}
        />
      </React.Fragment>
    );

  return (
    <fieldset
      className="openk9-filter-category-container"
      css={css`
        ${isUniqueLoadMore ? "width: 50%" : null}
        @media  (max-width: 768px) {
          width: 100%;
          ${isUniqueLoadMore ? "height: 50%" : null}
        }
        margin: 0;
        padding: 0;
        border: none;
        box-shadow: none;
        background-color: transparent;
        background-image: none;
        font: inherit;
        color: inherit;
        display: flex;
        flex-direction: column;
        gap: 10px;
        padding: 8px;
      `}
    >
      <div
        className="openk9-filter-category-title"
        css={css`
          user-select: none;
          display: flex;
          align-items: center;
          justify-content: space-between;
        `}
      >
        <legend
          css={css`
            :first-letter {
              text-transform: uppercase;
            }
          `}
        >
          <strong>{suggestionCategoryName}</strong>
        </legend>

        <button
          aria-label={
            t("openk9-collapsable-filter") || "openk9 collapsable filter"
          }
          aria-expanded={isOpen ? "true" : "false"}
          style={{ background: "inherit", border: "none" }}
          onClick={() => setIsOpen(!isOpen)}
        >
          <FontAwesomeIcon
            icon={isOpen ? faChevronUp : faChevronDown}
            style={{
              color: "var(--openk9-embeddable-search--secondary-text-color)",
              cursor: "pointer",
            }}
          />
        </button>
      </div>
      {isOpen && (
        <React.Fragment>
          {!isUniqueLoadMore && (
            <>
              <label
                htmlFor={"search-category-" + suggestionCategoryId}
                className="visually-hidden"
                css={css`
                  border: 0;
                  padding: 0;
                  margin: 0;
                  position: absolute !important;
                  height: 1px;
                  width: 1px;
                  overflow: hidden;
                  clip: rect(
                    1px 1px 1px 1px
                  ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
                  clip: rect(
                    1px,
                    1px,
                    1px,
                    1px
                  ); /*maybe deprecated but we need to support legacy browsers */
                  clip-path: inset(50%);
                  white-space: nowrap;
                `}
              >
                {t("search-filters")}
              </label>
              <div
                css={css`
                  position: relative;
                `}
              >
                {iconCustom?.Search ? (
                  iconCustom?.Search
                ) : (
                  <FontAwesomeIcon
                    icon={faSearch}
                    width={10}
                    css={css`
                      position: absolute;
                      top: 45%;
                      left: 10px;
                      transform: translateY(-50%);
                      color: silver;
                      display: inline-block;
                      width: 0.75em;
                      height: 0.75em;
                      stroke-width: 0;
                      stroke: currentColor;
                      fill: currentColor;
                    `}
                  />
                )}
                <input
                  type="text"
                  placeholder={placeholder || t("search-filters") || ""}
                  onChange={(event) => setText(event.currentTarget.value)}
                  className="openk9-filter-category-search"
                  id={"search-category-" + suggestionCategoryId}
                  value={text}
                  css={css`
                    padding-left: calc(1em + 10px + 8px);
                    height: 2em;
                    width: -moz-available;
                    width: -webkit-fill-available;
                    width: fill-available;
                    padding: 3px;
                    flex-grow: 1;
                    text-indent: 25px;
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
            </>
          )}
          <ul
            className="openk9-filter-form-check-container"
            css={css`
              display: flex;
              flex-direction: ${isUniqueLoadMore ? "row" : "column"};
              gap: ${isUniqueLoadMore ? "0" : "5px"};
              flex-wrap: ${isUniqueLoadMore ? "wrap" : "initial"};
              padding-left: unset;
              margin: 0;
            `}
          >
            {filters.map((suggestion, index) => {
              const asSearchToken = mapSuggestionToSearchToken(
                suggestion,
                true,
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
                  <li
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
                      <CheckBoxSelect
                        isChecked={isChecked}
                        suggestion={suggestion}
                        asSearchToken={asSearchToken}
                        multiSelect={multiSelect}
                        onAdd={onAdd}
                        onRemove={onRemove}
                        suggestionCategoryId={suggestionCategoryId}
                        tokens={tokens}
                      />
                    ) : (
                      <SingleSelect
                        isChecked={isChecked}
                        multiSelect={multiSelect}
                        asSearchToken={asSearchToken}
                        onAdd={onAdd}
                        onRemove={onRemove}
                        singleSelect={singleSelect}
                        setSingleSelect={setSingleselect}
                        suggestionValue={suggestion.value}
                        suggestionCategoryId={"" + suggestionCategoryId}
                      />
                    )}
                    <label
                      className="form-check-label"
                      htmlFor={
                        multiSelect
                          ? "checkbox-dynamic-" +
                            suggestion.value +
                            "-" +
                            suggestionCategoryId
                          : "radio-button-dynamic-" +
                            suggestion.value +
                            "-" +
                            suggestionCategoryId
                      }
                      css={css`
                        text-overflow: ellipsis;
                        font-style: normal;
                        font-weight: 600;
                        line-height: 22px;
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
                  </li>
                </React.Fragment>
              );
            })}
          </ul>
          {!isUniqueLoadMore && suggestions.hasNextPage && (
            <div
              className="openk9-container-load-more"
              css={css`
                text-align: center;
                width: 100%;
                display: flex;
                margin-top: 10px;
                margin-bottom: 20px;
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
    </fieldset>
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
      language,
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
  suggestionValue,
  suggestionCategoryId,
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
  suggestionValue: string;
  suggestionCategoryId: string;
}) {
  return (
    <React.Fragment>
      <div>
        <input
          id={
            "radio-button-dynamic-" +
            suggestionValue +
            "-" +
            suggestionCategoryId
          }
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
          element.tokenType === "FILTER" &&
          "goToSuggestion" in element &&
          element.suggestionCategoryId === suggestionCategoryId,
      )
      .flatMap((element) =>
        (element.values || []).map((value) => ({
          tokenType: "FILTER",
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
      element.tokenType === "FILTER" &&
      "goToSuggestion" in element &&
      element.suggestionCategoryId === suggestionCategoryId,
  );

  for (const element of unsortedArray) {
    const foundElement = mergedArray.find((obj) => {
      if (
        obj.tokenType === "FILTER" &&
        element.tokenType === "FILTER" &&
        element.values[0] !== obj.value &&
        element.suggestionCategoryId === suggestionCategoryId
      ) {
        return (
          obj.tokenType === element.tokenType && obj.value === element.values[0]
        );
      }
    });

    if (foundElement && element.tokenType === "FILTER") {
      element.values.forEach((singlevalue) => {
        const newElement: SuggestionResult = {
          tokenType: "FILTER",
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

export type WhoIsDynamic = "tab" | "filter" | "search" | "date";

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
      case "date":
        searchQueryNotFilter?.forEach((searchToken) => {
          if (searchToken.tokenType === "DATE") {
            searchQuery.push(searchToken);
          }
        });
      default:
        return searchQueryNotFilter;
    }
  });

  return searchQuery;
}

export function haveSomeValue(values: string[], value: string[]) {
  const singleValue = value[0];
  return values.includes(singleValue);
}

function CheckBoxSelect({
  isChecked,
  suggestion,
  asSearchToken,
  suggestionCategoryId,
  onAdd,
  multiSelect,
  tokens,
  onRemove,
}: {
  isChecked: boolean;
  suggestion: SuggestionResult;
  asSearchToken: SearchToken;
  suggestionCategoryId: number;
  onAdd: (searchToken: SearchToken) => void;
  multiSelect: boolean;
  tokens: SearchToken[];
  onRemove: (searchToken: SearchToken) => void;
}) {
  return (
    <React.Fragment>
      <input
        className={`form-check-input ${
          isChecked
            ? "checked-checkbox filter-dynamic-check"
            : "not-checked-checkbox filter-dynamic-not-check"
        }`}
        id={"checkbox-dynamic-" + suggestion.value + "-" + suggestionCategoryId}
        type="checkbox"
        checked={isChecked}
        onChange={(event) => {
          if (event.currentTarget.checked) {
            onAdd(asSearchToken);
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
        `}
      />
    </React.Fragment>
  );
}
