import React from "react";
import {
  doSearch,
  GenericResultItem,
  getPlugins,
  getTokenSuggestions,
  InputSuggestionToken,
  SearchQuery,
  SearchToken,
} from "@openk9/http-api";
import { useQuery, useInfiniteQuery } from "react-query";
import { Result } from "./Result";
import useDebounce from "../hooks/useDebounce";
import { TokenComponent } from "./Token";
import { getPluginResultRenderers } from "@openk9/search-ui-components";
import { useClickAway } from "../hooks/useClickAway";
import { useInView } from "react-intersection-observer";
import { Detail } from "./Detail";
import { OpenK9UITemplates } from "../api";
import { EmbedElement } from "./EmbedElement";
import { ScrollIntoView } from "./ScrollIntoView";

type MainProps = {
  children: (widgets: {
    search: React.ReactNode;
    suggestions: React.ReactNode;
    tabs: React.ReactNode;
    results: React.ReactNode;
    details: React.ReactNode;
  }) => React.ReactNode;
  templates: OpenK9UITemplates;
}

type State = {
  tokens: SearchToken[];
  tabIndex: number;
  focusedToken: number | null;
  text: string;
  searchQuery: SearchQuery | null;
  showSuggestions: boolean;
  suggestionIndex: number | null;
  entityKind: string | undefined;
  detail: GenericResultItem<any> | null;
}

const initialState: State = {
  tokens: [],
  tabIndex: 0,
  text: "",
  searchQuery: null,
  showSuggestions: false,
  focusedToken: null,
  suggestionIndex: null,
  entityKind: undefined,
  detail: null,
}

