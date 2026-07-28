import React from "react";
import Markdown from "react-markdown";
import { css, keyframes } from "styled-components";
import { useTranslation } from "react-i18next";
import { useCopilotChat } from "./useCopilotChat";
import { ChatSource, useOpenK9Client } from "./client";
import { Message } from "./useGenerateResponse";

type CopilotProps = {
  endpoint?: string | null;
  /** separate base URL/tenant for the generative calls */
  baseUrl?: string | null;
  language: string;
  searchText?: string;
  onClose?: () => void;
  /** show the "related searches" suggestions (default: true) */
  suggestions?: boolean;
  /** how many related searches to request (default: 3) */
  maxSuggestions?: number;
  /** override the input placeholder */
  placeholder?: string;
  /** override the empty-thread hint */
  emptyState?: React.ReactNode;
  onMessageSent?: (question: string) => void;
  onResponse?: (message: Message) => void;
  onError?: (message: Message) => void;
  onSourceClick?: (source: ChatSource) => void;
};

const PRIMARY = "var(--openk9-embeddable-search--primary-color, #c22525)";
const BORDER = "var(--openk9-embeddable-search--border-color, #ced4da)";
const MUTED = "var(--openk9-embeddable-search--secondary-text-color, #3e4244)";
const TEXT = "var(--openk9-embeddable-search--primary-text-color, #1e1c21)";
const SURFACE =
  "var(--openk9-embeddable-search--primary-background-color, #ffffff)";
const SURFACE_2 =
  "var(--openk9-embeddable-search--secondary-background-color, #eeeeee)";
const DANGER = "var(--openk9-embeddable-search--error-color, #b3261e)";
const DANGER_BG =
  "var(--openk9-embeddable-search--error-background-color, #fdecea)";

// icon-only controls use inline SVGs (not emoji glyphs) so they render
// consistently across host operating systems and fonts
function SparkleIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <path
        d="M12 2l1.8 5.2L19 9l-5.2 1.8L12 16l-1.8-5.2L5 9l5.2-1.8L12 2z"
        fill="currentColor"
      />
    </svg>
  );
}

function SendIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" fill="currentColor" />
    </svg>
  );
}

function StopIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <rect x="6" y="6" width="12" height="12" rx="2" fill="currentColor" />
    </svg>
  );
}

function ResetIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <path
        d="M12 5V2L8 6l4 4V7a5 5 0 1 1-5 5H5a7 7 0 1 0 7-7z"
        fill="currentColor"
      />
    </svg>
  );
}

const blink = keyframes`
  0%, 80%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
`;

function TypingIndicator({ label }: { label?: string }) {
  return (
    <span
      role="status"
      aria-label={label}
      css={css`
        display: inline-flex;
        gap: 4px;
        align-items: center;
        span {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: ${MUTED};
          animation: ${blink} 1.2s infinite ease-in-out both;
        }
        span:nth-of-type(2) {
          animation-delay: 0.2s;
        }
        span:nth-of-type(3) {
          animation-delay: 0.4s;
        }
      `}
    >
      <span />
      <span />
      <span />
    </span>
  );
}

