import React from "react";
import ReactDOM from "react-dom";
import { useDebounce } from "../components/useDebounce";
import { DetailMemo } from "../components/Detail";
import { ResultsMemo } from "../components/ResultList";
import {
  getAutoSelections,
  isOverlapping,
  useSelections,
} from "../components/useSelections";
import { useLoginInfo } from "../components/useLogin";
import { LoginInfoComponentMemo } from "../components/LoginInfo";
import {
  AnalysisRequest,
  AnalysisRequestEntry,
  AnalysisResponse,
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
  SearchToken,
} from "@openk9/rest-api";
import isEqual from "lodash/isEqual";
import { Configuration, ConfigurationUpdateFunction } from "./entry";
import { Tab, TabsMemo, useTenantTabTokens } from "../components/Tabs";
import { FiltersMemo } from "../components/Filters";
import { SimpleErrorBoundary } from "../components/SimpleErrorBoundary";
import { Search } from "../components/Search";
import { useOpenK9Client } from "../components/client";
import { useQuery } from "react-query";

type MainProps = {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  onQueryStateChange(queryState: QueryState): void;
};
export function Main({
  configuration,
  onConfigurationChange,
  onQueryStateChange,
}: MainProps) {
  const { tabs, selectedTabIndex, setSelectedTabIndex, tabTokens } = useTabs(
    configuration.overrideTabs,
  );
  const { filterTokens, addFilterToken, removeFilterToken } = useFilters({
    configuration,
    onConfigurationChange,
  });
  const {
    searchQuery,
    spans,
    selectionsState,
    selectionsDispatch,
    isQueryAnalysisComplete,
  } = useSearch({
    configuration,
    tabTokens,
    filterTokens,
    onQueryStateChange,
  });
  const { detail, setDetail } = useDetails(searchQuery);
  const login = useLoginInfo();
  return (
    <React.Fragment>
      {renderPortal(
        <Search
          configuration={configuration}
          onConfigurationChange={onConfigurationChange}
          spans={spans}
          selectionsState={selectionsState}
          selectionsDispatch={selectionsDispatch}
          showSyntax={isQueryAnalysisComplete}
          onDetail={setDetail}
        />,
        configuration.search,
      )}
      {renderPortal(
        <TabsMemo
          tabs={tabs}
          selectedTabIndex={selectedTabIndex}
          onSelectedTabIndexChange={setSelectedTabIndex}
          onConfigurationChange={onConfigurationChange}
        />,
        configuration.tabs,
      )}
      {renderPortal(
        <FiltersMemo
          searchQuery={searchQuery}
          onAddFilterToken={addFilterToken}
          onRemoveFilterToken={removeFilterToken}
        />,
        configuration.filters,
      )}
      {renderPortal(
        <ResultsMemo
          displayMode={configuration.resultsDisplayMode}
          searchQuery={searchQuery}
          onDetail={setDetail}
        />,
        configuration.results,
      )}
      {renderPortal(<DetailMemo result={detail} />, configuration.details)}
      {renderPortal(
        <LoginInfoComponentMemo
          loginState={login.state}
          onLogin={login.login}
          onLogout={login.logout}
        />,
        configuration.login,
      )}
    </React.Fragment>
  );
}

function useSearch({
  configuration,
  tabTokens,
  filterTokens,
  onQueryStateChange,
}: {
  configuration: Configuration;
  tabTokens: SearchToken[];
  filterTokens: SearchToken[];
  onQueryStateChange(queryState: QueryState): void;
}) {
  const { searchAutoselect, searchReplaceText, defaultTokens } = configuration;
  const [selectionsState, selectionsDispatch] = useSelections();
  const debounced = useDebounce(selectionsState, 600);
  const queryAnalysis = useQueryAnalysis({
    searchText: debounced.text,
    tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
      token ? [{ text, start, end, token }] : [],
    ),
  });
  const spans = React.useMemo(
    () => calculateSpans(selectionsState.text, queryAnalysis.data?.analysis),
    [queryAnalysis.data?.analysis, selectionsState.text],
  );
  const searchTokens = React.useMemo(
    () =>
      deriveSearchQuery(
        spans,
        selectionsState.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      ),
    [spans, selectionsState.selection],
  );
  const searchQueryMemo = React.useMemo(
    () => [...defaultTokens, ...tabTokens, ...filterTokens, ...searchTokens],
    [defaultTokens, tabTokens, filterTokens, searchTokens],
  );
  const searchQuery = useDebounce(searchQueryMemo, 600);
  const isQueryAnalysisComplete =
    selectionsState.text === debounced.text &&
    queryAnalysis.data !== undefined &&
    !queryAnalysis.isPreviousData;

  React.useEffect(() => {
    onQueryStateChange({
      defaultTokens,
      tabTokens,
      filterTokens,
      searchTokens,
    });
  }, [
    onQueryStateChange,
    defaultTokens,
    tabTokens,
    filterTokens,
    searchTokens,
  ]);
  React.useEffect(() => {
    if (
      searchAutoselect &&
      queryAnalysis.data &&
      queryAnalysis.data.searchText === selectionsState.text
    ) {
      const autoSelections = getAutoSelections(
        selectionsState.selection,
        queryAnalysis.data.analysis,
      );
      for (const selection of autoSelections) {
        selectionsDispatch({
          type: "set-selection",
          replaceText: false,
          selection,
        });
      }
    }
  }, [
    searchAutoselect,
    searchReplaceText,
    selectionsDispatch,
    queryAnalysis.data,
    selectionsState.selection,
    selectionsState.text,
  ]);
  return {
    searchQuery,
    spans,
    selectionsState,
    selectionsDispatch,
    isQueryAnalysisComplete,
  };
}

