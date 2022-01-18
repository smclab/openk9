import React from "react";
import { css } from "styled-components/macro";

type DetailTitleProps = {
  children: React.ReactNode;
};
export function DetailTitle({ children }: DetailTitleProps) {
  return (
    <div
      css={css`
        font-size: 1.5em;
        font-weight: 500;
        margin-bottom: 8px;
      `}
    >
      {children}
    </div>
  );
}
