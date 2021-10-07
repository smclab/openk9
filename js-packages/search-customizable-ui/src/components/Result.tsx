import React from "react";
import { GenericResultItem } from "@openk9/http-api";
import { ResultRenderersType } from "@openk9/search-ui-components";

export function Result<E>({
  result,
  resultRenderers,
  onSelect,
}: {
  result: GenericResultItem<E>;
  resultRenderers: ResultRenderersType<E>;
  onSelect(): void;
}) {
  const Renderer = (
    typeof result.source.type === "string"
      ? [result.source.type]
      : result.source.type
  )
    .map((k) => resultRenderers[k as any])
    .find(Boolean);
  if (Renderer) {
    return <Renderer data={result} onSelect={onSelect} loginInfo={null} />;
  } else {
    return (
      <div dangerouslySetInnerHTML={{ __html: result.source.rawContent }}></div>
    );
  }
}
