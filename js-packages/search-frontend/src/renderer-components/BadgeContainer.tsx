import React from "react";
import { css } from "styled-components/macro";

type BadgeContainerProps = {
  children: React.ReactNode;
};
export function BadgeContainer({ children }: BadgeContainerProps) {
  return (
    <div
      css={css`
        display: flex;
        margin-bottom: 8px;
      `}
    >
      {children}
    </div>
  );
}
