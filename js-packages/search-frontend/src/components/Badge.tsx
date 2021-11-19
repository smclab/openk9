import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "../utils/myTheme";

type BadgeProps = {
  children: React.ReactNode;
};
export function Badge({ children }: BadgeProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
        background-color: ${myTheme.backgroundColor2};
        margin-right: 8px;
        border-radius: 4px;
      `}
    >
      {children}
    </div>
  );
}