export function Main({
  children,
  templates,
}: MainProps) {
  const [state, setState] = React.useState<State>(initialState);
  const { tokens, text, showSuggestions, searchQuery } = state;
  const tabTokens = defaultTabTokens;
  const selectedTabTokens = tabTokens[state.tabIndex].tokens;
  const inputRef = React.useRef<HTMLInputElement | null>(null);
  const suggestionsRef = React.useRef<HTMLInputElement | null>(null);
  const pageSize = 4;
  const results = useInfiniteQuery(
    ["search", searchQuery] as const,
    async ({ queryKey, pageParam = 0 }) => {
      if (!queryKey[1]) throw new Error();
      const result = await doSearch(
        {
          searchQuery: queryKey[1],
          range: [pageParam * pageSize, pageParam * pageSize + pageSize],
        },
        null,
      );
      return {
        ...result,
        page: pageParam,
        last: pageParam * pageSize + pageSize > result.total,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (lastPage.last) return undefined;
        return lastPage.page + 1;
      },
    },
  );
  const debouncedSearchQuery: SearchQuery = useDebounce(
    React.useMemo(
      () =>
        text
          ? [
              ...tokens,
              ...selectedTabTokens,
              { tokenType: "TEXT", values: [text] },
            ]
          : [...tokens, ...selectedTabTokens],
      [text, tokens, selectedTabTokens],
    ),
    500,
  );
  const suggestions = useQuery(
    ["suggestion", debouncedSearchQuery, state.entityKind] as const,
    ({ queryKey }) => {
      return getTokenSuggestions(queryKey[1], null, queryKey[2]);
    },
    { enabled: showSuggestions, keepPreviousData: true },
  );
  const { data: pluginInfos } = useQuery(["plugins"], () => {
    return getPlugins(null);
  });
  const renderers = pluginInfos ? getPluginResultRenderers(pluginInfos) : null;
  function updateSearch() {
    setState((state) => {
      // ATTENTION reread from state otherwise it will lag 1 interactions behind
      const { text, tokens } = state;
      const selectedTabTokens = tabTokens[state.tabIndex].tokens;
      const searchQuery: SearchQuery = text
        ? [
            ...tokens,
            ...selectedTabTokens,
            { tokenType: "TEXT", values: [text] },
          ]
        : [...tokens, ...selectedTabTokens];
      if (searchQuery.length > 0) {
        return {
          ...state,
          searchQuery,
        };
      } else {
        return { ...state, searchQuery: null };
      }
    });
  }
  function addToken(token: SearchToken) {
    setState((state) => {
      return { ...state, tokens: [...state.tokens, token] };
    });
  }
  function focusInput() {
    inputRef.current?.focus();
  }
  function addSuggestion(suggestion: InputSuggestionToken) {
    switch (suggestion.kind) {
      case "ENTITY": {
        addToken({
          tokenType: "ENTITY",
          entityType: suggestion.type,
          values: [suggestion.id],
        });
        break;
      }
      case "PARAM": {
        addToken({
          tokenType: "TEXT",
          keywordKey: suggestion.id,
          values: [""],
        });
        break;
      }
      case "TOKEN": {
        addToken({
          tokenType: (suggestion.outputTokenType as any) ?? "TEXT",
          keywordKey: suggestion.outputKeywordKey,
          values: [suggestion.id],
        });
        break;
      }
    }
    setState((state) => ({
      ...state,
      text: "",
      suggestionIndex: null,
      showSuggestions: false,
      focusedToken: state.tokens.length - 1,
    }));
    updateSearch();
    focusInput();
  }
  useClickAway(
    React.useMemo(() => [inputRef, suggestionsRef], []),
    React.useCallback(() => {
      setState((state) => ({ ...state, showSuggestions: false }));
    }, []),
  );
  const { ref, inView } = useInView({ delay: 200, initialInView: false });
  const { fetchNextPage } = results;
  React.useEffect(() => {
    if (inView) fetchNextPage();
  }, [fetchNextPage, inView]);
  function suggestionUp() {
    setState((state) => {
      if (state.suggestionIndex !== null && state.suggestionIndex > 0) {
        return { ...state, suggestionIndex: state.suggestionIndex - 1 };
      }
      if (state.suggestionIndex === 0) {
        return { ...state, suggestionIndex: null };
      } else return state;
    });
  }
  function suggestionDown() {
    setState((state) => {
      if (state.suggestionIndex !== null) {
        return { ...state, suggestionIndex: state.suggestionIndex + 1 };
      } else return { ...state, suggestionIndex: 0 };
    });
  }
  React.useEffect(() => {
    if (
      suggestions.data &&
      state.suggestionIndex !== null &&
      state.suggestionIndex >= suggestions.data?.length
    ) {
      setState((state) => ({ ...state, suggestionIndex: 0 }));
    }
  }, [state.suggestionIndex, suggestions.data]);
  function setActiveTabIndex(index: number) {
    setState((state) => ({ ...state, tabIndex: index }));
    updateSearch();
  }
  function setResultForDetail(result: GenericResultItem<unknown>) {
    setState((state) => ({ ...state, detail: result }));
  }
  function setSuggestionKind(id: string) {
    setState((state) => ({
      ...state,
      entityKind: id,
    }));
  }
  return children({
    search: (
      <div
        style={{
          display: "flex",
          height: "100%",
          alignItems: "center",
        }}
      >
        {tokens.map((token, index) => {
          if (templates.token) {
            return (
              <React.Fragment key={index}>
                <EmbedElement
                  element={templates.token({ token, entity: null })}
                />
              </React.Fragment>
            );
          }
          return (
            <TokenComponent
              key={index}
              token={token}
              onChange={(token) => {
                setState((state) => {
                  const tokens = [...state.tokens];
                  tokens[index] = token;
                  return { ...state, tokens };
                });
              }}
              onRemove={() => {
                setState((state) => ({
                  ...state,
                  tokens: state.tokens.filter((t, i) => i !== index),
                }));
              }}
              onSubmit={updateSearch}
              isFocused={state.focusedToken === index}
              onFocus={() =>
                setState((state) => ({
                  ...state,
                  focusedToken: index,
                  showSuggestions: false,
                }))
              }
            />
          );
        })}
        <input
          ref={inputRef}
          value={text}
          onChange={(event) => {
            const text = event.currentTarget.value;
            setState((state) => ({
              ...state,
              text,
              suggestionIndex: null,
            }));
          }}
          onFocus={() => {
            setState((state) => ({
              ...state,
              focusedToken: null,
              showSuggestions: true,
            }));
          }}
          onKeyDown={(event) => {
            switch (event.key) {
              case "Enter": {
                if (state.suggestionIndex !== null) {
                  const suggestion = suggestions.data?.[state.suggestionIndex];
                  if (suggestion) addSuggestion(suggestion);
                } else {
                  setState((state) => ({
                    ...state,
                    showSuggestions: false,
                  }));
                  updateSearch();
                }
                break;
              }
              case "Escape": {
                setState((state) => ({
                  ...state,
                  suggestionIndex: null,
                  showSuggestions: false,
                }));
                break;
              }
              case "Backspace": {
                if (event.currentTarget.value === "") {
                  event.preventDefault();
                  setState((state) => ({
                    ...state,
                    tokens: state.tokens.slice(0, -1),
                  }));
                }
                break;
              }
              case "ArrowUp": {
                event.preventDefault();
                suggestionUp();
                break;
              }
              case "ArrowDown": {
                event.preventDefault();
                suggestionDown();
                break;
              }
              default:
                setState((state) => ({ ...state, showSuggestions: true }));
            }
          }}
          style={{
            appearance: "none",
            outline: "none",
            border: "none",
            padding: "0px",
            font: "inherit",
            flexGrow: 1,
          }}
          placeholder={templates.inputPlaceholder ?? "Search"}
        />
      </div>
    ),
    suggestions: showSuggestions && (
      <div
        ref={suggestionsRef}
        style={{
          height: "200px",
          display: "flex",
        }}
      >
        <div
          style={{
            width: "200px",
            overflowX: "hidden",
            overflowY: "scroll",
          }}
        >
          {tokenKinds.map((tokenK) => {
            const isActive = tokenK.id === state.entityKind;
            const customizedTokenKind = templates.suggestionKind?.({
              label: tokenK.label,
              active: isActive,
              select: () => setSuggestionKind(tokenK.id ?? ""),
            });
            if (customizedTokenKind) {
              return (
                <React.Fragment key={tokenK.id ?? ""}>
                  <EmbedElement element={customizedTokenKind} />
                </React.Fragment>
              );
            }
            return (
              <div
                key={tokenK.id ?? ""}
                onClick={() => {
                  setSuggestionKind(tokenK.id ?? "");
                }}
                style={{
                  padding: "8px 16px",
                  backgroundColor: isActive ? "lightgray" : "",
                  cursor: "pointer",
                }}
              >
                {tokenK.label}
              </div>
            );
          })}
        </div>
        <div
          style={{
            flexGrow: 1,
            overflowX: "hidden",
            overflowY: "scroll",
          }}
        >
          {suggestions.data?.map((suggestion, index) => {
            const customizedItem = templates.suggestionItem?.({
              label: suggestion.displayDescription,
              kind: suggestion.id as string,
              select: () => {
                addSuggestion(suggestion);
              },
            });
            const type = (() => {
              switch (suggestion.kind) {
                case "ENTITY":
                  return suggestion.type;
                case "PARAM":
                  return suggestion.id;
                case "TOKEN":
                  return (
                    suggestion.entityType ??
                    suggestion.outputKeywordKey ??
                    suggestion.id
                  );
              }
            })();
            const isHighlighted = state.suggestionIndex === index;
            if (customizedItem) {
              return (
                <React.Fragment key={suggestion.id}>
                  <EmbedElement element={customizedItem} />
                </React.Fragment>
              );
            }
            return (
              <ScrollIntoView<HTMLDivElement>
                key={suggestion.id}
                enabled={isHighlighted}
              >
                {(ref) => (
                  <div
                    ref={ref}
                    style={{
                      padding: "8px 16px",
                      backgroundColor: isHighlighted ? "lightgray" : "",
                      cursor: "pointer",
                    }}
                    onMouseEnter={() => {
                      setState((state) => ({
                        ...state,
                        suggestionIndex: index,
                      }));
                    }}
                    onClick={() => {
                      addSuggestion(suggestion);
                    }}
                  >
                    <strong>{type}</strong> {suggestion.displayDescription}
                  </div>
                )}
              </ScrollIntoView>
            );
          })}
        </div>
      </div>
    ),
    tabs: templates.tabs ? (
      <EmbedElement
        element={templates.tabs({
          tabs: tabTokens.map(({ label }) => label),
          activeIndex: state.tabIndex,
          setActiveIndex: setActiveTabIndex,
        })}
      />
    ) : (
      <div style={{ display: "flex" }}>
        {tabTokens.map((tabToken, index) => {
          return (
            <div
              key={index}
              style={{
                width: "100px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: "8px 16px",
                backgroundColor: state.tabIndex === index ? "lightgray" : "",
                cursor: "pointer",
              }}
              onClick={() => setActiveTabIndex(index)}
            >
              {tabToken.label} {state.tabIndex === index}
            </div>
          );
        })}
      </div>
    ),
    results: (
      <div>
        <div style={{ margin: "8px 16px" }}>
          {results.data?.pages[0] && <>{results.data.pages[0].total} results</>}
        </div>
        {renderers &&
          results.data?.pages.map(({ result }, index) => {
            return (
              <React.Fragment key={index}>
                {result.map((result) => {
                  const cusomizedResult = templates.result?.({
                    result,
                    setDetail: setResultForDetail,
                  });
                  return cusomizedResult ? (
                    <React.Fragment key={result.source.id}>
                      <EmbedElement element={cusomizedResult} />
                    </React.Fragment>
                  ) : (
                    <div
                      key={result.source.id}
                      style={{ marginLeft: "16px", marginRight: "16px" }}
                    >
                      <Result
                        result={result}
                        resultRenderers={renderers.resultRenderers}
                        onSelect={() => setResultForDetail(result)}
                      />
                    </div>
                  );
                })}
              </React.Fragment>
            );
          })}
        {results.hasNextPage && !results.isFetchingNextPage && (
          <div style={{ margin: "8px 16px" }} ref={ref}>
            Loading more...
          </div>
        )}
      </div>
    ),
    details:
      renderers &&
      state.detail &&
      (() => {
        const customizedDetail = templates.detail?.({
          result: state.detail,
        });
        if (customizedDetail)
          return <EmbedElement element={customizedDetail} />;
        return (
          <div style={{ padding: "8px 16px" }}>
            <Detail
              result={state.detail}
              sidebarRenderers={renderers.sidebarRenderers}
            />
          </div>
        );
      })(),
  }) as JSX.Element;
}

// TODO get it from back-end
const tokenKinds = [
  { id: undefined, label: "All" },
  { id: "person", label: "People" },
  { id: "organization", label: "Organizations" },
  { id: "email", label: "Emails" },
  { id: "loc", label: "Locations" },
  { id: "*.topic", label: "Topics" },
  { id: "*.documentType", label: "Document Type" },
  { id: "type", label: "Types" },
  { id: "PARAM", label: "Filters" },
];

const defaultTabTokens: Array<{ label: string; tokens: Array<SearchToken> }> = [
  {
    label: "All",
    tokens: [],
  },
  {
    label: "Web",
    tokens: [{ tokenType: "DOCTYPE", keywordKey: "type", values: ["web"] }],
  },
  {
    label: "Document",
    tokens: [
      { tokenType: "DOCTYPE", keywordKey: "type", values: ["document"] },
    ],
  },
];
