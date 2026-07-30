import React from "react";
import { css } from "styled-components";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultTitleProps = { children: React.ReactNode };
export function ResultTitle({ children }: ResultTitleProps) {
  return (
    <h3
      className="openk9-embeddable-result-title-component"
      css={css`
        font-size: 1.5em;
        font-weight: var(--openk9-embeddable-search--font-weight-medium, 500);
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
        ${truncatedLineStyle}
        margin-top: 0px;
      `}
    >
      {children}
    </h3>
  );
}
