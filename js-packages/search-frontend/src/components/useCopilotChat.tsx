import { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  CHAT_TOOL_ENDPOINT,
  ChatHistoryEntry,
  ChatRequest,
  ChatSource,
  useOpenK9Client,
} from "./client";
import { Message } from "./useGenerateResponse";

type SendArgs = {
  /** the raw user question, shown in the thread */
  question: string;
  /** the text actually sent to the backend (may include stuffed context) */
  searchText: string;
  language: string;
};

/**
 * A question/answer pair with an identity of its own. Stream events are applied
 * by `id`, never by position in the thread: a turn that is superseded, stopped
 * or restarted has to keep owning its own chunks and its own sources.
 */
export type Turn = Message & { id: string };

/** the stream, bound to the turn it was started for */
type InFlight = { controller: AbortController; turnId: string };

type StreamEvent = {
  type?: string;
  /**
   * a text delta on `CHUNK`, the cited document on `DOCUMENT`. It comes off the
   * wire, so it is narrowed before use rather than declared.
   */
  chunk?: unknown;
  message?: string;
};

function optionalString(value: unknown): string | undefined {
  return typeof value === "string" && value.trim() !== "" ? value : undefined;
}

/**
 * Read the cited document out of a `DOCUMENT` event. The RAG modules nest it
 * under `chunk` - see `_stream_documents` in `agentic_rag.py` and the
 * `"type": "DOCUMENT"` yield in `rag-module/app/utils/llm.py` - so its fields
 * sit one level deeper than the event's own.
 */
function toChatSource(chunk: unknown): ChatSource | null {
  if (typeof chunk !== "object" || chunk === null) return null;
  const fields = chunk as Record<string, unknown>;
  const source: ChatSource = {
    title: optionalString(fields.title),
    url: optionalString(fields.url),
    source: optionalString(fields.source),
    filename: optionalString(fields.filename),
    file_extension: optionalString(fields.file_extension),
  };
  return Object.values(source).some((value) => value !== undefined)
    ? source
    : null;
}

/**
 * What makes two citations the same document. The event carries no document id
 * (the RAG module strips it before yielding), so the url is the identity when
 * there is one, and the file name or the title otherwise.
 */
function sourceKey(source: ChatSource): string | undefined {
  if (source.url !== undefined) return source.url;
  if (source.filename !== undefined) {
    return source.filename + (source.file_extension ?? "");
  }
  return source.title;
}

function toHistoryEntry(turn: Turn, index: number): ChatHistoryEntry {
  return {
    question: turn.question,
    answer: turn.answer,
    title: "",
    sources: turn.sources ?? [],
    chat_id: "",
    timestamp: turn.sendTime ?? "",
    chat_sequence_number: index,
  };
}

