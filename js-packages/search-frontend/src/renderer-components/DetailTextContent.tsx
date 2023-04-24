import React from "react";
import {
  GenericResultItem,
  GenericResultItemFields,
} from "../components/client";
import get from "lodash/get";
import { HighlightedText } from "./HighlightedText";

type DetailTextContentProps<E> = {
  result: GenericResultItem<E>;
  path: GenericResultItemFields<E>;
};
export function DetailTextContent<E>({
  result,
  path,
}: DetailTextContentProps<E>) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  if (hihglithTextLines) {
    return (
      <div className="openk9-embeddable-detail-text-content">
        {hihglithTextLines.map((text, index) => (
          <div className="openk9-embeddable-hihglith-text-lines" key={index}>
            <HighlightedText text={text} />
          </div>
        ))}
      </div>
    );
  }
  return <div>{text}</div>;
}
