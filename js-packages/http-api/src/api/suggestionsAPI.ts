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

import {
  InputSuggestionToken,
  ParamSuggestion,
  SearchQuery,
  SearchRequest,
  SearchToken,
} from "../types";
import { capitalize } from "../utilities";
import { LoginInfo } from "./authAPI";
import { authFetch } from "./common";
import { doSearchEntities } from "./entitiesAPI";

const defaultParams: ParamSuggestion[] = [
  {
    kind: "PARAM",
    id: "email.from",
    alternatives: ["da", "mittente", "sender", "from"],
    displayDescription: "Email Sender",
  },
  {
    kind: "PARAM",
    id: "email.to",
    alternatives: ["a", "destinatario", "to", "recipient"],
    displayDescription: "Email Recipient",
  },
  {
    kind: "PARAM",
    id: "type",
    alternatives: ["tipo", "type"],
    displayDescription: "Result Type",
  },
  {
    kind: "PARAM",
    id: "datasource",
    alternatives: ["sorgente", "source"],
    displayDescription: "Data Source",
  },
  {
    kind: "PARAM",
    id: "document.documentType",
    alternatives: ["tipo", "type", "document"],
    displayDescription: "Doc Type",
  },
  {
    kind: "PARAM",
    id: "istat.topic",
    alternatives: ["topic", "argomento"],
    displayDescription: "Topic",
  },
];

function findInDefault(
  query?: string | number,
  noParams = false,
  exact = false,
) {
  if (!query) return defaultParams;

  const exactParams = defaultParams.filter((p) => p.id === query);

  const params = exact
    ? []
    : defaultParams.filter(
        (p) =>
          p.id !== query &&
          (p.id.toString().startsWith(query.toString()) ||
            p.alternatives.some((a) => a.startsWith(query.toString())) ||
            p.displayDescription.includes(query.toString())),
      );

  return [...(noParams ? [] : exactParams), ...(noParams ? [] : params)];
}

export interface ServerSuggestion {
  keywordKey: string;
  tokenType: string;
  value: string;
  count: number;
}

export interface ServerSuggestionsResponse {
  result: ServerSuggestion[];
  total: number;
  last: boolean;
}

export async function getServerSuggestions(
  searchRequest: SearchRequest,
  loginInfo: LoginInfo | null,
): Promise<ServerSuggestionsResponse> {
  const request = await authFetch(`/api/searcher/v1/suggestions`, loginInfo, {
    method: "POST",
    headers: { ContentType: "application/json" },
    body: JSON.stringify(searchRequest),
  });
  const response: ServerSuggestionsResponse = await request.json();
  return response;
}

export async function getTokenInfo(
  token: SearchToken,
  loginInfo: LoginInfo | null,
): Promise<InputSuggestionToken[]> {
  const entityId = token.tokenType === "ENTITY" && token.values[0];
  const entitySuggestions =
    (entityId &&
      (await doSearchEntities({ entityId }, loginInfo)).result.map((e) => ({
        kind: "ENTITY" as const,
        id: e.entityId,
        alternatives: [e.name],
        displayDescription: e.name,
        compatibleKeywordKeys: [],
        type: e.type,
      }))) ||
    [];

  const keywordKey = token.keywordKey;
  const keywordKeySuggestions =
    (keywordKey && findInDefault(keywordKey, false, true)) || [];

  const unknownSuggestion: InputSuggestionToken = {
    kind: "TOKEN",
    id: token.values[0].toString(),
    alternatives: [],
    displayDescription: capitalize(token.values[0].toString()),
  };

  return [
    ...entitySuggestions,
    ...keywordKeySuggestions,
    ...(entitySuggestions.length === 0 ? [unknownSuggestion] : []),
  ];
}

export async function getTokenSuggestions(
  searchQuery: SearchQuery,
  loginInfo: LoginInfo | null,
  entityKind?: string,
): Promise<InputSuggestionToken[]> {
  const writingToken = searchQuery[searchQuery.length - 1] || {
    tokenType: "TEXT",
    values: [],
  };
  const writingText =
    (writingToken.tokenType === "TEXT" && writingToken.values[0]) || undefined;

  const serverSuggestions = await getServerSuggestions(
    { searchQuery, range: [0, 16] },
    loginInfo,
  );
  const fromServer = serverSuggestions.result
    .filter((ss) => !entityKind || entityKind === ss.keywordKey)
    .map((ss) => ({
      id: ss.value,
      kind: "TOKEN" as const,
      alternatives: [],
      displayDescription: capitalize(ss.value),
      outputTokenType:
        ss.keywordKey === "type"
          ? ("DOCTYPE" as const)
          : ("TEXT-TOKEN" as const),
      outputKeywordKey: ss.keywordKey,
    }));

  const foundEntities = await doSearchEntities(
    { type: entityKind, all: writingText },
    loginInfo,
  );
  const entities = foundEntities.result.map((e) => ({
    kind: "ENTITY" as const,
    id: e.entityId,
    alternatives: [e.name],
    displayDescription: e.name,
    compatibleKeywordKeys: [],
    type: e.type,
  }));

  const defaults = findInDefault(
    writingText,
    Boolean(writingToken.keywordKey),
  ).filter((ss) => !entityKind || entityKind === ss.kind);

  const suggestions: InputSuggestionToken[] = [
    ...entities,
    ...fromServer,
    ...defaults,
  ];

  return suggestions;
}
