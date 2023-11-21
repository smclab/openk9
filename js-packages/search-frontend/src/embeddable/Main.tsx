import React from "react";
import ReactDOM from "react-dom";
import { useDebounce } from "../components/useDebounce";
import { DetailMemo } from "../components/Detail";
import { ResultsMemo } from "../components/ResultList";
import {
  getAutoSelections,
  isOverlapping,
  useSelections,
  useSelectionsOnClick,
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
import { ChangeLanguage } from "../components/ChangeLanguage";
import { DataRangePickerVertical } from "../components/DateRangePickerVertical";
import { TotalResults } from "../components/TotalResults";
import { ResultsPaginationMemo } from "../components/ResultListPagination";
import _ from "lodash";
import { RemoveFilters } from "../components/RemoveFilters";

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
  const { sort, setSortResult } = useSortResult({
    configuration,
    onConfigurationChange,
  });
  const { filterTokens, addFilterToken, removeFilterToken } = useFilters({
    configuration,
    onConfigurationChange,
  });
  const { dateRange, setDateRange, dateTokens } = useDateTokens();

  const client = useOpenK9Client();
  const dynamicFilters = useQuery(["handle-dynamic-filters", {}], async () => {
    return await client.handle_dynamic_filters();
  });
  const languageQuery = useQuery(["language", {}], async () => {
    return await client.getLanguageDefault();
  });

  const { i18n } = useTranslation();

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

  const [isMobileMinWidth, setIsMobileMinWIdth] = React.useState(false);
  React.useEffect(() => {
    const checkIfMobile = () => {
      const isMobileMinWidth = window.innerWidth <= 380;
      setIsMobileMinWIdth(isMobileMinWidth);
    };
    checkIfMobile();
    window.addEventListener("resize", checkIfMobile);

    return () => {
      window.removeEventListener("resize", checkIfMobile);
    };
  }, []);

  const [isVisibleFilters, setIsVisibleFilters] = React.useState(false);
  const activeLanguage = i18next.language;
  const languages = useQuery(["date-label-languages", {}], async () => {
    return await client.getLanguages();
  });
  const [languageSelect, setLanguageSelect] = React.useState("");
  const { tabs, selectedTabIndex, setSelectedTabIndex, tabTokens } = useTabs(
    configuration.overrideTabs,
    languageSelect,
  );
  const [currentPage, setCurrentPage] = React.useState<number>(0);
  const isSearchOnInputChange = !configuration.searchConfigurable?.btnSearch;
  const isDynamicElement = configuration.dynamicElement || [
    "filter",
    "search",
    "tab",
  ];
  const {
    searchQuery,
    spans,
    selectionsState,
    selectionsDispatch,
    isQueryAnalysisComplete,
    completelySort,
    setIsSaveQuery,
  } = isSearchOnInputChange
    ? useSearchOnClick({
        configuration,
        tabTokens,
        filterTokens,
        dateTokens,
        onQueryStateChange,
        setCurrentPage,
      })
    : useSearch({
        configuration,
        tabTokens,
        filterTokens,
        dateTokens,
        onQueryStateChange,
        setCurrentPage,
      });
  const { detail, setDetail } = useDetails(searchQuery);
  const { detailMobile, setDetailMobile, idPreview, setIdPreview } = useDetailsMobile(searchQuery);
  React.useEffect(() => {
    if (languageQuery.data?.value) {
      i18n.changeLanguage(
        remappingLanguage({ language: languageQuery.data.value }),
      );
      setLanguageSelect(languageQuery.data.value);
    }
  }, [languageQuery.data, i18n]);
  const [sortAfterKey, setSortAfterKey] = React.useState("");
  const [totalResult, setTotalResult] = React.useState<number | null>(null);
  const numberOfResults = configuration.numberResult || 10;
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
            isSearchOnInputChange={isSearchOnInputChange}
          />
        </I18nextProvider>,
        configuration.search,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <Search
            configuration={configuration}
            spans={spans}
            selectionsState={selectionsState}
            selectionsDispatch={selectionsDispatch}
            showSyntax={
              configuration.searchConfigurable?.isShowSyntax === false
                ? false
                : isQueryAnalysisComplete
            }
            onDetail={setDetail}
            isMobile={isMobile}
            filtersSelect={configuration.filterTokens}
            isVisibleFilters={isVisibleFilters}
            btnSearch={configuration.searchConfigurable?.btnSearch ?? false}
            isSearchOnInputChange={isSearchOnInputChange}
            defaultValue={configuration.searchConfigurable?.defaultValue ?? ""}
            htmlKey={configuration.searchConfigurable?.htmlKey}
          />
        </I18nextProvider>,
        configuration.searchConfigurable
          ? configuration.searchConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <TabsMemo
            tabs={tabs}
            selectedTabIndex={selectedTabIndex}
            onSelectedTabIndexChange={setSelectedTabIndex}
            onConfigurationChange={onConfigurationChange}
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.tabs,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <RemoveFilters
            onConfigurationChange={onConfigurationChange}
          />
        </I18nextProvider>,
        configuration.removeFilters,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <TabsMemo
            tabs={tabs}
            selectedTabIndex={selectedTabIndex}
            onSelectedTabIndexChange={setSelectedTabIndex}
            onConfigurationChange={onConfigurationChange}
            language={languageSelect}
            onAction={configuration.tabsConfigurable?.onAction}
          />
        </I18nextProvider>,
        configuration.tabsConfigurable
          ? configuration.tabsConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersMemo
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            numberOfResults={numberOfResults}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            sortAfterKey={sortAfterKey}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            language={languageSelect}
            isDynamicElement={isDynamicElement}
          />
        </I18nextProvider>,
        configuration.filters,
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
            sortAfterKey={sortAfterKey}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            language={languageSelect}
            isCollapsable={
              configuration.filtersConfigurable?.isCollapsable ?? true
            }
            numberItems={configuration.filtersConfigurable?.numberItems}
            numberOfResults={numberOfResults}
            isDynamicElement={isDynamicElement}
            noResultMessage={configuration.filtersConfigurable?.noResultMessage}
          />
        </I18nextProvider>,
        configuration.filtersConfigurable
          ? configuration.filtersConfigurable.element
          : null,
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
            isDynamicElement={isDynamicElement}
            numberOfResults={numberOfResults}
            sortAfterKey={sortAfterKey}
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
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.filtersHorizontal
          ? configuration.filtersHorizontal.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ResultsMemo
            setTotalResult={setTotalResult}
            displayMode={configuration.resultsDisplayMode}
            searchQuery={searchQuery}
            onDetail={setDetail}
            setDetailMobile={setDetailMobile}
            sort={completelySort}
            setSortResult={setSortResult}
            isMobile={isMobile}
            overChangeCard={true}
            language={languageSelect}
            setSortAfterKey={setSortAfterKey}
            sortAfterKey={sortAfterKey}
            numberOfResults={numberOfResults}
            setIdPreview={setIdPreview}
          />
        </I18nextProvider>,
        configuration.results,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <TotalResults totalResult={totalResult} />
        </I18nextProvider>,
        configuration.totalResult,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ResultsMemo
            setTotalResult={setTotalResult}
            displayMode={configuration.resultsDisplayMode}
            searchQuery={searchQuery}
            onDetail={setDetail}
            setDetailMobile={setDetailMobile}
            sort={completelySort}
            setSortResult={setSortResult}
            isMobile={isMobile}
            overChangeCard={configuration.resultList?.changeOnOver || false}
            language={languageSelect}
            setSortAfterKey={setSortAfterKey}
            sortAfterKey={sortAfterKey}
            numberOfResults={numberOfResults}
            setIdPreview={setIdPreview}
          />
        </I18nextProvider>,
        configuration.resultList ? configuration.resultList.element : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ResultsPaginationMemo
            setTotalResult={setTotalResult}
            displayMode={configuration.resultsDisplayMode}
            searchQuery={searchQuery}
            onDetail={setDetail}
            setDetailMobile={setDetailMobile}
            sort={completelySort}
            setSortResult={setSortResult}
            isMobile={isMobile}
            overChangeCard={false}
            language={languageSelect}
            setSortAfterKey={setSortAfterKey}
            sortAfterKey={sortAfterKey}
            numberOfResults={totalResult || 0}
            pagination={numberOfResults}
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            anchor={configuration.resultListPagination?.anchor}
          />
        </I18nextProvider>,
        configuration.resultListPagination
          ? configuration.resultListPagination.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMemo result={detail} actionOnCLose={()=>{}}/>
        </I18nextProvider>,
        configuration.details,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <LoginInfoComponentMemo isMobile={isMobileMinWidth} />
        </I18nextProvider>,
        configuration.login,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultList
            setSortResult={setSortResult}
            relevance={
              configuration.sortResultConfigurable?.relevance || "relevance"
            }
            HtmlString={configuration.sortResultConfigurable?.htmlKey || ""}
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.sortResultConfigurable
          ? configuration.sortResultConfigurable.sort
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultList setSortResult={setSortResult} />
        </I18nextProvider>,
        configuration.sortable,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultList
            setSortResult={setSortResult}
            relevance={configuration.sortableConfigurable?.relevance}
          />
        </I18nextProvider>,
        configuration.sortableConfigurable
          ? configuration.sortableConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <ChangeLanguage
            setChangeLanguage={setLanguageSelect}
            languages={languages.data}
            activeLanguage={languageSelect}
            i18nElement={i18n}
          />
        </I18nextProvider>,
        configuration.changeLanguage,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMobileMemo
            result={detailMobile}
            setDetailMobile={setDetailMobile}
            onClose={()=>{
              const recoveryButton= document.getElementById("preview-card-"+idPreview) as HTMLButtonElement
              if(recoveryButton)
              recoveryButton.focus();
            }}
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
            language={languageSelect}
            sortAfterKey={sortAfterKey}
            isDynamicElement={isDynamicElement}
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
            sortAfterKey={sortAfterKey}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            configuration={configuration}
            whoIsDynamic={isDynamicElement}
            numberItems={configuration.filtersConfigurable?.numberItems}
            isVisibleFilters={
              configuration.filtersMobileLiveChange?.isVisible || false
            }
            setIsVisibleFilters={
              configuration.filtersMobileLiveChange?.setIsVisible
            }
            tabs={tabs}
            onSelectedTabIndexChange={setSelectedTabIndex}
            selectedTabIndex={selectedTabIndex}
            viewTabs={configuration.filtersMobileLiveChange?.viewTabs ?? false}
            language={languageSelect}
            isCollapsable={
              configuration.filtersMobileLiveChange?.isCollapsable ?? true
            }
            numberOfResults={numberOfResults}
          />
        </I18nextProvider>,
        configuration.filtersMobileLiveChange?.element !== undefined
          ? configuration.filtersMobileLiveChange?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DataRangePicker
            onChange={setDateRange}
            calendarDate={dateRange}
            start={configuration.dataRangePicker?.start}
            end={configuration.dataRangePicker?.end}
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.dataRangePicker?.element !== undefined
          ? configuration.dataRangePicker?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DataRangePickerVertical
            onChange={setDateRange}
            calendarDate={dateRange}
            language={languageSelect}
            start={configuration.dataRangePickerVertical?.start}
            end={configuration.dataRangePickerVertical?.end}
            classTab={tabs[selectedTabIndex]?.label}
            readOnly={configuration.dataRangePickerVertical?.readOnly ?? false}
          />
        </I18nextProvider>,
        configuration.dataRangePickerVertical?.element !== undefined
          ? configuration.dataRangePickerVertical?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DataRangePicker
            onChange={setDateRange}
            calendarDate={dateRange}
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.calendar,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <CalendarMobile
            onChange={setDateRange}
            calendarDate={dateRange}
            isVisibleCalendar={configuration.calendarMobile?.isVisible || false}
            setIsVisibleCalendar={configuration.calendarMobile?.setIsVisible}
            startDate={configuration.calendarMobile?.startDate}
            endDate={configuration.calendarMobile?.endDate}
            focusedInput={configuration.calendarMobile?.focusedInput}
            setStartDate={configuration.calendarMobile?.setStartDate}
            setEndDate={configuration.calendarMobile?.setEndDate}
            setFocusedInput={configuration.calendarMobile?.setFocusedInput}
            activeLanguage={activeLanguage}
            isCLickReset={configuration.calendarMobile?.isCLickReset || false}
            setIsCLickReset={configuration.calendarMobile?.setIsCLickReset}
            language={languageSelect}
          />
        </I18nextProvider>,
        configuration.calendarMobile?.element !== undefined
          ? configuration.calendarMobile?.element
          : null,
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

function useSearchOnClick({
  configuration,
  tabTokens,
  filterTokens,
  dateTokens,
  onQueryStateChange,
  setCurrentPage,
}: {
  configuration: Configuration;
  tabTokens: SearchToken[];
  filterTokens: SearchToken[];
  dateTokens: SearchToken[];
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  onQueryStateChange(queryState: QueryState): void;
}) {
  const { searchAutoselect, searchReplaceText, defaultTokens, sort } =
    configuration;
  const [selectionsState, selectionsDispatch] = useSelectionsOnClick();
  const [isSvaleQuery, setIsSaveQuery] = React.useState(false);

  const debounced = useDebounce(selectionsState, 600);
  const queryAnalysis = useQueryAnalysis({
    searchText: debounced.text || "",
    tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
      token ? [{ text, start, end, token }] : [],
    ),
  });
  const spans = React.useMemo(
    () =>
      calculateSpans(selectionsState.text || "", queryAnalysis.data?.analysis),
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

  const newSearch: SearchToken[] = searchTokens.map((searchTokenS) => {
    searchTokenS.isSearch = true;
    return searchTokenS;
  });
  const newTokenFilter: SearchToken[] = React.useMemo(
    () => createFilter(filterTokens),
    [filterTokens],
  );

  const completelySort = React.useMemo(() => sort, [sort]);
  const searchQueryMemo = React.useMemo(
    () => [
      ...defaultTokens,
      ...tabTokens,
      ...newTokenFilter,
      ...newSearch,
      ...dateTokens,
    ],
    [defaultTokens, tabTokens, newTokenFilter, searchTokens, dateTokens],
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
    setCurrentPage(0);
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
    setIsSaveQuery,
  };
}

function useSearch({
  configuration,
  tabTokens,
  filterTokens,
  dateTokens,
  onQueryStateChange,
  setCurrentPage,
}: {
  configuration: Configuration;
  tabTokens: SearchToken[];
  filterTokens: SearchToken[];
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  dateTokens: SearchToken[];
  onQueryStateChange(queryState: QueryState): void;
}) {
  const { searchAutoselect, searchReplaceText, defaultTokens, sort } =
    configuration;
  const [selectionsState, selectionsDispatch] = useSelections();
  const [isSvaleQuery, setIsSaveQuery] = React.useState(false);
  const debounced = useDebounce(selectionsState, 600);

  const queryAnalysis = useQueryAnalysis({
    searchText: debounced.textOnChange || "",
    tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
      token ? [{ text, start, end, token }] : [],
    ),
  });

  const spans = React.useMemo(
    () =>
      calculateSpans(
        debounced.textOnChange || "",
        queryAnalysis.data?.analysis,
      ),
    [queryAnalysis.data?.analysis, debounced.textOnChange],
  );
  const searchTokens = React.useMemo(() => {
    return deriveSearchQuery(
      spans,
      selectionsState.selection.flatMap(({ text, start, end, token }) =>
        token ? [{ text, start, end, token }] : [],
      ),
    );
  }, [isSvaleQuery, debounced.text]);
  const newSearch: SearchToken[] = searchTokens.map((searchTokenS) => {
    searchTokenS.isSearch = true;
    return searchTokenS;
  });

  const newTokenFilter: SearchToken[] = React.useMemo(
    () => createFilter(filterTokens),
    [filterTokens],
  );
  const completelySort = React.useMemo(() => sort, [sort]);
  const searchQueryMemo = React.useMemo(
    () => [
      ...defaultTokens,
      ...tabTokens,
      ...newTokenFilter,
      ...newSearch,
      ...dateTokens,
    ],
    [defaultTokens, tabTokens, newTokenFilter, dateTokens, searchTokens],
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
    setCurrentPage(0);
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
          selection: {
            token: selection.token,
            end: selection.end,
            isAuto: selection.isAuto,
            start: selection.start,
            text: selection.text,
            textOnChange: selection.text,
          },
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
    setIsSaveQuery,
  };
}

