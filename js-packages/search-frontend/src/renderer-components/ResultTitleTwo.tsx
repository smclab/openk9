import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultTitleProps = { children: React.ReactNode; isTruncate?: boolean };
export function ResultTitleTwo({
  children,
  isTruncate = true,
}: ResultTitleProps) {
  return (
    <div
      className="openk9-embeddable-result-title-component"
      css={css`
        font-weight: 500;
        margin-bottom: 8px;
        font-weight: 600;
        font-size: 19px;
        line-height: 26px;
        ${isTruncate ? truncatedLineStyle : ""}
      `}
    >
      {children}
    </div>
  );
}
