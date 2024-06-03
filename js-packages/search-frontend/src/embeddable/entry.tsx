import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "react-query";
import * as RestApi from "../components/client";
import {
  OpenK9Client,
  SearchToken,
  OpenK9ClientContext,
} from "../components/client";
import * as RendererComponents from "../renderer-components";
import { Main, QueryState } from "./Main";
import { ResultsDisplayMode } from "../components/ResultList";
import { Tab } from "../components/Tabs";
import { Options } from "../components/Select";

export const rendererComponents = RendererComponents;

export class OpenK9 {
  static dependencies = {
    React,
    ReactDOM,
    RestApi,
    SearchFrontend: {
      OpenK9,
      rendererComponents,
    },
  };

  private configuration: Configuration;
  /**
   * rest client instance used by this ui
   * @see {@link OpenK9Client} from **@openk9/rest-api** package
   */
  readonly client;
  private root;
  private queryClient;

  constructor(configuration: Partial<Configuration> = {}) {
    this.configuration = { ...defaultConfiguration, ...configuration };
    this.root = ReactDOM.createRoot(this.getRootElement());
    this.queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          refetchOnWindowFocus: false,
          notifyOnChangeProps: ["data", "error"], // for better performance
        },
      },
    });
    this.client = OpenK9Client({
      onAuthenticated: () => {
        this.queryClient.invalidateQueries();
      },
      tenant: this.configuration.tenant ?? "",
      useKeycloak: this.configuration.useKeycloak,
      waitKeycloackForToken: this.configuration.waitKeycloackForToken,
    });
    this.render();
  }

  /** returns current configuration */
  getConfiguration() {
    return { ...this.configuration };
  }

  /**
   * updates configuration with provided fields
   *
   * also accepts a function that produces an object with fields to update
   */
  updateConfiguration: ConfigurationUpdateFunction = (configuration) => {
    if (typeof configuration === "function") {
      this.configuration = {
        ...this.configuration,
        ...configuration(this.configuration),
      };
    } else {
      this.configuration = { ...this.configuration, ...configuration };
    }
    this.notify("configurationChange", this.configuration);
    this.render();
  };

  private render() {
    if (this.configuration.enabled) {
      this.root.render(
        <React.StrictMode>
          <React.Suspense>
            <OpenK9ClientContext.Provider value={this.client}>
              <QueryClientProvider client={this.queryClient}>
                <Main
                  configuration={this.configuration}
                  onConfigurationChange={this.updateConfiguration}
                  onQueryStateChange={this.onQueryStateChange}
                />
              </QueryClientProvider>
            </OpenK9ClientContext.Provider>
          </React.Suspense>
        </React.StrictMode>,
      );
    } else {
      this.root.unmount();
    }
  }

  private getRootElement() {
    const isDevelopment = process.env.NODE_ENV === "development";
    if (!isDevelopment) {
      return document.createDocumentFragment();
    } else {
      // needed for hot-reloading in development
      const win: Window & { ["openk9-react-root"]?: DocumentFragment } =
        window as any;
      if (win["openk9-react-root"]) {
        return win["openk9-react-root"];
      } else {
        win["openk9-react-root"] = document.createDocumentFragment();
        return win["openk9-react-root"];
      }
    }
  }

  /** authenticate the user with given token */
  authenticate = ({ token }: { token?: string }) => {
    return this.client.authenticate({ token: token || "" });
  };

  /** logs out the user, all subsequent queries will be anonymous */
  deauthenticate = () => {
    return this.client.deauthenticate();
  };

  private listeners: {
    [K in keyof Events]: Set<(payload: Events[K]) => void>;
  } = {
    configurationChange: new Set(),
    queryStateChange: new Set(),
  };
  private notify<E extends keyof Events>(event: E, payload: Events[E]) {
    this.listeners[event].forEach((listener) => listener(payload as any));
  }
  addEventListener<E extends keyof Events>(
    event: E,
    listener: (payload: Events[E]) => void,
  ) {
    this.listeners[event].add(listener as any);
  }
  removeEventListener<E extends keyof Events>(
    event: E,
    listener: (payload: Events[E]) => void,
  ) {
    this.listeners[event].delete(listener as any);
  }

  private onQueryStateChange = (queryState: QueryState) => {
    this.notify("queryStateChange", queryState);
  };
}

type FiltersHorizontalConfiguration = {
  element: Element | string | null;
  callback: () => void | null;
  callbackSubmit: () => void | null;
  callbackReset: () => void | null;
  refButton?: React.RefObject<HTMLButtonElement>;
};