export function useCopilotChat({
  endpoint,
  baseUrl,
  client: clientOverride,
}: {
  endpoint?: string;
  baseUrl?: string;
  /** pass `openk9.client` to use the hook without the React context provider */
  client?: ReturnType<typeof useOpenK9Client>;
} = {}) {
  const contextClient = useOpenK9Client();
  const client = clientOverride ?? contextClient;
  const { t } = useTranslation();
  const [messages, setMessages] = useState<Turn[]>([]);
  const [isChatting, setIsChatting] = useState(false);
  const inFlightRef = useRef<InFlight | null>(null);
  // turns the user stopped: whatever their stream still delivers is dropped
  const cancelledRef = useRef<Set<string>>(new Set());
  // never reset, so a turn id stays unique for the life of the hook and a stale
  // stream can never collide with a turn created after it
  const turnCounterRef = useRef(0);

  // abort any in-flight stream when the consumer unmounts, otherwise the
  // reader loop keeps calling setState on an unmounted tree
  useEffect(
    () => () => {
      inFlightRef.current?.controller.abort();
    },
    [],
  );

  const updateTurn = useCallback(
    (turnId: string, patch: (prev: Turn) => Turn) => {
      setMessages((prev) => {
        const index = prev.findIndex((turn) => turn.id === turnId);
        if (index === -1) return prev;
        const next = prev.slice();
        next[index] = patch(next[index]);
        return next;
      });
    },
    [],
  );

  const send = useCallback(
    async ({ question, searchText, language }: SendArgs) => {
      const history = messages.map(toHistoryEntry);
      turnCounterRef.current += 1;
      const turnId = `turn-${turnCounterRef.current}`;
      const newTurn: Turn = {
        id: turnId,
        question,
        answer: "",
        sendTime: new Date().toISOString(),
        status: "CHUNK",
        sources: [],
      };
      setMessages((prev) => [...prev, newTurn]);
      setIsChatting(true);

      const controller = new AbortController();
      inFlightRef.current = { controller, turnId };
      // true while this stream is still the current one: a reset() or a newer
      // send() supersedes us and owns `isChatting` from there on
      const owns = () => inFlightRef.current?.controller === controller;

      const request: ChatRequest = {
        searchText,
        chatSequenceNumber: history.length + 1,
        timestamp: new Date().toISOString(),
        language,
        chatHistory: history,
      };

      try {
        const response = await client.getChatResponse({
          searchQuery: request,
          controller,
          url: endpoint || CHAT_TOOL_ENDPOINT,
          baseUrl,
        });

        const stream = response.body;
        if (!stream) {
          if (owns()) {
            updateTurn(turnId, (prev) => ({
              ...prev,
              status: "ERROR",
              answer: t("copilot-error"),
            }));
            setIsChatting(false);
            inFlightRef.current = null;
          }
          return;
        }

        const reader = stream.getReader();
        const decoder = new TextDecoder("utf-8");
        let buffer = "";
        let done = false;

        const flushEvent = (raw: string) => {
          const dataLines = raw
            .replace(/\r/g, "")
            .split("\n")
            .filter((l) => l.startsWith("data: "))
            .map((l) => l.slice(6));
          if (dataLines.length === 0) return;
          try {
            const data = JSON.parse(dataLines.join("")) as StreamEvent;
            switch (data.type) {
              case "START":
                break;
              case "CHUNK":
                updateTurn(turnId, (prev) => ({
                  ...prev,
                  answer: prev.answer + (optionalString(data.chunk) ?? ""),
                  status: "CHUNK",
                }));
                break;
              case "DOCUMENT": {
                // a stopped turn keeps nothing that arrives after the stop
                if (cancelledRef.current.has(turnId)) break;
                const source = toChatSource(data.chunk);
                if (!source) break;
                const key = sourceKey(source);
                updateTurn(turnId, (prev) => {
                  const sources = prev.sources ?? [];
                  // the same document cited twice stays at its first mention
                  const alreadyCited = sources.some(
                    (cited) => sourceKey(cited) === key,
                  );
                  return alreadyCited
                    ? prev
                    : { ...prev, sources: [...sources, source] };
                });
                break;
              }
              case "END":
                updateTurn(turnId, (prev) => ({ ...prev, status: "END" }));
                if (owns()) setIsChatting(false);
                break;
              case "ERROR":
                updateTurn(turnId, (prev) => ({
                  ...prev,
                  status: "ERROR",
                  answer:
                    data.message ??
                    optionalString(data.chunk) ??
                    t("copilot-error"),
                }));
                if (owns()) setIsChatting(false);
                break;
              case "GUARDRAIL":
                updateTurn(turnId, (prev) => ({
                  ...prev,
                  answer: t("guardrail-violation"),
                  status: "END",
                }));
                if (owns()) setIsChatting(false);
                break;
              default: {
                const text = optionalString(data.chunk);
                if (text !== undefined) {
                  updateTurn(turnId, (prev) => ({
                    ...prev,
                    answer: prev.answer + text,
                    status: "CHUNK",
                  }));
                }
                break;
              }
            }
          } catch {}
        };

        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          buffer += decoder.decode(value || new Uint8Array(), {
            stream: !done,
          });

          let idx: number;
          while ((idx = buffer.indexOf("\n")) !== -1) {
            const line = buffer.slice(0, idx);
            buffer = buffer.slice(idx + 1);
            if (line.trim() === "") continue;
            if (line.startsWith("data: ")) {
              flushEvent(line);
            } else {
              const possible = line.split("\r").join("");
              if (possible.startsWith("data: ")) flushEvent(possible);
            }

            let dblIdx: number;
            while ((dblIdx = buffer.indexOf("\n\n")) !== -1) {
              const rawEvent = buffer.slice(0, dblIdx);
              buffer = buffer.slice(dblIdx + 2);
              flushEvent(rawEvent);
            }
          }
        }
        const tail = buffer.trim();
        if (tail.length) flushEvent(tail);

        // only finalize if this stream is still the current one
        if (owns()) {
          updateTurn(turnId, (prev) =>
            prev.status === "CHUNK" ? { ...prev, status: "END" } : prev,
          );
          setIsChatting(false);
          inFlightRef.current = null;
        }
      } catch (error) {
        // a superseded stream must not clobber the current one's state
        if (owns()) {
          // a deliberate abort (unmount / stop) is not a user-facing error
          const aborted =
            error instanceof DOMException && error.name === "AbortError";
          updateTurn(turnId, (prev) => ({
            ...prev,
            status: aborted ? "END" : "ERROR",
            answer: aborted || prev.answer ? prev.answer : t("copilot-error"),
          }));
          setIsChatting(false);
          inFlightRef.current = null;
        }
      }
    },
    [client, endpoint, baseUrl, messages, t, updateTurn],
  );

  const cancel = useCallback(() => {
    const inFlight = inFlightRef.current;
    if (!inFlight) return;
    inFlightRef.current = null;
    cancelledRef.current.add(inFlight.turnId);
    inFlight.controller.abort();
    updateTurn(inFlight.turnId, (prev) => ({ ...prev, status: "END" }));
    setIsChatting(false);
  }, [updateTurn]);

  const reset = useCallback(() => {
    const inFlight = inFlightRef.current;
    if (inFlight) {
      inFlightRef.current = null;
      cancelledRef.current.add(inFlight.turnId);
      inFlight.controller.abort();
    }
    setMessages([]);
    setIsChatting(false);
  }, []);

  return { messages, isChatting, send, cancel, reset };
}

export default useCopilotChat;
