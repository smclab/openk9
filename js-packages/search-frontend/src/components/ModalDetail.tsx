import React from "react";
import { css } from "styled-components/macro";
import { GenericResultItem, DetailRendererProps } from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";

export function ModalDetail({
  content,
  padding = "8px",
}: {
  content: React.ReactNode;
  padding?: string;
}) {
  return (
    <div
      className="openk9-modal-mobile openk9-modal"
      css={css`
        position: relative;
      `}
    >
      <div
        className="openk9-wrapper-modal openk9-container-modal"
        css={css`
          padding: ${padding};
          z-index: 500;
          position: fixed;
          top: 0px;
          left: 0px;
          right: 0px;
          bottom: 0px;
          background: #89878794;
        `}
      >
        {content}
      </div>
    </div>
  );
}
