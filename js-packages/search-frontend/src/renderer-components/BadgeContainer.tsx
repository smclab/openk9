import React from "react";
import { css } from "styled-components";

type BadgeContainerProps = {
  children: React.ReactNode;
};
export function BadgeContainer({ children }: BadgeContainerProps) {
  return (
    <div
      className="openk9-embeddable-card--badge-container"
      css={css`
        display: flex;
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
    >
      {children}
    </div>
  );
}
