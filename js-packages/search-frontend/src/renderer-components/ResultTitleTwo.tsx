import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultTitleProps = { children: React.ReactNode };
export function ResultTitleTwo({ children }: ResultTitleProps) {
  return (
    <div
      css={css`
        font-weight: 500;
        margin-bottom: 8px;
        font-weight: 600;
        font-size: 19px;
        line-height: 26px;
        ${truncatedLineStyle}
      `}
    >
      {children}
    </div>
  );
}
