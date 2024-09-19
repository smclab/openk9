import React from "react";
import ReactDOM from "react-dom";
import { useDebounce } from "../components/useDebounce";
import { DetailMemo } from "../components/Detail";
import { ResultsMemo } from "../components/ResultList";
import {
  SelectionsAction,
  SelectionsState,
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
import { Configuration, ConfigurationUpdateFunction } from "./entry";
import TabsSkeleton, { Tab, TabsMemo, useTabTokens } from "../components/Tabs";
import { FiltersMemo, SkeletonFilters } from "../components/Filters";
import { SimpleErrorBoundary } from "../components/SimpleErrorBoundary";
import { Search } from "../components/Search";
import { useOpenK9Client } from "../components/client";
import { useQuery } from "react-query";
import { SortResultListMemo } from "../components/SortResultList";
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
import { TotalResultsMobile } from "../components/TotalResultsMobile";
import {
  ResultsPaginationMemo,
  SkeletonResult,
} from "../components/ResultListPagination";
import _, { isEqual } from "lodash";
import { RemoveFilters } from "../components/RemoveFilters";
import { WhoIsDynamic } from "../components/FilterCategoryDynamic";
import { SortResultListCustom } from "../components/SortResultListCustom";
import SelectComponent from "../components/Select";
import SortResults, { Options } from "../components/SortResults";
import { SearchWithSuggestions } from "../components/SearchWithSuggestions";

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
  const activeLanguage = i18next.language;

  //retrieving information from the configuration.
  const classNameLabelSort = configuration.sortResults?.classNameLabel;
  const debounceTimeSearch = configuration.debounceTimeSearch || 600;
  const memoryResults = configuration.memoryResults || false;
  const numberOfResults = configuration.numberResult || 10;
  const numberResultOfFilters = configuration.numberResultOfFilters || 10;
  const useQueryString = configuration.useQueryString;
  const useQueryStringFilters = configuration.useQueryStringFilters;
  const useKeycloak = configuration.useKeycloak;
  const isHoverChangeDetail = configuration.resultList?.changeOnOver ?? true;
  const isActiveSkeleton = configuration?.isActiveSkeleton || false;
  const viewButton = configuration?.viewButton;
  const queryStringValues = configuration?.queryStringValues || [
    "text",
    "selection",
    "textOnChange",
    "filters",
  ];
  const skeletonCustom = {
    tabs: configuration.skeletonTabsCustom,
    results: configuration.skeletonResultsCustom,
    filters: configuration.skeletonFiltersCustom,
    suggestion: configuration.skeletonSuggestionCustom,
  };

  //state
  const [currentPage, setCurrentPage] = React.useState<number>(0);
  const [dynamicData, setDynamicData] = React.useState<Array<WhoIsDynamic>>([]);
  const [isMobile, setIsMobile] = React.useState(false);
  const [isMobileMinWidth, setIsMobileMinWIdth] = React.useState(false);
  const [languageSelect, setLanguageSelect] = React.useState("");
  const [selectionsState, selectionsDispatch] = useSelections({
    useKeycloak,
    useQueryString,
    defaultString: configuration.defaultString || "",
    queryStringValues,
  });
  const [selectionsStateSuggestions, selectionsDispatchSuggestions] =
    useSelections({
      useKeycloak,
      useQueryString,
      defaultString: configuration.defaultString || "",
      queryStringValues,
    });
  const [sortAfterKey, setSortAfterKey] = React.useState("");
  const [totalResult, setTotalResult] = React.useState<number | null>(null);
  const [prevSearchQuery, setPrevSearchQuery] = React.useState([]);
  const [prevSearchQueryMobile, setPrevSearchQueryMobile] = React.useState([]);
  const [viewButtonDetail, setViewButtonDetail] = React.useState(false);

  const { dateRange, setDateRange, dateTokens } = useDateTokens();
  const { filterTokens, addFilterToken, removeFilterToken, resetFilter } =
    useFilters({
      configuration,
      onConfigurationChange,
      selectionsState,
      selectionsDispatch,
      useQueryStringFilters,
    });
  const { i18n } = useTranslation();

  const {
    tabs,
    selectedTabIndex,
    setSelectedTabIndex,
    tabTokens,
    sortList,
    tabSelected,
    setSort,
  } = useTabs(configuration.overrideTabs, languageSelect);
  const { resetSort, selectedSort, setSelectedSort } = useSortResult({
    configuration,
    onConfigurationChange,
    setSortAfterKey,
    sortList,
    setSort,
  });

  const { searchQuery, spans, isQueryAnalysisComplete, completelySort } =
    useSearch({
      configuration,
      debounceTimeSearch,
      tabTokens: { tabToken: tabTokens.tabTokens, sort: tabTokens.sort },
      filterTokens,
      dateTokens,
      onQueryStateChange,
      setCurrentPage,
      selectionsState,
      selectionsDispatch,
    });
  const { isQueryAnalysisCompleteSuggestions, spansSuggestions } =
    useQueryAnalysisWithoutSearch({
      configuration,
      selectionsState: selectionsStateSuggestions,
      debounceTimeSearch,
    });
  const { detail, setDetail } = useDetails(
    searchQuery,
    setPrevSearchQuery,
    prevSearchQuery,
  );
  const { detailMobile, setDetailMobile, idPreview, setIdPreview } =
    useDetailsMobile(
      searchQuery,
      prevSearchQueryMobile,
      setPrevSearchQueryMobile,
    );
  const { dynamicFilters, languageQuery, whoIsDynamicResponse, languages } =
    recoveryDataBackEnd();

  //Effect
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

  React.useEffect(() => {
    if (whoIsDynamicResponse.isSuccess) {
      const newData = factoryWhoIsDynamic({
        whoIsDynamicResponse: whoIsDynamicResponse.data,
      });
      setDynamicData(newData);
    }
  }, [whoIsDynamicResponse.isSuccess, whoIsDynamicResponse.data]);

  React.useEffect(() => {
    if (languageQuery.data?.value) {
      i18n.changeLanguage(
        remappingLanguage({ language: languageQuery.data.value }),
      );
      setLanguageSelect(languageQuery.data.value);
    }
  }, [languageQuery.data, i18n]);

  const isSearchLoading =
    dynamicFilters.isLoading ||
    languageQuery.isLoading ||
    whoIsDynamicResponse.isLoading ||
    languages.isLoading;

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
            viewColor={configuration.showSyntax}
          />
        </I18nextProvider>,
        configuration.search,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? null : (
            <Search
              configuration={configuration}
              spans={spans}
              selectionsState={selectionsState}
              selectionsDispatch={selectionsDispatch}
              showSyntax={isQueryAnalysisComplete}
              btnSearch={configuration.searchConfigurable?.btnSearch ?? false}
              htmlKey={configuration.searchConfigurable?.htmlKey}
              viewColor={configuration.showSyntax}
              messageSearchIsVisible={
                configuration?.searchConfigurable?.messageSearchIsVisible ??
                true
              }
              customMessageSearch={
                configuration?.searchConfigurable?.customMessageSearch
              }
              actionOnClick={configuration?.searchConfigurable?.actionOnClick}
              callbackClickSearch={
                configuration?.searchConfigurable?.callbackClickSearch
              }
            />
          )}
        </I18nextProvider>,
        configuration.searchConfigurable
          ? configuration.searchConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SearchWithSuggestions
            configuration={configuration}
            spans={spansSuggestions}
            selectionsState={selectionsStateSuggestions}
            selectionsDispatch={selectionsDispatchSuggestions}
            showSyntax={isQueryAnalysisCompleteSuggestions}
            callbackSearch={
              configuration?.searchWithSuggestions?.callbackSearchButton
            }
            placeholder={configuration?.searchWithSuggestions?.placeholder}
            labelIcon={configuration?.searchWithSuggestions?.ariaLabelIcon}
          />
        </I18nextProvider>,
        configuration.searchWithSuggestions
          ? configuration.searchWithSuggestions.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {tabs.length >= 0 ? (
            <TabsMemo
              tabs={tabs}
              selectedTabIndex={selectedTabIndex}
              onSelectedTabIndexChange={setSelectedTabIndex}
              language={languageSelect}
              resetFilter={resetFilter}
              resetSort={resetSort}
              selectionsDispatch={selectionsDispatch}
            />
          ) : skeletonCustom.tabs ? (
            isActiveSkeleton && skeletonCustom.tabs
          ) : (
            isActiveSkeleton && <TabsSkeleton />
          )}
        </I18nextProvider>,
        configuration.tabs,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <RemoveFilters
            onConfigurationChange={onConfigurationChange}
            selectionsDispatch={selectionsDispatch}
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
            language={languageSelect}
            resetFilter={resetFilter}
            onAction={configuration.tabsConfigurable?.onAction}
            scrollMode={configuration.tabsConfigurable?.scrollMode}
            speed={configuration.tabsConfigurable?.speed}
            distance={configuration.tabsConfigurable?.distance}
            step={configuration.tabsConfigurable?.step}
            reset={configuration.tabsConfigurable?.reset}
            resetSort={resetSort}
            selectionsDispatch={selectionsDispatch}
            readMessageScreenReader={
              configuration.tabsConfigurable?.readMessageScreenReader
            }
            textLabelScreenReader={
              configuration.tabsConfigurable?.textLabelScreenReader
            }
          />
        </I18nextProvider>,
        configuration.tabsConfigurable
          ? configuration.tabsConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? (
            skeletonCustom.filters ? (
              isActiveSkeleton && skeletonCustom.filters
            ) : (
              isActiveSkeleton && <SkeletonFilters />
            )
          ) : (
            <FiltersMemo
              searchQuery={searchQuery}
              onAddFilterToken={addFilterToken}
              numberOfResults={numberOfResults}
              onRemoveFilterToken={removeFilterToken}
              onConfigurationChange={onConfigurationChange}
              filtersSelect={configuration.filterTokens}
              sort={completelySort}
              sortAfterKey={sortAfterKey}
              dynamicFilters={
                dynamicFilters.data?.handleDynamicFilters || false
              }
              language={languageSelect}
              isDynamicElement={dynamicData}
              selectionsDispatch={selectionsDispatch}
              isActiveSkeleton={isActiveSkeleton}
              skeletonCategoryCustom={skeletonCustom.suggestion}
              memoryResults={memoryResults}
            />
          )}
        </I18nextProvider>,
        configuration.filters,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? (
            skeletonCustom.results ? (
              isActiveSkeleton && skeletonCustom.results
            ) : (
              isActiveSkeleton && <SkeletonFilters />
            )
          ) : (
            <FiltersMemo
              searchQuery={searchQuery}
              onAddFilterToken={addFilterToken}
              onRemoveFilterToken={removeFilterToken}
              onConfigurationChange={onConfigurationChange}
              filtersSelect={configuration.filterTokens}
              sort={completelySort}
              sortAfterKey={sortAfterKey}
              dynamicFilters={
                dynamicFilters.data?.handleDynamicFilters || false
              }
              language={languageSelect}
              isCollapsable={
                configuration.filtersConfigurable?.isCollapsable ?? true
              }
              numberItems={configuration.filtersConfigurable?.numberItems}
              numberOfResults={numberOfResults}
              isDynamicElement={dynamicData}
              noResultMessage={
                configuration.filtersConfigurable?.noResultMessage
              }
              selectionsDispatch={selectionsDispatch}
              isActiveSkeleton={isActiveSkeleton}
              skeletonCategoryCustom={skeletonCustom.suggestion}
              memoryResults={memoryResults}
            />
          )}
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
          <ActiveFilter
            searchQuery={searchQuery}
            onRemoveFilterToken={removeFilterToken}
            onConfigurationChange={onConfigurationChange}
            actioneRemoveFilters={
              configuration.activeFiltersConfigurable?.actioneRemoveFilters
            }
            callbackRemoveFilter={
              configuration.activeFiltersConfigurable?.callbackRemoveFilter
            }
          />
        </I18nextProvider>,
        configuration.activeFiltersConfigurable
          ? configuration.activeFiltersConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <FiltersHorizontalMemo
            searchQuery={searchQuery}
            isDynamicElement={dynamicData}
            numberOfResults={numberOfResults}
            numberResultOfFilters={numberResultOfFilters}
            sortAfterKey={sortAfterKey}
            onConfigurationChange={onConfigurationChange}
            selectionsDispatch={selectionsDispatch}
            onConfigurationChangeExt={
              configuration.filtersHorizontal
                ? configuration.filtersHorizontal.callback
                : () => {}
            }
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            language={languageSelect}
            refButton={configuration.filtersHorizontal?.refButton}
            callbackSubmit={
              configuration.filtersHorizontal
                ? configuration.filtersHorizontal.callbackSubmit
                : () => {}
            }
            callbackReset={
              configuration.filtersHorizontal
                ? configuration.filtersHorizontal.callbackReset
                : () => {}
            }
            memoryResults={memoryResults}
          />
        </I18nextProvider>,
        configuration.filtersHorizontal
          ? configuration.filtersHorizontal.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? (
            skeletonCustom.results ? (
              isActiveSkeleton && skeletonCustom.results
            ) : (
              isActiveSkeleton && <SkeletonResult />
            )
          ) : (
            <ResultsMemo
              setTotalResult={setTotalResult}
              displayMode={configuration.resultsDisplayMode}
              searchQuery={searchQuery}
              onDetail={setDetail}
              setDetailMobile={setDetailMobile}
              sort={completelySort}
              setSortResult={setSort}
              isMobile={isMobile}
              overChangeCard={true}
              language={languageSelect}
              setSortAfterKey={setSortAfterKey}
              sortAfterKey={sortAfterKey}
              numberOfResults={numberOfResults}
              setIdPreview={setIdPreview}
              selectOptions={sortList}
              classNameLabel={classNameLabelSort}
              memoryResults={memoryResults}
              viewButton={viewButton}
              setViewButtonDetail={setViewButtonDetail}
              selectedSort={selectedSort}
              setSelectedSort={setSelectedSort}
            />
          )}
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
          <TotalResults
            totalResult={totalResult}
            saveTotalResultState={
              configuration.totalResultConfigurable?.saveTotalResultState
            }
          />
        </I18nextProvider>,
        configuration.totalResultConfigurable
          ? configuration.totalResultConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <TotalResultsMobile totalResult={totalResult} />
        </I18nextProvider>,
        configuration.totalResultMobile,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? (
            skeletonCustom.results ? (
              isActiveSkeleton && skeletonCustom.results
            ) : (
              isActiveSkeleton && <SkeletonResult />
            )
          ) : (
            <ResultsMemo
              setTotalResult={setTotalResult}
              displayMode={configuration.resultsDisplayMode}
              searchQuery={searchQuery}
              onDetail={setDetail}
              setDetailMobile={setDetailMobile}
              sort={completelySort}
              setSortResult={setSort}
              isMobile={isMobile}
              overChangeCard={configuration.resultList?.changeOnOver || false}
              language={languageSelect}
              setSortAfterKey={setSortAfterKey}
              sortAfterKey={sortAfterKey}
              numberOfResults={numberOfResults}
              setIdPreview={setIdPreview}
              selectedSort={selectedSort}
              setSelectedSort={setSelectedSort}
              counterIsVisible={
                configuration.resultList?.counterIsVisible || false
              }
              label={configuration.resultList?.label}
              selectOptions={sortList}
              classNameLabel={classNameLabelSort}
              memoryResults={memoryResults}
              viewButton={viewButton}
              setViewButtonDetail={setViewButtonDetail}
              NoResultsCustom={configuration.resultList?.noResultsCustom}
            />
          )}
        </I18nextProvider>,
        configuration.resultList ? configuration.resultList.element : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? (
            skeletonCustom.results ? (
              isActiveSkeleton && skeletonCustom.results
            ) : (
              isActiveSkeleton && <SkeletonResult />
            )
          ) : (
            <ResultsPaginationMemo
              setTotalResult={setTotalResult}
              displayMode={configuration.resultsDisplayMode}
              searchQuery={searchQuery}
              onDetail={setDetail}
              setDetailMobile={setDetailMobile}
              sort={completelySort}
              setSortResult={setSort}
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
          )}
        </I18nextProvider>,
        configuration.resultListPagination
          ? configuration.resultListPagination.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMemo
            result={detail}
            actionOnCLose={() => {}}
            cardDetailsOnOver={isHoverChangeDetail}
            callbackFocusedButton={() => {
              const recoveryButton = document.getElementById(
                "openk9-button-card-" + idPreview,
              ) as any;
              if (recoveryButton) recoveryButton.focus();
            }}
            setViewButtonDetail={setViewButtonDetail}
            viewButtonDetail={viewButtonDetail}
          />
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
          <SortResultListCustom
            classTab={tabs[selectedTabIndex]?.label}
            setSortResult={setSort}
            selectOptions={
              configuration.sortResultListCustom?.selectOptions ?? []
            }
          />
        </I18nextProvider>,
        configuration.sortResultListCustom
          ? configuration.sortResultListCustom.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultListMemo setSortResult={setSort} />
        </I18nextProvider>,
        configuration.sortable,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultListMemo
            setSortResult={setSort}
            relevance={configuration.sortableConfigurable?.relevance}
          />
        </I18nextProvider>,
        configuration.sortableConfigurable
          ? configuration.sortableConfigurable.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResultListMemo
            setSortResult={setSort}
            relevance={configuration.sortResultConfigurable?.relevance}
            HtmlString={
              configuration.sortResultConfigurable?.htmlKey || undefined
            }
          />
        </I18nextProvider>,
        configuration.sortResultConfigurable
          ? configuration.sortResultConfigurable?.sort
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
          <SelectComponent
            language={languageSelect}
            selectOptions={[]}
            extraClass={configuration.select?.extraClass}
            labelDefault=""
            handleChange={configuration.select?.handleChange}
          />
        </I18nextProvider>,
        configuration.select ? configuration.select?.element : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <SortResults
            language={languageSelect}
            selectOptions={sortList ?? []}
            extraClass={configuration.sortResults?.extraClass}
            labelDefault={configuration.sortResults?.defaultLabelName}
            labelText={configuration.sortResults?.labelText}
            classNameLabel={configuration.sortResults?.classNameLabel}
            setSort={setSort}
            sort={tabTokens.sort}
          />
        </I18nextProvider>,
        configuration.sortResults ? configuration.sortResults?.element : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          <DetailMobileMemo
            result={detailMobile}
            setDetailMobile={setDetailMobile}
            cardDetailsOnOver={isHoverChangeDetail}
            onClose={() => {
              const recoveryButton = document.getElementById(
                "preview-card-" + idPreview,
              ) as HTMLButtonElement;
              if (recoveryButton) recoveryButton.focus();
            }}
          />
        </I18nextProvider>,
        configuration.detailMobile,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? null : (
            <FiltersMobileMemo
              searchQuery={searchQuery}
              onConfigurationChange={onConfigurationChange}
              filtersSelect={configuration.filterTokens}
              sort={completelySort}
              selectionsDispatch={selectionsDispatch}
              dynamicFilters={
                dynamicFilters.data?.handleDynamicFilters || false
              }
              isVisibleFilters={configuration.filtersMobile?.isVisible || false}
              setIsVisibleFilters={configuration.filtersMobile?.setIsVisible}
              language={languageSelect}
              sortAfterKey={sortAfterKey}
              isDynamicElement={dynamicData}
              numberResultOfFilters={numberResultOfFilters}
              memoryResults={memoryResults}
            />
          )}
        </I18nextProvider>,
        configuration.filtersMobile?.element !== undefined
          ? configuration.filtersMobile?.element
          : null,
      )}
      {renderPortal(
        <I18nextProvider i18n={i18next}>
          {isSearchLoading ? null : (
            <FiltersMobileLiveChangeMemo
              searchQuery={searchQuery}
              onAddFilterToken={addFilterToken}
              onRemoveFilterToken={removeFilterToken}
              onConfigurationChange={onConfigurationChange}
              sortAfterKey={sortAfterKey}
              filtersSelect={configuration.filterTokens}
              sort={completelySort}
              skeletonCategoryCustom={skeletonCustom.suggestion}
              isActiveSkeleton={isActiveSkeleton}
              dynamicFilters={
                dynamicFilters.data?.handleDynamicFilters || false
              }
              configuration={configuration}
              whoIsDynamic={dynamicData}
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
              viewTabs={
                configuration.filtersMobileLiveChange?.viewTabs ?? false
              }
              language={languageSelect}
              isCollapsable={
                configuration.filtersMobileLiveChange?.isCollapsable ?? true
              }
              numberOfResults={numberOfResults}
              selectionsDispatch={selectionsDispatch}
              memoryResults={memoryResults}
            />
          )}
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
            setSortResult={setSort}
            searchQuery={searchQuery}
            onAddFilterToken={addFilterToken}
            onRemoveFilterToken={removeFilterToken}
            filtersSelect={configuration.filterTokens}
            sort={completelySort}
            dynamicFilters={dynamicFilters.data?.handleDynamicFilters || false}
            isVisible={configuration.searchMobile?.isVisible || false}
            setIsVisible={configuration.searchMobile?.setIsVisible}
            btnSearch={configuration.searchConfigurable?.btnSearch ?? false}
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
  debounceTimeSearch,
  tabTokens,
  filterTokens,
  dateTokens,
  onQueryStateChange,
  setCurrentPage,
  selectionsState,
  selectionsDispatch,
}: {
  configuration: Configuration;
  debounceTimeSearch: number;
  tabTokens: {
    tabToken: any;
    sort:
      | {
          sort: {
            field: string;
            type: string;
          };
          isSort: boolean;
        }
      | undefined;
  };
  filterTokens: SearchToken[];
  dateTokens: SearchToken[];
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  onQueryStateChange(queryState: QueryState): void;
  selectionsState: SelectionsState;
  selectionsDispatch: React.Dispatch<SelectionsAction>;
}) {
  const { searchAutoselect, searchReplaceText, defaultTokens, sort } =
    configuration;
  const [previousSearchTokens, setPreviousSearchTokens] = React.useState<
    Array<SearchToken>
  >([]);
  const debounced = useDebounce(selectionsState, debounceTimeSearch);
  const infoSort = tabTokens?.sort?.sort;

  const queryAnalysis = !configuration.useQueryAnalysis
    ? { data: undefined }
    : useQueryAnalysis({
        searchText: debounced.textOnChange,
        tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      });

  const spans = React.useMemo(
    () =>
      calculateSpans(
        selectionsState.textOnChange,
        queryAnalysis.data?.analysis,
      ),
    [queryAnalysis.data?.analysis, selectionsState.textOnChange],
  );

  const searchTokens = React.useMemo(() => {
    const value = deriveSearchQuery(
      spans,
      selectionsState.selection.flatMap(({ text, start, end, token }) =>
        token ? [{ text, start, end, token }] : [],
      ),
      selectionsState.text,
    );
    if (selectionsState.text === selectionsState.textOnChange) {
      setPreviousSearchTokens(value as SearchToken[]);
      return value;
    }
    return previousSearchTokens;
  }, [selectionsState.text, spans]) as SearchToken[];
  const newSearch: SearchToken[] = searchTokens
    .filter((search) => search)
    .map((searchToken) => {
      return { ...searchToken, isSearch: true };
    });

  const newTokenFilter: SearchToken[] = React.useMemo(
    () => createFilter(filterTokens),
    [filterTokens],
  );

  const sortField = {
    sort: {
      [infoSort?.field || ""]: {
        sort: infoSort?.type as "asc" | "desc",
        missing: "_last",
      },
    },
    isSort: true,
  };

  const completelySort = React.useMemo(() => sort, [sort]);
  const searchQueryMemo = React.useMemo(
    () => [
      ...defaultTokens,
      ...(tabTokens?.tabToken ?? []),
      ...newTokenFilter,
      ...newSearch,
      ...dateTokens,
      ...(tabTokens?.sort ? [sortField] : []),
    ],
    [
      defaultTokens,
      tabTokens?.tabToken,
      newTokenFilter,
      searchTokens,
      dateTokens,
    ],
  );

  const searchQuery = useDebounce(searchQueryMemo, 600);
  const isQueryAnalysisComplete =
    selectionsState.textOnChange === debounced.textOnChange &&
    queryAnalysis.data !== undefined &&
    !queryAnalysis.isPreviousData;

  const counterTotalFilters = newTokenFilter.reduce((total, element) => {
    return total + (element?.values?.length || 0);
  }, 0);

  React.useEffect(() => {
    onQueryStateChange({
      defaultTokens,
      tabTokens: tabTokens?.tabToken ?? [],
      filterTokens,
      searchTokens,
      numberOfFilters: counterTotalFilters,
    });
    setCurrentPage(0);
  }, [
    onQueryStateChange,
    defaultTokens,
    // tabTokens.tabToken,
    //if you rewrite filters component you should remove filterTokens or the pagination doesn't work
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
            textOnChange:
              "textOnChange" in selection &&
              typeof selection.textOnChange === "string"
                ? selection.textOnChange
                : "",
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
    isQueryAnalysisComplete,
    completelySort,
  };
}
function useQueryAnalysisWithoutSearch({
  configuration,
  debounceTimeSearch,
  selectionsState,
}: {
  configuration: Configuration;
  debounceTimeSearch: number;
  selectionsState: SelectionsState;
}) {
  const debounced = useDebounce(selectionsState, debounceTimeSearch);
  const queryAnalysis = !configuration.useQueryAnalysis
    ? { data: undefined }
    : useQueryAnalysis({
        searchText: debounced.textOnChange,
        tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      });
  const queryAnalysisWithoutSearch = !configuration.useQueryAnalysis
    ? { data: undefined }
    : useQueryAnalysis({
        searchText: debounced.textOnChange,
        tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      });
  const spans = React.useMemo(
    () =>
      calculateSpans(
        selectionsState.textOnChange,
        queryAnalysis.data?.analysis,
      ),
    [queryAnalysis.data?.analysis, selectionsState.textOnChange],
  );

  const isQueryAnalysisComplete =
    selectionsState.textOnChange === debounced.textOnChange &&
    queryAnalysis.data !== undefined &&
    !queryAnalysis.isPreviousData;

  return {
    spansSuggestions: spans,
    isQueryAnalysisCompleteSuggestions: isQueryAnalysisComplete,
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

  const tabSelected = React.useMemo(
    () => tabs[selectedTabIndex],
    [tabs, selectedTabIndex],
  );
  const sortList = tabSelected?.sortings;

  const tabTokens = React.useMemo(() => {
    const createTab = tabs[selectedTabIndex]?.tokens;
    const sort =
      sortList && sortList.find((sortElement) => sortElement.isDefault);

    const completeTab = createTab?.map((tab) => ({ ...tab, isTab: true }));
    return (
      {
        tabTokens: completeTab,
        sort: sort?.field
          ? { sort: { field: sort.field, type: sort.type }, isSort: true }
          : undefined,
      } ?? {}
    );
  }, [selectedTabIndex, tabs, language]);
  const [tabsValue, setTabsValue] = React.useState(tabTokens);
  React.useEffect(() => {
    setTabsValue(tabTokens);
  }, [tabTokens]);

  const setSort = (
    sortField: { field: string; type: "asc" | "desc" } | null | undefined,
  ) => {
    setTabsValue({
      ...tabTokens,
      sort: sortField ? { sort: sortField, isSort: true } : undefined,
    });
  };
  return {
    tabTokens: tabsValue,
    tabs,
    selectedTabIndex,
    setSelectedTabIndex,
    sortList,
    tabSelected,
    setSort,
  };
}

function recoveryDataBackEnd() {
  const client = useOpenK9Client();
  const dynamicFilters = useQuery(["handle-dynamic-filters", {}], async () => {
    return await client.handle_dynamic_filters();
  });
  const languageQuery = useQuery(["language", {}], async () => {
    return await client.getLanguageDefault();
  });
  const whoIsDynamicResponse = useQuery(
    ["refresh-components", {}],
    async () => {
      return await client.getRefreshFilters();
    },
  );
  const languages = useQuery(["date-label-languages", {}], async () => {
    return await client.getLanguages();
  });

  return {
    dynamicFilters,
    languageQuery,
    whoIsDynamicResponse,
    languages,
  };
}

function useFilters({
  configuration,
  onConfigurationChange,
  selectionsState,
  selectionsDispatch,
  useQueryStringFilters,
}: {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  selectionsState: SelectionsState;
  useQueryStringFilters: boolean;
  selectionsDispatch: React.Dispatch<SelectionsAction>;
}) {
  const filterTokens: SearchToken[] = configuration.useFilterConfiguration
    ? [...configuration.filterTokens, ...selectionsState.filters]
    : useQueryStringFilters
    ? selectionsState.filters
    : [];

  const addFilterToken = React.useCallback(
    (searchToken: SearchToken) => {
      const newFilters = configuration.filterTokens.map((token) => {
        if (token.suggestionCategoryId === searchToken.suggestionCategoryId) {
          const updatedToken: SearchToken = {
            ...token,
            values: token.values
              ? [...token.values, ...(searchToken.values ?? [])]
              : [...(searchToken.values ?? [])],
          };
          return updatedToken;
        }
        return token;
      });

      selectionsDispatch({ type: "set-filters", filter: searchToken });

      onConfigurationChange((configuration) => ({
        ...configuration,
        filterTokens: [...newFilters],
      }));
    },
    [configuration.filterTokens, onConfigurationChange, selectionsDispatch],
  );

  const removeFilterToken = React.useCallback(
    (searchToken: SearchToken) => {
      selectionsDispatch({ type: "remove-filter", filter: searchToken });
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

  const resetFilter = React.useCallback(() => {
    selectionsDispatch({ type: "reset-filters" });
    onConfigurationChange(() => ({ filterTokens: [] }));
  }, [onConfigurationChange]);

  const defaultTokens = configuration.defaultTokens;
  return {
    defaultTokens,
    filterTokens,
    addFilterToken,
    removeFilterToken,
    resetFilter,
  };
}

function useSortResult({
  configuration,
  onConfigurationChange,
  setSortAfterKey,
  sortList,
}: {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  setSortAfterKey: React.Dispatch<React.SetStateAction<string>>;
  sortList: Options;
  setSort: (
    sortField: { field: string; type: "asc" | "desc" } | undefined | null,
  ) => void;
}) {
  const sort = configuration.sort;
  const option = sortList?.find((option) => option.isDefault);
  const defaultOption:
    | { field: string; type: "asc" | "desc" }
    | null
    | undefined = {
    field: option?.field || "",
    type: option?.type as "asc" | "desc",
  };
  const [selectedSort, setSelectedSort] = React.useState<
    { field: string; type: "asc" | "desc" } | null | undefined
  >(defaultOption);

  const resetSort = React.useCallback(() => {
    setSortAfterKey("");
    onConfigurationChange(() => ({
      sort: [],
    }));
    setSelectedSort(defaultOption);
  }, [configuration, selectedSort]);
  return { sort, resetSort, selectedSort, setSelectedSort };
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

function useDetails(
  searchQuery: Array<SearchToken>,
  setPrevSearchQuery: any,
  prevSearchQuery: Array<SearchToken>,
) {
  const [detail, setDetail] = React.useState<GenericResultItem<unknown> | null>(
    null,
  );
  React.useEffect(() => {
    if (prevSearchQuery !== null && !isEqual(searchQuery, prevSearchQuery)) {
      setDetail(null);
    }
    setPrevSearchQuery(searchQuery);
  }, [searchQuery, prevSearchQuery]);
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

function useDetailsMobile(
  searchQuery: Array<SearchToken>,
  prevSearchQueryMobile: Array<SearchToken>,
  setPrevSearchQueryMobile: any,
) {
  const [idPreview, setIdPreview] = React.useState("");
  const [detailMobile, setDetailMobile] =
    React.useState<GenericResultItem<unknown> | null>(null);
  React.useEffect(() => {
    if (
      prevSearchQueryMobile !== null &&
      !isEqual(searchQuery, prevSearchQueryMobile)
    ) {
      setDetailMobile(null);
    }
    setPrevSearchQueryMobile(searchQuery);
  }, [searchQuery, prevSearchQueryMobile]);

  return { detailMobile, setDetailMobile, idPreview, setIdPreview };
}

export type QueryState = {
  defaultTokens: Array<SearchToken>;
  tabTokens: Array<SearchToken>;
  filterTokens: Array<SearchToken>;
  searchTokens: Array<SearchToken>;
  numberOfFilters: number;
};

function deriveSearchQuery(
  spans: AnalysisResponseEntry[],
  selection: AnalysisRequestEntry[],
  text: string | undefined,
) {
  // console.log(spans);

  return spans
    .map((span) => ({ ...span, text: span.text.trim() }))
    .filter((span) => span.text)
    .map((span) => {
      // console.log(span);

      const token =
        selection.find((selection) => {
          return selection.start === span.start && selection.end === span.end;
        })?.token ?? null;

      return (
        (token && analysisTokenToSearchToken(token)) ??
        (text !== undefined
          ? {
              tokenType: "TEXT",
              values: [text],
              filter: false,
            }
          : false)
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
        extra: token.extra,
      };
    case "FILTER":
      return {
        tokenType: "FILTER",
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
    const found = analysis?.find(({ start, text }) => {
      return i === start && text !== "";
    });

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

function useQueryAnalysisWithoutSearchData(request: AnalysisRequest) {
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

export const containsAtLeastOne = (
  array1: string[],
  array2: string[],
): boolean => {
  return array1.some((element1) => {
    return array2.includes(element1);
  });
};

function factoryWhoIsDynamic({
  whoIsDynamicResponse,
}: {
  whoIsDynamicResponse:
    | {
        refreshOnSuggestionCategory: boolean;
        refreshOnTab: boolean;
        refreshOnDate: boolean;
        refreshOnQuery: boolean;
      }
    | undefined;
}): WhoIsDynamic[] {
  if (whoIsDynamicResponse === undefined) {
    return [];
  }

  const resultArray: WhoIsDynamic[] = [];

  if (whoIsDynamicResponse.refreshOnQuery) {
    resultArray.push("search");
  }
  if (whoIsDynamicResponse.refreshOnSuggestionCategory) {
    resultArray.push("filter");
  }
  if (whoIsDynamicResponse.refreshOnTab) {
    resultArray.push("tab");
  }
  if (whoIsDynamicResponse.refreshOnDate) {
    resultArray.push("date");
  }
  return resultArray;
}
