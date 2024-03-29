import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultTitleProps = { children: React.ReactNode };
export function ResultTitle({ children }: ResultTitleProps) {
  return (
    <h3
      className="openk9-embeddable-result-title-component"
      css={css`
        font-size: 1.5em;
        font-weight: 500;
        margin-bottom: 8px;
        ${truncatedLineStyle}
        margin-top: 0px;
      `}
    >
      {children}
    </h3>
  );
}
