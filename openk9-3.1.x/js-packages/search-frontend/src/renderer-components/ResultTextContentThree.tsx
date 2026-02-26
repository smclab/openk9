import React from "react";
import { HighlightedText } from "./HighlightedText";
import get from "lodash/get";
import { css } from "styled-components";
import { truncatedLineStyle } from "./truncatedLineStyle";
import { HighlightableTextProps } from "./HighlightableText";

export function ResultTextContentThree<E>({
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
        <React.Fragment>
          <div
            css={truncatedLineStyle}
            className="openk9--result-text-content-three"
          >
            {hihglithTextLines.map((text, index) => (
              <HighlightedText text={text} key={index} />
            ))}
          </div>
        </React.Fragment>
      ) : (
        <div
          className="openk9-embeddable--result-text-content"
          css={
            isTruncate
              ? css`
                  display: -webkit-box;
                  -webkit-line-clamp: 3;
                  -webkit-box-orient: vertical;
                  overflow: hidden;
                `
              : undefined
          }
        >
          {text}
        </div>
      )}
    </p>
  );
}
