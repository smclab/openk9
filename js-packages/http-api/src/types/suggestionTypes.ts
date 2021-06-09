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

import { SearchToken } from "./searchQueryTypes";

export interface BaseSuggestion {
  alternatives: string[];
  displayDescription: string;
  compatibleKeywordKeys?: string[];
}

export interface EntitySuggestion extends BaseSuggestion {
  kind: "ENTITY";
  type: string;
  id: number;
}

export interface ParamSuggestion extends BaseSuggestion {
  kind: "PARAM";
  id: string;
  compatibleKeywordKeys?: [];
}

export interface TokenSuggestion extends BaseSuggestion {
  kind: "TOKEN";
  id: string;
  entityType?: string;
  outputKeywordKey?: string;
  outputTokenType?: SearchToken["tokenType"];
}

export type InputSuggestionToken =
  | EntitySuggestion
  | ParamSuggestion
  | TokenSuggestion;
