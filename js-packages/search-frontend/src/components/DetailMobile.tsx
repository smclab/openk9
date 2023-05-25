import React from "react";
import { css } from "styled-components/macro";
import { GenericResultItem, DetailRendererProps } from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";
import { ModalDetail } from "./ModalDetail";

export type DetailMobileProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile: any;
};
function DetailMobile<E>(props: DetailMobileProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile as any;
  const renderers = useRenderers();
  const componet = (
    <DetailMemo result={result} setDetailMobile={setDetailMobile} />
  );

  if (!result) {
    document.body.style.overflow = "auto";
    return null;
  }
  document.body.style.overflow = "hidden";
  return <ModalDetail content={componet} />;
}
export const DetailMobileMemo = React.memo(DetailMobile);
