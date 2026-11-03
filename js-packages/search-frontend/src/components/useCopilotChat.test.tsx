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
import { act, renderHook, waitFor } from "@testing-library/react";
import { TextDecoder, TextEncoder } from "util";
import { describe, expect, test, vi } from "vitest";
import useCopilotChat from "./useCopilotChat";

// jsdom does not expose TextEncoder/TextDecoder
const globalWithEncoding = globalThis as typeof globalThis & {
  TextEncoder?: typeof TextEncoder;
  TextDecoder?: typeof TextDecoder;
};
globalWithEncoding.TextEncoder = globalWithEncoding.TextEncoder || TextEncoder;
globalWithEncoding.TextDecoder = globalWithEncoding.TextDecoder || TextDecoder;

vi.mock("react-i18next", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

const mockGetChatResponse = vi.fn();

vi.mock("./client", () => ({
  CHAT_TOOL_ENDPOINT: "/api/rag/chat-tool",
  useOptionalOpenK9Client: () => ({
    getChatResponse: (...args: unknown[]) => mockGetChatResponse(...args),
  }),
}));

function sseResponse(events: Array<Record<string, unknown>>) {
  const encoder = new TextEncoder();
  const frames = events.map((event) => `data: ${JSON.stringify(event)}\n\n`);
  let index = 0;
  return {
    ok: true,
    body: {
      getReader: () => ({
        read: async () =>
          index < frames.length
            ? { value: encoder.encode(frames[index++]), done: false }
            : { value: undefined, done: true },
      }),
    },
  };
}

describe("useCopilotChat", () => {
  test("STATUS events of the orchestrated module are not answer text", async () => {
    mockGetChatResponse.mockResolvedValue(
      sseResponse([
        { chunk: "planning", type: "STATUS" },
        { chunk: "discovering", type: "STATUS" },
        { chunk: "retrieving", type: "STATUS" },
        { chunk: "generating", type: "STATUS" },
        { chunk: "", type: "START" },
        { chunk: "OpenK9 è un motore di ricerca.", type: "CHUNK" },
        { chunk: "", type: "END" },
      ]),
    );
    const { result } = renderHook(() => useCopilotChat());

    await act(async () => {
      await result.current.send({
        question: "Che cos'è OpenK9?",
        searchText: "Che cos'è OpenK9?",
        language: "it_IT",
      });
    });

    await waitFor(() => expect(result.current.messages[0]?.status).toBe("END"));
    expect(result.current.messages[0]?.answer).toBe(
      "OpenK9 è un motore di ricerca.",
    );
  });
});
