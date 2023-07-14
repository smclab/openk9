import React from "react";
import { HighlightedText } from "./HighlightedText";
import get from "lodash/get";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";
import { HighlightableTextProps } from "./HighlightableText";

export function ResultTextContentTwo<E>({
  result,
  path,
  isTruncate = true,
}: HighlightableTextProps<E> & { isTruncate?: boolean }) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  return (
    <p
      className="openk9--result-text-content"
      css={css`
        ${hihglithTextLines ? truncatedLineStyle : ""};
        margin: 0;
      `}
    >
      {hihglithTextLines ? (
        hihglithTextLines.map((text, index) => (
          <div key={index} css={truncatedLineStyle}>
            <HighlightedText text={text} />
          </div>
        ))
      ) : (
        <div
          className="openk9-embeddable--result-text-content"
          css={css`
            height: ${isTruncate ? "calc(15.5px * 3)" : "auto"};
            overflow: ${isTruncate ? "hidden" : "visible"};
            word-wrap: break-word;
            word-break: break-word;
            text-overflow: ${isTruncate ? "ellipsis" : "clip"};
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: ${isTruncate ? 3 : "inherit"};
          `}
        >
          {text}
        </div>
      )}
    </p>
  );
}
