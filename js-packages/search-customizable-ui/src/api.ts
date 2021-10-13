import {
  EntityDescription,
  GenericResultItem,
  SearchToken,
  SuggestionResult,
} from "../../http-api/src";

export type OpenK9UIConfiguration = {
  widgets?: OpenK9UIWidgets;
  templates?: OpenK9UITemplates;
  interactions?: OpenK9UIInteractions;
};

type OpenK9UIWidgets = {
  search?: Element | null;
  suggestions?: Element | null;
  tabs?: Element | null;
  results?: Element | null;
  details?: Element | null;
};

export type OpenK9UITemplates = {
  tabs?(params: {
    tabs: Array<string>;
    activeIndex: number;
    setActiveIndex(index: number): void;
  }): Element;
  result?(params: {
    result: GenericResultItem<unknown>;
    setDetail(result: GenericResultItem<unknown>): void;
  }): Element | null;
  detail?(params: { result: GenericResultItem<unknown> }): Element | null;
  suggestionKind?(params: {
    label: string;
    active: boolean;
    select(): void;
  }): Element;
  suggestionItem?(params: {
    suggestion: SuggestionResult;
    select(): void;
  }): Element;
  token?(params: {
    token: SearchToken;
    entity: EntityDescription | null;
  }): Element;
  inputPlaceholder?: string;
};

export type OpenK9UIInteractions = {
  searchAsYouType?: boolean;
};
