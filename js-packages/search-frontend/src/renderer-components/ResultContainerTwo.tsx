import React from "react";
import { css } from "styled-components/macro";

type ResultContainerProps = {
  icon?: React.ReactNode;
  children: React.ReactNode;
};
export function ResultContainerTwo({ icon, children }: ResultContainerProps) {
  return (
    <div
      css={css`
        display: flex;
        padding: 8px 16px;
      `}
    >
      <div
        css={css`
          @media (min-width: 320px) and (max-width: 480px) {
            display: none;
          }
          width: 30px;
          margin-right: 7px;
        `}
      ></div>
      <div
        css={css`
          margin-left: 2px;
          margin-right: 5px;
          overflow: hidden;
        `}
      >
        {children}
      </div>
    </div>
  );
}
