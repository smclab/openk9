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

const useGenerateResponse = ({
  initialMessages,
  isMockEnabled = false,
  setIsRequestLoading,
}: {
  initialMessages: Message[];
  isMockEnabled?: boolean;
  setIsRequestLoading?: any;
}) => {
  const [message, setMessage] = useState<Message | null>(null);
  const [abortController, setAbortController] =
    useState<AbortController | null>(null);
  const [isChatting, setIsChatting] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const client = useOpenK9Client();

  useEffect(() => {
    if (initialMessages.length > 0) {
      setMessage(initialMessages[0]);
      setIsChatting(false);
      setIsRequestLoading(false);
    }
  }, [initialMessages]);

  const generateMockResponse = async (query: string) => {
    const mockChunks = [
      `This is a mock chunk 1 for query: ${query}`,
      `This is a mock chunk 2 for query: ${query}`,
      `This is a mock chunk 3 for query: ${query}`,
    ];

    for (let i = 0; i < mockChunks.length; i++) {
      if (abortController?.signal.aborted) {
        setMessage((prev) =>
          prev
            ? {
                ...prev,
                status: "END",
                answer: prev.answer + "... Response was cancelled",
              }
            : prev,
        );
        setIsChatting(false);
        setIsRequestLoading(false);
        return;
      }

      await new Promise((resolve) => setTimeout(resolve, 1000));

      setMessage((prev) =>
        prev
          ? {
              ...prev,
              answer: prev.answer + mockChunks[i],
              status: "CHUNK",
            }
          : prev,
      );
    }

    setMessage((prev) =>
      prev
        ? {
            ...prev,
            status: "END",
            answer: prev.answer + " This is the end of the mock response.",
          }
        : prev,
    );
    setIsChatting(false);
    setIsRequestLoading(false);
  };

  const generateResponse = useCallback(
    async (
      query: string,
      searchQuery: any[],
      language: string,
      sortAfterKey: string,
      sort: any,
      range: [number, number],
    ) => {
      const newMessage: Message = {
        question: query,
        answer: "",
        sendTime: new Date().toISOString(),
        status: "CHUNK",
        sources: [],
      };
      setMessage(newMessage);
      setIsChatting(true);
      setIsRequestLoading(false);

      const controller = new AbortController();
      setAbortController(controller);

      if (isMockEnabled) {
        await generateMockResponse(query);
        return;
      }
      setIsLoading(true);

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
          controller: controller,
        });
        if (response.ok) {
          const reader = response.body?.getReader();
          setIsLoading(false);
          const decoder = new TextDecoder("utf-8");
          let done = false;
          let buffer = "";

          while (!done && reader) {
            const { value, done: readerDone } = await reader.read();
            done = readerDone;
            buffer += decoder.decode(value, { stream: true });
            let boundaryIndex;

            while ((boundaryIndex = buffer.indexOf("\n")) !== -1) {
              const chunk = buffer.slice(0, boundaryIndex);
              buffer = buffer.slice(boundaryIndex + 1);
              if (chunk.trim().startsWith("data: ")) {
                const dataStr = chunk.trim().slice(6);
                try {
                  const data = JSON.parse(dataStr);
                  if (data.type === "ERROR") {
                    setIsChatting(false);
                    setIsRequestLoading(false);
                    setMessage((prev) =>
                      prev
                        ? {
                            ...prev,
                            answer: data.chunk || "ERROR",
                            status: "ERROR",
                          }
                        : prev,
                    );
                  } else {
                    setMessage((prev) =>
                      prev
                        ? {
                            ...prev,
                            answer: prev.answer + data.chunk,
                            status: data.type === "END" ? "END" : "CHUNK",
                          }
                        : prev,
                    );
                    if (data.type === "END") {
                      setIsChatting(false);
                      setIsRequestLoading(false);
                    }
                  }
                } catch (e) {
                  console.error("Error parsing JSON", e);
                }
              }
            }
          }
        } else {
          console.error("Error generating response");
          setIsChatting(false);
          setIsRequestLoading(false);
          setMessage((prev) =>
            prev
              ? {
                  ...prev,
                  answer: response?.statusText || "Error",
                  status: "ERROR",
                }
              : prev,
          );
          setIsChatting(false);
          setIsRequestLoading(false);
        }
      } catch (error) {
        console.error("Error during response generation", error);
        setIsChatting(false);
        setIsRequestLoading(false);
      }

      setAbortController(null);
    },
    [isMockEnabled],
  );

  const cancelResponse = () => {
    if (abortController) {
      setIsLoading(false);
      abortController.abort();
      setMessage((prev) =>
        prev
          ? {
              ...prev,
              status: "END",
              answer: "Response was cancelled",
            }
          : prev,
      );
      setAbortController(null);
      setIsChatting(false);
    } else {
      console.warn("No AbortController found");
    }
  };

  const cancelAllResponses = () => {
    if (abortController) {
      setIsLoading(false);
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
      setIsRequestLoading(false);
    }
  };

  return {
    message,
    generateResponse,
    cancelResponse,
    cancelAllResponses,
    isChatting,
    isLoading,
  };
};

export default useGenerateResponse;
