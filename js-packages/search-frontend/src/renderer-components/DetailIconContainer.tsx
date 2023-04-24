import React from "react";
import { css } from "styled-components/macro";

type DetailIconContainerProps = {
  children: React.ReactNode;
};
export function DetailIconContainer({ children }: DetailIconContainerProps) {
  return (
    <div
      className="openk9-embeddable-detail-icon-container"
      css={css`
        margin-bottom: 8px;
        color: var(--openk9-embeddable-search--primary-color);
      `}
    >
      {children}
    </div>
  );
}
