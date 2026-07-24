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
import i18n from "../i18n";

/** RAG chat-tool endpoint shared by the chat response and refined-search calls */
export const CHAT_TOOL_ENDPOINT = "/api/rag/chat-tool";

export type ChatSource = { source?: string; title?: string; url?: string };

export type ChatHistoryEntry = {
  question: string;
  answer: string;
  title: string;
  sources: Array<ChatSource>;
  chat_id: string;
  timestamp: string;
  chat_sequence_number: number;
};

export type ChatRequest = {
  searchText: string;
  chatSequenceNumber: number;
  timestamp: string;
  language: string;
  chatHistory: Array<ChatHistoryEntry>;
};

/**
 * the authenticated fetch provided by `OpenK9Client`. The optional `base`
 * overrides the search tenant so the generative calls can target a separate
 * tenant/host while reusing the same auth token.
 */
type AuthFetch = (
  route: string,
  init?: RequestInit,
  base?: string,
) => Promise<Response>;

/**
 * Chat-tool (RAG) calls, split out of `client.ts` to keep the generation
 * concern in one place. Spread into the `OpenK9Client` instance so callers
 * keep using `client.getChatResponse` / `client.getRefinedSearches`.
 */
export function createChatClient(authFetch: AuthFetch) {
  return {
    async getChatResponse({
      searchQuery,
      controller,
      url = CHAT_TOOL_ENDPOINT,
      baseUrl,
    }: {
      searchQuery: ChatRequest;
      controller: AbortController;
      url?: string;
      baseUrl?: string;
    }) {
      const data = await authFetch(
        url,
        {
          method: "POST",
          headers: {
            accept: "application/json",
            "Content-Type": "application/json",
          },
          body: JSON.stringify(searchQuery),
          signal: controller.signal,
        },
        baseUrl,
      );
      return data;
    },
    async getRefinedSearches({
      searchText,
      language,
      url = CHAT_TOOL_ENDPOINT,
      baseUrl,
      max = 3,
    }: {
      searchText: string;
      language: string;
      url?: string;
      baseUrl?: string;
      max?: number;
    }): Promise<string[]> {
      const prompt = i18n.t("copilot-refine-prompt", { searchText });
      const body: ChatRequest = {
        searchText: prompt,
        chatSequenceNumber: 1,
        timestamp: new Date().toISOString(),
        language,
        chatHistory: [],
      };
      const response = await authFetch(
        url,
        {
          method: "POST",
          headers: {
            accept: "application/json",
            "Content-Type": "application/json",
          },
          body: JSON.stringify(body),
        },
        baseUrl,
      );
      const raw = await response.text();
      let answer = "";
      for (const line of raw.split("\n")) {
        const clean = line.replace(/\r/g, "");
        if (!clean.startsWith("data: ")) continue;
        try {
          const data = JSON.parse(clean.slice(6)) as {
            type?: string;
            chunk?: string;
          };
          if (typeof data.chunk === "string") answer += data.chunk;
        } catch {}
      }
      return answer
        .split("\n")
        .map((l) => l.replace(/^[\s\-*\d.)]+/, "").trim())
        .filter((l) => l.length > 0)
        .slice(0, max);
    },
  };
}
