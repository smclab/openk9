import React from "react";
import { css } from "styled-components/macro";

type DetailIconContainerProps = {
  children: React.ReactNode;
};
export function DetailIconContainer({ children }: DetailIconContainerProps) {
  return (
    <div
      css={css`
        margin-bottom: 8px;
      `}
    >
      {children}
    </div>
  );
}
