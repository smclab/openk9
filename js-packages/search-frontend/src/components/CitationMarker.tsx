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
import { css } from "styled-components";
import Markdown, { type Components } from "react-markdown";

import {
  type CitedSource,
  claimPanel,
  parseSourcesEvent,
  quotesByOrdinal,
  releasePanel,
  remarkCitationMarkers,
  sourcesByOrdinal,
} from "../../../shared/inline-citations/inlineCitations";
import { safeExternalUrl } from "../renderer-components/safeExternalUrl";
import type { ChatSource } from "./client";

import React from "react";

const chipStyle = css`
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  margin-left: 3px;
  padding: 1px 6px;
  border-radius: 999px;
  border: none;
  font-family: inherit;
  font-size: 0.72em;
  line-height: 1.6;
  cursor: default;
  color: var(--openk9-embeddable-search--text-muted, #6c757d);
  background-color: rgba(0, 0, 0, 0.05);
  &:hover {
    background-color: rgba(0, 0, 0, 0.1);
  }
`;

/** The site a source comes from, for the panel; the chip shows its title. */
function hostOf(url?: string): string | undefined {
  if (!url) return undefined;
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return undefined;
  }
}

/** A chip has to stay inside a line of prose, so the title is cut short. */
function shortTitle(source: CitedSource | undefined, ordinal: number): string {
  const title = source?.title ?? hostOf(source?.url);
  if (!title) return `[${ordinal}]`;
  return title.length > 28 ? `${title.slice(0, 27)}\u2026` : title;
}

/**
 * The citation of one sentence, as a chip naming its source. Several sources
 * on the same sentence become one chip with a count: one citation that
 * happens to rest on more than one document.
 *
 * On a corpus where everything comes from one site the domain would say
 * nothing, so the chip carries the document title and leaves the domain to
 * the panel. Sources nothing resolves keep their bare number, which is why
 * the RAG module never suppresses the marker.
 */
function CitationChip({
  ordinals,
  byOrdinal,
  quotes,
}: {
  ordinals: number[];
  byOrdinal: Map<number, CitedSource>;
  quotes?: Map<number, string>;
}) {
  const [open, setOpen] = React.useState(false);
  // Closing on the first mouseleave makes the panel vanish while the pointer
  // travels towards it: a diagonal path leaves the chip before it reaches the
  // link. A short grace period keeps it reachable.
  const closing = React.useRef<ReturnType<typeof setTimeout> | undefined>(
    undefined,
  );
  const closeNow = React.useRef(() => setOpen(false));
  const show = () => {
    if (closing.current) clearTimeout(closing.current);
    // only one panel at a time: the one being left is still inside its
    // grace period, so moving quickly between chips would show both
    claimPanel(closeNow.current);
    setOpen(true);
  };
  const hide = () => {
    if (closing.current) clearTimeout(closing.current);
    closing.current = setTimeout(() => {
      releasePanel(closeNow.current);
      setOpen(false);
    }, 200);
  };
  React.useEffect(
    () => () => {
      if (closing.current) clearTimeout(closing.current);
      releasePanel(closeNow.current);
    },
    [],
  );
  const chipRef = React.useRef<HTMLButtonElement>(null);
  const closeOnEscape = (event: React.KeyboardEvent) => {
    if (event.key !== "Escape" || !open) return;
    // back to the chip first: unmounting the panel under a focused link
    // would leave the focus on the body
    chipRef.current?.focus();
    if (closing.current) clearTimeout(closing.current);
    releasePanel(closeNow.current);
    setOpen(false);
  };

  const first = byOrdinal.get(ordinals[0]);
  const extra = ordinals.length - 1;
  const label = shortTitle(first, ordinals[0]);

  return (
    // Focus is handled on the container: moving with Tab from the chip to
    // the link in the panel blurs one and focuses the other, and the two
    // cancel out instead of closing the panel under the keyboard.
    <span
      css={css`
        position: relative;
        display: inline-block;
      `}
      onMouseEnter={show}
      onMouseLeave={hide}
      onFocus={show}
      onBlur={hide}
      onKeyDown={closeOnEscape}
    >
      <button
        type="button"
        ref={chipRef}
        aria-expanded={open}
        onClick={show}
        css={chipStyle}
      >
        {label}
        {extra > 0 && (
          <span
            css={css`
              opacity: 0.7;
            `}
          >
            +{extra}
          </span>
        )}
      </button>

      {open && (
        // not a tooltip: it holds links the reader moves into
        <span
          role="group"
          aria-label={label}
          onMouseEnter={show}
          onMouseLeave={hide}
          css={css`
            position: absolute;
            z-index: 10;
            top: 100%;
            left: 0;
            width: 320px;
            max-width: 80vw;
            /* padding and not margin: the gap between the chip and the panel
               has to belong to the panel, or the pointer crossing it leaves
               both and the panel closes under it */
            padding: 8px;
            border-radius: 12px;
            background: var(
              --openk9-embeddable-search--primary-background-color,
              #fff
            );
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
            text-align: left;
            font-size: 0.85rem;
            display: block;
          `}
        >
          {ordinals.map((ordinal) => {
            const source = byOrdinal.get(ordinal);
            // the url comes off the wire as the index holds it: only a safe
            // protocol becomes a link, the rest shows the title alone
            const url = safeExternalUrl(source?.url);
            const quote = quotes?.get(ordinal);
            return (
              <span
                key={ordinal}
                css={css`
                  position: relative;
                  display: block;
                  padding: 6px;
                  border-radius: 8px;
                  &:hover {
                    background-color: rgba(0, 0, 0, 0.04);
                  }
                `}
              >
                <span
                  css={css`
                    display: block;
                    font-size: 0.75rem;
                    font-weight: 600;
                    letter-spacing: 0.02em;
                    color: var(
                      --openk9-embeddable-search--secondary-text-color,
                      #495057
                    );
                  `}
                >
                  {hostOf(source?.url) ?? `[${ordinal}]`}
                </span>
                {url ? (
                  <a
                    href={url}
                    target="_blank"
                    rel="noreferrer"
                    css={css`
                      display: block;
                      font-weight: 600;
                      /* not the theme's primary: it can land almost white
                         against the panel */
                      color: var(
                        --openk9-embeddable-search--strong-text-color,
                        #212529
                      );
                      text-decoration: underline;
                      text-underline-offset: 2px;
                      &:hover {
                        opacity: 0.75;
                      }
                      /* the whole row opens the source, while the markup
                         stays one anchor: middle click, "copy link" and the
                         keyboard keep working, which an onClick on the
                         container would throw away */
                      &::after {
                        content: "";
                        position: absolute;
                        inset: 0;
                        border-radius: 8px;
                      }
                    `}
                  >
                    {source?.title ?? url}
                  </a>
                ) : (
                  <span
                    css={css`
                      display: block;
                      font-weight: 600;
                    `}
                  >
                    {source?.title ?? `[${ordinal}]`}
                  </span>
                )}
                {quote && (
                  <span
                    css={css`
                      display: block;
                      margin-top: 2px;
                      font-style: italic;
                      color: var(
                        --openk9-embeddable-search--text-muted,
                        #6c757d
                      );
                      /* above the stretched link, so the passage copied from
                         the source can still be selected */
                      position: relative;
                      z-index: 1;
                    `}
                  >
                    “{quote}”
                  </span>
                )}
              </span>
            );
          })}
        </span>
      )}
    </span>
  );
}

