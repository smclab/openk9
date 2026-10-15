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
import type { Components } from "react-markdown";

import {
  type CitedSource,
  sourcesByOrdinal,
} from "../../../shared/inline-citations/inlineCitations";

const markerStyle = css`
  vertical-align: super;
  font-size: 0.72em;
  line-height: 0;
  margin-left: 1px;
  padding: 0 3px;
  border-radius: 4px;
`;

/**
 * A `[n]` the answer wrote, rendered as the footnote it is: a link to the
 * source when one resolves, the bare number otherwise.
 *
 * Leaving the number visible when nothing resolves is deliberate. The RAG
 * module never suppresses the marker precisely so that it degrades into
 * something a reader can still make sense of, rather than vanishing.
 */
function CitationMarker({
  ordinal,
  source,
}: {
  ordinal: string;
  source?: CitedSource;
}) {
  const url = source?.url;
  if (!url) {
    return (
      <span
        css={css`
          ${markerStyle}
          color: var(--openk9-embeddable-search--text-muted, #6c757d);
        `}
      >
        [{ordinal}]
      </span>
    );
  }

  return (
    <a
      href={url}
      target="_blank"
      rel="noreferrer"
      title={source?.title ?? url}
      css={css`
        ${markerStyle}
        text-decoration: none;
        background-color: rgba(0, 0, 0, 0.04);
        &:hover {
          background-color: rgba(0, 0, 0, 0.1);
        }
      `}
    >
      [{ordinal}]
    </a>
  );
}

/**
 * The react-markdown components for an answer carrying inline markers. The
 * `remarkCitationMarkers` plugin turns each marker into a span tagged with
 * its number; this resolves that number against the sources the `SOURCES`
 * event announced before the answer started.
 */
export function citationComponents(
  sources: CitedSource[] | undefined,
): Components {
  const byOrdinal = sourcesByOrdinal(sources ?? []);
  return {
    span: ({ node, children, ...props }) => {
      const ordinal = (props as Record<string, unknown>)[
        "data-citation-ordinal"
      ];
      if (typeof ordinal !== "string") return <span {...props}>{children}</span>;
      return (
        <CitationMarker
          ordinal={ordinal}
          source={byOrdinal.get(Number(ordinal))}
        />
      );
    },
  };
}
