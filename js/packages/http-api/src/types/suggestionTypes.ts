import { SearchToken } from "./searchQueryTypes";

export interface BaseSuggestion {
  id: string;
  alternatives: string[];
  displayDescription: string;
  compatibleKeywordKeys?: string[];
}

export interface EntitySuggestion extends BaseSuggestion {
  kind: "ENTITY";
  type: string;
}

export interface ParamSuggestion extends BaseSuggestion {
  kind: "PARAM";
  id: string;
  compatibleKeywordKeys?: [];
}

export interface TokenSuggestion extends BaseSuggestion {
  kind: "TOKEN";
  entityType?: string;
  outputKeywordKey?: string;
  outputTokenType?: SearchToken["tokenType"];
}

export type InputSuggestionToken =
  | EntitySuggestion
  | ParamSuggestion
  | TokenSuggestion;