function useTabs(overrideTabs: (tabs: Array<Tab>) => Array<Tab>) {
  const [selectedTabIndex, setSelectedTabIndex] = React.useState(0);
  const tenantTabs = useTenantTabTokens();
  const tabs = React.useMemo(
    () => overrideTabs(tenantTabs),
    [tenantTabs, overrideTabs],
  );
  const tabTokens = React.useMemo(
    () => tabs[selectedTabIndex]?.tokens ?? [],
    [selectedTabIndex, tabs],
  );
  return { tabTokens, tabs, selectedTabIndex, setSelectedTabIndex };
}

function useFilters({
  configuration,
  onConfigurationChange,
}: {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
}) {
  const filterTokens = configuration.filterTokens;
  const addFilterToken = React.useCallback(
    (searchToken: SearchToken) => {
      onConfigurationChange((configuration) => ({
        filterTokens: [...configuration.filterTokens, searchToken],
      }));
    },
    [onConfigurationChange],
  );
  const removeFilterToken = React.useCallback(
    (searchToken: SearchToken) => {
      onConfigurationChange((configuration) => ({
        filterTokens: configuration.filterTokens.filter(
          (token) => !isEqual(token, searchToken),
        ),
      }));
    },
    [onConfigurationChange],
  );
  const defaultTokens = configuration.defaultTokens;
  return { defaultTokens, filterTokens, addFilterToken, removeFilterToken };
}

function useDetails(searchQuery: Array<SearchToken>) {
  const [detail, setDetail] = React.useState<GenericResultItem<unknown> | null>(
    null,
  );
  React.useEffect(() => {
    setDetail(null);
  }, [searchQuery]);
  return { detail, setDetail };
}

function renderPortal(
  node: React.ReactNode,
  container: Element | string | null,
): React.ReactNode {
  const element =
    typeof container === "string"
      ? document.querySelector(container)
      : container;
  return (
    <SimpleErrorBoundary>
      <React.Suspense>
        {element ? (ReactDOM.createPortal(node, element) as any) : null}
      </React.Suspense>
    </SimpleErrorBoundary>
  );
}

export type QueryState = {
  defaultTokens: Array<SearchToken>;
  tabTokens: Array<SearchToken>;
  filterTokens: Array<SearchToken>;
  searchTokens: Array<SearchToken>;
};

function deriveSearchQuery(
  spans: AnalysisResponseEntry[],
  selection: AnalysisRequestEntry[],
) {
  return spans
    .map((span) => ({ ...span, text: span.text.trim() }))
    .filter((span) => span.text)
    .map((span): SearchToken => {
      const token =
        selection.find(
          (selection) =>
            selection.start === span.start && selection.end === span.end,
        )?.token ?? null;
      if (token) {
        return analysisTokenToSearchToken(token);
      }
      return { tokenType: "TEXT", values: [span.text], filter: false };
    });
}

function analysisTokenToSearchToken(token: AnalysisToken): SearchToken {
  switch (token.tokenType) {
    case "DATASOURCE":
      return {
        tokenType: "DATASOURCE",
        values: [token.value],
        filter: false,
      };
    case "DOCTYPE":
      return {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: [token.value],
        filter: true,
      };
    case "ENTITY":
      return {
        tokenType: "ENTITY",
        keywordKey: token.keywordKey,
        entityType: token.entityType,
        entityName: token.entityName,
        values: [token.value],
        filter: false,
      };
    case "TEXT":
      return {
        tokenType: "TEXT",
        keywordKey: token.keywordKey,
        values: [token.value],
        filter: false,
      };
  }
}

function calculateSpans(
  text: string,
  analysis: AnalysisResponseEntry[] | undefined,
): AnalysisResponseEntry[] {
  const spans: Array<AnalysisResponseEntry> = [
    { text: "", start: 0, end: 0, tokens: [] },
  ];
  for (let i = 0; i < text.length; ) {
    const found = analysis?.find(
      ({ start, text }) => i === start && text !== "",
    );
    if (found) {
      spans.push(found);
      i += found.text.length;
      spans.push({ text: "", start: i, end: i, tokens: [] });
    } else {
      const last = spans[spans.length - 1];
      last.text += text[i];
      last.end += 1;
      i += 1;
    }
  }
  return spans.filter((span) => span.text);
}

function useQueryAnalysis(request: AnalysisRequest) {
  const client = useOpenK9Client();
  return useQuery(
    ["query-anaylis", request] as const,
    async ({ queryKey: [, request] }) =>
      fixQueryAnalysisResult(await client.fetchQueryAnalysis(request)),
  );
}
// TODO: togliere una volta implementata gestione sugestion sovrapposte
function fixQueryAnalysisResult(data: AnalysisResponse) {
  return {
    ...data,
    analysis: data.analysis
      .reverse()
      .filter((entry, index, array) =>
        array
          .slice(0, index)
          .every((previous) => !isOverlapping(previous, entry)),
      )
      .reverse()
      .filter(entry =>{
        // togliere validazione quando fixato lato be
        const isValidEntry = entry.start >= 0;
        if (!isValidEntry) {
          console.warn(`Invalid entry: `, entry);
        }
        return isValidEntry  
      }), 
  };
}
