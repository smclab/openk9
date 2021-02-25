//
// SEARCH TOKENS
// ============================================
//

import { GenericResultItem } from "./searchResults";

export type SearchToken = {
  keywordKey?: string;
  values: string[];

  // Parser location info
  start?: number;
  mid?: number;
  end?: number;
} & (EntityItem | DataSourceItem | DocTypeItem | TextItem | TextTokenItem);

export type EntityToken = SearchToken & EntityItem;

export type DataSourceToken = SearchToken & DataSourceItem;

export type DocTypeToken = SearchToken & DocTypeItem;

export type TextToken = SearchToken & TextTokenItem;

export type Token = EntityToken | DataSourceToken | DocTypeToken | TextToken;

export interface EntityItem {
  tokenType: "ENTITY";
  entityType: string;
}

export interface DataSourceItem {
  tokenType: "DATASOURCE";
  values: string[];
}

export interface DocTypeItem {
  tokenType: "DOCTYPE";
  values: string[];
}

export interface TextItem {
  tokenType: "TEXT";
}

export interface TextTokenItem {
  tokenType: "TEXT-TOKEN";
}

//
// REST CALL: Search Request
// ============================================
//
export interface SearchRequest {
  searchQuery: SearchQuery;
  range: [number, number];
}

export type SearchQuery = SearchToken[];

export type SearchResult<E> = {
  result: GenericResultItem<E>[];
  total: number;
  last: boolean;
};
