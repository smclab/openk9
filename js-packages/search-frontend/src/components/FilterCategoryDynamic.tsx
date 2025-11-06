import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";
import { SearchToken, SuggestionResult, useOpenK9Client } from "./client";
import { useTranslation } from "react-i18next";
import { capitalize } from "lodash";
import { ArrowDownSvg } from "../svgElement/ArrowDownSvg";
import { IconsCustom } from "../embeddable/entry";
import { Logo } from "./Logo";
import { useRange } from "./useRange";
import { NoFilter } from "./FilterCategory";

export type WhoIsDynamic = "tab" | "filter" | "search" | "date";

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
  haveSearch?: boolean | null | undefined;
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
  haveSearch = true,
}: FilterCategoryDynamicallyProps) {
  const [text, setText] = React.useState<string>("");
  const debounced = useDebounce(text, 600);
  const [showSearch, setShowSearch] = React.useState<boolean>(false);
  const tokensWithoutSearch = React.useMemo(
    () => (tokens ?? []).filter((t) => !t?.search),
    [tokens],
  );

  const suggestions = useInfiniteSuggestions(
    isDynamicElement,
    tokensWithoutSearch,
    suggestionCategoryId,
    debounced,
    loadAll,
    language,
    numberItems,
  );

  const { t } = useTranslation();
  const resultPages = suggestions?.data?.pages ?? [];

  const keyOfSuggestion = React.useCallback((s: SuggestionResult) => {
    return s?.tokenType === "ENTITY"
      ? `FILTER__${s?.entityType ?? ""}__${s?.entityValue ?? ""}`
      : `FILTER__${s?.keywordKey ?? ""}__${s?.value ?? ""}`;
  }, []);

  const filters = React.useMemo(
    () =>
      mergeAndSortObjects(
        resultPages,
        searchQuery ?? [],
        suggestionCategoryId,
        debounced,
      ),
    [resultPages, searchQuery, suggestionCategoryId, debounced],
  );

  const baseSelectedKeys = React.useMemo(() => {
    const set = new Set<string>();
    for (const t of tokens ?? []) {
      if (t?.suggestionCategoryId === suggestionCategoryId && t?.values) {
        const kk = t?.keywordKey ?? "";
        for (const v of t?.values ?? []) set.add(`FILTER__${kk}__${v}`);
      }
    }
    return set;
  }, [tokens, suggestionCategoryId]);

  const selectedCount = React.useMemo(
    () =>
      filters.filter((s) => baseSelectedKeys.has(keyOfSuggestion(s))).length,
    [filters, baseSelectedKeys, keyOfSuggestion],
  );

  const [optimistic, setOptimistic] = React.useState<Map<string, boolean>>(
    new Map(),
  );

  React.useEffect(() => {
    setOptimistic(new Map());
  }, [tokens]);

  React.useEffect(() => {
    if (setHasMoreSuggestionsCategories && suggestions?.hasNextPage != null) {
      setHasMoreSuggestionsCategories(Boolean(suggestions?.hasNextPage));
    }
  }, [suggestions?.hasNextPage, setHasMoreSuggestionsCategories]);

  const [isOpen, setIsOpen] = React.useState<boolean>(false);
  const [singleSelect, setSingleselect] = React.useState<
    SearchToken | undefined
  >(undefined);

  const show = Boolean(debounced || (filters?.length ?? 0) > 0);

  const handleClearCategory = React.useCallback(() => {
    for (const t of tokens ?? []) {
      if (t?.suggestionCategoryId === suggestionCategoryId && t?.values) {
        for (const v of t.values) {
          onRemove({ ...t, values: [v] });
        }
      }
    }
  }, [tokens, suggestionCategoryId, onRemove]);

  if (!show)
    return (
      <>
        <NoFilter
          isOpen={isOpen}
          setIsOpen={setIsOpen}
          suggestionCategoryName={suggestionCategoryName}
          noResultMessage={noResultMessage}
        />
      </>
    );

  return (
    <fieldset
      className={`openk9-filter-category-container openk9-filter-category-${suggestionCategoryName}`}
      css={css`
        ${isUniqueLoadMore ? "width: 50%" : null}
        @media (max-width: 768px) {
          ${isUniqueLoadMore ? "height: 50%" : null}
        }
        @media (max-width: 480px) {
          width: unset;
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
        padding: 8px 16px;
      `}
    >
      <div
        className="openk9-filter-category-title"
        css={css`
          user-select: none;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 8px;
          padding: 6px 0;
          border-bottom: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      >
        <legend
          className="legend-filters"
          css={css`
            display: flex;
            align-items: center;
            gap: 8px;
            :first-letter {
              text-transform: uppercase;
            }
          `}
        >
          <strong
            className="name-category-filter"
            css={css`
              font-size: 14px;
              letter-spacing: 0.2px;
              color: var(--openk9-embeddable-search--secondary-text-color);
            `}
          >
            {suggestionCategoryName}
          </strong>
        </legend>
        <div
          css={css`
            display: flex;
            align-items: center;
            gap: 8px;
          `}
        >
          {isOpen && (
            <button
              className="openk9-toggle-search-button"
              aria-label={
                showSearch
                  ? t("hide-search") || "Nascondi ricerca"
                  : t("show-search") || "Mostra ricerca"
              }
              css={css`
                background: transparent;
                border: 1px solid var(--openk9-embeddable-search--border-color);
                border-radius: 8px;
                padding: 6px 8px;
                cursor: pointer;
                transition: transform 120ms ease, background-color 120ms ease,
                  border-color 120ms ease;
                &:hover {
                  background: rgba(0, 0, 0, 0.03);
                }
                &:active {
                  transform: translateY(1px);
                }
                color: var(--openk9-embeddable-search--secondary-icon-color);
              `}
              onClick={() => setShowSearch((prev) => !prev)}
              title={
                showSearch
                  ? t("hide-search") || "Nascondi ricerca"
                  : t("show-search") || "Mostra ricerca"
              }
            >
              <FontAwesomeIcon icon={faSearch} />
            </button>
          )}
          <button
            className={`openk9-mobile-collapsable-filters openk9-collapsable-filters ${
              isOpen
                ? "openk9-dropdown-filters-open"
                : "openk9-dropdown-filters-close"
            }`}
            aria-label={
              t("openk9-collapsable-filter") || "openk9 collapsable filter"
            }
            aria-expanded={isOpen ? "true" : "false"}
            css={css`
              background: transparent;
              border: 1px solid var(--openk9-embeddable-search--border-color);
              border-radius: 8px;
              padding: 6px 8px;
              cursor: pointer;
              transition: transform 120ms ease, background-color 120ms ease,
                border-color 120ms ease;
              &:hover {
                background: rgba(0, 0, 0, 0.03);
              }
              &:active {
                transform: translateY(1px);
              }
            `}
            onClick={() => setIsOpen(!isOpen)}
          >
            <FontAwesomeIcon
              className="icon-search icon-search-filters"
              icon={isOpen ? faChevronUp : faChevronDown}
              css={css`
                color: var(--openk9-embeddable-search--secondary-text-color);
                cursor: pointer;
              `}
            />
          </button>
        </div>
      </div>
      {isOpen && (
        <>
          {!isUniqueLoadMore && haveSearch && (
            <>
              {showSearch && (
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
                      clip: rect(1px 1px 1px 1px);
                      clip: rect(1px, 1px, 1px, 1px);
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
                      onChange={(event) =>
                        setText(event?.currentTarget?.value ?? "")
                      }
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
                        border-radius: 8px;
                        border: 1px solid
                          var(--openk9-embeddable-search--border-color);
                        background: white;
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
              {baseSelectedKeys.size > 0 && (
                <button
                  aria-label={t("clear-category") || "clear category"}
                  className="openk9-clear-category-button"
                  onClick={handleClearCategory}
                  css={css`
                    background: transparent;
                    border: 1px solid
                      var(--openk9-embeddable-search--border-color);
                    border-radius: 8px;
                    padding: 6px 8px;
                    cursor: pointer;
                    transition: transform 120ms ease,
                      background-color 120ms ease, border-color 120ms ease;
                    &:hover {
                      background: rgba(0, 0, 0, 0.03);
                    }
                    &:active {
                      transform: translateY(1px);
                    }
                    color: var(
                      --openk9-embeddable-search--secondary-text-color
                    );
                  `}
                  title={t("clear-category") || "clear"}
                >
                  clear filter
                </button>
              )}
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
            {(filters?.length ?? 0) === 0 && <NoFiltersSearch />}
            {filters?.map((suggestion, index) => {
              const asSearchToken = mapSuggestionToSearchToken(
                suggestion,
                true,
              );
              const key = keyOfSuggestion(suggestion);
              const baseChecked = baseSelectedKeys.has(key);
              const optimisticOverride = optimistic.has(key)
                ? optimistic.get(key)!
                : undefined;
              const isChecked = optimisticOverride ?? baseChecked;
              const idValue =
                suggestion?.tokenType === "ENTITY"
                  ? `${suggestion?.entityType ?? ""}-${
                      suggestion?.entityValue ?? ""
                    }`
                  : suggestion?.value ?? String(index);
              const handleAdd = (tok: SearchToken) => {
                setOptimistic((prev) => {
                  const m = new Map(prev);
                  if (!multiSelect) {
                    for (const s of filters ?? []) {
                      const k = keyOfSuggestion(s);
                      m.set(k, false);
                    }
                  }
                  m.set(key, true);
                  return m;
                });
                onAdd(tok);
              };
              const handleRemove = (tok: SearchToken) => {
                setOptimistic((prev) => {
                  const m = new Map(prev);
                  m.set(key, false);
                  return m;
                });
                onRemove(tok);
              };
              const showSeparator =
                !isUniqueLoadMore &&
                selectedCount > 0 &&
                index === selectedCount &&
                (filters?.length ?? 0) > selectedCount;

              return (
                <React.Fragment
                  key={`fragment-filter-dynamic-${index}-${idValue}`}
                >
                  {showSeparator && (
                    <li
                      role="separator"
                      aria-hidden="true"
                      css={css`
                        height: 1px;
                        background: var(
                          --openk9-embeddable-search--border-color
                        );
                        margin: 4px 0;
                        list-style: none;
                        width: 100%;
                      `}
                    />
                  )}
                  <li
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
                        suggestionCategoryId={suggestionCategoryId}
                        onAdd={handleAdd}
                        onRemove={handleRemove}
                      />
                    ) : (
                      <SingleSelect
                        isChecked={isChecked}
                        asSearchToken={asSearchToken}
                        onAdd={handleAdd}
                        onRemove={handleRemove}
                        singleSelect={singleSelect}
                        setSingleSelect={setSingleselect}
                        suggestionValue={idValue}
                        suggestionCategoryId={String(suggestionCategoryId)}
                      />
                    )}
                    <label
                      className="form-check-label"
                      htmlFor={
                        multiSelect
                          ? "checkbox-dynamic-" +
                            idValue +
                            "-" +
                            suggestionCategoryId
                          : "radio-button-dynamic-" +
                            idValue +
                            "-" +
                            suggestionCategoryId
                      }
                      css={css`
                        text-overflow: ellipsis;
                        font-style: normal;
                        font-weight: 400;
                        line-height: 22px;
                        color: ${isChecked
                          ? "var(--openk9-embeddable-search--primary-color)"
                          : "#000000"};
                      `}
                    >
                      {suggestion?.tokenType === "ENTITY" ? (
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
                            {suggestion?.entityType}
                          </strong>
                          : {suggestion?.entityValue}
                        </>
                      ) : (
                        <CapitalizeValue value={suggestion.value} />
                      )}
                    </label>
                  </li>
                </React.Fragment>
              );
            })}
          </ul>
          {!isUniqueLoadMore && suggestions?.hasNextPage && (
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
                  border-radius: 8px;
                `}
                onClick={() => {
                  suggestions?.fetchNextPage?.();
                }}
              >
                {t("load-more") || "Load More"}
                <ArrowDownSvg size="16px" />
              </button>
            </div>
          )}
        </>
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
  const pageSizeBase = loadAll ? 19 : suggestKeyword === "" ? 8 : 19;
  const NPageSize = numberItems ?? pageSizeBase;
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
    async ({ queryKey: [_, sq, activeId, sk], pageParam }) => {
      if (!sq) throw new Error("missing searchQuery");
      const result = await client?.getSuggestions?.({
        searchQuery: sq,
        range: [0, NPageSize + 1],
        afterKey: pageParam,
        suggestionCategoryId: activeId,
        suggestKeyword: sk,
        order: sk ? "desc" : "asc",
        language,
      });
      return {
        result: result?.result ?? [],
        afterKey: result?.afterKey ?? undefined,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (!lastPage?.afterKey) return undefined;
        if ((pages?.[pages.length - 1]?.result?.length ?? 0) < NPageSize)
          return undefined;
        return lastPage?.afterKey;
      },
      suspense: true,
    },
  );

  return suggestionCategories;
}

