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
import { LoginInfoComponentMemo } from "../components/LoginInfo";
import {
  AnalysisRequest,
  AnalysisRequestEntry,
  AnalysisResponse,
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
  SearchToken,
  SortField,
} from "../components/client";
import isEqual from "lodash/isEqual";
import { Configuration, ConfigurationUpdateFunction } from "./entry";
import { Tab, TabsMemo, useTabTokens } from "../components/Tabs";
import { FiltersMemo } from "../components/Filters";
import { SimpleErrorBoundary } from "../components/SimpleErrorBoundary";
import { Search } from "../components/Search";
import { useOpenK9Client } from "../components/client";
import { useQuery } from "react-query";
import { SortResultList } from "../components/SortResultList";
import { FiltersHorizontalMemo } from "../components/FiltersHorizontal";
import { DetailMobileMemo } from "../components/DetailMobile";
import "../i18n";
import { I18nextProvider, useTranslation } from "react-i18next";
import i18next from "i18next";
import { ActiveFilter } from "../components/ActiveFilters";
import { FiltersMobileMemo } from "../components/FiltersMobile";
import { FiltersMobileLiveChangeMemo } from "../components/FiltersMobileLiveChange";
import { DataRangePicker } from "../components/DateRangePicker";
import { SearchMobile } from "../components/SearchMobile";
import { CalendarMobile } from "../components/CalendarModal";
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
  const { sort, setSortResult } = useSortResult({
    configuration,
    onConfigurationChange,
  });
  const { filterTokens, addFilterToken, removeFilterToken } = useFilters({
    configuration,
    onConfigurationChange,
  });
  const { dateRange, setDateRange, dateTokens } = useDateTokens();
  const {
    searchQuery,
    spans,
    selectionsState,
    selectionsDispatch,
    isQueryAnalysisComplete,
    completelySort,
  } = useSearch({
    configuration,
    tabTokens,
    filterTokens,
    dateTokens,
    onQueryStateChange,
  });
  const client = useOpenK9Client();
  const dynamicFilters = useQuery(["handle-dynamic-filters", {}], async () => {
    return await client.handle_dynamic_filters();
  });
  const languageQuery = useQuery(["language", {}], async () => {
    return await client.getLanguageDefault();
  });

  const { i18n } = useTranslation();

  React.useEffect(() => {
    if (languageQuery.data?.value) {
      i18n.changeLanguage(
        remappingLanguage({ language: languageQuery.data.value }),
      );
    }
  }, [languageQuery.data, i18n]);
  const [isMobile, setIsMobile] = React.useState(false);
  React.useEffect(() => {
    const checkIfMobile = () => {
      const isMobileDevice =
        window.innerWidth <= 1024 && window.innerWidth >= 320;
      if (!isMobileDevice) setDetailMobile(null);
      setIsMobile(isMobileDevice);
    };
    checkIfMobile();
    window.addEventListener("resize", checkIfMobile);

    return () => {
      window.removeEventListener("resize", checkIfMobile);
    };
  }, []);
  const { detail, setDetail } = useDetails(searchQuery);
  const { detailMobile, setDetailMobile } = useDetailsMobile(searchQuery);
  const [isVisibleFilters, setIsVisibleFilters] = React.useState(false);
  return (
    <React.Fragment>
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <Search
            configuration={configuration}
            spans={spans}
            selectionsState={selectionsState}
            selectionsDispatch={selectionsDispatch}
            showSyntax={isQueryAnalysisComplete}
            onDetail={setDetail}
            isMobile={isMobile}
            filtersSelect={configuration.filterTokens}
            isVisibleFilters={isVisibleFilters}
          />
        </I18nextProvider>,
        configuration.search,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <TabsMemo
            tabs={tabs}
            selectedTabIndex={selectedTabIndex}
            onSelectedTabIndexChange={setSelectedTabIndex}
            onConfigurationChange={onConfigurationChange}
          />
        </I18nextProvider>,
        configuration.tabs,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersMemo
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
          />
        </I18nextProvider>,
        configuration.filters,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ActiveFilter
            searchQuery={searchQuery}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
          />
        </I18nextProvider>,
        configuration.activeFilters,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersHorizontalMemo
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            onConfigurationChangeExt={
              configuration.filtersHorizontal
                ? configuration.filtersHorizontal.callback
                : () => {}
            }
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
          />
        </I18nextProvider>,
        configuration.filtersHorizontal
          ? configuration.filtersHorizontal.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ResultsMemo
            displayMode={configuration.resultsDisplayMode}
            searchQuery={searchQuery}
            onDetail={setDetail}
            setDetailMobile={setDetailMobile}
            sort={completelySort}
            setSortResult={setSortResult}
            isMobile={isMobile}
          />
        </I18nextProvider>,
        configuration.results,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMemo result={detail} />
        </I18nextProvider>,
        configuration.details,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <LoginInfoComponentMemo />
        </I18nextProvider>,
        configuration.login,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultList setSortResult={setSortResult} />
        </I18nextProvider>,
        configuration.sortable,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMobileMemo
            result={detailMobile}
            setDetailMobile={setDetailMobile}
          />
        </I18nextProvider>,
        configuration.detailMobile,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersMobileMemo
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            configuration={configuration}
            isVisibleFilters={configuration.filtersMobile?.isVisible || false}
            setIsVisibleFilters={configuration.filtersMobile?.setIsVisible}
          />
        </I18nextProvider>,
        configuration.filtersMobile?.element !== undefined
          ? configuration.filtersMobile?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersMobileLiveChangeMemo
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            configuration={configuration}
            isVisibleFilters={
              configuration.filtersMobileLiveChange?.isVisible || false
            }
            setIsVisibleFilters={
              configuration.filtersMobileLiveChange?.setIsVisible
            }
          />
        </I18nextProvider>,
        configuration.filtersMobileLiveChange?.element !== undefined
          ? configuration.filtersMobileLiveChange?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DataRangePicker onChange={setDateRange} calendarDate={dateRange} />
        </I18nextProvider>,
        configuration.dataRangePicker,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <CalendarMobile
            onChange={setDateRange}
            calendarDate={dateRange}
            isVisibleCalendar={true}
          />
        </I18nextProvider>,
        configuration.calendarMobile,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SearchMobile
            configuration={configuration}
            onConfigurationChange={onConfigurationChange}
            spans={spans}
            selectionsState={selectionsState}
            selectionsDispatch={selectionsDispatch}
            showSyntax={isQueryAnalysisComplete}
            onDetail={setDetail}
            dateRange={dateRange}
            onDateRangeChange={setDateRange}
            isMobile={isMobile}
            setSortResult={setSortResult}
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            isVisible={configuration.searchMobile?.isVisible || false}
            setIsVisible={configuration.searchMobile?.setIsVisible}
          />
        </I18nextProvider>,
        configuration.searchMobile?.search !== undefined
          ? configuration.searchMobile?.search
          : null,
      )}
    </React.Fragment>
  );
}

