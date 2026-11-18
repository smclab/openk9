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
import type { Source } from "./client";
import type { Message } from "./useGenerateResponse";

// sale quando cambia la forma salvata: i dati di un'altra versione si ignorano
const STORAGE_VERSION = 1;

/**
 * what the chatbot keeps in `sessionStorage` between the pages of the same tab:
 * the panel state and, for an authenticated user, the `chatId` only (the
 * backend keeps the messages); for an anonymous user the concluded messages
 */
export type SavedConversation = {
  /** the panel was open when the page was left */
  open: boolean;
  /** when the welcome message appeared, ISO 8601 */
  startedAt: string;
} & ({ chatId: string } | { messages: Message[] });

/** one key per tenant, so two chatbots on the same domain do not mix */
function storageKey(tenant: string) {
  return `openk9-chatbot-conversation:${tenant}`;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isDate(value: unknown): value is string {
  return typeof value === "string" && !Number.isNaN(Date.parse(value));
}

function optionalString(value: unknown): string | undefined {
  return typeof value === "string" ? value : undefined;
}

/** a sequence number from the backend can come back as a numeric string */
function toSequenceNumber(value: unknown): number | null {
  const number =
    typeof value === "string" && /^\d+$/.test(value) ? Number(value) : value;
  return typeof number === "number" && Number.isInteger(number) && number > 0
    ? number
    : null;
}

/** keeps the fields of a source the chatbot knows, dropping anything else */
function toSources(value: unknown): Source[] {
  if (!isRecord(value)) return [];
  return [
    {
      source: optionalString(value.source),
      title: optionalString(value.title),
      url: optionalString(value.url),
      filename: optionalString(value.filename),
      file_extension: optionalString(value.file_extension),
      domain: optionalString(value.domain),
      score: typeof value.score === "number" ? value.score : undefined,
      citations: Array.isArray(value.citations) ? value.citations : undefined,
    },
  ];
}

function toSavedMessage(value: unknown): Message | null {
  if (!isRecord(value)) return null;
  const { question, answer, status, sendTime, timestamp, sources } = value;
  const chatSequenceNumber = toSequenceNumber(value.chat_sequence_number);
  if (
    typeof question !== "string" ||
    typeof answer !== "string" ||
    (status !== "END" && status !== "ERROR") ||
    chatSequenceNumber === null ||
    !(sendTime === undefined || sendTime === null || isDate(sendTime)) ||
    !(timestamp === undefined || typeof timestamp === "string") ||
    !Array.isArray(sources)
  )
    return null;
  return {
    question,
    answer,
    status,
    sendTime: isDate(sendTime) ? sendTime : null,
    timestamp: optionalString(timestamp),
    sources: sources.flatMap(toSources),
    chat_sequence_number: chatSequenceNumber,
  };
}

/** what the previous page saved, or null when there is nothing usable */
function parseSavedConversation(value: unknown): SavedConversation | null {
  if (
    !isRecord(value) ||
    value.version !== STORAGE_VERSION ||
    typeof value.open !== "boolean" ||
    !isDate(value.startedAt)
  )
    return null;
  const panel = { open: value.open, startedAt: value.startedAt };
  if (typeof value.chatId === "string" && value.chatId !== "")
    return { ...panel, chatId: value.chatId };
  if (!Array.isArray(value.messages)) return null;
  const messages = value.messages.map(toSavedMessage);
  if (!messages.every((message) => message !== null)) return null;
  return { ...panel, messages };
}

/**
 * reads the conversation saved for `tenant`. Storage that cannot be read
 * (private browsing, site data blocked) and data that does not match the
 * expected shape both give null, as if nothing had been saved.
 */
export function loadSavedConversation(
  tenant: string,
): SavedConversation | null {
  try {
    const saved = sessionStorage.getItem(storageKey(tenant));
    if (saved === null) return null;
    const value: unknown = JSON.parse(saved);
    return parseSavedConversation(value);
  } catch {
    return null;
  }
}

/**
 * saves the conversation for `tenant`. Of the messages only the concluded ones
 * go in (complete answer or error): an answer still streaming when the page
 * changes is not resumed. A storage that refuses the write is ignored.
 */
export function saveConversation(
  tenant: string,
  conversation: SavedConversation,
) {
  const value =
    "chatId" in conversation
      ? { version: STORAGE_VERSION, ...conversation }
      : {
          version: STORAGE_VERSION,
          open: conversation.open,
          startedAt: conversation.startedAt,
          messages: conversation.messages.flatMap((message) =>
            message.status === "END" || message.status === "ERROR"
              ? [
                  {
                    question: message.question,
                    answer: message.answer,
                    status: message.status,
                    sendTime: message.sendTime,
                    timestamp: message.timestamp,
                    sources: message.sources ?? [],
                    chat_sequence_number: message.chat_sequence_number,
                  },
                ]
              : [],
          ),
        };
  try {
    sessionStorage.setItem(storageKey(tenant), JSON.stringify(value));
  } catch {
    // storage pieno o non disponibile: il chatbot funziona come senza
    // salvataggio, e la pagina dopo non deve riprendere una copia vecchia
    try {
      sessionStorage.removeItem(storageKey(tenant));
    } catch {
      // storage non disponibile: non c'e' niente da togliere
    }
  }
}

function toChatMessages(value: unknown): Message[] {
  if (!isRecord(value)) return [];
  const { question, answer, timestamp, sources } = value;
  const chatSequenceNumber = toSequenceNumber(value.chat_sequence_number);
  // un turno senza risposta salvata non ha niente da mostrare
  if (
    typeof question !== "string" ||
    typeof answer !== "string" ||
    chatSequenceNumber === null
  )
    return [];
  return [
    {
      question,
      answer,
      status: "END",
      // l'istante del checkpoint, cioe' di fine risposta: l'unico che il
      // backend restituisce, mostrato sotto la domanda
      sendTime: isDate(timestamp) ? timestamp : null,
      sources: Array.isArray(sources) ? sources.flatMap(toSources) : [],
      chat_sequence_number: chatSequenceNumber,
    },
  ];
}

/**
 * maps the body of `GET /api/rag/chat/{chat_id}` (`{ chat_id, messages: [{
 * question, answer, timestamp, chat_sequence_number, sources }] }`, sorted by
 * sequence number) to the chatbot messages. Turns it cannot read are dropped.
 */
export function parseChatResponse(value: unknown): Message[] {
  if (!isRecord(value) || !Array.isArray(value.messages)) return [];
  return value.messages.flatMap(toChatMessages);
}
