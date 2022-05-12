import { GenericResultItem, GenericResultItemFields } from "@openk9/rest-api";
import React from "react";
import { HighlightedText } from "./HighlightedText";
import get from "lodash/get";

export type HighlightableTextProps<E> = {
  result: GenericResultItem<E>;
  path: GenericResultItemFields<E>;
};
export function HighlightableText<E>({
  result,
  path,
}: HighlightableTextProps<E>) {
  const hihglithText = result.highlight[path]?.[0];
  const text = get(result.source, path);
  if (hihglithText) {
    return <HighlightedText text={hihglithText} />;
  }
  return <>{text}</>;
}
