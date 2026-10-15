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
 * A remark plugin that turns the `[n]` markers in the prose into a `<span>`
 * the app can style, carrying the numbers in `data-citation-ordinals`.
 * Markers written next to each other become one span: "[1][3]" is a sentence
 * with two sources, which the reader wants as one citation, not two.
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
  return (tree: unknown): void => visit(tree, "tag");
}

/**
 * A remark plugin that removes the markers instead of tagging them, for a
 * view that does not render citations: there the "[2]" would be noise the
 * reader cannot act on.
 *
 * A plugin and not a string replace for the same reason as above: an array
 * index inside a fenced block is a `code` node, not a `text` one, and must
 * survive.
 */
export function remarkStripCitationMarkers() {
  return (tree: unknown): void => visit(tree, "strip");
}

/**
 * Drop the markers from a line of prose, taking the space before each with
 * it: "in marketing [1]." has to read "in marketing.", not "in marketing .".
 */
export function stripCitationMarkers(text: string): string {
  return text.replace(/[ \t]*\[\d{1,3}\]/g, "");
}

type LooseNode = { type?: string; value?: unknown; children?: unknown[] };

function visit(node: unknown, mode: "tag" | "strip"): void {
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
      visit(child, mode);
      rebuilt.push(child);
      continue;
    }

    const pieces = splitAnswerOnMarkers(candidate.value);
    if (!pieces.some((piece) => piece.kind === "marker")) {
      rebuilt.push(child);
      continue;
    }

    if (mode === "strip") {
      rebuilt.push({ type: "text", value: stripCitationMarkers(candidate.value) });
      continue;
    }

    // Adjacent markers are one citation with several sources: the model
    // writes "[1][3]" for a sentence that draws on two, and the reader wants
    // one chip saying so, not two chips in a row.
    for (let index = 0; index < pieces.length; index += 1) {
      const piece = pieces[index];
      if (piece.kind === "text") {
        rebuilt.push({ type: "text", value: piece.text });
        continue;
      }

      const group = [piece.ordinal];
      while (index + 1 < pieces.length) {
        const next = pieces[index + 1];
        if (next.kind !== "marker") break;
        group.push(next.ordinal);
        index += 1;
      }

      rebuilt.push({
        type: "text",
        value: group.join(","),
        data: {
          hName: "span",
          hProperties: { "data-citation-ordinals": group.join(",") },
        },
      });
    }
  }

  parent.children = rebuilt;
}

/**
 * The first quote of each cited source, by the number the answer marks it
 * with, read off the `DOCUMENT` events.
 *
 * They arrive once the answer is complete - which sources were used is known
 * only then - so a panel shows the title while the answer is being written
 * and gains the quote at the end. Sources shown without one are normal: on
 * the measured corpus about a quarter of them have no passage that could be
 * copied word for word.
 */
export function quotesByOrdinal(
  documents: readonly unknown[] | undefined,
): Map<number, string> {
  const quotes = new Map<number, string>();
  for (const document of documents ?? []) {
    if (typeof document !== "object" || document === null) continue;
    const fields = document as Record<string, unknown>;
    const ordinal = fields.ordinal;
    if (typeof ordinal !== "number" || !Number.isInteger(ordinal)) continue;
    const citations = fields.citations;
    if (!Array.isArray(citations)) continue;
    for (const citation of citations) {
      if (typeof citation !== "object" || citation === null) continue;
      const quote = optionalString((citation as Record<string, unknown>).quote);
      if (quote) {
        quotes.set(ordinal, quote);
        break;
      }
    }
  }
  return quotes;
}

/**
 * Keeps one citation panel open at a time.
 *
 * Panels close on a delay, so that the pointer can travel from the chip to
 * the link without the panel vanishing under it. Moving quickly between two
 * chips then shows both: the one being left is still inside its grace period
 * when the next one opens. Opening a panel closes the previous one at once.
 *
 * Module state, which means one registry per bundle: an app that inlines
 * this module gets its own, and that is the right scope - panels in two
 * different apps on one page do not compete.
 */
let closeOpenPanel: (() => void) | undefined;

/** Open this panel, closing whichever other one is open. */
export function claimPanel(close: () => void): void {
  if (closeOpenPanel && closeOpenPanel !== close) closeOpenPanel();
  closeOpenPanel = close;
}

/** Give up the claim, if this panel still holds it. */
export function releasePanel(close: () => void): void {
  if (closeOpenPanel === close) closeOpenPanel = undefined;
}