export function Copilot({
  endpoint,
  baseUrl,
  language,
  searchText,
  onClose,
  suggestions: suggestionsEnabled = true,
  maxSuggestions = 3,
  placeholder,
  emptyState,
  onMessageSent,
  onResponse,
  onError,
  onSourceClick,
}: CopilotProps) {
  const { t } = useTranslation();
  const { messages, isChatting, send, cancel, reset } = useCopilotChat({
    endpoint: endpoint ?? undefined,
    baseUrl: baseUrl ?? undefined,
  });
  const client = useOpenK9Client();
  const [input, setInput] = React.useState("");
  const [suggestions, setSuggestions] = React.useState<string[]>([]);
  const [suggestLoading, setSuggestLoading] = React.useState(false);
  const suggestionsCache = React.useRef<Map<string, string[]>>(new Map());
  const threadRef = React.useRef<HTMLDivElement | null>(null);
  const inputRef = React.useRef<HTMLInputElement | null>(null);

  React.useEffect(() => {
    if (threadRef.current) {
      threadRef.current.scrollTop = threadRef.current.scrollHeight;
    }
  }, [messages]);

  // move focus into the input when the panel opens
  React.useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // a new search hides the chips and brings back the "suggest" button
  React.useEffect(() => {
    setSuggestions([]);
  }, [searchText]);

  // notify the host once per turn when a response completes or errors
  const firedRef = React.useRef<Set<string>>(new Set());
  React.useEffect(() => {
    const last = messages[messages.length - 1];
    if (!last) return;
    const id = last.sendTime ?? String(messages.length);
    if (last.status === "END" && !firedRef.current.has("res:" + id)) {
      firedRef.current.add("res:" + id);
      onResponse?.(last);
    } else if (last.status === "ERROR" && !firedRef.current.has("err:" + id)) {
      firedRef.current.add("err:" + id);
      onError?.(last);
    }
  }, [messages, onResponse, onError]);

  const sendMessage = (raw: string) => {
    const question = raw.trim();
    if (!question || isChatting) return;
    onMessageSent?.(question);
    void send({ question, searchText: question, language });
  };

  const handleSend = () => {
    if (!input.trim() || isChatting) return;
    sendMessage(input);
    setInput("");
  };

  const loadSuggestions = async () => {
    const query = (searchText ?? "").trim();
    if (!query || suggestLoading) return;
    const cached = suggestionsCache.current.get(query);
    if (cached) {
      setSuggestions(cached);
      return;
    }
    setSuggestLoading(true);
    try {
      const result = await client.getRefinedSearches({
        searchText: query,
        language,
        url: endpoint ?? undefined,
        baseUrl: baseUrl ?? undefined,
        max: maxSuggestions,
      });
      suggestionsCache.current.set(query, result);
      setSuggestions(result);
    } catch {
      setSuggestions([]);
    }
    setSuggestLoading(false);
  };

  return (
    <div
      className="openk9-embeddable-search--copilot"
      onKeyDown={(event) => {
        if (event.key === "Escape" && onClose) onClose();
      }}
      css={css`
        display: flex;
        flex-direction: column;
        height: 100%;
        min-height: 0;
        background: ${SURFACE};
        border-radius: 8px;
        overflow: hidden;
        font-size: 14px;
        color: ${TEXT};
        button:focus-visible,
        input:focus-visible,
        textarea:focus-visible {
          outline: 2px solid ${PRIMARY};
          outline-offset: 2px;
        }
      `}
    >
      <div
        className="openk9-embeddable-search--copilot-header"
        css={css`
          display: flex;
          flex-direction: column;
          gap: 8px;
          padding: 14px 16px;
          border-bottom: 1px solid ${BORDER};
        `}
      >
        <div
          css={css`
            display: flex;
            justify-content: space-between;
            align-items: center;
          `}
        >
          <strong
            className="openk9-embeddable-search--copilot-title"
            css={css`
              display: inline-flex;
              align-items: center;
              gap: 6px;
              font-size: 15px;
              color: ${PRIMARY};
            `}
          >
            <SparkleIcon />
            {t("copilot-title")}
          </strong>
          <div
            css={css`
              display: flex;
              align-items: center;
              gap: 8px;
            `}
          >
            {onClose && (
              <button
                type="button"
                className="openk9-embeddable-search--copilot-close"
                aria-label={t("copilot-close") ?? ""}
                title={t("copilot-close") ?? ""}
                onClick={onClose}
                css={css`
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  width: 28px;
                  height: 28px;
                  border: none;
                  border-radius: 50%;
                  background: none;
                  color: ${MUTED};
                  font-size: 18px;
                  line-height: 1;
                  cursor: pointer;
                  &:hover {
                    background: ${SURFACE_2};
                    color: ${PRIMARY};
                  }
                `}
              >
                ×
              </button>
            )}
          </div>
        </div>
      </div>

      {suggestionsEnabled && searchText && searchText.trim() !== "" && (
        <div
          className="openk9-embeddable-search--copilot-suggestions"
          css={css`
            display: flex;
            flex-direction: column;
            gap: 8px;
            padding: 12px 16px;
            border-bottom: 1px solid ${BORDER};
          `}
        >
          {suggestions.length === 0 ? (
            <button
              type="button"
              className="openk9-embeddable-search--copilot-suggest-button"
              onClick={loadSuggestions}
              disabled={suggestLoading}
              css={css`
                align-self: flex-start;
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                border: 1px solid ${PRIMARY};
                border-radius: 999px;
                background: transparent;
                color: ${PRIMARY};
                font-size: 13px;
                font-weight: 600;
                cursor: pointer;
                &:disabled {
                  opacity: 0.5;
                  cursor: default;
                }
              `}
            >
              {suggestLoading
                ? t("copilot-suggest-loading")
                : t("copilot-suggest")}
            </button>
          ) : (
            <>
              <div
                className="openk9-embeddable-search--copilot-refine-header"
                css={css`
                  font-size: 12px;
                  color: ${MUTED};
                `}
              >
                {t("copilot-refine-header", { query: searchText })}
              </div>
              <div
                css={css`
                  display: flex;
                  flex-wrap: wrap;
                  gap: 6px;
                `}
              >
                {suggestions.map((suggestion) => (
                  <button
                    type="button"
                    className="openk9-embeddable-search--copilot-suggestion"
                    key={suggestion}
                    onClick={() => {
                      sendMessage(suggestion);
                      setSuggestions([]);
                    }}
                    disabled={isChatting}
                    css={css`
                      padding: 6px 12px;
                      border: 1px solid ${BORDER};
                      border-radius: 999px;
                      background: ${SURFACE_2};
                      font-size: 12px;
                      cursor: pointer;
                      text-align: left;
                      &:hover {
                        border-color: ${PRIMARY};
                        color: ${PRIMARY};
                      }
                      &:disabled {
                        opacity: 0.5;
                        cursor: default;
                      }
                    `}
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            </>
          )}
        </div>
      )}

      <div
        ref={threadRef}
        className="openk9-embeddable-search--copilot-thread"
        role="log"
        aria-live="polite"
        aria-relevant="additions text"
        aria-busy={isChatting}
        css={css`
          flex: 1;
          min-height: 0;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 16px;
          padding: 16px;
        `}
      >
        {messages.length === 0 && (
          <div
            className="openk9-embeddable-search--copilot-empty"
            css={css`
              margin: auto;
              text-align: center;
              color: ${MUTED};
              font-size: 13px;
              padding: 24px;
            `}
          >
            {emptyState ?? t("copilot-empty-hint")}
          </div>
        )}
        {messages.map((message, index) => {
          const isStreaming =
            message.status === "CHUNK" && message.answer === "";
          const isError = message.status === "ERROR";
          return (
            <div
              key={message.sendTime ?? index}
              className="openk9-embeddable-search--copilot-message"
              css={css`
                display: flex;
                flex-direction: column;
                gap: 8px;
              `}
            >
              <div
                className="openk9-embeddable-search--copilot-question"
                css={css`
                  align-self: flex-end;
                  background: ${PRIMARY};
                  color: white;
                  border-radius: 14px 14px 2px 14px;
                  padding: 8px 12px;
                  max-width: 85%;
                  line-height: 1.4;
                  overflow-wrap: anywhere;
                `}
              >
                {message.question}
              </div>
              <div
                className="openk9-embeddable-search--copilot-answer"
                css={css`
                  align-self: flex-start;
                  background: ${isError ? DANGER_BG : SURFACE_2};
                  color: ${isError ? DANGER : "inherit"};
                  border: ${isError ? `1px solid ${DANGER}` : "none"};
                  border-radius: 14px 14px 14px 2px;
                  padding: 10px 14px;
                  max-width: 90%;
                  line-height: 1.5;
                  overflow-wrap: anywhere;
                  word-break: break-word;
                  p {
                    margin: 0 0 8px;
                  }
                  p:last-child {
                    margin-bottom: 0;
                  }
                  a {
                    color: ${PRIMARY};
                    overflow-wrap: anywhere;
                  }
                `}
              >
                {isStreaming ? (
                  <TypingIndicator label={t("copilot-loading") ?? undefined} />
                ) : (
                  <Markdown>{message.answer}</Markdown>
                )}
                {message.sources && message.sources.length > 0 && (
                  <ul
                    className="openk9-embeddable-search--copilot-sources"
                    css={css`
                      margin: 10px 0 0;
                      padding-left: 18px;
                      font-size: 12px;
                      color: ${MUTED};
                    `}
                  >
                    {message.sources.map((source, sourceIndex) => (
                      <li
                        key={(source.url ?? source.title ?? "") + sourceIndex}
                      >
                        {source.url ? (
                          <a
                            href={source.url}
                            target="_blank"
                            rel="noreferrer"
                            onClick={() => onSourceClick?.(source)}
                            css={css`
                              overflow-wrap: anywhere;
                            `}
                          >
                            {source.title || source.url}
                          </a>
                        ) : (
                          source.title || source.source
                        )}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div
        className="openk9-embeddable-search--copilot-input"
        css={css`
          display: flex;
          gap: 8px;
          align-items: center;
          padding: 12px 16px;
          border-top: 1px solid ${BORDER};
        `}
      >
        <input
          ref={inputRef}
          type="text"
          className="openk9-embeddable-search--copilot-input-field"
          value={input}
          aria-label={placeholder ?? t("copilot-input-placeholder") ?? ""}
          placeholder={placeholder ?? t("copilot-input-placeholder") ?? ""}
          onChange={(event) => setInput(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") handleSend();
          }}
          css={css`
            flex: 1;
            padding: 10px 14px;
            border: 1px solid ${BORDER};
            border-radius: 999px;
            outline: none;
            font-size: 14px;
            color: ${TEXT};
            background: ${SURFACE};
            &:focus {
              border-color: ${PRIMARY};
            }
          `}
        />
        {messages.length > 0 && (
          <button
            type="button"
            className="openk9-embeddable-search--copilot-reset"
            onClick={reset}
            aria-label={t("copilot-reset") ?? ""}
            title={t("copilot-reset") ?? ""}
            css={css`
              display: inline-flex;
              align-items: center;
              justify-content: center;
              border: 1px solid ${BORDER};
              background: ${SURFACE};
              border-radius: 50%;
              width: 38px;
              height: 38px;
              cursor: pointer;
              color: ${MUTED};
              &:hover {
                color: ${PRIMARY};
                border-color: ${PRIMARY};
              }
            `}
          >
            <ResetIcon />
          </button>
        )}
        {isChatting ? (
          <button
            type="button"
            className="openk9-embeddable-search--copilot-stop"
            onClick={cancel}
            aria-label={t("copilot-stop") ?? ""}
            title={t("copilot-stop") ?? ""}
            css={css`
              display: inline-flex;
              align-items: center;
              justify-content: center;
              width: 38px;
              height: 38px;
              border: none;
              border-radius: 50%;
              background: ${PRIMARY};
              color: white;
              cursor: pointer;
            `}
          >
            <StopIcon />
          </button>
        ) : (
          <button
            type="button"
            className="openk9-embeddable-search--copilot-send"
            onClick={handleSend}
            disabled={input.trim() === ""}
            aria-label={t("copilot-send") ?? ""}
            title={t("copilot-send") ?? ""}
            css={css`
              display: inline-flex;
              align-items: center;
              justify-content: center;
              width: 38px;
              height: 38px;
              border: none;
              border-radius: 50%;
              background: ${PRIMARY};
              color: white;
              cursor: pointer;
              &:disabled {
                opacity: 0.4;
                cursor: default;
              }
            `}
          >
            <SendIcon />
          </button>
        )}
      </div>
    </div>
  );
}

export const CopilotMemo = React.memo(Copilot);