function SingleSelect({
  isChecked,
  asSearchToken,
  onAdd,
  onRemove,
  singleSelect,
  setSingleSelect,
  suggestionValue,
  suggestionCategoryId,
}: {
  isChecked: boolean;
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
    <>
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
          checked={isChecked}
          onChange={(event) => {
            const checked = Boolean(event?.currentTarget?.checked);
            if (checked) {
              if (singleSelect) onRemove(singleSelect);
              setSingleSelect(asSearchToken);
              onAdd(asSearchToken);
            } else {
              onRemove(asSearchToken);
            }
          }}
          onClick={() => {
            if (isChecked) onRemove(asSearchToken);
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
              const target = event?.target as HTMLInputElement;
              if (target) target.style.backgroundColor = "#e6e6e6";
            }
          }}
          onMouseOut={(event) => {
            const target = event?.target as HTMLInputElement;
            if (target)
              target.style.backgroundColor = isChecked
                ? "var(--openk9-embeddable-search--secondary-active-color)"
                : "#fff";
          }}
        />
      </div>
    </>
  );
}

function CheckBoxSelect({
  isChecked,
  suggestion,
  asSearchToken,
  suggestionCategoryId,
  onAdd,
  onRemove,
}: {
  isChecked: boolean;
  suggestion: SuggestionResult;
  asSearchToken: SearchToken;
  suggestionCategoryId: number;
  onAdd: (searchToken: SearchToken) => void;
  onRemove: (searchToken: SearchToken) => void;
}) {
  const { resetPage } = useRange();

  const idValue =
    suggestion?.tokenType === "ENTITY"
      ? `${suggestion?.entityType ?? ""}-${suggestion?.entityValue ?? ""}`
      : suggestion?.value ?? "val";

  return (
    <>
      <input
        className={`form-check-input ${
          isChecked
            ? "checked-checkbox filter-dynamic-check"
            : "not-checked-checkbox filter-dynamic-not-check"
        }`}
        id={"checkbox-dynamic-" + idValue + "-" + suggestionCategoryId}
        type="checkbox"
        checked={isChecked}
        onChange={(event) => {
          const checked = Boolean(event?.currentTarget?.checked);
          if (checked) {
            onAdd(asSearchToken);
            resetPage?.();
          } else {
            onRemove(asSearchToken);
            resetPage?.();
          }
        }}
        css={css`
          cursor: pointer;
        `}
      />
    </>
  );
}

