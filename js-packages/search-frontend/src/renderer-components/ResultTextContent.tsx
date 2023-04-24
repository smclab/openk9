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
      className="openk9--result-text-content"
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
          className="openk9-embeddable--result-text-content"
          css={css`
            height: calc(21px * 5);
            overflow: hidden;
            word-wrap: break-word;
            word-break: break-word;
            text-overflow: ellipsis;
          `}
        >
          {text}
        </div>
      )}
    </div>
  );
}
