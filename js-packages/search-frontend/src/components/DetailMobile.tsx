import React from "react";
import { css } from "styled-components/macro";
import { GenericResultItem, DetailRendererProps } from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";

export type DetailMobileProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile: any;
};
function DetailMobile<E>(props: DetailMobileProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile as any;
  const renderers = useRenderers();

  if (!result) {
    return null;
  }
  return (
    <div
      className="openk9-modal"
      css={css`
        position: relative;
      `}
    >
      <div
        className="openk9-wrapper-modal"
        css={css`
          background-color: var(
            --openk9-embeddable-search--secondary-background-color
          );
          padding: 16px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
          z-index: 2;
          @media (min-width: 320px) and (max-width: 480px) {
            position: fixed;
            top: 0px;
            left: 0px;
            right: 0px;
            bottom: 0px;
            padding: 8px;
            margin-right: -5px;
            background: #89878794;
          }
        `}
      >
        <DetailMemo result={result} setDetailMobile={setDetailMobile} />
      </div>
    </div>
  );
}
export const DetailMobileMemo = React.memo(DetailMobile);
