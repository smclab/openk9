import { useState, useEffect, useCallback } from "react";
import { GenerateRequest, useOpenK9Client } from "./client";

type Source = { source?: string; title?: string; url?: string };

export interface Message {
  question: string;
  answer: string;
  sendTime?: string | null;
  status?: "END" | "CHUNK" | "ERROR";
  sources?: Source[];
}

type UseArgs = {
  initialMessages: Message[];
  isMockEnabled?: boolean;
  setIsRequestLoading?: (loading: boolean) => void;
};

type GenerateFn = (
  query: string,
  searchQuery: any[],
  language: string,
  sortAfterKey: string,
  sort: any,
  range: [number, number],
) => Promise<void>;

type Client = ReturnType<typeof useOpenK9Client>;

const useGenerateResponse = ({
  initialMessages,
  isMockEnabled = false,
  setIsRequestLoading,
}: UseArgs) => {
  const [message, setMessage] = useState<Message | null>(null);
  const [abortController, setAbortController] =
    useState<AbortController | null>(null);
  const [isChatting, setIsChatting] = useState<boolean>(false);
  const client: Client = useOpenK9Client();

  useEffect(() => {
    if (initialMessages.length > 0) {
      setMessage(initialMessages[0]);
      setIsChatting(false);
      setIsRequestLoading?.(false);
    }
  }, [initialMessages, setIsRequestLoading]);

  const generateResponse: GenerateFn = useCallback(
    async (query, searchQuery, language, sortAfterKey, sort, range) => {
      const newMessage: Message = {
        question: query,
        answer: "",
        sendTime: new Date().toISOString(),
        status: "CHUNK",
        sources: [],
      };

      setMessage(newMessage);
      setIsChatting(true);
      setIsRequestLoading?.(true);

      const controller = new AbortController();
      setAbortController(controller);

      const searchQueryT: GenerateRequest = {
        searchText: query,
        language,
        range,
        sort,
        sortAfterKey,
        searchQuery,
      };

      try {
        const response = await client.getGenerateResponse({
          searchQuery: searchQueryT,
          controller,
        });

        const stream = response.body;
        if (!stream) {
          setIsChatting(false);
          setIsRequestLoading?.(false);
          setMessage((prev) =>
            prev ? { ...prev, status: "ERROR", answer: "No stream" } : prev,
          );
          setAbortController(null);
          return;
        }

        const reader = stream.getReader();
        const decoder = new TextDecoder("utf-8");

        let buffer = "";
        let sawAnyChunk = false;
        let done = false;

        const flushEvent = (raw: string) => {
          const clean = raw.replace(/\r/g, "");
          const lines = clean.split("\n");
          const dataLines = lines
            .filter((l) => l.startsWith("data: "))
            .map((l) => l.slice(6));
          if (dataLines.length === 0) return;
          const dataStr = dataLines.join("");
          try {
            const data = JSON.parse(dataStr) as {
              type?: string;
              chunk?: string;
              message?: string;
            };
            switch (data.type) {
              case "START":
                setIsRequestLoading?.(false);
                break;
              case "CHUNK":
                if (!sawAnyChunk) {
                  sawAnyChunk = true;
                  setIsRequestLoading?.(false);
                }
                setMessage((prev) =>
                  prev
                    ? {
                        ...prev,
                        answer: prev.answer + (data.chunk ?? ""),
                        status: "CHUNK",
                      }
                    : prev,
                );
                break;
              case "END":
                setMessage((prev) =>
                  prev ? { ...prev, status: "END" } : prev,
                );
                setIsChatting(false);
                setIsRequestLoading?.(false);
                break;
              case "ERROR":
                setMessage((prev) =>
                  prev
                    ? {
                        ...prev,
                        status: "ERROR",
                        answer: data.message || data.chunk || "ERROR",
                      }
                    : prev,
                );
                setIsChatting(false);
                setIsRequestLoading?.(false);
                break;
              default:
                if (typeof data.chunk === "string") {
                  if (!sawAnyChunk) {
                    sawAnyChunk = true;
                    setIsRequestLoading?.(false);
                  }
                  setMessage((prev) =>
                    prev
                      ? {
                          ...prev,
                          answer: prev.answer + data.chunk,
                          status: "CHUNK",
                        }
                      : prev,
                  );
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

            if (line.trim() === "") {
              continue;
            }

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

          let dblIdx: number;
          while ((dblIdx = buffer.indexOf("\n\n")) !== -1) {
            const rawEvent = buffer.slice(0, dblIdx);
            buffer = buffer.slice(dblIdx + 2);
            flushEvent(rawEvent);
          }
        }

        const tail = buffer.trim();
        if (tail.length) flushEvent(tail);

        setIsRequestLoading?.(false);
        setIsChatting(false);
      } catch {
        setIsChatting(false);
        setIsRequestLoading?.(false);
      }

      setAbortController(null);
    },
    [client, setIsRequestLoading],
  );

  const cancelAllResponses = useCallback(() => {
    if (abortController) {
      abortController.abort();
      setMessage((prev) =>
        prev
          ? {
              ...prev,
              status: "END",
              answer: prev.answer + "... Response was cancelled",
            }
          : prev,
      );
      setAbortController(null);
      setIsChatting(false);
      setIsRequestLoading?.(false);
    }
  }, [abortController, setIsRequestLoading]);

  return {
    message,
    generateResponse,
    cancelAllResponses,
    isChatting,
  };
};

export default useGenerateResponse;
export type { GenerateFn };