function useTabs(
  overrideTabs: (tabs: Array<Tab>) => Array<Tab>,
  language: string,
) {
  const [selectedTabIndex, setSelectedTabIndex] = React.useState(0);
  const tenantTabs = useTabTokens();

  const tabs = React.useMemo(
    () => overrideTabs(tenantTabs),
    [tenantTabs, overrideTabs, language],
  );

  const tabTokens = React.useMemo(() => {
    const createTab = tabs[selectedTabIndex]?.tokens;
    const completeTab = createTab?.map((tab) => ({ ...tab, isTab: true }));
    return completeTab ?? [];
  }, [selectedTabIndex, tabs, language]);

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
        filterTokens: configuration.filterTokens.filter((token) => {
          if (searchToken && searchToken.values && token && token.values)
            return !(
              token.suggestionCategoryId === searchToken.suggestionCategoryId &&
              token.isFilter === searchToken.isFilter &&
              token.keywordKey === searchToken.keywordKey &&
              token.tokenType === searchToken.tokenType &&
              containsAtLeastOne(searchToken.values, token.values)
            );
          return true;
        }),
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
  const [idPreview,setIdPreview]=React.useState("");
  const [detailMobile, setDetailMobile] =
    React.useState<GenericResultItem<unknown> | null>(null);
  React.useEffect(() => {
    setDetailMobile(null);
  }, [searchQuery]);
  return { detailMobile, setDetailMobile,idPreview, setIdPreview };
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
        selection.find((selection) => {
          return selection.start === span.start && selection.end === span.end;
        })?.token ?? null;
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

function useQueryAnalysisOnCLick(request: AnalysisRequest) {
  const client = useOpenK9Client();
  return useQuery(
    ["query-anaylis", request] as const,
    async ({ queryKey: [, request] }) =>
      fixQueryAnalysisResult(await client.fetchQueryAnalysis(request)),
  );
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
function fixQueryAnalysisResult(data: AnalysisResponse | null) {
  if (data)
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

function createFilter(filterTokens: SearchToken[]): SearchToken[] {
  const groupedTokens: { [key: number]: SearchToken } = {};

  for (const item of filterTokens) {
    if (
      item &&
      item.values &&
      item.values.length > 0 &&
      item.suggestionCategoryId
    ) {
      const suggestionCategoryId = item.suggestionCategoryId;

      if (!groupedTokens[suggestionCategoryId]) {
        groupedTokens[suggestionCategoryId] = _.cloneDeep(item);
      } else {
        groupedTokens[suggestionCategoryId].values!.push(item.values[0]);
      }
    }
  }

  return Object.values(groupedTokens);
}

export function remappingLanguage({ language }: { language: string }) {
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

function remappingLanguageToBack({ language }: { language: string }) {
  switch (language) {
    case "it":
      return "it_IT";
    case "es":
      return "es_ES";
    case "en":
      return "en_US";
    case "fr":
      return "fr_FR";
    default:
      return "en_US";
  }
}

const containsAtLeastOne = (array1: string[], array2: string[]): boolean => {
  return array1.some((element1) => {
    return array2.includes(element1);
  });
};
