import React from "react";
import { css } from "styled-components/macro";

type DetailLinkProps = {
  children: React.ReactNode;
};
export function DetailLink({ children }: DetailLinkProps) {
  return (
    <div
      css={css`
        font-size: 0.8em;
        margin-bottom: 8px;
      `}
    >
      {children}
    </div>
  );
}