function useSearch({
  configuration,
  tabTokens,
  filterTokens,
  dateTokens,
  onQueryStateChange,
}: {
  configuration: Configuration;
  tabTokens: SearchToken[];
  filterTokens: SearchToken[];
  dateTokens: SearchToken[];
  onQueryStateChange(queryState: QueryState): void;
}) {
  const { searchAutoselect, searchReplaceText, defaultTokens, sort } =
    configuration;
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
  const completelySort = React.useMemo(() => sort, [sort]);
  const searchQueryMemo = React.useMemo(
    () => [
      ...defaultTokens,
      ...tabTokens,
      ...filterTokens,
      ...searchTokens,
      ...dateTokens,
    ],
    [defaultTokens, tabTokens, filterTokens, searchTokens, dateTokens],
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
    completelySort,
  };
}

function useTabs(overrideTabs: (tabs: Array<Tab>) => Array<Tab>) {
  const [selectedTabIndex, setSelectedTabIndex] = React.useState(0);
  const tenantTabs = useTabTokens();
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

function useSortResult({
  configuration,
  onConfigurationChange,
}: {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
}) {
  const sort = configuration.sort;
  const setSortResult = React.useCallback(
    (sortResultNew: SortField) => {
      onConfigurationChange((configuration) => ({
        sort: [sortResultNew],
      }));
    },
    [onConfigurationChange, sort],
  );

  return { sort, setSortResult };
}

export type SearchDateRange = {
  startDate: Date | undefined;
  endDate: Date | undefined;
  keywordKey: string | undefined;
};

function useDateTokens() {
  const [dateRange, setDateRange] = React.useState<SearchDateRange>({
    startDate: undefined,
    endDate: undefined,
    keywordKey: undefined,
  });
  const dateTokens = React.useMemo(() => {
    if (
      dateRange.keywordKey === undefined &&
      dateRange.startDate === undefined &&
      dateRange.endDate === undefined
    ) {
      return [];
    }
    return [
      {
        tokenType: "DATE",
        keywordKey: dateRange.keywordKey,
        extra: {
          gte: dateRange.startDate?.getTime(),
          lte: dateRange.endDate?.getTime(),
        },
      } as SearchToken,
    ];
  }, [dateRange.endDate, dateRange.keywordKey, dateRange.startDate]);
  return { dateRange, setDateRange, dateTokens };
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

function useDetailsMobile(searchQuery: Array<SearchToken>) {
  const [detailMobile, setDetailMobile] =
    React.useState<GenericResultItem<unknown> | null>(null);
  React.useEffect(() => {
    setDetailMobile(null);
  }, [searchQuery]);
  return { detailMobile, setDetailMobile };
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
      return (
        (token && analysisTokenToSearchToken(token)) ?? {
          tokenType: "TEXT",
          values: [span.text],
          filter: false,
        }
      );
    });
}

function analysisTokenToSearchToken(token: AnalysisToken): SearchToken | null {
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
    case "AUTOCOMPLETE":
      return null;
    case "AUTOCORRECT":
      return null;
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
      .filter((entry) => {
        // togliere validazione quando fixato lato be
        const isValidEntry = entry.start >= 0;
        if (!isValidEntry) {
          console.warn(`Invalid entry: `, entry);
        }
        return isValidEntry;
      }),
  };
}

function remappingLanguage({ language }: { language: string }) {
  switch (language) {
    case "it_IT":
      return "it";
    case "es_ES":
      return "es";
    case "en_US":
      return "en";
    case "fr_FR":
      return "fr";
    default:
      return "en";
  }
}