type FiltersHorizontalMobileConfiguration = {
  element: Element | string | null;
  isVisible: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>>;
};

type FiltersLiveMobileConfiguration = {
  element: Element | string | null;
  isVisible: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>>;
  viewTabs?: boolean | null;
  isCollapsable?: boolean | null;
};

type SearchMobileConfiguration = {
  search: Element | string | null;
  isVisible: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>>;
  isShowSyntax?: boolean | undefined | null;
};

type CalendarMobileConfiguration = {
  element: Element | string | null;
  isVisible: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>>;
  startDate: any;
  setStartDate: any;
  endDate: any;
  setEndDate: any;
  focusedInput: any;
  setFocusedInput: any;
  isCLickReset: boolean;
  setIsCLickReset: React.Dispatch<React.SetStateAction<boolean>>;
};

type DataRangePickerProps = {
  element: Element | string | null;
  start?: any;
  end?: any;
};

type DataRangePickerVerticalProps = {
  element: Element | string | null;
  start?: any;
  end?: any;
  readOnly?: boolean;
};

type ResultListProps = {
  element: Element | string | null;
  changeOnOver: boolean;
  counterIsVisible?: boolean;
  label?: string;
  noResultsCustom?: React.ReactNode;
};

type SortResultConfigurableProps = {
  sort: Element | string | null;
  relevance: string;
  htmlKey?: string | null | undefined;
};

type SortResultListCustomProps = {
  selectOptions: Array<{
    value: { value: string; sort: string };
    label: string;
    sort: string;
    isDefault: boolean;
    hasAscDesc: boolean;
  }>;
  element: Element | string | null;
};

type SortableProps = {
  element: Element | string | null;
  relevance: string;
};

type SearchProps = {
  element: Element | string | null;
  btnSearch: boolean;
  defaultValue?: string | undefined | null;
  htmlKey?: string | undefined | null;
  messageSearchIsVisible?: boolean;
  customMessageSearch?: string;
  actionOnClick?(): void;
  callbackClickSearch?(): void;
};

type FilterProps = {
  element: Element | string | null;
  isCollapsable?: boolean;
  numberItems?: number | null | undefined;
  noResultMessage?: string | null | undefined;
};

type ResulListPaginationProps = {
  element: Element | string | null;
  anchor?: React.MutableRefObject<HTMLDivElement | null>;
};

type TabsProps = {
  element: Element | string | null;
  onAction(): void;
  scrollMode?: boolean;
  speed?: number;
  distance?: number;
  step?: number;
  pxHiddenRightArrow?: number;
  reset?: {
    filters: boolean;
    calendar: boolean;
    sort: boolean;
    search: boolean;
  };
  readMessageScreenReader?: boolean;
  textLabelScreenReader?: string;
};

type SelectProps = {
  element: Element | string | null;
  options: Options;
  extraClass?: string;
  handleChange: (event: React.ChangeEvent<HTMLSelectElement>) => void;
};

type SortResultsProps = {
  element: Element | string | null;
  extraClass?: string;
  classNameLabel?: string | undefined;
  labelText?: string | undefined;
  defaultLabelName?: string | undefined;
};

type totalResultProps = {
  element: Element | string | null;
  saveTotalResultState?: React.Dispatch<React.SetStateAction<number | null>>;
};

type activeFiltersConfigurableProps = {
  element: Element | string | null;
  actioneRemoveFilters?(): void;
  callbackRemoveFilter?(): void;
};

type searchWithSuggestionsProps =
  | {
      element: Element | string | null;
      callbackSearchButton?(): void;
      ariaLabelIcon?: string;
      placeholder?: string;
    }
  | null
  | undefined;

