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

/**
 * The RAG answer marks each sentence with the number of the source it took
 * its statement from: "SMC opera dal 1995 [2]." The `SOURCES` stream event,
 * which arrives before the first token of the answer, says what each number
 * stands for, so the marker can be turned into a link while the answer is
 * still being written.
 *
 * This module holds the part that does not render: narrowing the event and
 * cutting the text around the markers. Each app turns the pieces into its own
 * elements.
 */

/** A source the answer may cite, as the `SOURCES` event describes it. */
export type CitedSource = {
  ordinal: number;
  documentId?: string;
  title?: string;
  url?: string;
};

/** A piece of answer text, or a marker pointing at one of the sources. */
export type AnswerPiece =
  | { kind: "text"; text: string }
  | { kind: "marker"; ordinal: number };

/**
 * One to three digits in square brackets. Kept in step with the pattern the
 * RAG module parses (`CITATION_MARKER` in `agentic_rag.py`): a marker the
 * server counts as cited and the client does not render, or the other way
 * round, is a source the reader cannot reach.
 */
const MARKER = /\[(\d{1,3})\]/g;

function optionalString(value: unknown): string | undefined {
  return typeof value === "string" && value.trim() !== "" ? value : undefined;
}

/**
 * Read the sources out of a `SOURCES` event. Everything comes off the wire,
 * so each entry is narrowed rather than trusted; one without a usable ordinal
 * is dropped, because nothing in the answer could point at it.
 */
export function parseSourcesEvent(chunk: unknown): CitedSource[] {
  if (!Array.isArray(chunk)) return [];
  return chunk.flatMap((entry) => {
    if (typeof entry !== "object" || entry === null) return [];
    const fields = entry as Record<string, unknown>;
    const ordinal = fields.ordinal;
    if (typeof ordinal !== "number" || !Number.isInteger(ordinal)) return [];
    return [
      {
        ordinal,
        documentId: optionalString(fields.document_id),
        title: optionalString(fields.title),
        url: optionalString(fields.url),
      },
    ];
  });
}

/**
 * Cut an answer into text and markers.
 *
 * Call it on the whole answer accumulated so far, never on a single streamed
 * delta: a marker can be split across two tokens, "[" arriving in one and
 * "2]" in the next, and a delta would show the halves as plain text.
 *
 * Markers are returned whether or not a source matches them. Deciding what to
 * do with one that resolves to nothing is the caller's: showing "[2]" as text
 * is the intended fallback, which is why the server never suppresses it.
 */
export function splitAnswerOnMarkers(text: string): AnswerPiece[] {
  const pieces: AnswerPiece[] = [];
  let last = 0;

  MARKER.lastIndex = 0;
  for (let match = MARKER.exec(text); match; match = MARKER.exec(text)) {
    if (match.index > last) {
      pieces.push({ kind: "text", text: text.slice(last, match.index) });
    }
    pieces.push({ kind: "marker", ordinal: Number(match[1]) });
    last = match.index + match[0].length;
  }

  if (last < text.length) pieces.push({ kind: "text", text: text.slice(last) });
  return pieces;
}

/** The sources by the number the answer marks them with. */
export function sourcesByOrdinal(
  sources: CitedSource[],
): Map<number, CitedSource> {
  return new Map(sources.map((source) => [source.ordinal, source]));
}

/**
 * A remark plugin that turns every `[n]` in the prose into a `<span>` the app
 * can style, carrying the number in `data-citation-ordinal`.
 *
 * A plugin rather than a `components` entry because react-markdown has no
 * hook for text nodes: intercepting them from the block elements would mean
 * handling `p`, `li`, `td`, headings and whatever else, and would still reach
 * inside code blocks. remark visits `text` nodes only, so a `[2]` inside a
 * fenced block or inline code is left alone - the node there is `code`, not
 * `text`.
 *
 * The tree is typed loosely on purpose: this folder takes no dependencies, so
 * it cannot import mdast's types.
 */
export function remarkCitationMarkers() {
  return (tree: unknown): void => visit(tree);
}

type LooseNode = { type?: string; value?: unknown; children?: unknown[] };

function visit(node: unknown): void {
  if (typeof node !== "object" || node === null) return;
  const parent = node as LooseNode;
  if (!Array.isArray(parent.children)) return;

  const rebuilt: unknown[] = [];
  for (const child of parent.children) {
    const candidate = child as LooseNode | null;
    if (
      typeof candidate !== "object" ||
      candidate === null ||
      candidate.type !== "text" ||
      typeof candidate.value !== "string"
    ) {
      visit(child);
      rebuilt.push(child);
      continue;
    }

    const pieces = splitAnswerOnMarkers(candidate.value);
    if (!pieces.some((piece) => piece.kind === "marker")) {
      rebuilt.push(child);
      continue;
    }

    for (const piece of pieces) {
      if (piece.kind === "text") {
        rebuilt.push({ type: "text", value: piece.text });
      } else {
        rebuilt.push({
          type: "text",
          value: String(piece.ordinal),
          data: {
            hName: "span",
            hProperties: { "data-citation-ordinal": String(piece.ordinal) },
          },
        });
      }
    }
  }

  parent.children = rebuilt;
}
