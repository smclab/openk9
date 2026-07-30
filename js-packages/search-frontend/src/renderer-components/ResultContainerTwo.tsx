import React from "react";
import { css } from "styled-components";

type ResultContainerProps = {
  icon?: React.ReactNode;
  children: React.ReactNode;
};
export function ResultContainerTwo({ icon, children }: ResultContainerProps) {
  return (
    <div
      css={css`
        display: flex;
        padding: var(--openk9-embeddable-search--spacing-sm, 8px)
          var(--openk9-embeddable-search--spacing-lg, 16px);
      `}
    >
      <div
        css={css`
          @media (max-width: 480px) {
            display: none;
          }
          width: 30px;
          margin-right: var(--openk9-embeddable-search--spacing-sm, 8px);
        `}
      ></div>
      <div
        css={css`
          margin-left: var(--openk9-embeddable-search--spacing-xs, 4px);
          margin-right: var(--openk9-embeddable-search--spacing-xs, 4px);
          overflow: hidden;
        `}
      >
        {children}
      </div>
    </div>
  );
}