export type Configuration = {
  // simple types
  debounceTimeSearch: number | null | undefined;
  defaultTokens: Array<SearchToken>;
  defaultString: string | null | undefined;
  enabled: boolean;
  filterTokens: Array<SearchToken>;
  isQueryAnalysis: boolean | null;
  isActiveSkeleton: boolean | null;
  numberResult: number | null | undefined;
  numberResultOfFilters: number | null | undefined;
  memoryResults: boolean | null | undefined;
  searchAutoselect: boolean;
  searchReplaceText: boolean;
  skeletonTabsCustom: React.ReactNode | null;
  skeletonResultsCustom: React.ReactNode | null;
  skeletonFiltersCustom: React.ReactNode | null;
  skeletonSuggestionCustom: React.ReactNode | null;
  sort: Array<RestApi.SortField>;
  showSyntax: boolean;
  tenant: string | null;
  token: string | null;
  useQueryAnalysis: boolean;
  useKeycloak: boolean;
  useQueryString: boolean;
  useQueryStringFilters: boolean;
  useFilterConfiguration: boolean;
  viewButton: boolean;
  waitKeycloackForToken: boolean;
  // element types
  activeFilters: Element | string | null;
  calendar: Element | string | null;
  changeLanguage: Element | string | null;
  detailMobile: Element | string | null;
  details: Element | string | null;
  filters: Element | string | null;
  login: Element | string | null;
  results: Element | string | null;
  search: Element | string | null;
  sortable: Element | string | null;
  tabs: Element | string | null;
  totalResult: Element | string | null;
  totalResultMobile: Element | string | null;
  removeFilters: Element | string | null;
  // configurable types
  activeFiltersConfigurable: activeFiltersConfigurableProps | null | undefined;
  calendarMobile: CalendarMobileConfiguration | null;
  dataRangePicker: DataRangePickerProps | null;
  dataRangePickerVertical: DataRangePickerVerticalProps | null;
  filtersConfigurable: FilterProps | null;
  filtersHorizontal: FiltersHorizontalConfiguration | null;
  filtersMobile: FiltersHorizontalMobileConfiguration | null;
  filtersMobileLiveChange: FiltersLiveMobileConfiguration | null;
  resultList: ResultListProps | null;
  resultListPagination: ResulListPaginationProps | null;
  resultsDisplayMode: ResultsDisplayMode;
  searchConfigurable: SearchProps | null;
  searchWithSuggestions: searchWithSuggestionsProps;
  searchMobile: SearchMobileConfiguration | null;
  select: SelectProps | null;
  sortableConfigurable: SortableProps | null;
  sortResults: SortResultsProps | null;
  sortResultConfigurable: SortResultConfigurableProps | null;
  sortResultListCustom: SortResultListCustomProps | null;
  tabsConfigurable: TabsProps | null;
  totalResultConfigurable: totalResultProps | null;
  // functions
  changeSortResult: (
    sort: Array<RestApi.SortField>,
  ) => Array<RestApi.SortField>;
  overrideTabs: (tabs: Array<Tab>) => Array<Tab>;
};

const defaultConfiguration: Configuration = {
  activeFilters: null,
  activeFiltersConfigurable: null,
  calendar: null,
  calendarMobile: null,
  changeLanguage: null,
  dataRangePicker: null,
  dataRangePickerVertical: null,
  defaultString: null,
  debounceTimeSearch: null,
  defaultTokens: [],
  detailMobile: null,
  details: null,
  enabled: false,
  filters: null,
  filtersConfigurable: null,
  filtersHorizontal: null,
  filtersMobile: null,
  filtersMobileLiveChange: null,
  filterTokens: [],
  isActiveSkeleton: null,
  isQueryAnalysis: true,
  login: null,
  memoryResults: null,
  numberResult: null,
  numberResultOfFilters: null,
  removeFilters: null,
  resultList: null,
  resultListPagination: null,
  results: null,
  resultsDisplayMode: { type: "infinite" },
  search: null,
  select: null,
  searchAutoselect: true,
  searchConfigurable: null,
  searchMobile: null,
  searchReplaceText: true,
  searchWithSuggestions: null,
  showSyntax: true,
  sort: [],
  sortResults: null,
  sortable: null,
  sortableConfigurable: null,
  sortResultConfigurable: null,
  sortResultListCustom: null,
  skeletonFiltersCustom: null,
  skeletonResultsCustom: null,
  skeletonSuggestionCustom: null,
  skeletonTabsCustom: null,
  tabs: null,
  tabsConfigurable: null,
  tenant: null,
  token: null,
  totalResult: null,
  totalResultConfigurable: null,
  totalResultMobile: null,
  useQueryAnalysis: true,
  useKeycloak: true,
  useQueryString: true,
  useQueryStringFilters: true,
  useFilterConfiguration: true,
  viewButton: false,
  waitKeycloackForToken: false,
  changeSortResult: (sort) => sort,
  overrideTabs: (tabs) => tabs,
};

export type ConfigurationUpdateFunction = (
  configuration:
    | Partial<Configuration>
    | ((configuration: Configuration) => Partial<Configuration>),
) => void;

type Events = {
  configurationChange: Configuration;
  queryStateChange: QueryState;
};

window.OpenK9 = OpenK9;
declare global {
  interface Window {
    OpenK9: typeof OpenK9;
  }
}
