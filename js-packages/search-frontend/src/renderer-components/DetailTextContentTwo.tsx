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
  isHighlighted?: boolean;
};
export function DetailTextContentTwo<E>({
  result,
  path,
  isHighlighted = true,
}: DetailTextContentProps<E>) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  const textWithBreaksAndTabsAndCarriageReturns = text
    .replace(/\r/g, "")
    .replace(/\n/g, "<br />")
    .replace(/\t/g, "&emsp;");
  if (!isHighlighted)
    return (
      <div
        dangerouslySetInnerHTML={{
          __html: textWithBreaksAndTabsAndCarriageReturns,
        }}
      />
    );
  if (hihglithTextLines) {
    return (
      <div className="openk9-embeddable-detail-text-content-two">
        {hihglithTextLines.map((text, index) => (
          <div
            className="openk9-embeddable-content-two-hihglithTextLines"
            key={index}
          >
            <HighlightedText text={text} />
          </div>
        ))}
      </div>
    );
  }
  return <div>{text}</div>;
}
