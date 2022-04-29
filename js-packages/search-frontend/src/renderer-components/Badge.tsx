import React from "react";
import { css } from "styled-components/macro";

type BadgeProps = {
  children: React.ReactNode;
};
export function Badge({ children }: BadgeProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
        background-color: var(--openk9-embeddable-search--secondary-background-color);
        margin-right: 8px;
        border-radius: 4px;
      `}
    >
      {children}
    </div>
  );
}
