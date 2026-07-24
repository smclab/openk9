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

type StreamData = {
  type?: string;
  chunk?: string;
  message?: string;
  title?: string;
  url?: string;
  source?: string;
};

function toHistoryEntry(message: Message, index: number): ChatHistoryEntry {
  return {
    question: message.question,
    answer: message.answer,
    title: "",
    sources: message.sources ?? [],
    chat_id: "",
    timestamp: message.sendTime ?? "",
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
  const [messages, setMessages] = useState<Message[]>([]);
  const [isChatting, setIsChatting] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  // abort any in-flight stream when the consumer unmounts, otherwise the
  // reader loop keeps calling setState on an unmounted tree
  useEffect(
    () => () => {
      abortRef.current?.abort();
    },
    [],
  );

  const updateLast = useCallback((patch: (prev: Message) => Message) => {
    setMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = prev.slice();
      next[next.length - 1] = patch(next[next.length - 1]);
      return next;
    });
  }, []);

  const send = useCallback(
    async ({ question, searchText, language }: SendArgs) => {
      const history = messages.map(toHistoryEntry);
      const newMessage: Message = {
        question,
        answer: "",
        sendTime: new Date().toISOString(),
        status: "CHUNK",
        sources: [],
      };
      setMessages((prev) => [...prev, newMessage]);
      setIsChatting(true);

      const controller = new AbortController();
      abortRef.current = controller;

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
          updateLast((prev) => ({
            ...prev,
            status: "ERROR",
            answer: t("copilot-error"),
          }));
          setIsChatting(false);
          abortRef.current = null;
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
            const data = JSON.parse(dataLines.join("")) as StreamData;
            switch (data.type) {
              case "START":
                break;
              case "CHUNK":
                updateLast((prev) => ({
                  ...prev,
                  answer: prev.answer + (data.chunk ?? ""),
                  status: "CHUNK",
                }));
                break;
              case "DOCUMENT": {
                const source: ChatSource = {
                  title: data.title,
                  url: data.url,
                  source: data.source,
                };
                updateLast((prev) => ({
                  ...prev,
                  sources: [...(prev.sources ?? []), source],
                }));
                break;
              }
              case "END":
                updateLast((prev) => ({ ...prev, status: "END" }));
                setIsChatting(false);
                break;
              case "ERROR":
                updateLast((prev) => ({
                  ...prev,
                  status: "ERROR",
                  answer: data.message || data.chunk || t("copilot-error"),
                }));
                setIsChatting(false);
                break;
              case "GUARDRAIL":
                updateLast((prev) => ({
                  ...prev,
                  answer: t("guardrail-violation"),
                  status: "END",
                }));
                setIsChatting(false);
                break;
              default:
                if (typeof data.chunk === "string") {
                  updateLast((prev) => ({
                    ...prev,
                    answer: prev.answer + data.chunk,
                    status: "CHUNK",
                  }));
                }
                break;
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

        updateLast((prev) =>
          prev.status === "CHUNK" ? { ...prev, status: "END" } : prev,
        );
        setIsChatting(false);
      } catch (error) {
        // a deliberate abort (unmount / reset / stop) is not a user-facing error
        const aborted =
          error instanceof DOMException && error.name === "AbortError";
        updateLast((prev) => ({
          ...prev,
          status: aborted ? "END" : "ERROR",
          answer: aborted || prev.answer ? prev.answer : t("copilot-error"),
        }));
        setIsChatting(false);
      }
      abortRef.current = null;
    },
    [client, endpoint, baseUrl, messages, t, updateLast],
  );

  const cancel = useCallback(() => {
    if (abortRef.current) {
      abortRef.current.abort();
      abortRef.current = null;
      updateLast((prev) => ({ ...prev, status: "END" }));
      setIsChatting(false);
    }
  }, [updateLast]);

  const reset = useCallback(() => {
    if (abortRef.current) {
      abortRef.current.abort();
      abortRef.current = null;
    }
    setMessages([]);
    setIsChatting(false);
  }, []);

  return { messages, isChatting, send, cancel, reset };
}

export default useCopilotChat;
