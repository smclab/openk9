import React from "react";
import { css } from "styled-components";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultTitleProps = { children: React.ReactNode; isTruncate?: boolean };
export function ResultTitleTwo({
  children,
  isTruncate = true,
}: ResultTitleProps) {
  return (
    <h3
      className="openk9-embeddable-result-title-component"
      css={css`
        font-weight: var(--openk9-embeddable-search--font-weight-medium, 500);
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
        font-weight: var(--openk9-embeddable-search--font-weight-semibold, 600);
        font-size: var(--openk9-embeddable-search--font-size-xl, 20px);
        line-height: 26px;
        margin-top: 0px;
        ${isTruncate ? truncatedLineStyle : ""}
      `}
    >
      {children}
    </h3>
  );
}
