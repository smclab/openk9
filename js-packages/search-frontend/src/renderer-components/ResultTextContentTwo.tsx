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
            display: ${isTruncate ? "-webkit-box" : ""};
            -webkit-line-clamp: ${isTruncate ? "3" : ""};
            overflow: ${isTruncate ? "hidden" : "visible"};
            word-wrap: break-word;
            word-break: break-word;
            display: -webkit-box;
            -webkit-box-orient: vertical;
          `}
        >
          {text}
        </div>
      )}
    </p>
  );
}
