import React from "react";
import {
  GenericResultItem,
  GenericResultItemFields,
} from "../components/client";
import get from "lodash/get";
import { HighlightedText } from "./HighlightedText";
import { css } from "styled-components/macro";

type DetailTextContentProps<E> = {
  result: GenericResultItem<E>;
  path: GenericResultItemFields<E>;
};
export function DetailTextContentTwo<E>({
  result,
  path,
}: DetailTextContentProps<E>) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  if (hihglithTextLines) {
    return (
      <div>
        {hihglithTextLines.map((text, index) => (
          <div key={index}>
            <HighlightedText text={text} />
          </div>
        ))}
      </div>
    );
  }
  return <div css={css``}>{text}</div>;
}
