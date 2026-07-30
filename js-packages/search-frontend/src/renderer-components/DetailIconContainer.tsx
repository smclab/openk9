import React from "react";
import { css } from "styled-components";

type DetailIconContainerProps = {
  children: React.ReactNode;
};
export function DetailIconContainer({ children }: DetailIconContainerProps) {
  return (
    <div
      className="openk9-embeddable-detail-icon-container"
      css={css`
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
        color: var(--openk9-embeddable-search--primary-color);
      `}
    >
      {children}
    </div>
  );
}
