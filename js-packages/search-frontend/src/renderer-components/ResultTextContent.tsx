import React from "react";
import { HighlightedText } from "./HighlightedText";
import get from "lodash/get";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";
import { HighlightableTextProps } from "./HighlightableText";

export function ResultTextContent<E>({
  result,
  path,
}: HighlightableTextProps<E>) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  return (
    <div
      css={css`
        ${hihglithTextLines ? truncatedLineStyle : ""};
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
          css={css`
            height: calc(17.2px * 2);
            overflow: hidden;
            word-wrap: break-word;
            word-break: break-word;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
          `}
        >
          {text}
        </div>
      )}
    </div>
  );
}
