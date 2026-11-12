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
import React from "react";
import { beforeEach, describe, expect, test, vi } from "vitest";
import i18n from "../i18n";
import { OpenK9ClientContext } from "./client";
import useGenerateResponse, { Message } from "./useGenerateResponse";

type Client = NonNullable<React.ContextType<typeof OpenK9ClientContext>>;
type GetGenerateResponse = Client["getGenerateResponse"];

const noMessages: Message[] = [];

function renderGenerate(getGenerateResponse: GetGenerateResponse) {
  const client = { getGenerateResponse } as unknown as Client;
  return renderHook(
    () => useGenerateResponse({ initialMessages: noMessages }),
    {
      wrapper: ({ children }) => (
        <OpenK9ClientContext.Provider value={client}>
          {children}
        </OpenK9ClientContext.Provider>
      ),
    },
  );
}

type View = ReturnType<typeof renderGenerate>;

function startGenerate(view: View, question = "Chi ha acquisito SMC") {
  return view.result.current.generateResponse(
    question,
    [],
    "it_IT",
    "",
    undefined,
    [0, 10],
  );
}

async function generate(view: View) {
  await act(() => startGenerate(view));
}

function sseEvent(event: Record<string, string>): string {
  return `data: ${JSON.stringify(event)}\n\n`;
}

function sse(events: Array<Record<string, string>>): Response {
  return new Response(events.map(sseEvent).join(""));
}

// the requests started by pendingRequest, to answer them from the test
const answers: Array<{
  resolve: (response: Response) => void;
  signal: AbortSignal;
}> = [];

// a request that stays pending until the test answers it or it is aborted
function pendingRequest({ controller }: { controller: AbortController }) {
  return new Promise<Response>((resolve, reject) => {
    controller.signal.addEventListener("abort", () =>
      reject(new DOMException("Aborted", "AbortError")),
    );
    answers.push({ resolve, signal: controller.signal });
  });
}

const generateError = i18n.t("generate-error");

describe("the generated answer", () => {
  beforeEach(() => {
    answers.length = 0;
  });

  test("has a translated error text", () => {
    expect(i18n.exists("generate-error")).toBe(true);
  });

  test("shows an error when /api/rag/generate answers 500", async () => {
    // an error body is never read as the stream, even when it looks like one
    const view = renderGenerate(
      vi.fn(
        async () =>
          new Response(sseEvent({ type: "CHUNK", chunk: "Internal error" }), {
            status: 500,
          }),
      ),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "ERROR",
      answer: generateError,
    });
    expect(view.result.current.isChatting).toBe(false);
  });

  test("shows an error when the RAG service can't be reached", async () => {
    const view = renderGenerate(
      vi.fn(async () => {
        throw new TypeError("Failed to fetch");
      }),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "ERROR",
      answer: generateError,
    });
    expect(view.result.current.isChatting).toBe(false);
  });

  test("streams the answer as before when the response is 200", async () => {
    const view = renderGenerate(
      vi.fn(async () =>
        sse([
          { type: "START" },
          { type: "CHUNK", chunk: "SMC è stata " },
          { type: "CHUNK", chunk: "acquisita." },
          { type: "END" },
        ]),
      ),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "END",
      answer: "SMC è stata acquisita.",
    });
    expect(view.result.current.isChatting).toBe(false);
  });

  test("shows an error when the stream ends without any content", async () => {
    const view = renderGenerate(
      vi.fn(async () => sse([{ type: "START" }, { type: "END" }])),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "ERROR",
      answer: generateError,
    });
  });

  test("shows an error instead of the partial answer when the stream breaks", async () => {
    let pulls = 0;
    const body = new ReadableStream<Uint8Array>({
      pull(controller) {
        if (pulls++ === 0) {
          controller.enqueue(
            new TextEncoder().encode(sseEvent({ type: "CHUNK", chunk: "SMC" })),
          );
        } else {
          controller.error(new TypeError("network error"));
        }
      },
    });
    const view = renderGenerate(vi.fn(async () => new Response(body)));

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "ERROR",
      answer: generateError,
    });
    expect(view.result.current.isChatting).toBe(false);
  });

  test("hides the raw backend exception of an ERROR event", async () => {
    const view = renderGenerate(
      vi.fn(async () =>
        sse([
          { type: "START" },
          {
            type: "ERROR",
            chunk: "openai.APIConnectionError: Connection error.",
          },
        ]),
      ),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "ERROR",
      answer: generateError,
    });
  });

  test("replaces the partial answer with the redirect of the scope gate", async () => {
    const view = renderGenerate(
      vi.fn(async () =>
        sse([
          { type: "START" },
          { type: "CHUNK", chunk: "Parte della risposta" },
          { type: "CANCEL", chunk: "Posso rispondere solo su questo sito." },
        ]),
      ),
    );

    await generate(view);

    expect(view.result.current.message).toMatchObject({
      status: "END",
      answer: "Posso rispondere solo su questo sito.",
    });
  });

  test.each(["Inappropriate content", "  "])(
    "shows the translated guardrail text for the CANCEL chunk %j",
    async (chunk) => {
      const view = renderGenerate(
        vi.fn(async () =>
          sse([
            { type: "START" },
            { type: "CHUNK", chunk: "Testo bloccato" },
            { type: "CANCEL", chunk },
          ]),
        ),
      );

      await generate(view);

      expect(view.result.current.message).toMatchObject({
        status: "END",
        answer: i18n.t("guardrail-violation"),
      });
    },
  );

  test("keeps the cancelled message when the user stops the request", async () => {
    const view = renderGenerate(vi.fn(pendingRequest));

    let request: Promise<void> = Promise.resolve();
    act(() => {
      request = startGenerate(view);
    });
    await waitFor(() => expect(view.result.current.isChatting).toBe(true));

    act(() => view.result.current.cancelAllResponses());
    await act(() => request);

    expect(view.result.current.message).toMatchObject({
      status: "END",
      answer: "... Response was cancelled",
    });
  });

  test("lets only the latest of quick searches write the answer", async () => {
    const view = renderGenerate(vi.fn(pendingRequest));

    // as GenerateResponse does on every change of the search
    const requests: Array<Promise<void>> = [];
    for (const question of ["A", "B", "C"]) {
      await act(async () => {
        view.result.current.cancelAllResponses();
        requests.push(startGenerate(view, question));
      });
    }

    expect(answers.map(({ signal }) => signal.aborted)).toEqual([
      true,
      true,
      false,
    ]);
    expect(view.result.current.isChatting).toBe(true);

    answers[2].resolve(
      sse([{ type: "CHUNK", chunk: "Risposta C" }, { type: "END" }]),
    );
    await act(() => Promise.all(requests));

    expect(view.result.current.message).toMatchObject({
      question: "C",
      status: "END",
      answer: "Risposta C",
    });
  });

  test("an older request that ends does not drop the controller of the newer one", async () => {
    const view = renderGenerate(vi.fn(pendingRequest));

    // without a cancel in between, unlike GenerateResponse: only the
    // controller is checked here, not the message the two requests share
    let older: Promise<void> = Promise.resolve();
    await act(async () => {
      older = startGenerate(view, "A");
      void startGenerate(view, "B");
    });

    answers[0].resolve(sse([{ type: "END" }]));
    await act(() => older);
    act(() => view.result.current.cancelAllResponses());

    expect(answers[1].signal.aborted).toBe(true);
  });
});
