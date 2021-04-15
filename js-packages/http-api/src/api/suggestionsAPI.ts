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
  SearchToken,
  TokenSuggestion,
} from "../types";
import { LoginInfo } from "./authAPI";
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
];

const defaultTokens: TokenSuggestion[] = [
  {
    kind: "TOKEN",
    id: "Realizzazione",
    alternatives: ["technical", "analysis", "analisi", "tecnica"],
    displayDescription: "Technical Analysis",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "Storico Baseline",
    alternatives: ["scope", "work", "of"],
    displayDescription: "Scope of Work",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "Analisi",
    alternatives: ["requirements"],
    displayDescription: "Requirements",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "Sal",
    alternatives: ["sal", "stato", "lavori", "project", "review", "meeting"],
    displayDescription: "Project Review",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "Contratto",
    alternatives: ["contratto", "contract", "agreement"],
    displayDescription: "Contract",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "Verbale",
    alternatives: ["verbale", "report", "notes", "meeting"],
    displayDescription: "Meeting Notes",
    outputTokenType: "TEXT-TOKEN",
    outputKeywordKey: "document.documentType",
  },
  {
    kind: "TOKEN",
    id: "email",
    alternatives: ["email", "mail", "posta"],
    displayDescription: "Email",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "document",
    alternatives: ["document", "documento", "office"],
    displayDescription: "Document",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "office-word",
    alternatives: ["document", "documento", "office", "word"],
    displayDescription: "Word Document",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "office-powerpoint",
    alternatives: [
      "presentazione",
      "power",
      "office",
      "point",
      "powerpoint",
      "presentation",
      "slide",
      "slides",
    ],
    displayDescription: "Slides",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "office-excel",
    alternatives: [
      "sheet",
      "excel",
      "office",
      "calcolo",
      "foglio",
      "spreadsheet",
    ],
    displayDescription: "Spreadsheet",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "pdf",
    alternatives: ["pdf", "documento", "acrobat", "document"],
    displayDescription: "PDF Document",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "calendar",
    alternatives: ["calendar", "calendario", "evento", "data", "event", "date"],
    displayDescription: "Calendar",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "user",
    alternatives: ["user", "utente", "contatto", "contact"],
    displayDescription: "User",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "application",
    alternatives: [
      "applications",
      "application",
      "applicationi",
      "programmi",
      "app",
      "strumenti",
      "tools",
    ],
    displayDescription: "Applications",
    outputTokenType: "DOCTYPE",
    outputKeywordKey: "type",
  },
  {
    kind: "TOKEN",
    id: "spaces",
    alternatives: ["spaces", "open", "square"],
    displayDescription: "OS Spaces",
    outputTokenType: "DATASOURCE",
    outputKeywordKey: "dataSource",
  },
  {
    kind: "TOKEN",
    id: "liferay",
    alternatives: ["liferay"],
    displayDescription: "Liferay",
    outputTokenType: "DATASOURCE",
    outputKeywordKey: "dataSource",
  },
  {
    kind: "TOKEN",
    id: "web",
    alternatives: ["web"],
    displayDescription: "web",
    outputTokenType: "DATASOURCE",
    outputKeywordKey: "dataSource",
  },
];

function findInDefault(query: string, noParams = false, exact = false) {
  const exactParams = defaultParams.filter((p) => p.id === query);
  const exactTokens = defaultTokens.filter((p) => p.id === query);

  const params = exact
    ? []
    : defaultParams.filter(
        (p) =>
          p.id !== query &&
          (p.id.startsWith(query) ||
            p.alternatives.some((a) => a.startsWith(query)) ||
            p.displayDescription.includes(query)),
      );
  const tokens = exact
    ? []
    : defaultTokens.filter(
        (p) =>
          p.id !== query &&
          (p.id.startsWith(query) ||
            p.alternatives.some((a) => a.startsWith(query)) ||
            p.displayDescription.includes(query)),
      );

  return [
    ...exactTokens,
    ...(noParams ? [] : exactParams),
    ...(noParams ? [] : params),
    ...tokens,
  ];
}

export async function getTokenSuggestions(
  writingToken: SearchToken,
  loginInfo: LoginInfo | null,
  noParams?: boolean,
): Promise<InputSuggestionToken[]> {
  const writingText = writingToken.values[0];
  const entityId =
    writingToken.tokenType === "ENTITY" && writingToken.values[0];
  const keywordKey = writingToken.keywordKey;

  if (entityId) {
    const foundEntities = await doSearchEntities({ entityId }, loginInfo);
    return foundEntities.result.map((e) => ({
      kind: "ENTITY",
      id: e.entityId,
      alternatives: [e.name],
      displayDescription: e.name,
      compatibleKeywordKeys: [],
      type: e.type,
    }));
  }

  const defaultsKK = keywordKey
    ? findInDefault(keywordKey, noParams, true)
    : [];

  if (writingText && writingText.length > 0) {
    const foundEntities = await doSearchEntities(
      { all: writingText },
      loginInfo,
    );
    const entities = foundEntities.result.map((e) => ({
      kind: "ENTITY",
      id: e.entityId,
      alternatives: [e.name],
      displayDescription: e.name,
      compatibleKeywordKeys: [],
      type: e.type,
    }));

    const defaultsText = findInDefault(writingText, noParams, false);

    const suggestions: InputSuggestionToken[] = [
      ...defaultsText,
      ...defaultsKK,
      ...entities.map((entity) => ({
        ...entity,
        kind: "ENTITY" as const,
      })),
    ];

    return suggestions;
  }

  return defaultsKK;
}
