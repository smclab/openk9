/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//
// SEARCH TOKENS
// ============================================
//

import { GenericResultItem } from "./searchResults";

export type SearchToken = {
  keywordKey?: string;

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
  values: number[];
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
  values: string[];
}

export interface TextTokenItem {
  tokenType: "TEXT-TOKEN";
  values: string[];
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