export function NoFiltersSearch() {
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
        `}
      >
        <Logo size={100} />
        <h4>{t("no-filters")} </h4>
      </div>
    </div>
  );
}

export function mergeAndSortObjects(
  pages: { result: SuggestionResult[]; afterKey?: string }[],
  selectedTokens: SearchToken[],
  suggestionCategoryId: number,
  suggestKeyword?: string,
): SuggestionResult[] {
  const fromApi: SuggestionResult[] =
    pages?.flatMap((p) => p?.result ?? []) ?? [];

  type SelEntry = { keywordKey?: string; value: string; count?: string };
  const selectedEntries: SelEntry[] = [];
  for (const t of selectedTokens ?? []) {
    if (t?.suggestionCategoryId === suggestionCategoryId && t?.values) {
      for (const v of t?.values ?? []) {
        if (v != null)
          selectedEntries.push({
            keywordKey: t?.keywordKey,
            value: v,
            count: t?.count,
          });
      }
    }
  }

  const keyOf = (s: SuggestionResult): string => {
    if (s?.tokenType === "ENTITY")
      return `FILTER__${s?.entityType ?? ""}__${s?.entityValue ?? ""}`;
    return `FILTER__${s?.keywordKey ?? ""}__${s?.value ?? ""}`;
  };

  const labelOf = (s: SuggestionResult): string => {
    if (s?.tokenType === "ENTITY")
      return `${s?.entityType ?? ""}: ${s?.entityValue ?? ""}`;
    return s?.value ?? "";
  };

  const existingKeys = new Set<string>(fromApi?.map((x) => keyOf(x)) ?? []);
  const merged: SuggestionResult[] = [...fromApi];

  for (const e of selectedEntries) {
    const key = `FILTER__${e?.keywordKey ?? ""}__${e?.value}`;
    if (!existingKeys.has(key)) {
      merged.push({
        tokenType: "FILTER",
        keywordKey: e?.keywordKey ?? "",
        value: e?.value ?? "",
        suggestionCategoryId,
        count: e?.count,
      });
      existingKeys.add(key);
    }
  }

  const selectedKeys = new Set<string>(
    selectedEntries?.map(
      (e) => `FILTER__${e?.keywordKey ?? ""}__${e?.value}`,
    ) ?? [],
  );

  const filtered =
    suggestKeyword && suggestKeyword.trim().length > 0
      ? merged.filter((m) =>
          labelOf(m).toLowerCase().includes(suggestKeyword.toLowerCase()),
        )
      : merged;

  filtered.sort((a, b) => {
    const aSel = selectedKeys.has(keyOf(a));
    const bSel = selectedKeys.has(keyOf(b));
    if (aSel && !bSel) return -1;
    if (!aSel && bSel) return 1;
    return labelOf(a).localeCompare(labelOf(b));
  });

  return filtered;
}

export function createSuggestion(
  searchQueryNotFilter: SearchToken[] | null,
  whoIsDynamic: WhoIsDynamic[],
): SearchToken[] | null {
  const searchQuery: SearchToken[] = [];
  whoIsDynamic?.forEach((add) => {
    switch (add) {
      case "tab":
        searchQueryNotFilter?.forEach((st) => {
          if (st?.isTab) searchQuery.push(st);
        });
        break;
      case "filter":
        searchQueryNotFilter?.forEach((st) => {
          if (
            (st as { goToSuggestion: boolean })?.goToSuggestion ||
            st?.isFilter
          )
            searchQuery.push(st);
        });
        break;
      case "search":
        searchQueryNotFilter?.forEach((st) => {
          if (st?.search) searchQuery.push(st);
        });
        break;
      case "date":
        searchQueryNotFilter?.forEach((st) => {
          if (st?.tokenType === "DATE") searchQuery.push(st);
        });
        break;
      default:
        break;
    }
  });
  return searchQuery;
}

export function haveSomeValue(values: string[], value: string[]) {
  const singleValue = value?.[0];
  return (values ?? []).includes(singleValue as string);
}

function mapSuggestionToSearchToken(
  s: SuggestionResult,
  forceGoToSuggestion = true,
): SearchToken {
  if (s?.tokenType === "ENTITY") {
    return {
      tokenType: "FILTER",
      keywordKey: s?.entityType,
      values: [s?.entityValue ?? ""],
      filter: true,
      suggestionCategoryId: s?.suggestionCategoryId,
      count: (s?.count as string) ?? undefined,
      ...(forceGoToSuggestion ? { goToSuggestion: true } : {}),
    };
  }
  return {
    tokenType: "FILTER",
    keywordKey: s?.keywordKey,
    values: [s?.value ?? ""],
    filter: true,
    suggestionCategoryId: s?.suggestionCategoryId,
    count: (s?.count as string) ?? undefined,
    ...(forceGoToSuggestion ? { goToSuggestion: true } : {}),
  };
}

function CapitalizeValue({ value }: { value: string | undefined }) {
  const [isHover, setIsHover] = React.useState(false);
  if (!value || value.length === 0) return value;
  return (
    <span
      className={`openk9-capitalize-value ${isHover ? "is-hover" : ""}`}
      css={css`
        cursor: pointer;
        :hover {
          color: var(--openk9-embeddable-search--primary-light-color);
          font-weight: 600;
        }
      `}
      onMouseOver={() => setIsHover(true)}
      onMouseOut={() => setIsHover(false)}
      title={isHover ? value : ""}
    >
      {capitalize(value ?? "")}
    </span>
  );
}
