import React from "react";
import { GenericResultItem } from "@openk9/http-api";
import { SidebarRenderersType } from "@openk9/search-ui-components";

export function Detail<E>({
  result,
  sidebarRenderers,
}: {
  result: GenericResultItem<E>;
  sidebarRenderers: SidebarRenderersType<E>;
}) {
  const Renderer = (
    typeof result.source.documentTypes === "string"
      ? [result.source.documentTypes]
      : result.source.documentTypes
  )
    .map((k) => sidebarRenderers[k as any])
    .find(Boolean);
  if (Renderer) {
    return <Renderer result={result} loginInfo={null} />;
  } else {
    return (
      <div dangerouslySetInnerHTML={{ __html: result.source.rawContent }}></div>
    );
  }
}