/**
 * The react-markdown components for an answer carrying inline markers. The
 * `remarkCitationMarkers` plugin tags each citation with its numbers; this
 * resolves them against the sources the `SOURCES` event announced before the
 * answer started, and against the quotes the `DOCUMENT` events bring at the
 * end.
 */
export function citationComponents(
  sources: CitedSource[] | undefined,
  quotes?: Map<number, string>,
): Components {
  const byOrdinal = sourcesByOrdinal(sources ?? []);
  return {
    span: ({ node, children, ...props }) => {
      const raw = (props as Record<string, unknown>)["data-citation-ordinals"];
      if (typeof raw !== "string") return <span {...props}>{children}</span>;
      const ordinals = raw.split(",").map(Number).filter(Number.isInteger);
      if (ordinals.length === 0) return <span {...props}>{children}</span>;
      // a number no source resolves stays the bare "[n]": a chip for it
      // would open a panel with nothing to reach
      const resolved = ordinals.filter((ordinal) => byOrdinal.has(ordinal));
      const unresolved = ordinals.filter((ordinal) => !byOrdinal.has(ordinal));
      const bare =
        unresolved.length > 0 ? (
          <span>{unresolved.map((ordinal) => `[${ordinal}]`).join("")}</span>
        ) : null;
      if (resolved.length === 0) return bare;
      return (
        <>
          <CitationChip
            ordinals={resolved}
            byOrdinal={byOrdinal}
            quotes={quotes}
          />
          {bare}
        </>
      );
    },
  };
}

type CitedAnswerProps = {
  answer: string;
  citedSources?: CitedSource[];
  sources?: ChatSource[];
};

/**
 * An answer with its citations as chips. The components are built once per
 * answer, not on every render: a new `span` on each streamed token would
 * remount every chip and close an open panel.
 */
export function CitedAnswer({
  answer,
  citedSources,
  sources,
}: CitedAnswerProps) {
  const components = React.useMemo(
    () =>
      citationComponents(
        // a chat reloaded from history has no `SOURCES` event, but its
        // stored sources carry the same ordinal, title and url
        citedSources ?? parseSourcesEvent(sources),
        quotesByOrdinal(sources),
      ),
    [citedSources, sources],
  );
  return (
    <Markdown remarkPlugins={[remarkCitationMarkers]} components={components}>
      {answer}
    </Markdown>
  );
}
